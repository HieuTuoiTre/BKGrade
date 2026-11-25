package com.gradecalc.model.Bo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.gradecalc.model.Bean.Grade;
import com.gradecalc.model.Bean.SemesterSummary;

public class GradeCalculatorBOTest {

    private final GradeCalculatorBO calculator = new GradeCalculatorBO();

    @Test
    public void testEvaluateFormulaCompleteAndRounding() {
        Double s10 = GradeCalculatorBO.evaluateFormula(
                "[GK]*0.20+[BT]*0.20+[CK]*0.60",
                7.5, null, 8.75, null, 8.2, null, null);
        // 8.2*0.2 + 7.5*0.2 + 8.75*0.6 = 1.64 + 1.50 + 5.25 = 8.39 -> 8.4
        assertEquals(Double.valueOf(8.4), s10);
    }

    @Test
    public void testEvaluateFormulaMissingComponentReturnsNull() {
        Double s10 = GradeCalculatorBO.evaluateFormula(
                "[GK]*0.20+[BT]*0.20+[CK]*0.60",
                8.0, null, null, null, 9.0, null, null);
        assertNull(s10);
        assertNull(GradeCalculatorBO.evaluateFormula("", 8.0, null, 8.0, null, 8.0, null, null));
    }

    @Test
    public void testScore10ConversionsAtBoundaries() {
        assertNull(GradeCalculatorBO.convertScore10ToScore4(null));
        assertNull(GradeCalculatorBO.convertScore10ToLetter(null));

        assertEquals(Double.valueOf(4.0), GradeCalculatorBO.convertScore10ToScore4(8.5));
        assertEquals("A", GradeCalculatorBO.convertScore10ToLetter(8.5));

        assertEquals(Double.valueOf(3.5), GradeCalculatorBO.convertScore10ToScore4(8.49));
        assertEquals("B+", GradeCalculatorBO.convertScore10ToLetter(8.49));

        assertEquals(Double.valueOf(3.5), GradeCalculatorBO.convertScore10ToScore4(8.0));
        assertEquals("B+", GradeCalculatorBO.convertScore10ToLetter(8.0));

        assertEquals(Double.valueOf(3.0), GradeCalculatorBO.convertScore10ToScore4(7.0));
        assertEquals("B", GradeCalculatorBO.convertScore10ToLetter(7.0));

        assertEquals(Double.valueOf(2.5), GradeCalculatorBO.convertScore10ToScore4(6.5));
        assertEquals("C+", GradeCalculatorBO.convertScore10ToLetter(6.5));

        assertEquals(Double.valueOf(2.0), GradeCalculatorBO.convertScore10ToScore4(5.5));
        assertEquals("C", GradeCalculatorBO.convertScore10ToLetter(5.5));

        assertEquals(Double.valueOf(1.5), GradeCalculatorBO.convertScore10ToScore4(5.0));
        assertEquals("D+", GradeCalculatorBO.convertScore10ToLetter(5.0));

        assertEquals(Double.valueOf(1.0), GradeCalculatorBO.convertScore10ToScore4(4.0));
        assertEquals("D", GradeCalculatorBO.convertScore10ToLetter(4.0));

        assertEquals(Double.valueOf(0.0), GradeCalculatorBO.convertScore10ToScore4(3.99));
        assertEquals("F", GradeCalculatorBO.convertScore10ToLetter(3.99));

        assertEquals(Double.valueOf(0.0), GradeCalculatorBO.convertScore10ToScore4(0.0));
        assertEquals("F", GradeCalculatorBO.convertScore10ToLetter(0.0));
    }

    @Test
    public void testCalculateIgnoresZeroCreditInProgressAndPredictedGrades() {
        Grade g1 = new Grade();
        g1.setSemester("1/2025-2026");
        g1.setCredits(3.0);
        g1.setScore10(9.0);
        g1.setScore4(4.0);
        g1.setScoreLetter("A");
        g1.setPredicted(false);

        Grade g2 = new Grade();
        g2.setSemester("1/2025-2026");
        g2.setCredits(2.0);
        g2.setScore10(7.5);
        g2.setScore4(3.0);
        g2.setScoreLetter("B");
        g2.setPredicted(false);

        // Môn 0 tín chỉ (GDTC) phải bị loại khỏi CPA
        Grade gZero = new Grade();
        gZero.setSemester("1/2025-2026");
        gZero.setCredits(0.0);
        gZero.setScore4(4.0);
        gZero.setScoreLetter("Đạt");
        gZero.setPredicted(false);

        // Môn đang học (score4 == null)
        Grade gInProgress = new Grade();
        gInProgress.setSemester("2/2025-2026");
        gInProgress.setCredits(3.0);
        gInProgress.setScore4(null);
        gInProgress.setPredicted(false);

        // Môn điểm dự kiến (predicted == true) KHÔNG được tính vào CPA chính thức
        Grade gPredicted = new Grade();
        gPredicted.setSemester("2/2025-2026");
        gPredicted.setCredits(4.0);
        gPredicted.setScore10(4.0);
        gPredicted.setScore4(1.0);
        gPredicted.setScoreLetter("D");
        gPredicted.setPredicted(true);

        List<Grade> grades = Arrays.asList(g1, g2, gZero, gInProgress, gPredicted);
        SemesterSummary total = calculator.calculate(grades);

        // Chỉ tính g1 (3 TC * 4.0 = 12) + g2 (2 TC * 3.0 = 6) => 18 / 5 = 3.60 ("Xuất sắc")
        assertEquals(5.0, total.getTotalCredits(), 0.001);
        assertEquals(3.60, total.getGpa4(), 0.001);
        assertEquals(8.40, total.getGpa10(), 0.001);
        assertEquals("Xuất sắc", total.getAcademicRank());

        List<SemesterSummary> bySem = calculator.calculateBySemester(grades);
        assertEquals(2, bySem.size());
        assertEquals("1/2025-2026", bySem.get(0).getSemester());
        assertEquals(5.0, bySem.get(0).getTotalCredits(), 0.001);
        assertEquals(0.0, bySem.get(1).getTotalCredits(), 0.001);
    }
}
