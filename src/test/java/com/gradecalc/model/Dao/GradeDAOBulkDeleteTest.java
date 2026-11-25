package com.gradecalc.model.Dao;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class GradeDAOBulkDeleteTest {

    @Test
    public void testBuildDeleteBatchSqlMatchesPlaceholderCount() {
        assertEquals(
                "DELETE FROM grades WHERE user_id = ? AND id IN (?)",
                GradeDAO.buildDeleteBatchSql(1));
        assertEquals(
                "DELETE FROM grades WHERE user_id = ? AND id IN (?, ?, ?)",
                GradeDAO.buildDeleteBatchSql(3));
    }
}
