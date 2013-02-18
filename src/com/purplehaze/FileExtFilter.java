package com.purplehaze;

import java.io.File;
import java.io.FilenameFilter;

/**
 * A generic file filter based on ext name.
 */
class FileExtFilter implements FilenameFilter {

  private FileNameFilter filter;

  FileExtFilter(String extent) {
    this.filter = new FileNameFilter(".*\\." + extent + "$", false);
  }


  public boolean accept(File dir, String name) {
    return filter.accept(dir, name);
  }

}
