package com.gradecalc.model.Bo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Test;

import com.gradecalc.model.Bean.Grade;

public class DutParserBOTest {

    private final DutParserBO parser = new DutParserBO();

    @Test
    public void testParseValidHeaderZeroCreditInProgressAndFormulaEval() {
        String raw = String.join("\n",
                "TT\tKỳ\tKỳ phụ\tMã lớp học phần\tTên lớp học phần\tSố TC\tCông thức điểm\tBT\tBV\tCK\tDA\tGK\tQT\tTH\tĐiểm 10\tĐiểm 4\tĐiểm chữ",
                "1\t1/2025-2026\t\t102001\tGiải tích 1\t3\t[GK]*0.20+[BT]*0.20+[CK]*0.60\t8.0\t\t9.0\t\t8.5\t\t\t\t\t",
                "2\t1/2025-2026\t\t102002\tGiáo dục thể chất 1\t0\t\t\t\t\t\t\t\t\t\t\t",
                "3\t2/2025-2026\t\t102003\tCông nghệ Web\t3\t[GK]*0.20+[BT]*0.20+[CK]*0.60\t8.0\t\t\t\t7.5\t\t\t\t\t",
                "4\t2/2025-2026\t\t102004\tMôn lỗi số TC\tabc\t",
                "dong thieu cot"
        );

        List<Grade> parsed = parser.parse(raw);
        assertEquals(3, parsed.size());

        // Môn 1: tự tính điểm 10 = 8.5*0.2 + 8.0*0.2 + 9.0*0.6 = 1.7 + 1.6 + 5.4 = 8.7 -> A (4.0)
        Grade g1 = parsed.get(0);
        assertEquals("Giải tích 1", g1.getCourseName());
        assertEquals(3.0, g1.getCredits(), 0.001);
        assertEquals(Double.valueOf(8.7), g1.getScore10());
        assertEquals(Double.valueOf(4.0), g1.getScore4());
        assertEquals("A", g1.getScoreLetter());
        assertFalse(g1.isPredicted());

        // Môn 2: 0 TC -> "Đạt"
        Grade g2 = parsed.get(1);
        assertEquals(0.0, g2.getCredits(), 0.001);
        assertEquals("Đạt", g2.getScoreLetter());

        // Môn 3: đang học (thiếu CK) -> null
        Grade g3 = parsed.get(2);
        assertNull(g3.getScore10());
        assertNull(g3.getScore4());
        assertNull(g3.getScoreLetter());
    }
}
