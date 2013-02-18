package com.purplehaze.output;

import com.purplehaze.Utils;

import java.io.*;
import java.util.Date;

/**
 * A cached for parsed article content.
 */
class ArticleContentCache {

  private static final String SYS_TMP_PROPERTY = "java.io.tmpdir";
  private static final ArticleContentCache INSTANCE = new ArticleContentCache();
  private final File tmpDir;

  private ArticleContentCache() {
    tmpDir = new File(System.getProperty(SYS_TMP_PROPERTY));
    try {
      Utils.verifyDir(tmpDir);
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }

  static ArticleContentCache getInstance() {
    return INSTANCE;
  }

  boolean cacheHit(Serializable key, Date modifiedTime) {
    File cacheFile = getCacheFile(key);
    if (cacheFile.exists() && cacheFile.canRead() && cacheFile.isFile()) {
      Date cacheTime = new Date(cacheFile.lastModified());
      return !cacheTime.before(modifiedTime);
    } else {
      return false;
    }
  }

  boolean update(Serializable key, Serializable value) {
    File cacheFile = getCacheFile(key);
    try {
      makeSureDir(cacheFile.getParentFile());
      ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(cacheFile));
      oos.writeObject(value);
      oos.flush();
      oos.close();
      return true;
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }

  private void makeSureDir(File dir) {
    if (dir == null) {
      return;
    }
    makeSureDir(dir.getParentFile());
    if (!dir.exists()) {
      dir.mkdir();
    }
  }

  Serializable get(Serializable key) {
    File cacheFile = getCacheFile(key);
    try {
      ObjectInputStream ois = new ObjectInputStream(new FileInputStream(cacheFile));
      Object o = ois.readObject();
      return (Serializable) o;
    } catch (Exception e) {
      System.err.println(e.getMessage());
      return null;
    }
  }

  private File getCacheFile(Serializable key) {
    return new File(tmpDir, key.toString());
  }
}
