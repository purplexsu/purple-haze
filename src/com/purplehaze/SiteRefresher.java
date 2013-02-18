package com.purplehaze;

import com.purplehaze.input.ArticleDataAggregator;
import com.purplehaze.input.PhotoIndexAggregator;
import com.purplehaze.input.SiteContent;
import com.purplehaze.output.*;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.EntityRef;
import org.jdom.JDOMException;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

import static com.purplehaze.Utils.verifyDir;

/**
 * Refresh the entire site.
 */
public class SiteRefresher implements Runnable {

  private final File hazePath;
  private final File templatePath;

  public SiteRefresher(String hazePathString, String templatePathString) throws IOException, JDOMException, ClassNotFoundException, ParseException {
    this.hazePath = new File(hazePathString);
    verifyDir(hazePath);
    this.templatePath = new File(templatePathString);
    verifyDir(templatePath);
  }

  @Override
  public void run() {
    try {
      final Context baseContext = new Context(new FileManager(hazePath, templatePath), null);
      SiteContent siteContent = new SiteContent(baseContext);
      siteContent.loadFromDisk();
      final String input =
          Utils.stringInput("which type of files:[a]rticle,[c]olumn,[d]ivision,[i]ndex,c[o]mment,[p]hoto:", "");
      for (String s : input.split(",")) {
        switch (s.charAt(0)) {
          case 'a': {
            rewriteAllArticles(baseContext, siteContent);
            break;
          }
          case 'c': {
            rewriteColumns(baseContext, siteContent);
            break;
          }
          case 'd': {
            rewriteDivisions(baseContext, siteContent);
            break;
          }
          case 'i': {
            rewriteIndex(baseContext, siteContent);
            break;
          }
          case 'o': {
            rewriteComments(baseContext, siteContent);
            break;
          }
          case 'p': {
            rewriteAllPhotoAlbums(baseContext, siteContent);
            break;
          }
        }
      }
      ArticleTransfer at = new ArticleTransfer(hazePath);
      at.addFiles(baseContext.getFileManager().getUpdatedFiles());
      at.zip();
      Utils.booleanInput("!!!!", false);
      at.upload();
      at.extract();
    } catch (IOException e) {
      e.printStackTrace();  //TODO: Need further handling
    } catch (JDOMException e) {
      e.printStackTrace();  //TODO: Need further handling
    } catch (ParseException e) {
      e.printStackTrace();  //TODO: Need further handling
    } catch (ClassNotFoundException e) {
      e.printStackTrace();  //TODO: Need further handling
    }
  }

  private void rewriteComments(Context baseContext, SiteContent siteContent) throws JDOMException, IOException, ParseException {
    for (Division d : Division.values()) {
      if (!d.enableComment()) {
        continue;
      }
      Context context = new Context(baseContext, d);
      System.out.print("Processing " + d + ". ");
      // TODO
      final ArticleDataAggregator ada = siteContent.getArticleAggregator(d);
      AbstractArticleManager am = AbstractArticleManager.getInstance(siteContent, context);
      for (int i = 1; i <= ada.size(); i++) {
        System.out.print(i + ".");
        ada.setCurrentWorkingArticleId(i);
        File commentFile = new File(context.getDivisionPath(), Utils.getCommentFileName(i));
        Document doc = context.getFileManager().nonValidatedBuild(commentFile);
        // backup user comment
        final Element userComment = Utils.findElement(doc, "div", "CommentArea");
        userComment.detach();
        // work around a bug:
        if (userComment.getContent().size() == 0) {
          userComment.addContent(new EntityRef("nbsp"));
        }
        // apply new template
        am.writeComment(false);
        // rewrite user comment back
        doc = context.getFileManager().nonValidatedBuild(commentFile);
        final Element newComment = Utils.findElement(doc, "div", "CommentArea");
        newComment.removeContent();
        newComment.addContent(userComment.removeContent());
        context.getFileManager().xmlOutput(doc, commentFile);
      }
      System.out.println();
    }
  }

