package com.gradecalc.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Custom thread-safe JDBC Connection Pool tối ưu cho tải cao:
 * - Giới hạn cứng số kết nối bằng Semaphore (không rò rỉ khi vượt ngưỡng).
 * - Không giữ lock toàn cục trên getConnection() để nhiều luồng xử lý song song.
 * - Chỉ validate kết nối (isValid) khi kết nối rảnh quá VALIDATION_IDLE_MS.
 * - Tách thông tin nhạy cảm ra khỏi mã nguồn (Env vars / db.properties).
 */
public class CustomConnectionPool {
    private static final int INITIAL_POOL_SIZE = 5;
    private static final int MAX_POOL_SIZE = 20;
    private static final long BORROW_TIMEOUT_SECONDS = 5L;
    private static final long VALIDATION_IDLE_MS = 30_000L;

    private final String url;
    private final String user;
    private final String password;
    private final BlockingQueue<PooledConnection> connectionPool;
    private final Semaphore permits;

    private static volatile CustomConnectionPool instance;

    private static final class PooledConnection {
        final Connection conn;
        volatile long lastUsedAt;

        PooledConnection(Connection conn) {
            this.conn = conn;
            this.lastUsedAt = System.currentTimeMillis();
        }
    }

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Không tìm thấy MySQL Driver: " + e.getMessage(), e);
        }
    }

    private CustomConnectionPool() throws SQLException {
        Properties props = loadDbConfig();
        this.url = props.getProperty("db.url");
        this.user = props.getProperty("db.user");
        this.password = props.getProperty("db.password");

        this.connectionPool = new ArrayBlockingQueue<>(MAX_POOL_SIZE);
        this.permits = new Semaphore(MAX_POOL_SIZE, true);

        try {
            for (int i = 0; i < INITIAL_POOL_SIZE; i++) {
                connectionPool.add(new PooledConnection(createConnection()));
            }
        } catch (SQLException e) {
            shutdown();
            throw e;
        }
    }

    private static Properties loadDbConfig() {
        Properties fileProps = new Properties();
        try (InputStream in = CustomConnectionPool.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                fileProps.load(in);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Không thể đọc file cấu hình db.properties: " + e.getMessage(), e);
        }

        String url = resolveConfig("DB_URL", fileProps, "db.url");
        String user = resolveConfig("DB_USER", fileProps, "db.user");
        String password = resolveConfig("DB_PASSWORD", fileProps, "db.password");

        if (isBlank(url) || isBlank(user) || isBlank(password)) {
            throw new IllegalStateException(
                    "Thiếu cấu hình kết nối CSDL. Hãy thiết lập biến môi trường DB_URL/DB_USER/DB_PASSWORD "
                    + "hoặc tạo file src/main/resources/db.properties.");
        }

        Properties resolved = new Properties();
        resolved.setProperty("db.url", url.trim());
        resolved.setProperty("db.user", user.trim());
        resolved.setProperty("db.password", password);
        return resolved;
    }

    private static String resolveConfig(String envKey, Properties props, String propKey) {
        String envVal = System.getenv(envKey);
        if (!isBlank(envVal)) {
            return envVal;
        }
        return props.getProperty(propKey);
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private Connection createConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public static CustomConnectionPool getInstance() throws SQLException {
        CustomConnectionPool current = instance;
        if (current == null) {
            synchronized (CustomConnectionPool.class) {
                current = instance;
                if (current == null) {
                    current = new CustomConnectionPool();
                    instance = current;
                }
            }
        }
        return current;
    }

    public Connection getConnection() throws SQLException, InterruptedException {
        if (!permits.tryAcquire(BORROW_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            throw new SQLException("Hệ thống đang quá tải kết nối CSDL, vui lòng thử lại sau!");
        }

        try {
            PooledConnection pooled = connectionPool.poll();
            if (pooled == null) {
                return createConnection();
            }

            if (isUsable(pooled)) {
                return pooled.conn;
            }

            closeQuietly(pooled.conn);
            return createConnection();
        } catch (SQLException | RuntimeException e) {
            permits.release();
            throw e;
        }
    }

    private boolean isUsable(PooledConnection pooled) {
        try {
            if (pooled.conn == null || pooled.conn.isClosed()) {
                return false;
            }
            long idleMs = System.currentTimeMillis() - pooled.lastUsedAt;
            if (idleMs > VALIDATION_IDLE_MS) {
                return pooled.conn.isValid(1);
            }
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public void releaseConnection(Connection conn) {
        if (conn == null) {
            return;
        }
        try {
            if (!conn.isClosed()) {
                if (!conn.getAutoCommit()) {
                    conn.setAutoCommit(true);
                }
                if (!connectionPool.offer(new PooledConnection(conn))) {
                    closeQuietly(conn);
                }
            } else {
                closeQuietly(conn);
            }
        } catch (SQLException e) {
            closeQuietly(conn);
        } finally {
            permits.release();
        }
    }

    public void shutdown() {
        PooledConnection pooled;
        while ((pooled = connectionPool.poll()) != null) {
            closeQuietly(pooled.conn);
        }
    }

    public static void shutdownIfInitialized() {
        CustomConnectionPool current = instance;
        if (current != null) {
            synchronized (CustomConnectionPool.class) {
                if (instance != null) {
                    instance.shutdown();
                    instance = null;
                }
            }
        }
    }

    private static void closeQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ignored) {
            }
        }
    }
}
