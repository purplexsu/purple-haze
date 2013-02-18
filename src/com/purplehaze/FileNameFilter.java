package com.purplehaze;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Pattern;

/**
 * A generic file filter based on regex.
 */
public class FileNameFilter implements FilenameFilter {

  private Pattern pattern;
  private boolean directory;

  public FileNameFilter(String regex, boolean directory) {
    this.pattern = Pattern.compile(regex);
    this.directory = directory;
  }

  public boolean accept(File dir, String name) {
    File file = new File(dir, name);
    return (directory == file.isDirectory()) && (pattern.matcher(name).matches());
  }
}
