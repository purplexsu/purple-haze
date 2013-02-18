package com.purplehaze.output;

import com.purplehaze.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * A central place to calculate paging info.
 */
class DivisionPageInfo {

  private int totalPageCount;
  private int currentPageIndex;
  private int articleCountInCurrentPage;
  private int articleStartInCurrentPage;
  private String filePrefix;


  private DivisionPageInfo() {
  }

  static List<DivisionPageInfo> getSchema(int totalArticleCount, int maxArticlePerPage,
                                          int minArticlePerPage, int step, String filePrefix) {
    int totalPageCount = (totalArticleCount / maxArticlePerPage) +
        ((totalArticleCount % maxArticlePerPage) == 0 ? 0 : 1);
    ArrayList<DivisionPageInfo> result = new ArrayList<DivisionPageInfo>(totalPageCount);
    for (int i = 0; i < totalPageCount; i++) {
      DivisionPageInfo dpi = new DivisionPageInfo();
      result.add(i, dpi);
      dpi.setFilePrefix(filePrefix);
      dpi.setTotalPageCount(totalPageCount);
      dpi.setCurrentPageIndex(i);
      dpi.setArticleStartInCurrentPage(totalArticleCount - i * maxArticlePerPage);
      if (i + 1 == totalPageCount && totalArticleCount % maxArticlePerPage != 0) {
        dpi.setArticleCountInCurrentPage(totalArticleCount % maxArticlePerPage);
      } else {
        dpi.setArticleCountInCurrentPage(maxArticlePerPage);
      }
    }
    if (result.size() > 1) {
      int pageToCut = result.size() - 2;// cut from the one before the last
      final DivisionPageInfo lastDpi = result.get(result.size() - 1);
      while (lastDpi.getArticleCountInCurrentPage() < minArticlePerPage & pageToCut > 0) {
        DivisionPageInfo dpiToCut = result.get(pageToCut);
        dpiToCut.setArticleCountInCurrentPage(dpiToCut.getArticleCountInCurrentPage() - step);
        lastDpi.setArticleCountInCurrentPage(lastDpi.getArticleCountInCurrentPage() + step);
        for (int i = pageToCut + 1; i < result.size(); i++) {
          DivisionPageInfo dpiWhoseArticleStartChanges = result.get(i);
          dpiWhoseArticleStartChanges.setArticleStartInCurrentPage(
              dpiWhoseArticleStartChanges.getArticleStartInCurrentPage() + step);
        }
        pageToCut--;
        if (pageToCut == 0) {
          // don't cut the first page
          pageToCut = result.size() - 2;
        }
      }
    }
    return result;
  }

  int getTotalPageCount() {
    return totalPageCount;
  }

  private void setTotalPageCount(int totalPageCount) {
    this.totalPageCount = totalPageCount;
  }

  /**
   * start from 0
   *
   * @return
   */
  int getCurrentPageIndex() {
    return currentPageIndex;
  }

  /**
   * start from 0
   *
   * @param currentPageIndex
   */
  private void setCurrentPageIndex(int currentPageIndex) {
    this.currentPageIndex = currentPageIndex;
  }

  int getArticleCountInCurrentPage() {
    return articleCountInCurrentPage;
  }

  private void setArticleCountInCurrentPage(int articleCountInCurrentPage) {
    this.articleCountInCurrentPage = articleCountInCurrentPage;
  }

  int getArticleStartInCurrentPage() {
    return articleStartInCurrentPage;
  }

  private void setArticleStartInCurrentPage(int snippetStartIncurrentPage) {
    this.articleStartInCurrentPage = snippetStartIncurrentPage;
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Current page:")
        .append(getCurrentPageIndex())
        .append("/")
        .append(getTotalPageCount())
        .append("\r\n")
        .append(getArticleCountInCurrentPage())
        .append(" articles in this page, ")
        .append(getArticleStartInCurrentPage())
        .append(" ~ ")
        .append(getArticleStartInCurrentPage() - getArticleCountInCurrentPage() + 1)
        .append("\r\n")
        .append("CurrentFile:")
        .append(getFileName());
    if (hasNextPage()) {
      sb.append("\r\n").append("NextPage:").append(getNextFileName());
    }
    if (hasPreviousPage()) {
      sb.append("\r\n").append("PreviousPage:").append(getPreviousFileName());
    }

    return sb.toString();
  }

  String getFileName() {
    int index = getCurrentPageIndex();
    return getFileName(index);
  }

  private String getFileName(int index) {
    boolean indexFile = Utils.equals("index", filePrefix);
    if (index > 0 || !indexFile) {
      if (indexFile) {
        return filePrefix + "-" + Utils.formatInteger(index, 2) + ".html";
      } else {
        return filePrefix + "-" + Utils.formatInteger(index + 1, 2) + ".html";
      }
    } else {
      return "index.html";
    }
  }

  String getPreviousFileName() {
    return getFileName(getCurrentPageIndex() - 1);
  }

  String getNextFileName() {
    return getFileName(getCurrentPageIndex() + 1);
  }

  boolean hasPreviousPage() {
    return getCurrentPageIndex() > 0;
  }

  boolean hasNextPage() {
    return getCurrentPageIndex() < getTotalPageCount() - 1;
  }

  private void setFilePrefix(String filePrefix) {
    this.filePrefix = filePrefix;
  }
}
