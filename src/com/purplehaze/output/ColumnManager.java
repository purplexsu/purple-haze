package com.purplehaze.output;

import com.purplehaze.Context;
import com.purplehaze.Utils;
import com.purplehaze.input.ArticleDataAggregator;
import com.purplehaze.input.ArticleDataReader;
import com.purplehaze.input.SiteContent;
import org.jdom.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;
import java.util.Random;

/**
 * The output manager for column files.
 */
public class ColumnManager {

  private static final int MAX_ARTICLE_PER_PAGE = 10;
  private static final int MIN_ARTICLE_PER_PAGE = 4;
  private static final int CELLS_PER_ROW = 3;
  private final Context context;
  private final SiteContent siteContent;

  public ColumnManager(Context context, SiteContent siteContent) {
    this.context = context;
    this.siteContent = siteContent;
  }

  public void updateColumns() throws IOException {
    ArticleDataAggregator ada = siteContent.getArticleAggregator(context);
    if (!ada.getDivision().enableColumn()) {
      return;
    }
    for (String column : ada.getCurrentWorkingReader().getColumns()) {
      updateOneColumn(column);
    }
  }

  public ArticleSnippet generateSnippet(ArticleDataReader adr) throws IOException, ClassNotFoundException {
    String title = adr.getFullTitle();
    String snippet = adr.getSnippet();
    if (snippet == null) {
      snippet = new ArticleContentParser(context, siteContent)
          .getFormatedPages(adr.getArticleId(), FormatLevel.RAW, null).get(0).getTextTrim();
      snippet = snippet.substring(0, Math.min(new Random().nextInt(20) + 50, snippet.length()))
          + Translations.SUSPENSION_POINTS;
    }
    return new ArticleSnippet(getArticleFile(adr.getArticleId()).getName(),
        title, snippet, adr.getImgSnippetPath(context.getDivisionPath()));
  }


  private File getArticleFile(String newArticleId) {
    return new File(context.getDivisionPath(), Utils.getArticleFileName(newArticleId));
  }

  /**
   * @param columnId
   */
  public void updateOneColumn(String columnId) {
    try {
      File columnDataFile = new File(context.getDivisionDataPath(), Utils.getColumnDataFileName(columnId));
      if (columnDataFile.exists() && columnDataFile.canRead()) {
        Properties meta = new Properties();
        meta.load(new InputStreamReader(new FileInputStream(columnDataFile), Utils.DEFAULT_CHARSET));
        Document doc = context.getFileManager().nonValidatedBuild(context.getColumnTemplateFile());
        final List<ArticleDataReader> allArticles = siteContent.getArticleAggregator(context)
            .getReaders(columnId);
        List<DivisionPageInfo> schema = DivisionPageInfo.getSchema(allArticles.size(),
            MAX_ARTICLE_PER_PAGE, MIN_ARTICLE_PER_PAGE, 1, "column-" + columnId);
        for (DivisionPageInfo dpi : schema) {
          int toIndex = dpi.getArticleStartInCurrentPage();
          int fromIndex = toIndex - dpi.getArticleCountInCurrentPage();
          List<ArticleDataReader> subList = allArticles.subList(fromIndex, toIndex);
          updateOneColumnPage(columnId, doc, subList, dpi, meta);
        }
      } else {
        System.out.println("Skip updating column:" + columnId);
      }
    } catch (Exception e) {
      System.err.println("Error in update:" + Utils.getColumnFileName(columnId));
      e.printStackTrace();  //TODO: Need further handling
    }
  }

  private void updateOneColumnPage(
      String columnId,
      Document doc,
      List<ArticleDataReader> adrsInThisColumn,
      DivisionPageInfo dpi,
      Properties meta)
      throws IOException, ClassNotFoundException {
    Utils.setMenuFocus(doc, context.getDivision());
    final Element htmlE = doc.getRootElement();
    Namespace ns = htmlE.getNamespace();
    final Element headE = htmlE.getChild("head", ns);
    headE.getChild("title", ns)
        .setText(Utils.getTitleText(
            context.getDivision().getChinese(),
            meta.getProperty("title"),
            dpi.getCurrentPageIndex(),
            dpi.getTotalPageCount()));
    Utils.findElement(doc, "meta", "name", "keywords").setAttribute("content", meta.getProperty("keywords"));
    Utils.findElement(doc, "meta", "name", "description").setAttribute("content", meta.getProperty("description"));

    Element headerE = Utils.findElement(doc, "div", "ColumnHeader");
    headerE.removeContent();
    headerE.addContent(new Comment("InstanceBeginEditable name=\"ColumnHeader\""));
    String headerImgFileName = "../images/" + columnId + ".gif";
    File headerImgFile = new File(context.getDivisionPath(), headerImgFileName);
    if (headerImgFile.exists()) {
      Element headerImgE = new Element("img", ns);
      Utils.fillImgTag(headerImgFileName, headerImgFile, headerImgE, columnId);
      headerE.addContent(headerImgE);
    }
    if (!Utils.isEmptyString(meta.getProperty("columnheader"))) {
      headerE.addContent(new Element("h2", ns).setText(meta.getProperty("columnheader")));
    }
    headerE.addContent(new Comment("InstanceEndEditable"));

    Element listE = Utils.findElement(doc, "div", "ColumnList");
    listE.removeContent();
    Element listTableE = new Element("div", ns).setAttribute("id", "ColumnListTable");
    listE.addContent(new Comment("InstanceBeginEditable name=\"ColumnList\""))
        .addContent(listTableE)
        .addContent(new Comment("InstanceEndEditable"));
    writeColumnList(adrsInThisColumn, listTableE);
    File columnFile = new File(context.getDivisionPath(), dpi.getFileName());
    if (dpi.hasNextPage() || dpi.hasPreviousPage()) {
      Element navDivE = Utils.findElement(doc, "div", "NavArea");
      navDivE.removeContent();
      navDivE.addContent(new Comment("InstanceBeginEditable name=\"NavArea\""));
      if (dpi.hasPreviousPage()) {
        navDivE.addContent(new Element("a", ns)
            .setAttribute("href", dpi.getPreviousFileName())
            .setText(Translations.PREVIOIUS_PAGE));
      }
      if (dpi.hasNextPage()) {
        navDivE.addContent(new EntityRef("nbsp"))
            .addContent(new EntityRef("nbsp"))
            .addContent(new Element("a", ns)
                .setAttribute("href", dpi.getNextFileName())
                .setText(Translations.NEXT_PAGE));
      }
      navDivE.addContent(new Comment("InstanceEndEditable"));
    }
    context.getFileManager().xmlOutput(doc, columnFile);
    System.out.println("Successful update:" + columnFile.getAbsolutePath());
  }

