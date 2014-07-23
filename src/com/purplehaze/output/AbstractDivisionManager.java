package com.purplehaze.output;

import com.purplehaze.Context;
import com.purplehaze.Utils;
import com.purplehaze.input.*;
import org.jdom.*;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static com.purplehaze.Utils.findElement;

/**
 * Output manager for all division files.
 */
public abstract class AbstractDivisionManager {

  protected final SiteContent siteContent;
  protected Context context;

  public AbstractDivisionManager(SiteContent siteContent, Context context) {
    this.siteContent = siteContent;
    this.context = context;
  }

  public static AbstractDivisionManager getInstance(Context context, SiteContent siteContent) {
    switch (context.getDivision()) {
      case BLOG: {
        return new BlogManager(siteContent, context);
      }
      case TRAVEL: {
        return new TravelManager(siteContent, context);
      }
      case MUSIC: {
        return new MusicManager(siteContent, context);
      }
      case MOVIE: {
        return new MovieManager(siteContent, context);
      }
      case APPRAISAL: {
        return new AppraisalManager(siteContent, context);
      }
      case PHOTO: {
        return new PhotoManager(siteContent, context);
      }
      default: {
        return null;
      }
    }
  }

  public abstract void updateDivision() throws IOException, JDOMException, ClassNotFoundException;

  private static class BlogManager extends AbstractDivisionManager {

    private static final int MAX_ARTICLE_PER_PAGE = 10;
    private static final String READ_BLOG = "\u8bfb\u8bfb\u535a\u5ba2";
    private static final String READ_COMMENT = "\u7785\u7785\u8bc4\u8bba";

    public BlogManager(SiteContent siteContent, Context context) {
      super(siteContent, context);
    }


    @Override
    public void updateDivision() throws IOException, JDOMException, ClassNotFoundException {
      Document _sample = context.getFileManager().nonValidatedBuild(context.getDivisionIndexTemplateFile());
      Namespace ns = _sample.getRootElement().getNamespace();
      List<ArticleDataReader> readers = siteContent.getArticleAggregator(context).getReaders();

      List<DivisionPageInfo> schema = DivisionPageInfo.getSchema(readers.size(), MAX_ARTICLE_PER_PAGE, 4, 1, "index");
      for (DivisionPageInfo dpi : schema) {
        Document doc = (Document) _sample.clone();
        Utils.setMenuFocus(doc, context.getDivision());
        Element divE = Utils.findElement(doc, "div", "UpdateTable");
        divE.removeContent();
        for (int i = dpi.getArticleStartInCurrentPage(); dpi.getArticleStartInCurrentPage() - i < MAX_ARTICLE_PER_PAGE && i > 0; i--) {
          boolean lastSnippet = (dpi.getArticleStartInCurrentPage() - i == MAX_ARTICLE_PER_PAGE - 1) || (i == 1);
          writeOneArticleSnippet(divE, ns, readers.get(i - 1), lastSnippet);
        }
        if (dpi.getTotalPageCount() > 1) {
          Element navDivE = new Element("div", ns)
              .setAttribute("class", "header");
          divE.addContent(navDivE);
          if (dpi.hasPreviousPage()) {
            navDivE.addContent(new Element("a", ns)
                .setAttribute("href", dpi.getPreviousFileName())
                .setText(Translations.PREVIOIUS_PAGE));
          }
          if (dpi.hasPreviousPage() && dpi.hasNextPage()) {
            navDivE.addContent(new EntityRef("nbsp")).addContent(new EntityRef("nbsp"));
          }
          if (dpi.hasNextPage()) {
            //need >> link
            navDivE.addContent(new Element("a", ns)
                .setAttribute("href", dpi.getNextFileName())
                .setText(Translations.NEXT_PAGE));
          }
        }
        context.getFileManager().xmlOutput(doc, new File(context.getDivisionPath(), dpi.getFileName()));
      }
    }

