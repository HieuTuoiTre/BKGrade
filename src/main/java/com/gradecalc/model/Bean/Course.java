package com.gradecalc.model.Bean;

public class Course {
    private int id;
    private String courseName;
    private double credits;

    public Course() {
    }

    public Course(String courseName, double credits) {
        this.courseName = courseName;
        this.credits = credits;
    }

    public Course(int id, String courseName, double credits) {
        this.id = id;
        this.courseName = courseName;
        this.credits = credits;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
