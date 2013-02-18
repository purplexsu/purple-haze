package com.purplehaze.input;

import com.purplehaze.Utils;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * The reader for one photo album.
 */
public class PhotoIndexReader {

  private File dataFile;
  private Properties meta;
  private List<String> tags;
  private String associatedArticleTitle;
  private String associatedArticleLink;

  PhotoIndexReader(File dataFile) {
    this.dataFile = dataFile;
    meta = new Properties();
    tags = new LinkedList<String>();
  }

  void read() throws IOException {
    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(dataFile), Utils.DEFAULT_CHARSET));
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
    while ((line = br.readLine()) != null) {
      tags.add(line.trim());
    }
    br.close();
  }

  public String getTitle() {
    return meta.getProperty("title");
  }

  public boolean isIndexSkipped() {
    return Boolean.parseBoolean(meta.getProperty("skipindex", "false"));
  }

  public List<String> getTags() {
    return tags;
  }

  public String getAlbumId() {
    return dataFile.getName().substring(0, 3);
  }

  public String getAssociatedArticleTitle() {
    return associatedArticleTitle;
  }

  public String getAssociatedArticleLink() {
    return associatedArticleLink;
  }

  public void setAssociatedArticleTitle(String associatedArticleTitle) {
    this.associatedArticleTitle = associatedArticleTitle;
  }

  public void setAssociatedArticleLink(String associatedArticleLink) {
    this.associatedArticleLink = associatedArticleLink;
  }
}
