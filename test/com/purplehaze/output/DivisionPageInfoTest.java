package com.purplehaze.output;

import junit.framework.TestCase;

import java.util.List;

/**
 * Unittest for DivisionPageInfo.
 */
public class DivisionPageInfoTest extends TestCase {

    public void testGetSchema() {
        final List<DivisionPageInfo> schema = DivisionPageInfo.getSchema(148, 30, 12, 6, "column-beijing");
        assertNotNull(schema);
        assertEquals(5, schema.size());
        int[] expected_article_counts = {30, 30, 30, 30, 28};
        int[] expected_article_starts = {148, 118, 88, 58, 28};
        final int size = schema.size();
        for (int i = 0; i < size; i++) {
            DivisionPageInfo dpi = schema.get(i);
            assertNotNull(dpi);
            assertEquals(i, dpi.getCurrentPageIndex());
            assertEquals(size, dpi.getTotalPageCount());
            assertEquals(expected_article_counts[i], dpi.getArticleCountInCurrentPage());
            assertEquals(expected_article_starts[i], dpi.getArticleStartInCurrentPage());
            assertEquals(String.format("column-beijing-%02d.html", i + 1), dpi.getFileName());
            if (i == 0) {
                assertFalse(dpi.hasPreviousPage());
                assertTrue(dpi.hasNextPage());
                assertEquals(String.format("column-beijing-%02d.html", i + 2), dpi.getNextFileName());
            } else if (i == size - 1) {
                assertTrue(dpi.hasPreviousPage());
                assertEquals(String.format("column-beijing-%02d.html", i), dpi.getPreviousFileName());
                assertFalse(dpi.hasNextPage());
            } else {
                assertTrue(dpi.hasPreviousPage());
                assertEquals(String.format("column-beijing-%02d.html", i), dpi.getPreviousFileName());
                assertTrue(dpi.hasNextPage());
                assertEquals(String.format("column-beijing-%02d.html", i + 2), dpi.getNextFileName());
            }
        }
    }
}
