package com.purplehaze.input;

import com.purplehaze.Context;
import com.purplehaze.Division;
import com.purplehaze.FileNameFilter;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The aggregator of all article data from the same division.
 */
public class ArticleDataAggregator {

  public static final int MAX_RELATED_READERS = 5;
  private Map<Integer, ArticleDataReader> readers = new HashMap<Integer, ArticleDataReader>();

  private Map<String, List<ArticleDataReader>> tagIndex = new HashMap<String, List<ArticleDataReader>>();

  private Map<String, List<ArticleDataReader>> columnIndex = new HashMap<String, List<ArticleDataReader>>();

  private Context context;

  private int currentWorkingArticleId;

  ArticleDataAggregator(Context context) {
    this.context = context;
    currentWorkingArticleId = 0;
    File[] files = context.getDivisionDataPath().listFiles(new FileNameFilter("\\d{3}\\.txt", false));
    Arrays.sort(files);
    for (int i = 0; i < files.length; i++) {
      readers.put(i + 1, new ArticleDataReader(files[i]));
    }
  }

  void read() throws IOException {
    System.out.print("Loading article data of " + context.getDivision() + ":");
    for (int i = readers.size(); i > 0; i--) {
      ArticleDataReader reader = readers.get(i);
      reader.read();
      for (String tag : reader.getTags()) {
        if (!tagIndex.containsKey(tag)) {
          tagIndex.put(tag, new LinkedList<ArticleDataReader>());
        }
        tagIndex.get(tag).add(reader);
      }
      if (reader.getColumns() != null) {
        for (String column : reader.getColumns()) {
          if (!columnIndex.containsKey(column)) {
            columnIndex.put(column, new LinkedList<ArticleDataReader>());
          }
          columnIndex.get(column).add(0, reader);
        }
      }
      System.out.print("#");
    }
    System.out.println();
  }

  public int size() {
    return readers.size();
  }

  public Division getDivision() {
    return context.getDivision();
  }

  public List<ArticleDataReader> getRelatedReaders(ArticleDataReader reader) {
    final LinkedHashSet<ArticleDataReader> result = new LinkedHashSet<ArticleDataReader>();
    for (String tag : reader.getTags()) {
      for (ArticleDataReader relatedReader : tagIndex.get(tag)) {
        if (!relatedReader.equals(reader)) {
          result.add(relatedReader);
        }
        if (result.size() >= MAX_RELATED_READERS) {
          return new LinkedList<ArticleDataReader>(result);
        }
      }
    }
    return new LinkedList<ArticleDataReader>(result);
  }

  /**
   * @param articleId start from 1
   * @return
   */
  public ArticleDataReader getReader(int articleId) {
    return readers.get(articleId);
  }

  public ArticleDataReader getReader(String articleId) {
    return getReader(Integer.parseInt(articleId));
  }

  public List<ArticleDataReader> getReaders() {
    List<ArticleDataReader> result = new LinkedList<ArticleDataReader>();
    for (int i = 0; i < readers.size(); i++) {
      result.add(readers.get(i + 1));
    }
    return Collections.unmodifiableList(result);
  }

  /**
   * List of 1, 2, 5, 14, for example
   *
   * @param columnId
   * @return
   */
  public List<ArticleDataReader> getReaders(String columnId) {
    if (columnIndex.containsKey(columnId)) {
      return columnIndex.get(columnId);
    } else {
      return new LinkedList<ArticleDataReader>();
    }
  }

  public int getCurrentWorkingArticleId() {
    return currentWorkingArticleId;
  }

  public void setCurrentWorkingArticleId(int currentWorkingArticleId) {
    this.currentWorkingArticleId = currentWorkingArticleId;
  }

  public ArticleDataReader getCurrentWorkingReader() {
    return getReader(getCurrentWorkingArticleId());
  }

  public Set<String> getAllColumns() {
    final Set<String> allColumns = new HashSet<String>();
    for (ArticleDataReader reader : readers.values()) {
      final String[] columns = reader.getColumns();
      if (columns != null) {
        allColumns.addAll(Arrays.asList(columns));
      }
    }
    return allColumns;
  }


  @Override
  public int hashCode() {
    return context.getDivision().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ArticleDataAggregator) {
      ArticleDataAggregator ada = (ArticleDataAggregator) obj;
      return ada.context.getDivision() == context.getDivision();
    } else {
      return false;
    }
  }
}
