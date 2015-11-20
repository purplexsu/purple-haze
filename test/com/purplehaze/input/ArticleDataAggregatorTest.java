package com.purplehaze.input;

import com.purplehaze.Context;
import com.purplehaze.Division;
import com.purplehaze.TestUtil;
import com.purplehaze.output.FileManager;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Unittest for ArticleDataAggregator.
 */
public class ArticleDataAggregatorTest extends TestCase {

  ArticleDataAggregator ada;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    final FileManager fm = new FileManager(
        new File(TestUtil.getTestFile("com/purplehaze/input/testdata")),
        null);
    final Context context = new Context(fm, Division.BLOG);
    ada = new ArticleDataAggregator(context);
    ada.read();
  }

  public void testGetRelatedReader() throws IOException {
    {
      final ArticleDataReader reader = ada.getReader(90);
      final List<ArticleDataReader> relatedReaders = ada.getRelatedReaders(reader);
      assertEquals(ada.getReader(83), relatedReaders.get(0));
      assertEquals(ada.getReader(119), relatedReaders.get(1));
      assertEquals(ada.getReader(111), relatedReaders.get(2));
      assertEquals(ada.getReader(94), relatedReaders.get(3));
      assertEquals(ada.getReader(93), relatedReaders.get(4));
    }
    {
      final ArticleDataReader reader = ada.getReader(126);
      final List<ArticleDataReader> relatedReaders = ada.getRelatedReaders(reader);
      assertEquals(ada.getReader(127), relatedReaders.get(0));
      assertEquals(ada.getReader(115), relatedReaders.get(1));
      assertEquals(ada.getReader(109), relatedReaders.get(2));
      assertEquals(ada.getReader(95), relatedReaders.get(3));
      assertEquals(ada.getReader(81), relatedReaders.get(4));
    }
    {
      final ArticleDataReader reader = ada.getReader(81);
      final List<ArticleDataReader> relatedReaders = ada.getRelatedReaders(reader);
      assertEquals(ada.getReader(126), relatedReaders.get(0));
      assertEquals(ada.getReader(115), relatedReaders.get(1));
      assertEquals(ada.getReader(109), relatedReaders.get(2));
      assertEquals(ada.getReader(95), relatedReaders.get(3));
      assertEquals(ada.getReader(61), relatedReaders.get(4));
    }
    {
      final ArticleDataReader reader = ada.getReader(95);
      final List<ArticleDataReader> relatedReaders = ada.getRelatedReaders(reader);
      assertEquals(ada.getReader(127), relatedReaders.get(0));
      assertEquals(ada.getReader(126), relatedReaders.get(1));
      assertEquals(ada.getReader(115), relatedReaders.get(2));
      assertEquals(ada.getReader(109), relatedReaders.get(3));
      assertEquals(ada.getReader(81), relatedReaders.get(4));
    }
  }

  public void testGetReaders() {
    final List<ArticleDataReader> hebei = ada.getReaders("hebei");
    assertEquals(2, hebei.size());
    assertEquals(2, Integer.parseInt(hebei.get(0).getArticleId()));
    assertEquals(5, Integer.parseInt(hebei.get(1).getArticleId()));
    final List<ArticleDataReader> hubei = ada.getReaders("hubei");
    assertEquals(2, hubei.size());
    assertEquals(1, Integer.parseInt(hubei.get(0).getArticleId()));
    assertEquals(7, Integer.parseInt(hubei.get(1).getArticleId()));
    assertEquals(new LinkedList<ArticleDataReader>(), ada.getReaders("unknown"));
  }

  public void testGetAllColumns() {
    Set<String> all = new HashSet<String>();
    all.add("nemenggu");
    all.add("jiangxi");
    all.add("henan");
    all.add("shaanxi");
    all.add("shandong");
    all.add("hebei");
    all.add("hubei");
    all.add("hunan");
    all.add("liaoning");
    all.add("jilin");
    all.add("xinjiang");
    all.add("all");
    assertEquals(all, ada.getAllColumns());
  }
}
