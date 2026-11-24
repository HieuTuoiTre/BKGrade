package com.gradecalc.model.Dao;
import com.gradecalc.config.CustomConnectionPool;
import com.gradecalc.model.Bean.Course;
import com.gradecalc.model.Bean.User;

import java.awt.*;
import java.sql.*;

public class UserDAO {
    public User findByUsername(String username) throws SQLException, InterruptedException {
        Connection conn = null;
        String sql = "SELECT id, fullname, username, password FROM users WHERE username = ?";
        try {
            conn = CustomConnectionPool.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int id = rs.getInt("id");
                        String fullname = rs.getString("fullname");
                        String password = rs.getString("password");
                        return new User(id, fullname, username, password);
                    }
                    return null;
                }
            }
        } finally {
            if (conn != null) {
                CustomConnectionPool.getInstance().releaseConnection(conn);
            }
        }
    }

    public void save(User user) throws SQLException, InterruptedException {
        Connection connection = null;
        boolean needsLookup = false;
        String sql = "INSERT INTO users (fullname, username, password) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id), fullname=VALUES(fullname), password=VALUES(password)";
        try {
            connection = CustomConnectionPool.getInstance().getConnection();
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1, user.getFullname());
                preparedStatement.setString(2, user.getUsername());
                preparedStatement.setString(3, user.getPassword());
                preparedStatement.executeUpdate();

                try (ResultSet rs = preparedStatement.getGeneratedKeys()) {
                    if (rs.next()) {
                        user.setId(rs.getInt(1));
                    } else {
                        //database server doesnt send back id for some reason
                        needsLookup = true;
                    }
                }
            }
        } finally {
            if (connection != null) {
                CustomConnectionPool.getInstance().releaseConnection(connection);
            }
        }

        //get the id again
        if (needsLookup) {
            User existing = findByUsername(user.getUsername());
            if (existing != null) {
                user.setId(existing.getId());
            }
        }
    }

    public boolean checkUsernameExists(String username) throws SQLException, InterruptedException {
        Connection connection = null;
        String sql = "SELECT 1 FROM users WHERE username = ? LIMIT 1";
        try {
            connection = CustomConnectionPool.getInstance().getConnection();
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, username);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    return resultSet.next(); //return true if there is a result matching the username, vice versa
                }
            }
        } finally {
            if (connection != null) {
                CustomConnectionPool.getInstance().releaseConnection(connection);
            }
        }
    }
}
