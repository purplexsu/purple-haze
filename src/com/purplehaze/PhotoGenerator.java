package com.purplehaze;

import com.purplehaze.input.PhotoIndexAggregator;
import com.purplehaze.input.SiteContent;
import com.purplehaze.output.AbstractArticleManager;
import com.purplehaze.output.AbstractDivisionManager;
import com.purplehaze.output.FileManager;
import com.purplehaze.output.IndexManager;
import org.jdom.JDOMException;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;

import static com.purplehaze.Utils.isEmptyString;
import static com.purplehaze.Utils.verifyDir;

/**
 * The entry point to generate a photo album.
 */
public class PhotoGenerator implements Runnable {

  private Context context;
  private SiteContent siteContent;
  private ArticleTransfer at;
  private AbstractArticleManager am;
  private AbstractDivisionManager dm;
  private IndexManager im;
  private SiteMapGenerator smg;

  public PhotoGenerator(String hazePathString, String templatePathString) throws IOException {
    File hazePath = new File(hazePathString.replaceAll("\\\\", "/"));
    verifyDir(hazePath);
    File templatePath = new File(templatePathString);
    verifyDir(templatePath);
    this.context = new Context(new FileManager(hazePath, templatePath), Division.PHOTO);
    siteContent = new SiteContent(context);
    IntegerDirFilter filter = new IntegerDirFilter(3);
    File[] subDirs = context.getDivisionPath().listFiles(filter);
    Arrays.sort(subDirs);
    File lastAlbum = subDirs[subDirs.length - 1];
    String line = Utils.stringInput("Photo Album ID[" + lastAlbum.getName() + "]:", lastAlbum.getName());
    line = Utils.formatInteger(Integer.parseInt(line), 3);
    siteContent.getPhotoAggregator()
        .setCurrentWorkingAlbumId(Integer.parseInt(line));
    PhotoIndexAggregator pia = siteContent.getPhotoAggregator();
    if (!isEmptyString(line) && filter.accept(context.getDivisionPath(), line)) {
      pia.setCurrentWorkingAlbumId(Integer.parseInt(line));
    } else {
      pia.setCurrentWorkingAlbumId(Integer.parseInt(lastAlbum.getName()));
    }
    am = AbstractArticleManager.getInstance(siteContent, context);
    dm = AbstractDivisionManager.getInstance(context, siteContent);
    im = new IndexManager(context, siteContent);
    smg = new SiteMapGenerator(context);
    at = new ArticleTransfer(hazePath);
  }

  public void run() {
    try {
      siteContent.loadFromDisk();
      am.updateArticle();
      dm.updateDivision();
      im.updateIndex();
      smg.photoSiteMap();
      at.addFiles(context.getFileManager().getUpdatedFiles());
      String time = Utils.stringInput("Include images modified within [1] days:", "1");
      PhotoIndexAggregator pia = siteContent.getPhotoAggregator();
      at.addFiles(Arrays.asList(
          new File(context.getDivisionPath(), Utils.formatInteger(pia.getCurrentWorkingAlbumId(), 3))
              .listFiles(new LatestModifiedFilter(time, "jpg"))));
      at.addDirectory(
          context.getDataPath(),
          new LatestModifiedFilter(time, "txt"));
      at.zip();
      at.upload();
      at.extract();
    } catch (Exception e) {
      e.printStackTrace();  //TODO: Need further handling
    }
  }


  public static void main(String[] args) throws IOException, JDOMException, ParseException, ClassNotFoundException {
    PhotoGenerator pg = new PhotoGenerator(args[0], args[1]);
    pg.run();
  }

}
