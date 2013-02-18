package com.purplehaze;

import com.purplehaze.output.FileManager;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;

/**
 * Unittest for Context.
 */
public class ContextTest extends TestCase {

  private static final String HOME = System.getProperty("user.home") + "/My Webs";

  private File templatePath;
  private File hazePath;

  @Override
  protected void setUp() throws Exception {
    this.templatePath = new File(HOME + "/PurpleTemplate");
    this.hazePath = new File(HOME + "/PurpleHaze");
  }

  public void testTravel() throws IOException {
    Context c = new Context(new FileManager(hazePath, templatePath), Division.TRAVEL);
    assertEquals(new File(HOME + "/PurpleHaze/travel"), c.getDivisionPath());
    assertEquals(new File(HOME + "/PurpleHaze/data"), c.getDataPath());
    assertEquals(new File(HOME + "/PurpleHaze/data/travel"), c.getDivisionDataPath());
    assertEquals(new File(HOME + "/PurpleTemplate/travel"), c.getDivisionTemplatePath());
    assertEquals(new File(HOME + "/PurpleTemplate/_sample/article.htm"),
        c.getArticleTemplateFile());
    assertEquals(new File(HOME + "/PurpleTemplate/_sample/comment.htm"),
        c.getCommentTemplateFile());
    assertEquals(new File(HOME + "/PurpleTemplate/_sample/column.htm"),
        c.getColumnTemplateFile());
    assertEquals(new File(HOME + "/PurpleTemplate/index.html"),
        c.getRootIndexTemplateFile());
    assertEquals(new File(HOME + "/PurpleHaze/index.html"),
        c.getRootIndexFile());
    assertEquals(new File(HOME + "/PurpleHaze/travel/index.html"),
        c.getDivisionIndexFile());
    assertEquals(new File(HOME + "/PurpleTemplate/travel/index.html"),
        c.getDivisionIndexTemplateFile());
  }

  public void testPhoto() throws IOException {
    Context c = new Context(new FileManager(hazePath, templatePath), Division.PHOTO);
    assertEquals(new File(HOME + "/PurpleHaze/photo"), c.getDivisionPath());
    assertEquals(new File(HOME + "/PurpleHaze/data"), c.getDataPath());
    assertEquals(new File(HOME + "/PurpleHaze/data/photo"), c.getDivisionDataPath());
    assertEquals(new File(HOME + "/PurpleTemplate/photo"), c.getDivisionTemplatePath());
    assertEquals(new File(HOME + "/PurpleTemplate/photo/_sample/_sample.htm"),
        c.getArticleTemplateFile());
    assertEquals(new File(HOME + "/PurpleTemplate/index.html"),
        c.getRootIndexTemplateFile());
    assertEquals(new File(HOME + "/PurpleHaze/index.html"),
        c.getRootIndexFile());
    assertEquals(new File(HOME + "/PurpleHaze/photo/index.html"),
        c.getDivisionIndexFile());
    assertEquals(new File(HOME + "/PurpleTemplate/photo/index.html"),
        c.getDivisionIndexTemplateFile());
  }
}
