package com.gradecalc.model.Dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
        if (gradeList == null || gradeList.isEmpty()) {
            return;
        }

        Connection conn = null;
        boolean originalAutoCommit = true;
        try {
            conn = CustomConnectionPool.getInstance().getConnection();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            CourseDAO courseDAO = new CourseDAO();
            Map<String, Course> courseMap = courseDAO.findAllAsMap(conn);

            for (Grade grade : gradeList) {
                String courseName = grade.getCourseName();
                Course course = courseMap.get(courseName);
                if (course == null) {
                    course = new Course(courseName, grade.getCredits());
                    courseDAO.save(conn, course);
                    courseMap.put(courseName, course);
                }
                grade.setCourseId(course.getId());
            }

            String sql = "INSERT INTO grades (user_id, course_id, semester, credits, formula, "
                       + "score_bt, score_bv, score_ck, score_da, score_gk, score_qt, score_th, "
                       + "score_10, score_4, score_letter, is_predicted) "
                       + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                       + "ON DUPLICATE KEY UPDATE "
                       + "credits=VALUES(credits), "
                       + "formula=VALUES(formula), "
                       + "score_bt=VALUES(score_bt), score_bv=VALUES(score_bv), score_ck=VALUES(score_ck), "
                       + "score_da=VALUES(score_da), score_gk=VALUES(score_gk), score_qt=VALUES(score_qt), score_th=VALUES(score_th), "
                       + "score_10=VALUES(score_10), score_4=VALUES(score_4), score_letter=VALUES(score_letter), "
                       + "is_predicted=VALUES(is_predicted)";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (Grade grade : gradeList) {
                    ps.setInt(1, userId);
                    ps.setInt(2, grade.getCourseId());
                    ps.setString(3, grade.getSemester());
                    ps.setDouble(4, grade.getCredits());
                    ps.setString(5, grade.getFormula());

                    setDoubleOrNull(ps, 6, grade.getScoreBt());
                    setDoubleOrNull(ps, 7, grade.getScoreBv());
                    setDoubleOrNull(ps, 8, grade.getScoreCk());
                    setDoubleOrNull(ps, 9, grade.getScoreDa());
                    setDoubleOrNull(ps, 10, grade.getScoreGk());
                    setDoubleOrNull(ps, 11, grade.getScoreQt());
                    setDoubleOrNull(ps, 12, grade.getScoreTh());

                    setDoubleOrNull(ps, 13, grade.getScore10());
                    setDoubleOrNull(ps, 14, grade.getScore4());
                    ps.setString(15, grade.getScoreLetter());
                    ps.setBoolean(16, grade.isPredicted());

                    ps.addBatch();
                }
                ps.executeBatch();
            }

            conn.commit();
        } catch (SQLException | RuntimeException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    if (conn.getAutoCommit() != originalAutoCommit) {
                        conn.setAutoCommit(originalAutoCommit);
                    }
                } catch (SQLException ignored) {
                }
            }
            CustomConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    public List<Grade> getGradeByUserId(int userId) throws SQLException, InterruptedException {
        Connection conn = null;
        try {
            conn = CustomConnectionPool.getInstance().getConnection();
            String sql = "SELECT g.*, c.course_name, "
                       + "COALESCE(NULLIF(g.credits, 0), c.credits) AS effective_credits "
                       + "FROM grades g "
                       + "JOIN courses c ON g.course_id = c.id "
                       + "WHERE g.user_id = ? "
                       + "ORDER BY "
                       + "SUBSTRING_INDEX(g.semester, '/', -1) ASC, "
                       + "CAST(SUBSTRING_INDEX(g.semester, '/', 1) AS UNSIGNED) ASC, "
                       + "c.course_name ASC";

            List<Grade> gradeList = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
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
                        grade.setCredits(rs.getDouble("effective_credits"));
                        grade.setPredicted(rs.getBoolean("is_predicted"));

                        if (grade.getCredits() == 0 && (grade.getScoreLetter() == null || grade.getScoreLetter().trim().isEmpty())) {
                            grade.setScoreLetter("Đạt");
                        }

                        gradeList.add(grade);
                    }
                }
            }
            return gradeList;
        } finally {
            CustomConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    public boolean updateGrade(int userId, Grade grade) throws SQLException, InterruptedException {
        Connection conn = null;
        try {
            conn = CustomConnectionPool.getInstance().getConnection();
            String sql = "UPDATE grades SET formula=?, score_bt=?, score_bv=?, score_ck=?, "
                       + "score_da=?, score_gk=?, score_qt=?, score_th=?, "
                       + "score_10=?, score_4=?, score_letter=?, is_predicted=FALSE "
                       + "WHERE id=? AND user_id=?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
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
                ps.setInt(13, userId);
                return ps.executeUpdate() > 0;
            }
        } finally {
            CustomConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    public void updateGradeScoresBatch(int userId, List<Integer> gradeIds, List<Double> scores10,
                                       List<Double> scores4, List<String> letters) throws SQLException, InterruptedException {
        if (gradeIds == null || gradeIds.isEmpty()) {
            return;
        }
        Connection conn = null;
        try {
            conn = CustomConnectionPool.getInstance().getConnection();
            String sql = "UPDATE grades SET score_10=?, score_4=?, score_letter=?, is_predicted=TRUE "
                       + "WHERE id=? AND user_id=?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (int i = 0; i < gradeIds.size(); i++) {
                    setDoubleOrNull(ps, 1, scores10.get(i));
                    setDoubleOrNull(ps, 2, scores4.get(i));
                    ps.setString(3, letters.get(i));
                    ps.setInt(4, gradeIds.get(i));
                    ps.setInt(5, userId);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        } finally {
            CustomConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    public int clearPredictedGrades(int userId) throws SQLException, InterruptedException {
        Connection conn = null;
        try {
            conn = CustomConnectionPool.getInstance().getConnection();
            String sql = "UPDATE grades SET score_10=NULL, score_4=NULL, score_letter=NULL, is_predicted=FALSE "
                       + "WHERE user_id=? AND is_predicted=TRUE";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                return ps.executeUpdate();
            }
        } finally {
            CustomConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    public void addManualGrade(int userId, Grade grade) throws SQLException, InterruptedException {
        saveGradeList(userId, Collections.singletonList(grade));
    }

    public boolean deleteGrade(int userId, int gradeId) throws SQLException, InterruptedException {
        Connection conn = null;
        try {
            conn = CustomConnectionPool.getInstance().getConnection();
            String sql = "DELETE FROM grades WHERE id = ? AND user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, gradeId);
                ps.setInt(2, userId);
                return ps.executeUpdate() > 0;
            }
        } finally {
            CustomConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    static String buildDeleteBatchSql(int count) {
        StringBuilder sb = new StringBuilder("DELETE FROM grades WHERE user_id = ? AND id IN (");
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append('?');
        }
        sb.append(')');
        return sb.toString();
    }

    public int deleteGradesBatch(int userId, List<Integer> gradeIds) throws SQLException, InterruptedException {
        if (gradeIds == null || gradeIds.isEmpty()) {
            return 0;
        }
        Connection conn = null;
        try {
            conn = CustomConnectionPool.getInstance().getConnection();
            String sql = buildDeleteBatchSql(gradeIds.size());
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                for (int i = 0; i < gradeIds.size(); i++) {
                    ps.setInt(i + 2, gradeIds.get(i));
                }
                return ps.executeUpdate();
            }
        } finally {
            CustomConnectionPool.getInstance().releaseConnection(conn);
        }
    }
}