    private void writeOneArticleSnippet(Element tdE, Namespace ns, ArticleDataReader reader, boolean lastSnippet)
        throws IOException, ClassNotFoundException {
      tdE.addContent(new Element("div", ns).setAttribute("class", "timestamp").setText(reader.getDisplayTime()));
      tdE.addContent(new Element("div", ns).setAttribute("class", "snippetheader").setText(reader.getFullTitle()));
      Element snippetElement = new Element("div", ns).setAttribute("class", "snippettext");
      final String snippet = reader.getSnippet();
      if (Utils.isEmptyString(snippet)) {
        Element htmlE = new ArticleContentParser(context, siteContent)
            .getFormattedPages(reader.getArticleId(), FormatLevel.SNIPPET, ns).get(0);
        List content = htmlE.removeContent();
        int count = 0;
        for (Object o : content) {
          Content c = (Content) o;
          if (c instanceof Element && Utils.equals(((Element) c).getName(), "div")) {
            continue;
          }
          snippetElement.addContent(c);
          if (c instanceof Element && Utils.equals(((Element) c).getName(), "br")) {
            count++;
          }
          if (count >= 3) {
            break;
          }
        }
      } else {
        snippetElement.setText(snippet);
      }
      tdE.addContent(snippetElement);
      String articleName = Utils.getArticleFileName(reader.getArticleId());
      String commentName = Utils.getCommentFileName(reader.getArticleId());
      tdE.addContent(new Element("div", ns).setAttribute("class", "timestamp")
          .addContent(new Element("a", ns)
              .setAttribute("href", articleName).setText(READ_BLOG))
          .addContent(new EntityRef("nbsp"))
          .addContent(new EntityRef("nbsp"))
          .addContent(new Element("a", ns)
              .setAttribute("href", commentName).setText(READ_COMMENT)));
      if (!lastSnippet) {
        tdE.addContent(new Element("hr", ns));
      }
    }
  }

  private static class TravelManager extends AbstractDivisionManager {

    private static final int NUM_OF_SNIPPET_IN_DIVISION_PAGE = 4;

    public TravelManager(SiteContent siteContent, Context context) {
      super(siteContent, context);
    }


    @Override
    public void updateDivision() throws IOException, JDOMException {
      ArticleDataAggregator ada = siteContent.getArticleAggregator(context);
      int size = ada.size();
      if (size - Integer.parseInt(ada.getCurrentWorkingReader().getArticleId())
          >= NUM_OF_SNIPPET_IN_DIVISION_PAGE) {
        return;
      }
      final File divisionTemplate = context.getDivisionIndexTemplateFile();
      final File divisionIndex = context.getDivisionIndexFile();
      writeToDivisionFile(size, divisionTemplate, divisionIndex);
      final File chinaTemplate = new File(divisionTemplate.getParentFile(), "index-china.html");
      final File chinaIndex = new File(divisionIndex.getParentFile(), "index-china.html");
      writeToDivisionFile(size, chinaTemplate, chinaIndex);
    }

    private void writeToDivisionFile(int size, File divisionTemplate, File divisionFile)
        throws IOException, JDOMException {
      Document doc = context.getFileManager().nonValidatedBuild(divisionTemplate);
      Utils.setMenuFocus(doc, context.getDivision());
      Element divE = Utils.findElement(doc, "div", "UpdateTable");
      final Namespace ns = divE.getNamespace();
      divE.removeContent();
      for (int i = 0; i < NUM_OF_SNIPPET_IN_DIVISION_PAGE; i++) {
        generateSnippet(siteContent.getArticleAggregator(context).getReader(size - i))
            .writeLeftAligned(divE, ns);
      }
      context.getFileManager().xmlOutput(doc, divisionFile);
    }

    private ArticleSnippet generateSnippet(ArticleDataReader adr) {
      String snippetTitle = adr.getTitle() +
          (adr.getSubtitle() == null ? "" : (" -- " + adr.getSubtitle())) +
          "(" + adr.getDisplayTime() + ")";
      final String articleFileName = Utils.getArticleFileName(adr.getArticleId());
      return new ArticleSnippet(articleFileName, snippetTitle, adr.getSnippet(), null);
    }
  }

  private static class MusicManager extends AbstractDivisionManager {

    public static final int MAX_SNIPPETS_PER_COLUMN = 2;

    public MusicManager(SiteContent siteContent, Context context) {
      super(siteContent, context);
    }