  private void writeColumnList(List<ArticleDataReader> readers, Element divE) throws IOException, ClassNotFoundException {
    switch (readers.size()) {
      case 1: {
        writeType1(readers, divE);
        break;
      }
      case 2:
      case 3: {
        writeType2(readers, divE);
        break;
      }
      default: {
        writeType3(readers, divE);
        break;
      }
    }
  }

  private void writeType3(List<ArticleDataReader> readers, Element divE) throws IOException, ClassNotFoundException {
    divE.removeContent();
    Namespace ns = divE.getNamespace();
    for (int i = readers.size() - 1; i >= 0; i--) {
      ArticleDataReader reader = readers.get(i);
      Element subDivE = new Element("div", ns).setAttribute("class", "columncell_l");
      generateSnippet(reader).writeLeftAligned(subDivE, ns);
      Element indexE = getArticleIndexesElement(reader, ns);
      if (indexE != null) {
        subDivE.addContent(indexE);
      }
      divE.addContent(subDivE);
    }
  }

  private void writeType2(List<ArticleDataReader> readers, Element divE) throws IOException, ClassNotFoundException {
    divE.removeContent();
    Namespace ns = divE.getNamespace();
    for (ArticleDataReader adr : readers) {
      Element indexE = getArticleIndexesElement(adr, ns);
      if (indexE != null) {
        divE.addContent(0, indexE);
      }
      divE.addContent(0, generateSnippet(adr).writeCenterAligned(ns));
      if (readers.indexOf(adr) < readers.size() - 1) {
        divE.addContent(0, new Element("div", ns)
            .addContent(new Element("hr", ns)));
      }
    }
  }

  private void writeType1(List<ArticleDataReader> readers, Element divE) throws IOException, ClassNotFoundException {
    Namespace ns = divE.getNamespace();
    divE.removeContent();
    ArticleDataReader adr = readers.get(0);
    divE.addContent(generateSnippet(adr).writeCenterAligned(ns));
    Element indexE = getArticleIndexesElement(adr, ns);
    if (indexE != null) {
      divE.addContent(indexE);
    }
    Element imgE = new Element("img", ns);
    imgE.setAttribute("src", "../images/" + context.getDivisionPath().getName() + "decorate.gif");
    imgE.setAttribute("width", "289");
    imgE.setAttribute("height", "135");
    imgE.setAttribute("class", "columndecorate");
    divE.addContent(imgE);
  }

  private Element getArticleIndexesElement(ArticleDataReader adr, Namespace ns) throws IOException, ClassNotFoundException {
    if (!adr.disableArticleIndex()) {
      List<Element> subtitles = new ArticleContentParser(context, siteContent)
          .getPageSubtitles(adr.getArticleId(), ns);
      if (subtitles != null && subtitles.size() > 0) {
        int titleCount = subtitles.size();
        int cellCount = (titleCount / CELLS_PER_ROW + (titleCount % CELLS_PER_ROW == 0 ? 0 : 1)) * CELLS_PER_ROW;
        Element tableE = new Element("table", ns).setAttribute("class", "columncell_words");
        Element trE = null;
        for (int i = 0; i < cellCount; i++) {
          if (i % CELLS_PER_ROW == 0) {
            trE = new Element("tr", ns);
            tableE.addContent(trE);
          }
          if (trE != null) {
            Element tdE = new Element("td", ns);
            trE.addContent(tdE);
            if (i < titleCount) {
              tdE.addContent(subtitles.get(i));
            } else {
              tdE.addContent(new EntityRef("nbsp"));
            }
          }
        }
        return tableE;
      }
    }
    return null;
  }
}
