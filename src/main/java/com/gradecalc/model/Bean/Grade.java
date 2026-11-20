package com.gradecalc.model.Bean;

public class Grade {
    private int id;
    private int userId;
    private int courseId;
    private String semester;
    private String formula;
    private double score10;
    private double score4;
    private String scoreLetter;
    // Trường phụ
    private String courseCode;
    private String courseName;
    private int credits;
    public Grade() {
    }
    public Grade(int id, int userId, int courseId, String semester, String formula, double score10, double score4,
            String scoreLetter, String courseCode, String courseName, int credits) {
        this.id = id;
        this.userId = userId;
        this.courseId = courseId;
        this.semester = semester;
        this.formula = formula;
        this.score10 = score10;
        this.score4 = score4;
        this.scoreLetter = scoreLetter;
        this.courseCode = courseCode;
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
    public double getScore10() {
        return score10;
    }
    public void setScore10(double score10) {
        this.score10 = score10;
    }
    public double getScore4() {
        return score4;
    }
    public void setScore4(double score4) {
        this.score4 = score4;
    }
    public String getScoreLetter() {
        return scoreLetter;
    }
    public void setScoreLetter(String scoreLetter) {
        this.scoreLetter = scoreLetter;
    }
    public String getCourseCode() {
        return courseCode;
    }
    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }
    public String getCourseName() {
        return courseName;
    }
    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }
    public int getCredits() {
        return credits;
    }
    public void setCredits(int credits) {
        this.credits = credits;
    }
    
}
