package com.gradecalc.model.Dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import com.gradecalc.config.CustomConnectionPool;
import com.gradecalc.model.Bean.Course;

public class CourseDAO {

    public Map<String, Course> findAllAsMap() throws SQLException, InterruptedException {
        Connection conn = null;
        try {
            conn = CustomConnectionPool.getInstance().getConnection();
            return findAllAsMap(conn);
        } finally {
            CustomConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    public Map<String, Course> findAllAsMap(Connection conn) throws SQLException {
        Map<String, Course> map = new HashMap<>();
        String sql = "SELECT id, course_name, credits FROM courses";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("course_name");
                double credits = rs.getDouble("credits");
                map.put(name, new Course(id, name, credits));
            }
        }
        return map;
    }

    public Course findByName(String courseName) throws SQLException, InterruptedException {
        Connection conn = null;
        try {
            conn = CustomConnectionPool.getInstance().getConnection();
            return findByName(conn, courseName);
        } finally {
            CustomConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    public Course findByName(Connection conn, String courseName) throws SQLException {
        String sql = "SELECT id, credits FROM courses WHERE course_name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, courseName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Course(rs.getInt("id"), courseName, rs.getDouble("credits"));
                }
                return null;
            }
        }
    }

    public void save(Course course) throws SQLException, InterruptedException {
        Connection conn = null;
        try {
            conn = CustomConnectionPool.getInstance().getConnection();
            save(conn, course);
        } finally {
            CustomConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    /**
     * Chỉ thêm mới môn học vào danh mục chung nếu chưa có; KHÔNG ghi đè credits
     * của bảng chung để tránh làm sai lệch tín chỉ của người dùng khác.
     */
    public void save(Connection conn, Course course) throws SQLException {
        String sql = "INSERT INTO courses (course_name, credits) VALUES (?, ?) "
                   + "ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, course.getCourseName());
            ps.setDouble(2, course.getCredits());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    course.setId(rs.getInt(1));
                    return;
                }
            }
        }

        Course existing = findByName(conn, course.getCourseName());
        if (existing != null) {
            course.setId(existing.getId());
        }
    }
}
