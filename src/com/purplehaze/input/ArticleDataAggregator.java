package com.purplehaze.input;

import com.purplehaze.Context;
import com.purplehaze.Division;
import com.purplehaze.FileNameFilter;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * The aggregator of all article data from the same division.
 */
public class ArticleDataAggregator {
  private static final int MAX_RELATED_READERS = 4;

  private Map<Integer, ArticleDataReader> readers = new HashMap<>();

  private Map<String, List<ArticleDataReader>> tagIndex = new HashMap<>();

  private Map<String, List<ArticleDataReader>> columnIndex = new HashMap<>();

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
    final MarkableLimitedList<ArticleDataReader> result = new MarkableLimitedList<>(MAX_RELATED_READERS);
    final HashSet<ArticleDataReader> addedReaders = new HashSet<>();
    // We want to make sure at least one older reader is added to the related readers if there is.
    for (String tag : reader.getTags()) {
      for (ArticleDataReader relatedReader :  tagIndex.get(tag)) {
        if (addedReaders.contains(relatedReader)) {
          continue;
        }
        addedReaders.add(relatedReader);
        if (relatedReader.equals(reader)) {
          result.mark();
        } else {
          result.add(relatedReader);
        }
      }
    }
    return result.toList();
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

  private class MarkableLimitedList<E> {
    private static final int MAX_ELEMENT_AFTER_MARK_WHEN_FULL = 1;
    private final int limit;
    private int mark = -1;
    private int overflowedElementsAfterMarkedOne = 0;
    private List<E> data = new LinkedList<>();

    public MarkableLimitedList(int limit) {
      this.limit = limit;
    }

    public void add(E element) {
      if (data.size() >= limit &&
          mark >= 0 &&
          data.size() + overflowedElementsAfterMarkedOne >= mark + MAX_ELEMENT_AFTER_MARK_WHEN_FULL) {
        // If the list is ready full, we only allow 1 more element added after mark.
        // 1 = MAX_ELEMENT_AFTER_MARK_WHEN_FULL
        return;
      }
      data.add(element);
      if (data.size() > limit) {
        data.remove(0);
        if (mark >= 0) {
          overflowedElementsAfterMarkedOne++;
        }
      }
    }

    public void mark() {
      mark = data.size();
    }

    public List<E> toList() {
      return Collections.unmodifiableList(data);
    }
  }
}
