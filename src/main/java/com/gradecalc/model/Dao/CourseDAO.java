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
    /**
     * Tối ưu: Lấy toàn bộ danh mục môn học về nạp vào Map trong RAM chỉ với 1 truy vấn.
     */
    public Map<String, Course> findAllAsMap() throws SQLException, InterruptedException {
        Map<String, Course> map = new HashMap<>();
        Connection conn = null;
        try {
            conn = CustomConnectionPool.getInstance().getConnection();
            String sql = "SELECT id, course_name, credits FROM courses";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("course_name");
                double credits = rs.getDouble("credits");
                map.put(name, new Course(id, name, credits));
            }
        } finally {
            CustomConnectionPool.getInstance().releaseConnection(conn);
        }
        return map;
    }

    public Course findByName(String courseName) throws SQLException, InterruptedException {
        Connection conn = null;
        try {
            conn = CustomConnectionPool.getInstance().getConnection();
            String sql = "SELECT * FROM courses WHERE course_name = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, courseName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("id");
                double credits = rs.getDouble("credits");
                return new Course(id, courseName, credits);
            } else {
                return null;
            }
        } finally {
            CustomConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    public void save(Course course) throws SQLException, InterruptedException {
        Connection conn = null;
        try {
            conn = CustomConnectionPool.getInstance().getConnection();
            String sql = "INSERT INTO courses (course_name, credits) VALUES (?, ?) " +
                         "ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id), credits=VALUES(credits)";
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, course.getCourseName());
            ps.setDouble(2, course.getCredits());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                course.setId(rs.getInt(1));
            } else {
                Course existing = findByName(course.getCourseName());
                if (existing != null) {
                    course.setId(existing.getId());
                }
            }
        } finally {
            CustomConnectionPool.getInstance().releaseConnection(conn);
        }
    }
}
