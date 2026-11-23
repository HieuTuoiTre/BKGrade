package com.gradecalc.model.Bo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gradecalc.model.Bean.Grade;
import com.gradecalc.model.Bean.SemesterSummary;

public class GradeCalculatorBO {

    // Regex tìm từng số hạng trong công thức DUT, ví dụ: [GK]*0.20 hoặc [BT]*0.2 hoặc [CK]*1.00
    private static final Pattern FORMULA_TERM_PATTERN = Pattern.compile("\\[([A-Za-z0-9_]+)\\]\\s*\\*\\s*([0-9.]+)");

    /**
     * Đánh giá công thức tính điểm và trả về điểm hệ 10 (làm tròn 1 chữ số thập phân).
     * Nếu thiếu bất kỳ điểm thành phần nào trong công thức -> trả về null (môn chưa đủ điểm).
     */
    public static Double evaluateFormula(String formula, Double bt, Double bv, Double ck,
                                         Double da, Double gk, Double qt, Double th) {
        if (formula == null || formula.trim().isEmpty()) {
            return null;
        }

        Map<String, Double> scoreMap = new HashMap<>();
        if (bt != null) scoreMap.put("BT", bt);
        if (bv != null) scoreMap.put("BV", bv);
        if (ck != null) scoreMap.put("CK", ck);
        if (da != null) scoreMap.put("DA", da);
        if (gk != null) scoreMap.put("GK", gk);
        if (qt != null) scoreMap.put("QT", qt);
        if (th != null) scoreMap.put("TH", th);

        Matcher matcher = FORMULA_TERM_PATTERN.matcher(formula);
        double sum = 0.0;
        double totalWeight = 0.0;
        boolean hasTerms = false;

        while (matcher.find()) {
            hasTerms = true;
            String component = matcher.group(1).toUpperCase();
            double weight = Double.parseDouble(matcher.group(2));
            totalWeight += weight;

            Double score = scoreMap.get(component);
            if (score == null) {
                // Điểm thành phần này chưa có -> Chưa thể tính điểm tổng kết
                return null;
            }
            sum += score * weight;
        }

        if (!hasTerms || totalWeight <= 0) {
            return null;
        }

        // Làm tròn 1 chữ số thập phân chuẩn DUT
        return Math.round(sum * 10.0) / 10.0;
    }

    /**
     * Quy đổi điểm hệ 10 sang điểm hệ 4 theo quy chế tín chỉ DUT.
     */
    public static Double convertScore10ToScore4(Double score10) {
        if (score10 == null) return null;
        if (score10 >= 8.5) return 4.0;
        if (score10 >= 8.0) return 3.5;
        if (score10 >= 7.0) return 3.0;
        if (score10 >= 6.5) return 2.5;
        if (score10 >= 5.5) return 2.0;
        if (score10 >= 5.0) return 1.5;
        if (score10 >= 4.0) return 1.0;
        return 0.0;
    }

    /**
     * Quy đổi điểm hệ 10 sang điểm chữ theo chuẩn DUT.
     */
    public static String convertScore10ToLetter(Double score10) {
        if (score10 == null) return null;
        if (score10 >= 9.5) return "A"; // Hoặc A+
        if (score10 >= 8.5) return "A";
        if (score10 >= 8.0) return "B+";
        if (score10 >= 7.0) return "B";
        if (score10 >= 6.5) return "C+";
        if (score10 >= 5.5) return "C";
        if (score10 >= 5.0) return "D+";
        if (score10 >= 4.0) return "D";
        return "F";
    }

    /**
     * Tính tổng kết toàn khóa (CPA).
     * Bỏ qua các môn 0 tín chỉ (GDTC/GDQP) và các môn chưa có điểm hệ 4 (đang học).
     */
    public SemesterSummary calculate(List<Grade> gradeList) {
        double totalCredits = 0.0;
        double weightedSum10 = 0.0;
        double weightedSum4 = 0.0;

        for (Grade grade : gradeList) {
            if (grade.getCredits() == 0 || grade.getScore4() == null) {
                continue;
            }

            totalCredits += grade.getCredits();
            if (grade.getScore10() != null) {
                weightedSum10 += grade.getScore10() * grade.getCredits();
            }
            weightedSum4 += grade.getScore4() * grade.getCredits();
        }

        if (totalCredits == 0) {
            return new SemesterSummary("Toàn khóa", 0.0, 0.0, 0.0, "Chưa có");
        }

        double gpa10 = Math.round((weightedSum10 / totalCredits) * 100.0) / 100.0;
        double gpa4 = Math.round((weightedSum4 / totalCredits) * 100.0) / 100.0;
        String rank = getAcademicRank(gpa4);

        return new SemesterSummary("Toàn khóa", totalCredits, gpa10, gpa4, rank);
    }

    public String getAcademicRank(double gpa4) {
        if (gpa4 >= 3.60) return "Xuất sắc";
        else if (gpa4 >= 3.20) return "Giỏi";
        else if (gpa4 >= 2.50) return "Khá";
        else if (gpa4 >= 2.00) return "Trung bình";
        else return "Yếu - Cảnh báo";
    }

    /**
     * Tính tổng kết GPA theo từng học kỳ.
     */
    public List<SemesterSummary> calculateBySemester(List<Grade> gradeList) {
        Map<String, List<Grade>> bySemester = new LinkedHashMap<>();
        for (Grade g : gradeList) {
            String sem = (g.getSemester() != null && !g.getSemester().isEmpty()) ? g.getSemester() : "Khác";
            bySemester.computeIfAbsent(sem, k -> new ArrayList<>()).add(g);
        }

        List<SemesterSummary> summaries = new ArrayList<>();
        for (Map.Entry<String, List<Grade>> entry : bySemester.entrySet()) {
            SemesterSummary s = calculate(entry.getValue());
            s.setSemester(entry.getKey());
            summaries.add(s);
        }
        return summaries;
    }
}
