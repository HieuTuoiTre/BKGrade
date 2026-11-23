package com.gradecalc.model.Bo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.gradecalc.model.Bean.Grade;

public class DutParserBO {
    private static final Set<String> VALID_GRADES = new HashSet<>(
            Arrays.asList("A+", "A", "B+", "B", "C+", "C", "D+", "D", "F")
    );

    public List<Grade> parse(String rawText) {
        List<Grade> gradeList = new ArrayList<>();
        if (rawText == null || rawText.trim().isEmpty()) {
            return gradeList;
        }

        String[] lines = rawText.split("\\r?\\n");

        for (String line : lines) {
            if (line.trim().isEmpty()) continue;

            // Bỏ qua dòng tiêu đề bảng nếu có copy trúng
            if (line.contains("Mã lớp học phần") || line.contains("Tên lớp học phần") || line.contains("Công thức điểm")) {
                continue;
            }

            String[] cols = line.split("\\t", -1);
            if (cols.length < 6) continue;

            try {
                // Các cột: TT(0), Kỳ(1), Kỳ phụ(2), Mã HP(3), Tên HP(4), Số TC(5), Công thức(6)
                String semester = cols[1].trim();
                String courseName = cols[4].trim();

                if (courseName.isEmpty()) {
                    continue;
                }

                double credits = Double.parseDouble(cols[5].trim());
                String formula = cols.length > 6 ? cols[6].trim() : "";

                // 7 cột điểm thành phần chuẩn DUT (cột 7..13)
                Double scoreBt = getDoubleCol(cols, 7);
                Double scoreBv = getDoubleCol(cols, 8);
                Double scoreCk = getDoubleCol(cols, 9);
                Double scoreDa = getDoubleCol(cols, 10);
                Double scoreGk = getDoubleCol(cols, 11);
                Double scoreQt = getDoubleCol(cols, 12);
                Double scoreTh = getDoubleCol(cols, 13);

                // Các cột điểm tổng kết (cột 14: điểm 10, cột 15: điểm 4, cột 16: điểm chữ)
                Double score10 = getDoubleCol(cols, 14);
                Double score4 = getDoubleCol(cols, 15);
                String scoreLetter = getStringCol(cols, 16);

                // Kiểm tra điểm chữ hợp lệ
                if (scoreLetter != null && VALID_GRADES.contains(scoreLetter.toUpperCase())) {
                    scoreLetter = scoreLetter.toUpperCase();
                } else {
                    // Nếu cột 16 không phải điểm chữ, quét ngược từ cuối dòng để tìm xem có cột điểm chữ không
                    boolean found = false;
                    for (int i = cols.length - 1; i >= 7; i--) {
                        String cell = cols[i].trim().toUpperCase();
                        if (VALID_GRADES.contains(cell)) {
                            scoreLetter = cell;
                            score4 = getDoubleCol(cols, i - 1);
                            score10 = getDoubleCol(cols, i - 2);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        // Nếu là môn 0 tín chỉ (GDTC/GDQP/Môn điều kiện) đã học xong nhưng không có điểm chữ -> Ghi nhận "Đạt"
                        if (credits == 0) {
                            scoreLetter = "Đạt";
                        } else {
                            // Môn học chuyên ngành đang học (chưa có điểm chữ / điểm tổng kết)
                            scoreLetter = null;
                            score4 = null;
                            score10 = null;
                        }
                    }
                }

                // Nếu chưa có điểm 10 nhưng đã có công thức và điểm thành phần -> Tự tính bằng Engine
                if (score10 == null && !formula.isEmpty()) {
                    Double calculated = GradeCalculatorBO.evaluateFormula(formula, scoreBt, scoreBv, scoreCk, scoreDa, scoreGk, scoreQt, scoreTh);
                    if (calculated != null) {
                        score10 = calculated;
                        score4 = GradeCalculatorBO.convertScore10ToScore4(calculated);
                        scoreLetter = GradeCalculatorBO.convertScore10ToLetter(calculated);
                    }
                }

                Grade grade = new Grade();
                grade.setSemester(semester);
                grade.setCourseName(courseName);
                grade.setCredits(credits);
                grade.setFormula(formula);

                grade.setScoreBt(scoreBt);
                grade.setScoreBv(scoreBv);
                grade.setScoreCk(scoreCk);
                grade.setScoreDa(scoreDa);
                grade.setScoreGk(scoreGk);
                grade.setScoreQt(scoreQt);
                grade.setScoreTh(scoreTh);

                grade.setScore10(score10);
                grade.setScore4(score4);
                grade.setScoreLetter(scoreLetter);

                gradeList.add(grade);

            } catch (Exception e) {
                // Bỏ qua dòng lỗi định dạng và tiếp tục dòng tiếp theo
                continue;
            }
        }
        return gradeList;
    }

    private Double getDoubleCol(String[] cols, int index) {
        if (index >= cols.length) return null;
        String val = cols[index].trim();
        if (val.isEmpty()) return null;
        try {
            return Double.parseDouble(val);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String getStringCol(String[] cols, int index) {
        if (index >= cols.length) return null;
        String val = cols[index].trim();
        return val.isEmpty() ? null : val;
    }
}
