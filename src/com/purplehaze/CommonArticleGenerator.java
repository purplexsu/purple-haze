package com.purplehaze;

import com.purplehaze.input.ArticleDataAggregator;
import com.purplehaze.input.SiteContent;
import com.purplehaze.output.*;
import org.jdom.JDOMException;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;

import static com.purplehaze.Utils.verifyDir;

/**
 * The entry point to create a new article.
 */
public class CommonArticleGenerator implements Runnable {

  private final SiteContent siteContent;
  private AbstractArticleManager am;
  private ColumnManager cm;
  private AbstractDivisionManager dm;
  private IndexManager im;
  private SiteMapGenerator smg;
  private FeedGenerator fg;
  private ArticleTransfer at;
  private Context context;

  public CommonArticleGenerator(String hazePathString, String templatePathString) throws IOException {
    File hazePath = new File(hazePathString);
    verifyDir(hazePath);
    File templatePath = new File(templatePathString);
    verifyDir(templatePath);
    System.out.println("Which division?");
    System.out.println("[a]ppraisal");
    System.out.println("[b]log");
    System.out.println("[m]ovie");
    System.out.println("m[u]sic");
    char input = Utils.stringInput("[t]ravel", "b").charAt(0);
    context = new Context(new FileManager(hazePath, templatePath), Division.parse(input));
    File[] files = context.getDivisionDataPath().listFiles(new FileNameFilter("\\d{3}\\.txt", false));
    Arrays.sort(files);
    String id = files[files.length - 1].getName().substring(0, 3);
    id = Utils.stringInput("Update [" + id + "]:", id);
    id = Utils.formatInteger(Integer.parseInt(id), 3);
    Utils.stringInput("Please confirm the article data has correct timestamp!", "");
    File articleFile = new File(context.getDivisionPath(), Utils.getArticleFileName(id));
    if (articleFile.exists() && !Utils.booleanInput("Update " + articleFile.getName() + " [y|n]:", false)) {
      System.exit(1);
    }
    siteContent = new SiteContent(context);
    ArticleDataAggregator ada = siteContent.getArticleAggregator(context);
    ada.setCurrentWorkingArticleId(Integer.parseInt(id));
    am = AbstractArticleManager.getInstance(siteContent, context);
    cm = new ColumnManager(context, siteContent);
    dm = AbstractDivisionManager.getInstance(context, siteContent);
    im = new IndexManager(context, siteContent);
    smg = new SiteMapGenerator(context);
    fg = new FeedGenerator(context, siteContent);
    at = new ArticleTransfer(hazePath);
  }

  public void run() {
    try {
      siteContent.loadFromDisk();
      am.updateArticle();
      am.writeComment(true);
      cm.updateColumns();
      dm.updateDivision();
      am.updateRelatedPhotoAlbum();
      im.updateIndex();
      if (Utils.booleanInput("Site updated, go ahead?[y]", true)) {
        smg.articleSiteMap();
        fg.feeds();
        at.addFiles(context.getFileManager().getUpdatedFiles());
        String time = Utils.stringInput("Include associated files modified within [1] days:", "1");
        at.addFiles(Arrays.asList(context.getDivisionPath().listFiles(new LatestModifiedFilter(time, "jpg"))));
        at.addDirectory(
            context.getDataPath(),
            new LatestModifiedFilter(time, "txt"));
        at.zip();
        at.upload();
        at.extract();
        at.ping();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) throws IOException, JDOMException, ParseException, ClassNotFoundException {
    CommonArticleGenerator cag = new CommonArticleGenerator(args[0], args[1]);
    cag.run();
  }
}
