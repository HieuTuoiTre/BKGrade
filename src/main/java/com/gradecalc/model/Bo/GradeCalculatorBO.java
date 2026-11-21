package com.gradecalc.model.Bo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gradecalc.model.Bean.Grade;
import com.gradecalc.model.Bean.SemesterSummary;

public class GradeCalculatorBO {
    public SemesterSummary calculate(List<Grade> gradeList){
        int totalCredits = 0;
        double weightedSum10 = 0.0;
        double weightedSum4 = 0.0;
        for(Grade grade : gradeList){
            // Bỏ qua môn 0 tín
            if(grade.getCredits() == 0) continue;
            
            totalCredits += grade.getCredits();
            weightedSum10 += grade.getScore10() * grade.getCredits();
            weightedSum4 += grade.getScore4() * grade.getCredits();
        }
        if(totalCredits == 0) {
            return new SemesterSummary("Toàn khóa", 0, 0.0, 0.0, "N/A");
        }
        double gpa10 = Math.round((weightedSum10 / totalCredits) * 100.0) / 100.0;
        double gpa4 = Math.round((weightedSum4 / totalCredits) * 100.0) / 100.0;
        String rank = getAcademicRank(gpa4);

        return new SemesterSummary("Toàn khóa", totalCredits, gpa10, gpa4, rank);
    }

    private String getAcademicRank(double gpa4){
        if (gpa4 >= 3.60) return "Xuất sắc";
        else if (gpa4 >= 3.20) return "Giỏi";
        else if (gpa4 >= 2.50) return "Khá";
        else if (gpa4 >= 2.00) return "Trung bình";
        else return "Yếu - Cảnh báo học vụ";
    }
    
    public List<SemesterSummary> calculateBySemester(List<Grade> gradeList){
        // 1. Gom môn theo học kì
        Map<String, List<Grade>> bySemester = new HashMap<>();
        for(Grade g : gradeList){
            bySemester.computeIfAbsent(g.getSemester(), k -> new ArrayList<>()).add(g);
        }

        // 2. Tính GPA từng kỳ
        List<SemesterSummary> summaries = new ArrayList<>();
        for (Map.Entry<String, List<Grade>> entry : bySemester.entrySet()) {
            SemesterSummary s = calculate(entry.getValue());
            s.setSemester(entry.getKey()); // Gán đúng tên học kì
            summaries.add(s);
        }
        return summaries;
    }

    public static void main(String[] args) {
    // Giả lập kết quả đã bóc tách từ DutParserBO
    // (Tạo thủ công 3-4 đối tượng Grade với số TC và điểm 4 khác nhau)
    List<Grade> mockGrades = new ArrayList<>();

    Grade g1 = new Grade();
    g1.setCourseName("Nhập môn ngành");   g1.setCredits(2); g1.setScore10(8.2); g1.setScore4(3.5); g1.setScoreLetter("B+"); g1.setSemester("1/2024-2025");
    
    Grade g2 = new Grade();
    g2.setCourseName("Triết học Mác-Lênin"); g2.setCredits(3); g2.setScore10(7.3); g2.setScore4(3.0); g2.setScoreLetter("B"); g2.setSemester("1/2024-2025");
    
    Grade g3 = new Grade();
    g3.setCourseName("Giáo dục thể chất 1"); g3.setCredits(0); g3.setScore10(9.0); g3.setScore4(4.0); g3.setScoreLetter("A"); g3.setSemester("1/2024-2025");

    mockGrades.add(g1); mockGrades.add(g2); mockGrades.add(g3);

    GradeCalculatorBO calc = new GradeCalculatorBO();
    SemesterSummary result = calc.calculate(mockGrades);

    System.out.println("Tổng TC: " + result.getTotalCredits());        // Phải là 5 (loại g3 có 0 TC)
    System.out.println("GPA hệ 10: " + result.getGpa10());
    System.out.println("GPA hệ 4: " + result.getGpa4());
    System.out.println("Xếp loại: " + result.getAcademicRank());
}
}
