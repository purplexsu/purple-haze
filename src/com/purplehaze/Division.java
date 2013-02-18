package com.purplehaze;

import com.purplehaze.output.Translations;

/**
 * The division of the site content.
 */
public enum Division {
  BLOG('b', Translations.BLOG, false, true),
  TRAVEL('t', Translations.TRAVEL, true, true),
  PHOTO('p', Translations.PHOTO, false, false),
  MUSIC('u', Translations.MUSIC, true, true),
  MOVIE('m', Translations.MOVIE, true, true),
  APPRAISAL('a', Translations.APPRAISAL, true, true);

  private final char key;
  private final String chinese;
  private final boolean enableColumn;
  private final boolean enableComment;

  Division(char key, String chinese, boolean enableColumn, boolean enableComment) {
    this.key = key;
    this.chinese = chinese;
    this.enableColumn = enableColumn;
    this.enableComment = enableComment;
  }

  @Override
  public String toString() {
    return name().toLowerCase();
  }

  public static Division parse(char c) {
    for (Division d : values()) {
      if (d.key == c) {
        return d;
      }
    }
    return null;
  }

  public static Division parse(String s) {
    for (Division d : values()) {
      if (d.toString().equalsIgnoreCase(s)) {
        return d;
      }
    }
    return null;
  }

  public String getChinese() {
    return chinese;
  }

  public boolean enableColumn() {
    return enableColumn;
  }

  public boolean enableComment() {
    return enableComment;
  }
}