  private static void rewriteAllArticles(Context baseContext, SiteContent siteContent) throws IOException, JDOMException, ParseException, ClassNotFoundException {
    for (Division d : Division.values()) {
      if (d != Division.PHOTO) {
        Context context = new Context(baseContext, d);
        System.out.print("Processing " + d + ". ");
        final ArticleDataAggregator ada = siteContent.getArticleAggregator(d);
        AbstractArticleManager am = AbstractArticleManager.getInstance(siteContent, context);
        for (int i = 1; i <= ada.size(); i++) {
          System.out.print(i + ".");
          ada.setCurrentWorkingArticleId(i);
          am.updateArticle();
        }
        System.out.println();
      }
    }
  }

  private static void rewriteColumns(Context baseContext, SiteContent siteContent) throws IOException, JDOMException {
    for (Division d : Division.values()) {
      if (!d.enableColumn()) {
        continue;
      }
      Set<String> finishedColumns = new HashSet<String>();
      Context context = new Context(baseContext, d);
      ArticleDataAggregator ada = siteContent.getArticleAggregator(d);
      ColumnManager cm = new ColumnManager(context, siteContent);
      for (int i = 1; i < ada.getReaders().size(); i++) {
        ada.setCurrentWorkingArticleId(i);
        List<String> columns = Arrays.asList(ada.getCurrentWorkingReader().getColumns());
        List<String> columnsToProcess = new ArrayList<String>();
        for (String column : columns) {
          if (!finishedColumns.contains(column)) {
            columnsToProcess.add(column);
            finishedColumns.add(column);
          }
        }
        for (String column : columnsToProcess) {
          cm.updateOneColumn(column);
        }
      }
    }
  }

  private static void rewriteAllPhotoAlbums(Context baseContext, SiteContent siteContent) throws IOException, JDOMException, ParseException, ClassNotFoundException {
    Context context = new Context(baseContext, Division.PHOTO);
    PhotoIndexAggregator pia = siteContent.getPhotoAggregator();
    AbstractArticleManager am = AbstractArticleManager.getInstance(siteContent, context);
    for (int i = 1; i <= pia.getReaders().size(); i++) {
      pia.setCurrentWorkingAlbumId(i);
      am.updateArticle();
    }
  }

  private void rewriteDivisions(Context baseContext, SiteContent siteContent) throws IOException, JDOMException, ClassNotFoundException {
    final PhotoIndexAggregator pia = siteContent.getPhotoAggregator();
    pia.setCurrentWorkingAlbumId(pia.size());
    for (Division d : Division.values()) {
      Context context = new Context(baseContext, d);
      ArticleDataAggregator ada = siteContent.getArticleAggregator(context);
      if (ada != null) {
        ada.setCurrentWorkingArticleId(ada.size());
      }
      final AbstractDivisionManager dm = AbstractDivisionManager.getInstance(context, siteContent);
      dm.updateDivision();
    }
  }

  private static void rewriteIndex(Context baseContext, SiteContent siteContent) throws IOException, JDOMException, ParseException, ClassNotFoundException {
    PhotoIndexAggregator pia = siteContent.getPhotoAggregator();
    pia.setCurrentWorkingAlbumId(pia.size());
    IndexManager.getInstance(new Context(baseContext, Division.PHOTO), siteContent).updateIndex();
    for (Division d : Division.values()) {
      if (d != Division.PHOTO) {
        Context context = new Context(baseContext, d);
        ArticleDataAggregator ada = siteContent.getArticleAggregator(d);
        IndexManager aim = IndexManager.getInstance(context, siteContent);
        ada.setCurrentWorkingArticleId(ada.getReaders().size());
        aim.updateIndex();
      }
    }
  }

  public static void main(String[] args) throws IOException, JDOMException, ClassNotFoundException, ParseException {
    SiteRefresher refresher = new SiteRefresher(args[0], args[1]);
    refresher.run();
  }
}
