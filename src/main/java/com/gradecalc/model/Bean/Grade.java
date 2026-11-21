package com.gradecalc.model.Bean;

public class Grade {
    private int id;
    private int userId;
    private int courseId;
    private String semester;
    private String formula;

    // 7 cột điểm thành phần chuẩn DUT (Double nullable)
    private Double scoreBt;
    private Double scoreBv;
    private Double scoreCk;
    private Double scoreDa;
    private Double scoreGk;
    private Double scoreQt;
    private Double scoreTh;

    // Các điểm tổng kết (Double nullable)
    private Double score10;
    private Double score4;
    private String scoreLetter;

    // Trường phụ lấy từ bảng courses
    private String courseName;
    private double credits;

    public Grade() {
    }

    public Grade(int id, int userId, int courseId, String semester, String formula,
                 Double scoreBt, Double scoreBv, Double scoreCk, Double scoreDa, Double scoreGk, Double scoreQt, Double scoreTh,
                 Double score10, Double score4, String scoreLetter,
                 String courseName, double credits) {
        this.id = id;
        this.userId = userId;
        this.courseId = courseId;
        this.semester = semester;
        this.formula = formula;
        this.scoreBt = scoreBt;
        this.scoreBv = scoreBv;
        this.scoreCk = scoreCk;
        this.scoreDa = scoreDa;
        this.scoreGk = scoreGk;
        this.scoreQt = scoreQt;
        this.scoreTh = scoreTh;
        this.score10 = score10;
        this.score4 = score4;
        this.scoreLetter = scoreLetter;
        this.courseName = courseName;
        this.credits = credits;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public Double getScoreBt() {
        return scoreBt;
    }

    public void setScoreBt(Double scoreBt) {
        this.scoreBt = scoreBt;
    }

    public Double getScoreBv() {
        return scoreBv;
    }

    public void setScoreBv(Double scoreBv) {
        this.scoreBv = scoreBv;
    }

    public Double getScoreCk() {
        return scoreCk;
    }

    public void setScoreCk(Double scoreCk) {
        this.scoreCk = scoreCk;
    }

    public Double getScoreDa() {
        return scoreDa;
    }

    public void setScoreDa(Double scoreDa) {
        this.scoreDa = scoreDa;
    }

    public Double getScoreGk() {
        return scoreGk;
    }

    public void setScoreGk(Double scoreGk) {
        this.scoreGk = scoreGk;
    }

    public Double getScoreQt() {
        return scoreQt;
    }

    public void setScoreQt(Double scoreQt) {
        this.scoreQt = scoreQt;
    }

    public Double getScoreTh() {
        return scoreTh;
    }

    public void setScoreTh(Double scoreTh) {
        this.scoreTh = scoreTh;
    }

    public Double getScore10() {
        return score10;
    }

    public void setScore10(Double score10) {
        this.score10 = score10;
    }

    public Double getScore4() {
        return score4;
    }

    public void setScore4(Double score4) {
        this.score4 = score4;
    }

    public String getScoreLetter() {
        return scoreLetter;
    }

    public void setScoreLetter(String scoreLetter) {
        this.scoreLetter = scoreLetter;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public double getCredits() {
        return credits;
    }

    public void setCredits(double credits) {
        this.credits = credits;
    }
}
