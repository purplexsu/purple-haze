package com.purplehaze.input;

import com.purplehaze.Utils;

import java.io.*;
import java.util.Date;
import java.util.Properties;

/**
 * The data reader for one article.
 */
public class ArticleDataReader {

  private File dataFile;
  private Properties meta;
  private String rawContent;

  ArticleDataReader(File dataFile) {
    this.dataFile = dataFile;
    meta = new Properties();
  }

  void read() throws IOException {
    BufferedReader br = new BufferedReader(
        new InputStreamReader(
            new FileInputStream(dataFile), Utils.DEFAULT_CHARSET));
    StringBuilder sb = new StringBuilder();
    String line = null;
    //read meta
    while ((line = br.readLine()) != null) {
      if (Utils.isEmptyString(line)) {
        break;
      }
      sb.append(line).append("\n");
    }
    meta.load(new StringReader(sb.toString()));
    sb = new StringBuilder();
    //read article
    while ((line = br.readLine()) != null) {
      sb.append(line).append("\n");
    }
    rawContent = sb.toString();
  }

  public String getRawContent() {
    return rawContent;
  }

  public String getFullTitle() {
    if (getSubtitle() == null) {
      return getTitle();
    } else {
      return getTitle() + " -- " + getSubtitle();
    }
  }

  public String getImgSnippetPath(File divisionPath) {

    String snippetImgRel = null;
    if (getPhoto() != null) {
      snippetImgRel = "../photo/" + getPhoto() + "/snippet.jpg";
    } else {
      File img = null;
      if ((img = new File(divisionPath, Utils.getSnippetFileName(getArticleId()))).exists()) {
        snippetImgRel = img.getName();
      } else {
        snippetImgRel = "snippet.jpg";
      }
    }
    return snippetImgRel;
  }

  public String getTitle() {
    return meta.getProperty("title").trim();
  }

  public String getStyle() {
    if (meta.containsKey("style")) {
      return meta.getProperty("style").trim();
    } else {
      return null;
    }
  }

  public String getTime() {
    return meta.getProperty("time").trim();
  }

  public String getSubtitle() {
    if (meta.containsKey("subtitle")) {
      return meta.getProperty("subtitle").trim();
    } else {
      return null;
    }
  }

  public String getOptional() {
    if (meta.containsKey("optional")) {
      return meta.getProperty("optional").trim();
    } else {
      return null;
    }
  }

  public String[] getColumns() {
    if (meta.containsKey("columns")) {
      return meta.getProperty("columns").trim().split(",");
    } else {
      return null;
    }
  }

  public String[] getTags() {
    return meta.getProperty("tags").trim().split(",");
  }

  public String getPhoto() {
    if (meta.containsKey("photo")) {
      return meta.getProperty("photo").trim();
    } else {
      return null;
    }
  }

  public File getDataFile() {
    return dataFile;
  }

  public Date lastModified() {
    return new Date(dataFile.lastModified());
  }

  public String getArticleId() {
    return dataFile.getName().substring(0, 3);
  }

  public String getSnippet() {
    if (meta.containsKey("snippet")) {
      return meta.getProperty("snippet").trim();
    } else {
      return null;
    }
  }

  public String getDisplayTime() {
    String displayTime = meta.getProperty("disptime");
    if (displayTime == null) {
      return getTime();
    } else {
      return displayTime.trim();
    }
  }

  public boolean navByColumn() {
    return Boolean.parseBoolean(meta.getProperty("columnnav", "false"));
  }

  public boolean disableArticleIndex() {
    return Boolean.valueOf(meta.getProperty("disableindex", "false"));
  }

  @Override
  public String toString() {
    return getFullTitle() + "(" + getDataFile() + ")";
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof ArticleDataReader) {
      ArticleDataReader other = (ArticleDataReader) o;
      return this.dataFile.equals(other.dataFile);
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return dataFile.hashCode();
  }
}
