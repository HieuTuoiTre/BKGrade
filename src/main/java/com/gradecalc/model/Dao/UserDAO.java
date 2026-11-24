package com.gradecalc.model.Dao;
import com.gradecalc.config.CustomConnectionPool;
import com.gradecalc.model.Bean.Course;
import com.gradecalc.model.Bean.User;

import java.awt.*;
import java.sql.*;

public class UserDAO {
    public void save(User user) throws SQLException, InterruptedException {
        Connection connection = null;
        boolean needsLookup = false;
        String sql = "INSERT INTO users (fullname, username, password) VALUES (?, ?, ?)" +
                     "ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id), fullname=VALUES(fullname), username=VALUES(username), password=VALUES(password)";
        try{
            connection = CustomConnectionPool.getInstance().getConnection();
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)){
                preparedStatement.setString(1, user.getFullname());
                preparedStatement.setString(2, user.getUsername());
                preparedStatement.setString(3, user.getPassword());

                try (ResultSet resultSet = preparedStatement.getGeneratedKeys()){
                    if (resultSet.next()){
                        user.setId(resultSet.getInt(1));
                    } else {
                        needsLookup = true;
                    }
                }
            }
        } finally {
            if (connection != null){
                CustomConnectionPool.getInstance().releaseConnection(connection);
            }

            if (needsLookup){
                User existing = findByUsername(user.getUsername());
                if (existing != null){
                    user.setId(existing.getId());
                }
            }
        }

    }

    User findByUsername(String username) throws SQLException, InterruptedException{
        Connection connection = null;
        String sql = "SELECT * FROM users WHERE username = ?";
        try {
            connection = CustomConnectionPool.getInstance().getConnection();
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, username);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        User user = new User();
                        user.setId(resultSet.getInt("id"));
                        user.setUsername(resultSet.getString("username"));
                        user.setPassword(resultSet.getString("password"));
                        user.setFullname(resultSet.getString("fullname"));
                        return user;
                    }
                    return null;
                }
            }
        } finally {
            if (connection != null) {
                CustomConnectionPool.getInstance().releaseConnection(connection);
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
                    return resultSet.next();
                }
            }
        } finally {
            if (connection != null) {
                CustomConnectionPool.getInstance().releaseConnection(connection);
            }
        }
    }
}
