package com.gradecalc.model.Bean;

public class SemesterSummary {
    private String semester;
    private double totalCredits;
    private double gpa10;
    private double gpa4;
    private String academicRank;

    public SemesterSummary() {
    }

    public SemesterSummary(String semester, double totalCredits, double gpa10, double gpa4, String academicRank) {
        this.semester = semester;
        this.totalCredits = totalCredits;
        this.gpa10 = gpa10;
        this.gpa4 = gpa4;
        this.academicRank = academicRank;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public double getTotalCredits() {
        return totalCredits;
    }

    public void setTotalCredits(double totalCredits) {
        this.totalCredits = totalCredits;
    }

    public double getGpa10() {
        return gpa10;
    }

    public void setGpa10(double gpa10) {
        this.gpa10 = gpa10;
    }

    public double getGpa4() {
        return gpa4;
    }

    public void setGpa4(double gpa4) {
        this.gpa4 = gpa4;
    }

    public String getAcademicRank() {
        return academicRank;
    }

    public void setAcademicRank(String academicRank) {
        this.academicRank = academicRank;
    }
}
