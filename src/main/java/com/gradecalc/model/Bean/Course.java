package com.gradecalc.model.Bean;

public class Course {
    private int id;
    private String courseCode;
    private String courseName;
    private int credits;

    public Course(){

    }
    public Course(String courseCode, String courseName, int credits){
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.credits = credits;
    }
    public Course(int id, String courseCode, String courseName, int credits) {
        this.id = id;
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
    public String getCourse_code() {
        return courseCode;
    }
    public void setCourse_code(String course_code) {
        this.courseCode = course_code;
    }
    public String getCourse_name() {
        return courseName;
    }
    public void setCourse_name(String course_name) {
        this.courseName = course_name;
    }
    public int getCredits() {
        return credits;
    }
    public void setCredits(int credits) {
        this.credits = credits;
    }

    
}
