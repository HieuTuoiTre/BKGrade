package com.gradecalc.model.Dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.gradecalc.config.CustomConnectionPool;
import com.gradecalc.model.Bean.Course;
import com.gradecalc.model.Bean.Grade;

public class GradeDAO {

    private void setDoubleOrNull(PreparedStatement ps, int paramIndex, Double value) throws SQLException {
        if (value != null) {
            ps.setDouble(paramIndex, value);
        } else {
            ps.setNull(paramIndex, Types.DOUBLE);
        }
    }

    private Double getDoubleOrNull(ResultSet rs, String colName) throws SQLException {
        double val = rs.getDouble(colName);
        return rs.wasNull() ? null : val;
    }

    public void saveGradeList(int userId, List<Grade> gradeList) throws SQLException, InterruptedException {
        Connection conn = null;
        try {
            conn = CustomConnectionPool.getInstance().getConnection();
            String sql = "INSERT INTO grades (user_id, course_id, semester, formula, " +
                         "score_bt, score_bv, score_ck, score_da, score_gk, score_qt, score_th, " +
                         "score_10, score_4, score_letter) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                         "ON DUPLICATE KEY UPDATE " +
                         "formula=VALUES(formula), " +
                         "score_bt=VALUES(score_bt), score_bv=VALUES(score_bv), score_ck=VALUES(score_ck), " +
                         "score_da=VALUES(score_da), score_gk=VALUES(score_gk), score_qt=VALUES(score_qt), score_th=VALUES(score_th), " +
                         "score_10=VALUES(score_10), score_4=VALUES(score_4), score_letter=VALUES(score_letter)";

            PreparedStatement ps = conn.prepareStatement(sql);
            CourseDAO courseDAO = new CourseDAO();

            for (Grade grade : gradeList) {
                // 1. Tìm hoặc tạo mới môn học theo tên môn
                Course course = courseDAO.findByName(grade.getCourseName());
                if (course == null) {
                    course = new Course(grade.getCourseName(), grade.getCredits());
                    courseDAO.save(course);
                } else if (course.getCredits() != grade.getCredits()) {
                    course.setCredits(grade.getCredits());
                    courseDAO.save(course);
                }
                grade.setCourseId(course.getId());

                // 2. Điền tham số
                ps.setInt(1, userId);
                ps.setInt(2, grade.getCourseId());
                ps.setString(3, grade.getSemester());
                ps.setString(4, grade.getFormula());

                setDoubleOrNull(ps, 5, grade.getScoreBt());
                setDoubleOrNull(ps, 6, grade.getScoreBv());
                setDoubleOrNull(ps, 7, grade.getScoreCk());
                setDoubleOrNull(ps, 8, grade.getScoreDa());
                setDoubleOrNull(ps, 9, grade.getScoreGk());
                setDoubleOrNull(ps, 10, grade.getScoreQt());
                setDoubleOrNull(ps, 11, grade.getScoreTh());

                setDoubleOrNull(ps, 12, grade.getScore10());
                setDoubleOrNull(ps, 13, grade.getScore4());
                ps.setString(14, grade.getScoreLetter());

                ps.addBatch();
            }
            ps.executeBatch();
        } finally {
            CustomConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    public List<Grade> getGradeByUserId(int userId) throws SQLException, InterruptedException {
        Connection conn = null;
        try {
            conn = CustomConnectionPool.getInstance().getConnection();
            String sql = "SELECT g.*, c.course_name, c.credits " +
                         "FROM grades g " +
                         "JOIN courses c ON g.course_id = c.id " +
                         "WHERE g.user_id = ? " +
                         "ORDER BY " +
                         "SUBSTRING_INDEX(g.semester, '/', -1) ASC, " +
                         "CAST(SUBSTRING_INDEX(g.semester, '/', 1) AS UNSIGNED) ASC, " +
                         "c.course_name ASC";
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

                grade.setScoreBt(getDoubleOrNull(rs, "score_bt"));
                grade.setScoreBv(getDoubleOrNull(rs, "score_bv"));
                grade.setScoreCk(getDoubleOrNull(rs, "score_ck"));
                grade.setScoreDa(getDoubleOrNull(rs, "score_da"));
                grade.setScoreGk(getDoubleOrNull(rs, "score_gk"));
                grade.setScoreQt(getDoubleOrNull(rs, "score_qt"));
                grade.setScoreTh(getDoubleOrNull(rs, "score_th"));

                grade.setScore10(getDoubleOrNull(rs, "score_10"));
                grade.setScore4(getDoubleOrNull(rs, "score_4"));
                grade.setScoreLetter(rs.getString("score_letter"));

                grade.setCourseName(rs.getString("course_name"));
                grade.setCredits(rs.getDouble("credits"));

                // Xử lý các môn 0 tín chỉ (GDTC/GDQP/Môn điều kiện) đã học xong nhưng không có điểm chữ -> Mặc định là "Đạt"
                if (grade.getCredits() == 0 && (grade.getScoreLetter() == null || grade.getScoreLetter().trim().isEmpty())) {
                    grade.setScoreLetter("Đạt");
                }

                gradeList.add(grade);
            }
            return gradeList;
        } finally {
            CustomConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    public void updateGrade(Grade grade) throws SQLException, InterruptedException {
        Connection conn = null;
        try {
            conn = CustomConnectionPool.getInstance().getConnection();
            String sql = "UPDATE grades SET formula=?, score_bt=?, score_bv=?, score_ck=?, " +
                         "score_da=?, score_gk=?, score_qt=?, score_th=?, " +
                         "score_10=?, score_4=?, score_letter=? WHERE id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, grade.getFormula());
            setDoubleOrNull(ps, 2, grade.getScoreBt());
            setDoubleOrNull(ps, 3, grade.getScoreBv());
            setDoubleOrNull(ps, 4, grade.getScoreCk());
            setDoubleOrNull(ps, 5, grade.getScoreDa());
            setDoubleOrNull(ps, 6, grade.getScoreGk());
            setDoubleOrNull(ps, 7, grade.getScoreQt());
            setDoubleOrNull(ps, 8, grade.getScoreTh());
            setDoubleOrNull(ps, 9, grade.getScore10());
            setDoubleOrNull(ps, 10, grade.getScore4());
            ps.setString(11, grade.getScoreLetter());
            ps.setInt(12, grade.getId());
            ps.executeUpdate();
        } finally {
            CustomConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    public void updateGradeScores(int gradeId, Double score10, Double score4, String scoreLetter) throws SQLException, InterruptedException {
        Connection conn = null;
        try {
            conn = CustomConnectionPool.getInstance().getConnection();
            String sql = "UPDATE grades SET score_10=?, score_4=?, score_letter=? WHERE id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            setDoubleOrNull(ps, 1, score10);
            setDoubleOrNull(ps, 2, score4);
            ps.setString(3, scoreLetter);
            ps.setInt(4, gradeId);
            ps.executeUpdate();
        } finally {
            CustomConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    public void addManualGrade(int userId, Grade grade) throws SQLException, InterruptedException {
        CourseDAO courseDAO = new CourseDAO();
        Course course = courseDAO.findByName(grade.getCourseName());
        if (course == null) {
            course = new Course(grade.getCourseName(), grade.getCredits());
            courseDAO.save(course);
        } else if (course.getCredits() != grade.getCredits()) {
            course.setCredits(grade.getCredits());
            courseDAO.save(course);
        }
        grade.setCourseId(course.getId());

        List<Grade> list = new ArrayList<>();
        list.add(grade);
        saveGradeList(userId, list);
    }

    public void deleteGrade(int gradeId) throws SQLException, InterruptedException {
        Connection conn = null;
        try {
            conn = CustomConnectionPool.getInstance().getConnection();
            String sql = "DELETE FROM grades WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, gradeId);
            ps.executeUpdate();
        } finally {
            CustomConnectionPool.getInstance().releaseConnection(conn);
        }
    }
}
