package com.gradecalc.model.Dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.gradecalc.config.CustomConnectionPool;
import com.gradecalc.model.Bean.Course;
import com.gradecalc.model.Bean.Grade;

public class GradeDAO {
    public void saveGradeList(int userId, List<Grade> gradeList) throws SQLException, InterruptedException{
        Connection conn = null;
        try {
            conn = CustomConnectionPool.getInstance().getConnection();
            String sql = "INSERT INTO grades (user_id, course_id, semester, formula, score_10, score_4, score_letter) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                         "ON DUPLICATE KEY UPDATE score_10=VALUES(score_10), score_4=VALUES(score_4), score_letter=VALUES(score_letter)";
            PreparedStatement ps = conn.prepareStatement(sql);
            CourseDAO courseDAO = new CourseDAO();
            for(Grade grade : gradeList){
                // 1. Tìm xem môn học này đã có trong bảng courses chưa
                Course course = courseDAO.findByCode(grade.getCourseCode());
                    if (course == null) {
                        // Chưa có -> Tạo mới và lưu vào DB để lấy ID tự tăng
                        course = new Course(grade.getCourseCode(),grade.getCourseName(),grade.getCredits());
                        courseDAO.save(course);
                    }
                // 2. Gán ID vừa tìm được/tạo được cho grade
                grade.setCourseId(course.getId());
                
                ps.setInt(1, userId);
                ps.setInt(2, grade.getCourseId());
                ps.setString(3, grade.getSemester());
                ps.setString(4, grade.getFormula());
                ps.setDouble(5, grade.getScore10());
                ps.setDouble(6, grade.getScore4());
                ps.setString(7, grade.getScoreLetter());
                ps.addBatch();
            }
            ps.executeBatch();
        }finally{
            CustomConnectionPool.getInstance().releaseConnection(conn);
        }
    }
    public List<Grade> getGradeByUserId(int userId) throws SQLException, InterruptedException{
        Connection conn = null;
        try{
            conn = CustomConnectionPool.getInstance().getConnection();
            String sql = "SELECT g.*, c.course_code, c.course_name, c.credits " +
                     "FROM grades g " +
                     "JOIN courses c ON g.course_id = c.id " +
                     "WHERE g.user_id = ? " +
                     "ORDER BY g.semester, c.course_name";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            List<Grade> gradeList = new ArrayList<>();
            while (rs.next()) {
                Grade grade = new Grade();

                grade.setId(rs.getInt("id"));
                grade.setUserId(userId);
                grade.setCourseId(rs.getInt("course_id"));
                grade.setSemester(rs.getString("semester"));
                grade.setFormula(rs.getString("formula"));
                grade.setScore10(rs.getDouble("score_10"));
                grade.setScore4(rs.getDouble("score_4"));
                grade.setScoreLetter(rs.getString("score_letter"));
                // Các cột bảng Coures
                grade.setCourseCode(rs.getString("course_code"));
                grade.setCourseName(rs.getString("course_name"));
                grade.setCredits(rs.getInt("credits"));

                gradeList.add(grade);
            }
            return gradeList;
        }finally{
            CustomConnectionPool.getInstance().releaseConnection(conn);
        }
    }
}
