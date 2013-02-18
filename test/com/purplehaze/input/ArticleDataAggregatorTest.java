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
      assertEquals(ada.getReader(83), ada.getRelatedReaders(reader).get(0));
      assertEquals(ada.getReader(119), ada.getRelatedReaders(reader).get(1));
      assertEquals(ada.getReader(111), ada.getRelatedReaders(reader).get(2));
      assertEquals(ada.getReader(94), ada.getRelatedReaders(reader).get(3));
      assertEquals(ada.getReader(93), ada.getRelatedReaders(reader).get(4));
    }
    {
      final ArticleDataReader reader = ada.getReader(126);
      assertEquals(ada.getReader(115), ada.getRelatedReaders(reader).get(0));
      assertEquals(ada.getReader(109), ada.getRelatedReaders(reader).get(1));
      assertEquals(ada.getReader(95), ada.getRelatedReaders(reader).get(2));
      assertEquals(ada.getReader(81), ada.getRelatedReaders(reader).get(3));
      assertEquals(ada.getReader(61), ada.getRelatedReaders(reader).get(4));
    }
  /*  for (ArticleDataReader reader : ada.getReaders()) {
      System.out.println(reader.getArticleId()+reader.getTitle());
      System.out.println("+++++++++++++++++++++++++++");
      for (ArticleDataReader related : ada.getRelatedReaders(reader)) {
        System.out.println(related.getArticleId()+related.getTitle());
      }
      System.out.println("=================================");
    }*/
    System.out.println();
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
