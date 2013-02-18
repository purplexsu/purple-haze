package com.purplehaze;

import com.purplehaze.output.FileManager;

import java.io.File;
import java.io.IOException;

/**
 * The shared runtime context.
 */
public class Context {

  private final File hazePath;
  private final File templatePath;
  private final FileManager fileManager;
  private final Division division;

  public Context(FileManager fileManager, Division division) throws IOException {
    this.hazePath = fileManager.getHazePath();
    this.templatePath = fileManager.getTemplatePath();
    this.fileManager = fileManager;
    this.division = division;
  }

  public Context(Context context, Division division) throws IOException {
    this(context.getFileManager(), division);
  }

  public FileManager getFileManager() {
    return fileManager;
  }

  public File getHazePath() {
    return hazePath;
  }

  public File getTemplatePath() {
    return templatePath;
  }

  public Division getDivision() {
    return division;
  }

  public File getDivisionPath() {
    return new File(hazePath, division.toString());
  }

  public File getDivisionDataPath() {
    return new File(getDataPath(), division.toString());
  }

  public File getDataPath() {
    return new File(hazePath, "data");
  }

  public File getDivisionTemplatePath() {
    return new File(templatePath, division.toString());
  }

  @Override
  public int hashCode() {
    return hazePath.hashCode() * division.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Context) {
      Context c = (Context) obj;
      return c.hazePath.equals(hazePath) && c.division == division;
    }
    return false;
  }

  public File getArticleTemplateFile() {
    if (division == Division.PHOTO) {
      return new File(new File(getDivisionTemplatePath(), "_sample"), "_sample.htm");
    } else {
      return new File(new File(this.templatePath, "_sample"), "article.htm");
    }
  }

  public File getCommentTemplateFile() {
    return new File(new File(this.templatePath, "_sample"), "comment.htm");
  }

  public File getColumnFile(String columnId) {
    return new File(getDivisionPath(), Utils.getColumnFileName(columnId));
  }

  public File getColumnTemplateFile() {
    return new File(new File(this.templatePath, "_sample"), "column.htm");
  }

  public File getDivisionIndexTemplateFile() {
    return new File(getDivisionTemplatePath(), "index.html");
  }

  public File getDivisionIndexFile() {
    return new File(getDivisionPath(), "index.html");
  }

  public File getRootIndexTemplateFile() {
    return new File(this.templatePath, "index.html");
  }

  public File getRootIndexFile() {
    return new File(this.hazePath, "index.html");
  }
}
