package com.gradecalc.model.Bo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.gradecalc.model.Bean.Grade;

public class DutParserBO {
    // Các điểm chữ hợp lệ
    private static final Set<String> VALID_GRADES = new HashSet<>(Arrays.asList("A+", "A", "B+", "B", "C+", "C", "D+", "D", "F"));

    public List<Grade> parse(String rawText) {
        List<Grade> gradeList = new ArrayList<>();
        if(rawText == null || rawText.trim().isEmpty()){
            return  gradeList;
        }

        // 1. Tách thành từng dòng
        String[] lines = rawText.split("\\r?\\n");

        for(String line : lines){
            // Bỏ qua dòng trống
            if(line.trim().isEmpty()) continue;

            // 2. Tách từng dòng thành mảng cột theo thứ tự Tab
            String[] cols = line.split("\\t", -1);

            // Bỏ qua dòng có ít hơn 10 cột (Không phải dòng môn học hợp lệ)
            if(cols.length < 10) continue;

            try{
                // 3. Đọc các cột cố định
                String semester = cols[1].trim();
                String courseCode = cols[3].trim();
                String courseName = cols[4].trim();
                int credits = Integer.parseInt(cols[5].trim());
                String formula = cols[6].trim();

                // 4. Thuật toán tìm điểm chữ, điểm 4, điểm 10.
                String scoreLetter = "";
                double score4 = 0.0;
                double score10 = 0.0;

                for(int i = cols.length - 1; i >= 7; i--){
                    String cell = cols[i].trim();
                    if(VALID_GRADES.contains(cell)){
                        scoreLetter = cell; // Lấy điểm chữ
                        // Lùi 1 bước lấy điểm hệ 4
                        score4 = parseDouble(cols[i - 1]);
                        // Lùi 2 bước để lấy điểm hệ 10
                        score10 = parseDouble(cols[i - 2]);

                        break;
                    }
                }
                //Bỏ qua dòng không tìm được điểm chữ hợp lệ
                if(scoreLetter.isEmpty()) continue;

                // 5. Đóng gói vào Grade
                Grade grade = new Grade();
                grade.setSemester(semester);
                grade.setCourseCode(courseCode);
                grade.setCourseName(courseName);
                grade.setCredits(credits);
                grade.setFormula(formula);
                grade.setScore10(score10);
                grade.setScore4(score4);
                grade.setScoreLetter(scoreLetter);

                gradeList.add(grade);

            }catch (NumberFormatException | ArrayIndexOutOfBoundsException e){
                // Bỏ qua dòng lỗi, tiếp tục dòng tiếp theo
                continue;
            }
        }
        return gradeList;
    }

    // Hàm phụ : parse sang Double, trả về 0.0 nếu lỗi hoặc trống
    private double parseDouble(String s){
        try{
            return Double.parseDouble(s.trim());
        }catch (NumberFormatException e){
            return 0.0;
        }
    }
    public static void main(String[] args) {
        String testData = "38\t2/2025-2026\t\t2090170.2521.yy.92\tLịch sử Đảng Cộng sản Việt Nam\t2\t[GK]*0.20+[BT]*0.20+[CK]*0.60\t10.0\t\t8.0\t\t9.5\t\t\t8.7\t4.0\tA\t\t\r\n" +
                        "37\t2/2025-2026\t\t1020252.2520.24.12\tCông nghệ phần mềm\t2\t[GK]*0.20+[BT]*0.20+[CK]*0.60\t8.5\t\t9.1\t\t9.2\t\t\t9.0\t4.0\tA\t\t";

        DutParserBO parser = new DutParserBO();
        List<Grade> result = parser.parse(testData);

        System.out.println("====== KET QUA BOC TACH BANG DIEM DUT ======");
        System.out.println("Tổng số môn bóc tách được: " + result.size());
        System.out.println("--------------------------------------------------------------------------------------------------");
        System.out.printf("%-12s | %-18s | %-30s | %-4s | %-6s | %-6s | %-4s%n",
                "Học kỳ", "Mã môn", "Tên môn học", "TC", "Điểm10", "Điểm4", "Chữ");
        System.out.println("--------------------------------------------------------------------------------------------------");

        for (Grade g : result) {
            System.out.printf("%-12s | %-18s | %-30s | %-4d | %-6.1f | %-6.1f | %-4s%n",
                    g.getSemester(),
                    g.getCourseCode(),
                    g.getCourseName(),
                    g.getCredits(),
                    g.getScore10(),
                    g.getScore4(),
                    g.getScoreLetter());
        }
        System.out.println("--------------------------------------------------------------------------------------------------");
    }
}
