package com.purplehaze.input;

import com.purplehaze.Context;
import com.purplehaze.FileNameFilter;
import com.purplehaze.Utils;

import java.io.*;
import java.util.*;

/**
 * The aggregator for all photo album data.
 */
public class PhotoIndexAggregator {

  private Map<Integer, PhotoIndexReader> readers = new HashMap<Integer, PhotoIndexReader>();

  private int currentWorkingAlbumId;
  private static final String SEPARATOR = "`";
  private final Context context;

  PhotoIndexAggregator(Context context) {
    this.context = context;
    currentWorkingAlbumId = 0;
    File[] files = context.getDivisionDataPath()
        .listFiles(new FileNameFilter("\\d{3}\\.txt", false));
    Arrays.sort(files);
    for (int i = 0; i < files.length; i++) {
      readers.put(i + 1, new PhotoIndexReader(files[i]));
    }
  }

  void read() throws IOException {
    System.out.print("Loading photo index:");
    Properties p = loadArticlePhotoMap();
    for (PhotoIndexReader pir : readers.values()) {
      pir.read();
      if (p.containsKey(pir.getAlbumId())) {
        String[] value = p.getProperty(pir.getAlbumId()).split(SEPARATOR);
        pir.setAssociatedArticleTitle(value[0]);
        pir.setAssociatedArticleLink(value[1]);
      }
      System.out.print("#");
    }
    System.out.println();
  }

  /**
   * @return numbers of non-skipped
   */
  public int size() {
    return readers.size();
  }

  public int getCurrentWorkingAlbumId() {
    return currentWorkingAlbumId;
  }

  public void setCurrentWorkingAlbumId(int currentWorkingAlbumId) {
    this.currentWorkingAlbumId = currentWorkingAlbumId;
  }

  public List<PhotoIndexReader> getReaders() {
    List<PhotoIndexReader> result = new LinkedList<PhotoIndexReader>();
    for (int i = 0; i < readers.size(); i++) {
      result.add(readers.get(i + 1));
    }
    return Collections.unmodifiableList(result);
  }

  public PhotoIndexReader getReader(int albumId) {
    return readers.get(albumId);
  }

  public PhotoIndexReader getReader(String albumId) {
    return getReader(Integer.parseInt(albumId));
  }

  public PhotoIndexReader getCurrentWorkingAlbumReader() {
    return getReader(getCurrentWorkingAlbumId());
  }

  public void associateArticleWithCurrentWorkingAlbum(ArticleDataReader adr) throws IOException {
    String value = adr.getDataFile().getParentFile().getName();
    String link = "../../" + value + "/" + Utils.getArticleFileName(adr.getArticleId());
    value = SEPARATOR + link;
    value = adr.getFullTitle() + value;
    Properties p = loadArticlePhotoMap();
    PhotoIndexReader pir = getCurrentWorkingAlbumReader();
    p.setProperty(pir.getAlbumId(), value);
    storeArticlePhotoMap(p);
    pir.setAssociatedArticleLink(link);
    pir.setAssociatedArticleTitle(adr.getFullTitle());
  }

  private Properties loadArticlePhotoMap() throws IOException {
    File mapFile = new File(context.getDivisionDataPath(), "map.txt");
    Properties p = new Properties();
    p.load(new InputStreamReader(new FileInputStream(mapFile), Utils.DEFAULT_CHARSET));
    return p;
  }

  private void storeArticlePhotoMap(Properties p)
      throws IOException {
    File mapFile = new File(context.getDivisionDataPath(), "map.txt");
    p.store(new OutputStreamWriter(new FileOutputStream(mapFile), Utils.DEFAULT_CHARSET), null);
  }
}