    @Override
    public void updateDivision() throws IOException, JDOMException, ClassNotFoundException {
      Document doc = context.getFileManager().nonValidatedBuild(context.getDivisionIndexTemplateFile());
      Utils.setMenuFocus(doc, context.getDivision());
      ArticleDataAggregator ada = siteContent.getArticleAggregator(context);
      for (String column : ada.getAllColumns()) {
        Element divE = Utils.findElement(doc, "div", column + "music");
        Namespace ns = divE.getNamespace();
        divE.removeContent();
        List<ArticleDataReader> columnReaders = ada.getReaders(column);
        for (int i = columnReaders.size() - 1; columnReaders.size() - i <= MAX_SNIPPETS_PER_COLUMN && i >= 0; i--) {
          ArticleDataReader reader = columnReaders.get(i);
          ColumnManager cm = new ColumnManager(context, siteContent);
          ArticleSnippet snippet = cm.generateSnippet(reader);
          snippet.writeLeftAligned(divE, ns);
          divE.addContent(new Element("div", ns).setAttribute("class", "verticalspace").addContent(new EntityRef("nbsp")));
        }
      }
      context.getFileManager().xmlOutput(doc, context.getDivisionIndexFile());
    }
  }

  private static class MovieManager extends AbstractDivisionManager {

    private static final int ICON_PER_COLUMN = 12;

    public MovieManager(SiteContent siteContent, Context context) {
      super(siteContent, context);
    }

    @Override
    public void updateDivision() throws IOException, JDOMException {
      Document doc = context.getFileManager().nonValidatedBuild(context.getDivisionIndexTemplateFile());
      Utils.setMenuFocus(doc, context.getDivision());
      ArticleDataAggregator ada = siteContent.getArticleAggregator(context);
      for (String column : ada.getAllColumns()) {
        Element divE = Utils.findElement(doc, "div", column + "movies");
        divE.removeContent();
        Namespace ns = divE.getNamespace();
        Element ulE = new Element("ul", ns);
        divE.addContent(ulE);
        List<ArticleDataReader> readers = ada.getReaders(column);
        Collections.reverse(readers);
        int i = 0;
        for (ArticleDataReader reader : readers) {
          Element liE = new Element("li", ns)
              .setAttribute("class", "inlinedlist")
              .addContent(
                  new Element("a", ns)
                      .setAttribute("href", Utils.getArticleFileName(reader.getArticleId()))
                      .addContent(
                          new Element("img", ns)
                              .setAttribute("width", "90")
                              .setAttribute("height", "60")
                              .setAttribute("title", reader.getTitle())
                              .setAttribute("alt", reader.getTitle())
                              .setAttribute("src", reader.getImgSnippetPath(context.getDivisionPath()))));
          ulE.addContent(liE);
          if (++i == ICON_PER_COLUMN) {
            break;
          }
        }
      }
      context.getFileManager().xmlOutput(doc, context.getDivisionIndexFile());
    }
  }

  private static class AppraisalManager extends AbstractDivisionManager {

    private static final int MAX_SNIPPETS_PER_COLUMN = 3;

    public AppraisalManager(SiteContent siteContent, Context context) {
      super(siteContent, context);
    }

