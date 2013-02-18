package com.purplehaze;

import java.io.File;
import java.io.FilenameFilter;

/**
 * A file filter which only accepts directories with digit name.
 */
class IntegerDirFilter implements FilenameFilter {

  private FileNameFilter filter;

  public IntegerDirFilter(int digit) {
    String regex = "\\d{" + digit + "}";
    filter = new FileNameFilter(regex, true);
  }

  public boolean accept(File dir, String name) {
    return filter.accept(dir, name);
  }
}
