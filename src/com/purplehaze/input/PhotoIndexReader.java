package com.purplehaze.input;

import com.purplehaze.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * The reader for one photo album.
 */
public class PhotoIndexReader {

  private File dataFile;
  private Properties meta;
  private List<Media> medias;
  private String associatedArticleTitle;
  private String associatedArticleLink;

  PhotoIndexReader(File dataFile) {
    this.dataFile = dataFile;
    meta = new Properties();
    medias = new ArrayList<>();
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
      medias.add(new Media(line.trim()));
    }
    br.close();
  }

  public String getTitle() {
    return meta.getProperty("title");
  }

  public boolean isIndexSkipped() {
    return Boolean.parseBoolean(meta.getProperty("skipindex", "false"));
  }

  public int getNumOfMedias() {
    return medias.size();
  }

  public Media getMedia(int index) {
    return medias.get(index);
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

