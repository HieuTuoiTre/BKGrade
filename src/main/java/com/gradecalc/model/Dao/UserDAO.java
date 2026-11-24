package com.gradecalc.model.Dao;
import com.gradecalc.config.CustomConnectionPool;
import com.gradecalc.model.Bean.Course;
import com.gradecalc.model.Bean.User;

import java.sql.*;

public class UserDAO {
    void save(User user) throws SQLException, InterruptedException {
        Connection connection = null;
        String sql = "INSERT INTO users (fullname, username, password) VALUES (?, ?, ?)" +
                     "ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id), fullname=VALUES(fullname), username=VALUES(username), password=VALUES(password)";
        try{
            connection = CustomConnectionPool.getInstance().getConnection();
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)){

            }

        } finally {
            if (connection != null){
                CustomConnectionPool.getInstance().releaseConnection(connection);
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

    boolean checkUsernameExists(String username){
        return true;
    }
}
