package com.gradecalc.config;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;;

public class CustomConnectionPool {
    private static final int INITIAL_POOL_SIZE = 5;
    private static final int MAX_POOL_SIZE = 15;
    private static final String URL = "jdbc:mysql://gateway01.ap-southeast-1.prod.aws.tidbcloud.com:4000/BKGRADE?useSSL=true&characterEncoding=UTF-8&autoReconnect=true";
    private static final String USER = "SAvcDRiDPd4oMBk.root";
    private static final String PASSWORD = "LVuh2Bm7d9QBqlaa";
    private BlockingQueue<Connection> connectionPool; //hàng đợi chứa connection
    private static CustomConnectionPool instance; // biến Singleton

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Không tìm thấy MySQL Driver: " + e.getMessage());
        }
    }

    private CustomConnectionPool() throws SQLException{
        connectionPool = new ArrayBlockingQueue<>(MAX_POOL_SIZE);
        for (int i = 0; i < INITIAL_POOL_SIZE; i++) {
            connectionPool.add(createConnection());
        } // Thêm 5 connection vào hàng đợi
    }

    private Connection createConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Phải có synchronized để tránh nhiều luồng cùng gọi getInstance() -> có thể tạo ra 2 pool.
    public static synchronized CustomConnectionPool getInstance() throws SQLException { 
        if(instance == null){
            instance = new CustomConnectionPool();
        }
        return instance;
    }

    // synchronized để tránh nhiều luồng cùng gọi getConnection() -> có thể lấy ra cùng 1 connection
    public synchronized Connection getConnection() throws SQLException, InterruptedException {
        Connection conn = connectionPool.poll(5, TimeUnit.SECONDS); // Lấy connection từ hàng đợi, nếu không có thì chờ 5 giây
        if(conn == null){
            // Queue trống sau 5s -> thử tạo kết nối mới
            return createConnection();
        }

        // 👉 KIỂM TRA: Bọc try-catch để nếu TiDB ngắt socket thì không văng lỗi "No operations allowed after connection closed"
        boolean isValid = false;
        try {
            if (conn != null && !conn.isClosed() && conn.isValid(2)) {
                isValid = true;
            }
        } catch (Exception ignored) {
            isValid = false;
        }

        if (!isValid) {
            try {
                if (conn != null) conn.close();
            } catch (Exception ignored) {}
            conn = createConnection();
        }

        return conn;
    }

    // Không cần synchronized vì chỉ có 1 luồng được phép gọi releaseConnection() tại 1 thời điểm
    // Không được gọi conn.close() ở đây vì sẽ đóng connection, không thể tái sử dụng
    public void releaseConnection(Connection conn){
        if(conn != null){
            boolean isValid = false;
            try {
                if (!conn.isClosed() && conn.isValid(2)) {
                    isValid = true;
                }
            } catch (Exception ignored) {
                isValid = false;
            }

            if (isValid) {
                connectionPool.offer(conn);
            } else {
                try {
                    conn.close();
                } catch (Exception ignored) {}
                try {
                    connectionPool.offer(createConnection());
                } catch (SQLException e) {
                    System.err.println("Lỗi tạo kết nối bù vào pool: " + e.getMessage());
                }
            }
        }
    }
    public static void main(String[] args) throws Exception{
        CustomConnectionPool pool = CustomConnectionPool.getInstance();
        // mượn 3 connection từ pool
        Connection c1 = pool.getConnection();
        Connection c2 = pool.getConnection();
        Connection c3 = pool.getConnection();
        System.out.println("Đã lấy 3 connection. Queue còn: " + pool.connectionPool.size());
        //trả lại 3 connection
        pool.releaseConnection(c1);
        pool.releaseConnection(c2);
        pool.releaseConnection(c3);
        System.out.println("Đã trả lại 3 connection. Queue còn: " + pool.connectionPool.size());
    }
}
