package com.purplehaze;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

/**
 * A file filter based on latest modified timestamp.
 */
public class LatestModifiedFilter implements FilenameFilter {

  private long time;
  private FileExtFilter filter;

  public LatestModifiedFilter(String time, String ext) {
    this.time = Long.parseLong(time) * 24 * 60 * 60 * 1000;
    this.filter = new FileExtFilter(ext);
  }

  public boolean accept(File dir, String name) {
    if (filter.accept(dir, name)) {
      File file = new File(dir, name);
      if (file.exists() && file.isFile()) {
        if (time <= 0 || (time > 0 && (System.currentTimeMillis() - file.lastModified() <= time))) {
          return true;
        }
      }
    }
    return false;
  }
}