    @Override
    public void updateDivision() throws IOException, JDOMException, ClassNotFoundException {
      Document doc = context.getFileManager().nonValidatedBuild(context.getDivisionIndexTemplateFile());
      Utils.setMenuFocus(doc, context.getDivision());
      ArticleDataAggregator ada = siteContent.getArticleAggregator(context);
      for (String column : ada.getAllColumns()) {
        Element divE = Utils.findElement(doc, "div", column + "Area");
        Namespace ns = divE.getNamespace();
        List content = divE.getChildren("div", ns);
        Element titleDivE = (Element) content.get(0);
        Element introDivE = (Element) content.get(1);
        divE.removeContent();
        divE.addContent(titleDivE).addContent(introDivE);
        List<ArticleDataReader> readers = ada.getReaders(column);
        int count = Math.min(MAX_SNIPPETS_PER_COLUMN, readers.size());
        for (int i = readers.size() - 1; readers.size() - i <= count; i--) {
          ArticleDataReader reader = readers.get(i);
          String snippet = reader.getSnippet();
          if (snippet == null) {
            snippet = new ArticleContentParser(context, siteContent)
                .getFormattedPages(reader.getArticleId(), FormatLevel.RAW, ns).get(0).getTextTrim();
            snippet = snippet.substring(0, Math.min(new Random().nextInt(20) + 40, snippet.length()))
                + Translations.SUSPENSION_POINTS;
          }
          ArticleSnippet as = new ArticleSnippet(Utils.getArticleFileName(reader.getArticleId()),
              reader.getFullTitle(), snippet, reader.getImgSnippetPath(context.getDivisionPath()));
          Element cellDivE = new Element("div", ns).setAttribute("class", "columncell_l");
          divE.addContent(cellDivE);
          as.writeLeftAligned(cellDivE, ns);
        }
        divE.addContent(new Element("hr", ns));
      }
      context.getFileManager().xmlOutput(doc, context.getDivisionIndexFile());
    }
  }

  private static class PhotoManager extends AbstractDivisionManager {

    public PhotoManager(SiteContent siteContent, Context context) {
      super(siteContent, context);
    }

    @Override
    public void updateDivision() throws IOException, JDOMException {
      Document doc = context.getFileManager().nonValidatedBuild(context.getDivisionIndexTemplateFile());
      Utils.setMenuFocus(doc, context.getDivision());
      final PhotoIndexAggregator pia = siteContent.getPhotoAggregator();
      int snippetCount = pia.size();
      List<DivisionPageInfo> schema = DivisionPageInfo.getSchema(snippetCount, 35, 10, 5, "index");
      for (DivisionPageInfo dpi : schema) {
        updateDivisionOnePage(pia, doc, dpi);
      }
    }


    private void updateDivisionOnePage(PhotoIndexAggregator pia, Document doc, DivisionPageInfo dpi) throws IOException {
      Element divE = findElement(doc, "div", "ListTable");
      divE.removeContent();
      Namespace ns = divE.getNamespace();
      System.out.println();
      for (int i = 0; i < dpi.getArticleCountInCurrentPage(); i++) {
        //current=count ~ 1
        int current = dpi.getArticleStartInCurrentPage() - i;
        PhotoIndexReader currentPir = pia.getReader(current);
        if (currentPir.isIndexSkipped()) {
          continue;
        }
        int photoCount = currentPir.getTags().size();
        String subDirName = Utils.formatInteger(current, 3);
        Element cellDivE = new Element("div", ns);
        cellDivE.addContent(new Element("img", ns)
            .setAttribute("height", "60")
            .setAttribute("width", "90")
            .setAttribute("src", subDirName + "/snippet.jpg")
            .setAttribute("alt", currentPir.getTitle()));
        cellDivE.addContent(new Element("br", ns));
        cellDivE.addContent(new Element("a", ns)
            .setAttribute("href", subDirName + "/index.html")
            .addContent(currentPir.getTitle())
            .addContent(new Element("br", ns))
            .addContent("(" + photoCount + ")"));
        divE.addContent(cellDivE);
        System.out.print('#');
      }

      divE = Utils.findElement(doc, "div", "NavDiv");
      divE.removeContent();
      if (dpi.getTotalPageCount() > 1) {
        // export nav info
        if (dpi.hasPreviousPage()) {
          // not the first page
          divE.addContent(new EntityRef("nbsp"))
              .addContent(new EntityRef("nbsp"))
              .addContent(new Element("a", ns)
                  .setAttribute("href", dpi.getPreviousFileName())
                  .setText(Translations.PREVIOIUS_PAGE));
        }
        if (dpi.hasNextPage()) {
          // not the last page
          divE.addContent(new EntityRef("nbsp"))
              .addContent(new EntityRef("nbsp"))
              .addContent(new Element("a", ns)
                  .setAttribute("href", dpi.getNextFileName())
                  .setText(Translations.NEXT_PAGE));
        }
      }
      context.getFileManager().xmlOutput(doc, new File(context.getDivisionPath(), dpi.getFileName()));
    }
  }
}
