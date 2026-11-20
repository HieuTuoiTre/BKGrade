package com.gradecalc.model.Dao;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.gradecalc.config.CustomConnectionPool;
import com.gradecalc.model.Bean.Course;

public class CourseDAO {
    public Course findByCode(String courseCode) throws SQLException, InterruptedException{
        Connection conn = null;
        try {
            conn = CustomConnectionPool.getInstance().getConnection();
            String sql = "SELECT * FROM courses WHERE course_code = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, courseCode);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                int id = rs.getInt("id");
                String courseName = rs.getString("course_name");
                int credits = rs.getInt("credits");
                Course course = new Course(id,courseCode,courseName,credits);
                return course;
            }else
                return null;
        }finally{
            CustomConnectionPool.getInstance().releaseConnection(conn);
        }
    }
    public void save(Course course) throws SQLException, InterruptedException{
        Connection conn = null;
        try {
            conn = CustomConnectionPool.getInstance().getConnection();
            String sql = "INSERT INTO courses (course_code, course_name, credits) VALUES (?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, course.getCourseCode());
            ps.setString(2, course.getCourseName());
            ps.setInt(3, course.getCredits());
            ps.execute();
            ResultSet generatedKeys = ps.getGeneratedKeys();
            if(generatedKeys.next()){
                course.setId(generatedKeys.getInt(1));
            }
        }finally {
            CustomConnectionPool.getInstance().releaseConnection(conn);
        }
    }
}
