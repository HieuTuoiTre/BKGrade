package com.gradecalc.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class GradeControllerBulkDeleteTest {

    @Test
    public void testParsePositiveUniqueIdsFiltersInvalidAndDeduplicates() {
        assertTrue(GradeController.parsePositiveUniqueIds(null).isEmpty());
        assertTrue(GradeController.parsePositiveUniqueIds(new String[]{}).isEmpty());

        String[] raw = new String[]{" 12 ", "0", "-5", "abc", "", null, "7", "12", " 25", "7"};
        List<Integer> parsed = GradeController.parsePositiveUniqueIds(raw);

        assertEquals(Arrays.asList(12, 7, 25), parsed);
    }
}
