package com.purplehaze.output;

import com.purplehaze.Context;
import com.purplehaze.Division;
import com.purplehaze.FileNameFilter;
import com.purplehaze.Utils;
import com.purplehaze.input.ArticleDataAggregator;
import com.purplehaze.input.ArticleDataReader;
import com.purplehaze.input.PhotoIndexAggregator;
import com.purplehaze.input.PhotoIndexReader;
import com.purplehaze.input.SiteContent;
import org.jdom.Comment;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.EntityRef;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.Text;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import static com.purplehaze.Utils.findElement;
import static com.purplehaze.output.Translations.*;

/**
 * Output manager for all article files.
 */
public abstract class AbstractArticleManager {
  protected final SiteContent siteContent;
  protected final Context context;

  AbstractArticleManager(SiteContent siteContent, Context context) {
    this.siteContent = siteContent;
    this.context = context;
  }

  public static AbstractArticleManager getInstance(SiteContent siteContent, Context context) {
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

  public final void updateRelatedPhotoAlbum() throws IOException, JDOMException, ParseException, ClassNotFoundException {
    ArticleDataAggregator ada = siteContent.getArticleAggregator(context);
    ArticleDataReader adr = ada.getCurrentWorkingReader();
    String album = adr.getPhoto();
    if (album == null) {
      return;
    }
    PhotoIndexAggregator pia = siteContent.getPhotoAggregator();
    pia.setCurrentWorkingAlbumId(Integer.parseInt(album));
    pia.associateArticleWithCurrentWorkingAlbum(adr);
    AbstractArticleManager photoAm =
        AbstractArticleManager.getInstance(siteContent, new Context(context, Division.PHOTO));
    photoAm.updateArticle();
  }

  public void updateArticle() throws IOException, JDOMException, ParseException, ClassNotFoundException {

    ArticleDataAggregator ada = siteContent.getArticleAggregator(context);
    ArticleDataReader adr = ada.getCurrentWorkingReader();
    String articleId = adr.getArticleId();
    Document doc = context.getFileManager().nonValidatedBuild(context.getArticleTemplateFile());
    Namespace ns = doc.getRootElement().getNamespace();
    // Use a one-off in memory cache so that current working article could be rewritten always.
    List<Element> pages = new ArticleContentParser(context, siteContent, new InMemoryContentCache())
        .getFormattedPages(articleId, FormatLevel.FULL, ns);
    int totalPageCount = pages.size();
    for (int i = 0; i < totalPageCount; i++) {
      Element pageE = pages.get(i);
      writeToArticle(doc, pageE, i, totalPageCount);
      File target = new File(context.getDivisionPath(), Utils.getArticleFileName(articleId, i + 1));
      context.getFileManager().xmlOutput(doc, target);
    }
  }

  public final void writeComment(boolean confirmOverwrite) throws IOException, JDOMException, ParseException {
    if (!context.getDivision().enableComment()) {
      return;
    }
    ArticleDataReader adr = siteContent.getArticleAggregator(context)
        .getCurrentWorkingReader();
    String articleId = adr.getArticleId();
    String commentFileName = Utils.getCommentFileName(articleId);
    File commentFile = new File(context.getDivisionPath(), commentFileName);
    if (confirmOverwrite && commentFile.exists() && commentFile.isFile() &&
        !Utils.booleanInput("There's already the comment file, overwrite?[y]", true)) {
      return;
    }
    Document doc = context.getFileManager().nonValidatedBuild(context.getCommentTemplateFile());
    Utils.setMenuFocus(doc, context.getDivision());
    Namespace ns = doc.getRootElement().getNamespace();
    // write title
    doc.getRootElement()
        .getChild("head", ns)
        .getChild("title", ns)
        .setText("Purplexsu's Space - " + Division.parse(context.getDivisionPath().getName()).getChinese() + " - " + COMMENT);
    //write TitleArea
    Element divE = Utils.findElement(doc, "div", "TitleArea");
    divE.removeContent();
    divE.addContent(new Comment("InstanceBeginEditable name=\"TitleArea\""));
    divE.addContent(new Element("h1", ns).setText(adr.getTitle()));
    if (adr.getSubtitle() != null) {
      divE.addContent(new Element("h2", ns).setText(" -- " + adr.getSubtitle()));
    }
    divE.addContent(new Element("div", ns)
        .setAttribute("class", "snippettext")
        .setAttribute("style", "text-align:center;")
        .addContent(new Element("a", ns)
            .setAttribute("href", Utils.getArticleFileName(adr.getArticleId()))
            .setText(RETURN_TO_ARTICLE)));
    divE.addContent(new Comment("InstanceEndEditable"));
    //change the value of 2 hidden inputs
    Utils.findElement(doc, "input", "name", "title").setAttribute("value", adr.getTitle());
    Utils.findElement(doc, "input", "name", "source")
        .setAttribute("value", context.getDivisionPath().getName() + "/" + commentFileName);

    divE = Utils.findElement(doc, "div", "CommentArea");
    divE.removeContent();
    divE.addContent(new Comment("InstanceBeginEditable name=\"CommentArea\""))
        .addContent(new EntityRef("nbsp"))
        .addContent(new Comment("InstanceEndEditable"));

    context.getFileManager().xmlOutput(doc, commentFile);
  }

  private void writeArticleNav(Element divE, Namespace ns, int workingAritcleId) {
    divE.removeContent();
    divE.addContent(new Comment("InstanceBeginEditable name=\"ArticleNav\""));

    ArticleDataAggregator ada = siteContent.getArticleAggregator(context);
    if (workingAritcleId > 1) {
      int previousId = getPreviousArticleId(workingAritcleId);
      if (previousId > 0) {
        final Element spanE = new Element("span", ns);
        spanE.setAttribute("class", "previous");
        divE.addContent(spanE);
        spanE.addContent(PREVIOIUS_ARTICLE_COLON)
            .addContent(new Element("a", ns)
                .setAttribute("id", "PreviousArticle")
                .setAttribute("href", Utils.getArticleFileName(previousId))
                .setText(ada.getReader(previousId).getTitle()));
      }
    }
    if (workingAritcleId < ada.size()) {
      int nextId = getNextArticleId(workingAritcleId);
      if (nextId <= ada.size()) {
        final Element spanE = new Element("span", ns);
        spanE.setAttribute("class", "next");
        divE.addContent(spanE);
        spanE.addContent(NEXT_ARTICLE_COLON)
            .addContent(new Element("a", ns).setAttribute("id", "NextArticle")
                .setAttribute("href", Utils.getArticleFileName(nextId))
                .setText(ada.getReader(nextId).getTitle()));
      }
    }
    divE.addContent(new Comment("InstanceEndEditable"));
  }

  protected void writeToArticle(Document doc, Element pageE, int pageIndex, int totalPageCount) throws IOException, JDOMException {
    Namespace ns = doc.getRootElement().getNamespace();
    //insert article content
    Element articleDivE = Utils.findElement(doc, "div", "ArticleArea");
    articleDivE.removeContent();
    articleDivE.addContent(new Comment("InstanceBeginEditable name=\"ArticleArea\""));
    List content = pageE.removeContent();
    articleDivE.addContent(content);
    articleDivE.addContent(new Comment("InstanceEndEditable"));

    ArticleDataReader adr = siteContent.getArticleAggregator(context)
        .getCurrentWorkingReader();
    //set title
    Element titleE = doc.getRootElement().getChild("head", ns).getChild("title", ns);
    final String divisionText = Division.parse(context.getDivisionPath().getName()).getChinese();
    final String title = Utils.getTitleText(divisionText, adr.getFullTitle(), pageIndex, totalPageCount);
    titleE.setText(title);

    // set menu focus
    Utils.setMenuFocus(doc, context.getDivision());

    // write page metadata
    writeDocMeta(doc, adr, ns);

    //set comment link
    writeCommentLink(doc, adr, ns);

    //set article nav
    writeArticleNavs(doc, ns, pageIndex);

    writeRelatedArticleLinks(doc, ns);

    //write page nav
    writePageNav(doc, ns, totalPageCount, pageIndex);

    //set meta info
    XMLOutputter output = new XMLOutputter(Format.getRawFormat());
    StringWriter sw = new StringWriter();
    output.output(content, sw);
    handleMetaInfo(doc, Utils.stripHtmlTags(sw.toString()));
  }

  private void writeDocMeta(Document doc, ArticleDataReader adr, Namespace ns) {
    Element html = doc.getRootElement();
    Element head = html.getChild("head", ns);
    String style = adr.getStyle();
    if (style != null) {
      head.addContent(new Element("style", ns).setText(adr.getStyle()));
    }
  }

  private void writeRelatedArticleLinks(Document doc, Namespace ns) {
    final ArticleDataAggregator ada = siteContent.getArticleAggregator(context);
    final ArticleDataReader workingReader = ada.getCurrentWorkingReader();
    final List<ArticleDataReader> relatedReaders = ada.getRelatedReaders(workingReader);
    final Element relatedDivE = Utils.findElement(doc, "div", "RelatedArticles");
    relatedDivE.removeContent();
    if (relatedReaders.size() == 0) {
      relatedDivE.addContent(new EntityRef("nbsp"));
      return;
    }
    relatedDivE.setText(RELATED_ARTICLE_COLON);
    int i = 0;
    for (ArticleDataReader related : relatedReaders) {
      relatedDivE.addContent(
          new Element("div", ns).addContent(
              new Element("a", ns)
                  .setText(related.getFullTitle())
                  .setAttribute("href", Utils.getArticleFileName(related.getArticleId()))));
    }
  }

  private void writeCommentLink(Document doc, ArticleDataReader adr, Namespace ns) throws JDOMException, IOException {
    Element commentDivE = Utils.findElement(doc, "div", "CommentLink");
    commentDivE.removeContent();
    commentDivE.addContent(new Comment("InstanceBeginEditable name=\"CommentLink\""));
    commentDivE
        .addContent(new Text(CONTACT_ME_COLON))
        .addContent(new Element("a", ns)
            .setText(WRITE_COMMENT)
            .setAttribute("href", Utils.getCommentFileName(adr.getArticleId())))
        .addContent(new EntityRef("nbsp"))
        .addContent(new Element("a", ns)
            .setAttribute("id", "contact")
            .setText(SEND_EMAIL)
            .setAttribute("href", "#"))
        .addContent(new Comment("InstanceEndEditable"));
  }

  private void writeArticleNavs(Document doc, Namespace ns, int pageIndex) throws IOException, JDOMException {
    int currentArticleId = siteContent.getArticleAggregator(context)
        .getCurrentWorkingArticleId();
    writeArticleNav(Utils.findElement(doc, "div", "ArticleNav"), ns, currentArticleId);
    int previousArticleId = getPreviousArticleId(currentArticleId);
    if (pageIndex == 0 && previousArticleId > 0) {
      FileNameFilter fileNameFilter = new FileNameFilter("article\\-" + Utils.formatInteger(previousArticleId, 3) + "\\-\\d{2}\\.html", false);
      for (File previousArticle : context.getDivisionPath().listFiles(fileNameFilter)) {
        Document previousDoc = context.getFileManager().nonValidatedBuild(previousArticle);
        writeArticleNav(Utils.findElement(previousDoc, "div", "ArticleNav"), ns, previousArticleId);
        context.getFileManager().xmlOutput(previousDoc, previousArticle);
      }
    }
  }

  private int getPreviousArticleId(int currentArticleId) {
    ArticleDataAggregator ada = siteContent.getArticleAggregator(context);
    ArticleDataReader adr = ada.getReader(currentArticleId);
    if (adr.navByColumn()) {
      int previousId = currentArticleId - 1;
      while (previousId > 0) {
        if (Arrays.equals(ada.getReader(previousId).getColumns(), adr.getColumns())) {
          break;
        }
        previousId--;
      }
      return previousId;
    } else {
      return currentArticleId - 1;
    }
  }

  private int getNextArticleId(int currentId) {
    ArticleDataAggregator ada = siteContent.getArticleAggregator(context);
    ArticleDataReader adr = ada.getReader(currentId);
    if (adr.navByColumn()) {
      int nextid = currentId + 1;
      while (nextid <= ada.size()) {
        if (Arrays.equals(ada.getReader(nextid).getColumns(), adr.getColumns())) {
          break;
        }
        nextid++;
      }
      return nextid;
    } else {
      return currentId + 1;
    }
  }

  private void handleMetaInfo(Document doc, String content) throws IOException {
    ArticleDataAggregator ada = siteContent.getArticleAggregator(context);
    ArticleDataReader adr = ada.getCurrentWorkingReader();
    Division division = ada.getDivision();
    String keywords = Utils.commaString(adr.getTags());
    Utils.findElement(doc, "meta", "name", "keywords").setAttribute("content", keywords);
    String desc = division.getChinese() + " ";
    if (content.length() > 100) {
      desc = content.substring(0, 99);
    } else {
      if (Utils.isEmptyString(adr.getSnippet())) {
        desc = keywords + desc;
      } else {
        desc = adr.getSnippet() + desc;
        if (desc.length() < 100) {
          desc = desc + keywords;
        }
      }
    }
    Utils.findElement(doc, "meta", "name", "description").setAttribute("content", desc);
  }

  private void writePageNav(Document doc, Namespace ns, int totalPageCount, int index) {
    Element pageNavE = Utils.findElement(doc, "div", "pagenav");
    pageNavE.removeContent();
    pageNavE.addContent(new Comment("InstanceBeginEditable name=\"PageNavArea\""));
    if (totalPageCount > 1) {
      Element ulE = new Element("ul", ns);
      pageNavE.addContent(ulE);
      String articleId = siteContent.getArticleAggregator(context)
          .getCurrentWorkingReader()
          .getArticleId();
      List<Integer> nav = Utils.getPageNav(totalPageCount, index + 1);
      if (index > 0) {
        ulE.addContent(new Element("li", ns)
            .addContent(new Element("a", ns)
                .setAttribute("id", "PreviousPage")
                .setAttribute("href", Utils.getArticleFileName(articleId, index))
                .setText(PREVIOIUS_PAGE)));
      }
      for (int i : nav) {
        if (index + 1 == i) {
          // for current page, don't add link to it
          ulE.addContent(new Element("li", ns).setText(String.valueOf(i)));
        } else if (i == 0) {
          // use ... when nav is 0
          ulE.addContent(new Element("li", ns).setText("..."));
        } else {
          Element aE = new Element("a", ns)
              .setAttribute("href", Utils.getArticleFileName(articleId, i))
              .setText(String.valueOf(i));
          ulE.addContent(new Element("li", ns).addContent(aE));
        }
      }
      if (index < totalPageCount - 1) {
        ulE.addContent(new Element("li", ns)
            .addContent(new Element("a", ns)
                .setAttribute("id", "NextPage")
                .setAttribute("href", Utils.getArticleFileName(articleId, index + 2))
                .setText(NEXT_PAGE)));
      }
    } else {
      pageNavE.addContent(new EntityRef("nbsp"));
    }
    pageNavE.addContent(new Comment("InstanceEndEditable"));
  }

  private static class BlogManager extends AbstractArticleManager {

    public BlogManager(SiteContent siteContent, Context context) {
      super(siteContent, context);
    }
  }

  private static class TravelManager extends AbstractArticleManager {

    public TravelManager(SiteContent siteContent, Context context) {
      super(siteContent, context);
    }
  }

  private static class MusicManager extends AbstractArticleManager {

    private static final String TRY_ONE_SONG_COLON = "\u8bd5\u542c\u4e00\u9996\uff1a";
    private static final String MP3_URL_PREFIX = "";

    public MusicManager(SiteContent siteContent, Context context) {
      super(siteContent, context);
    }

    @Override
    protected void writeToArticle(Document doc, Element pageE, int pageIndex, int totalPageCount) throws IOException, JDOMException {
      Namespace ns = doc.getRootElement().getNamespace();
      Element pE = new Element("p", ns);
      pageE.addContent(pE);
      ArticleDataReader adr = siteContent.getArticleAggregator(context)
          .getCurrentWorkingReader();
      pE.addContent(CHINESE_INDENT)
          .addContent(new Element("b", ns).setText(TRY_ONE_SONG_COLON))
          .addContent(new Element("a", ns)
              .setAttribute("href", MP3_URL_PREFIX + adr.getArticleId() + ".mp3")
              .setText(adr.getOptional()));
      super.writeToArticle(doc, pageE, pageIndex, totalPageCount);
    }
  }

  private static class MovieManager extends AbstractArticleManager {

    public MovieManager(SiteContent siteContent, Context context) {
      super(siteContent, context);
    }
  }

  private static class AppraisalManager extends AbstractArticleManager {

    public AppraisalManager(SiteContent siteContent, Context context) {
      super(siteContent, context);
    }
  }

  private static class PhotoManager extends AbstractArticleManager {

    private File albumPath;

    PhotoManager(SiteContent siteContent, Context context) {
      super(siteContent, context);
    }

    @Override
    public void updateArticle() throws IOException, JDOMException, ParseException {
      PhotoIndexReader pir = siteContent.getPhotoAggregator()
          .getCurrentWorkingAlbumReader();
      Document _sample = context.getFileManager().nonValidatedBuild(context.getArticleTemplateFile());
      albumPath = new File(context.getDivisionPath(), pir.getAlbumId());
      File[] photos = albumPath.listFiles(new FileNameFilter("\\d{2}\\.jpg", false));
      if (photos.length != pir.getTags().size()) {
        throw new IOException("Photo number and tag number don't match! Tags:"
            + pir.getTags().size() + " & photos:" + photos.length);
      }
      Arrays.sort(photos);
      writeIndexPage(_sample);
      writeArticle(photos, _sample, pir);
    }

    private void writeIndexPage(Document _sample) throws IOException {
      Document doc = (Document) _sample.clone();
      PhotoIndexReader pir = siteContent.getPhotoAggregator()
          .getCurrentWorkingAlbumReader();

      Namespace ns = doc.getRootElement().getNamespace();
      doc.getRootElement()
          .getChild("head", ns)
          .getChild("title", ns).setText(Division.PHOTO.getChinese() + " - " + pir.getTitle() + " (Purplexsu's Space)");
      Element captionE = findElement(doc, "div", "CaptionArea");
      captionE.removeContent();
      captionE.addContent(new Comment("InstanceBeginEditable name=\"CaptionArea\""));
      captionE.addContent(pir.getTitle())
          .addContent(LEFT_BRACE)
          .addContent(new Element("a", ns)
              .setText(RETURN_TO + ALBUM)
              .setAttribute("href", "../index.html"))
          .addContent(RIGHT_BRACE);
      String articleTitle = pir.getAssociatedArticleTitle();
      String articleLink = pir.getAssociatedArticleLink();
      if (!Utils.isEmptyString(articleTitle) && !Utils.isEmptyString(articleLink)) {
        captionE.addContent(new Element("br", ns));
        captionE.addContent(RELATED_ARTICLE_COLON);
        Element aE = new Element("a", ns);
        aE.setText(articleTitle);
        aE.setAttribute("href", articleLink);
        captionE.addContent(aE);
      }
      captionE.addContent(new Comment("InstanceEndEditable"));

      File photo = new File(albumPath, "collage.jpg");
      Element imgE = findElement(doc, "img", "PhotoArea");
      Utils.fillImgTag(photo, imgE, pir.getTitle());
      Element aE = new Element("a", imgE.getNamespace());
      insertParent(imgE, aE);
      aE.setAttribute("href", "01.html").setAttribute("id", "NextPage");
      imgE.removeAttribute("id");

      StringBuilder description = new StringBuilder()
          .append(pir.getTitle())
          .append(PHOTO)
          .append(PERIOD);
      if (!Utils.isEmptyString(pir.getAssociatedArticleTitle())) {
        description.append(pir.getAssociatedArticleTitle())
            .append(PHOTO)
            .append(PERIOD);
      }
      Utils.findElement(doc, "meta", "name", "description").setAttribute("content", description.toString());

      context.getFileManager().xmlOutput(doc, new File(albumPath, "index.html"));
      System.out.print('#');

    }

    private void insertParent(Element child, Element newParent) {
      Element father = child.getParentElement();
      int index = child.getParent().indexOf(child);
      father.setContent(index, newParent);
      newParent.setContent(child);
    }

    private void writeArticle(File[] photos, Document _sample, PhotoIndexReader pir) throws IOException {
      NumberFormat nf = NumberFormat.getNumberInstance();
      nf.setMaximumIntegerDigits(2);
      nf.setMinimumIntegerDigits(2);
      nf.setGroupingUsed(false);
      for (int i = 0; i < photos.length; i++) {
        File photo = photos[i];
        Document doc = (Document) _sample.clone();
        Namespace ns = doc.getRootElement().getNamespace();

        Element imgE = findElement(doc, "img", "PhotoArea");
        ImageIcon img = new ImageIcon(photo.getCanonicalPath());
        String photoName = photo.getName();
        imgE.setAttribute("src", photoName);
        imgE.setAttribute("height", String.valueOf(img.getIconHeight()));
        imgE.setAttribute("width", String.valueOf(img.getIconWidth()));
        String alt = pir.getTags().get(i).replace("/", " ");
        imgE.setAttribute("alt", alt);
        imgE.setAttribute("title", alt);
        if (!Utils.isEmptyString(pir.getAssociatedArticleLink())) {
          Element aE = new Element("a", ns)
              .setAttribute("href", pir.getAssociatedArticleLink())
              .setAttribute("id", "PhotoLink");
          insertParent(imgE, aE);
        }

        Element titleE = doc.getRootElement()
            .getChild("head", ns)
            .getChild("title", ns);
        titleE.setText(new StringBuilder()
            .append(Division.PHOTO.getChinese())
            .append(" - ")
            .append(pir.getTitle())
            .append(" - ")
            .append(i + 1)
            .append(" (Purplexsu's Space)")
            .toString());

        int currentSequence = Integer.parseInt(photoName.substring(0, 2));
        int previousSequence = currentSequence - 1;
        int nextSequence = currentSequence + 1;
        if (currentSequence == 1) {
          previousSequence = photos.length;
        }
        if (currentSequence == photos.length) {
          nextSequence = 1;
        }
        Element previousLink = new Element("a", ns)
            .setAttribute("href", nf.format(previousSequence) + ".html")
            .setAttribute("id", "PreviousPage")
            .addContent(new Element("img", ns)
                .setAttribute("src", "../../images/blank.gif")
                .setAttribute("height", "77")
                .setAttribute("width", "48")
                .setAttribute("class", "photo_arrow_l"));
        Element nextLink = new Element("a", ns)
            .setAttribute("href", nf.format(nextSequence) + ".html")
            .setAttribute("id", "NextPage")
            .addContent(new Element("img", ns)
                .setAttribute("src", "../../images/blank.gif")
                .setAttribute("height", "77")
                .setAttribute("width", "48")
                .setAttribute("class", "photo_arrow_r"));

        Element captionDivE = findElement(doc, "div", "CaptionArea");
        captionDivE.removeContent();
        captionDivE.addContent(new Comment("InstanceBeginEditable name=\"CaptionArea\""));
        captionDivE.addContent(previousLink);
        captionDivE.addContent(new Element("a", ns).setAttribute("href", "../../index.html").setText(HOME_PAGE))
            .addContent(new EntityRef("nbsp"))
            .addContent(new Text("->"))
            .addContent(new EntityRef("nbsp"))
            .addContent(new Element("a", ns).setAttribute("href", "../index.html").setText(ALBUM))
            .addContent(new EntityRef("nbsp"))
            .addContent(new Text("->"))
            .addContent(new EntityRef("nbsp"))
            .addContent(new Element("a", ns).setAttribute("href", "index.html").setText(pir.getTitle()))
            .addContent(new Text(COLON));
        captionDivE.addContent(new Element("a", ns)
            .setAttribute("href", photoName)
            .setText(pir.getTags().get(i).replace("/", " ")));
        captionDivE.addContent(nextLink);
        captionDivE.addContent(new Comment("InstanceEndEditable"));

        String meta = pir.getTags().get(i) + "/" + Division.PHOTO.getChinese();
        String keywords = meta.replace("/", ",");
        Utils.findElement(doc, "meta", "name", "keywords").setAttribute("content", keywords);
        StringBuilder description = new StringBuilder(meta.replace("/", ""))
            .append(PERIOD)
            .append(pir.getTitle())
            .append(PHOTO)
            .append(PERIOD);
        if (!Utils.isEmptyString(pir.getAssociatedArticleTitle())) {
          description.append(pir.getAssociatedArticleTitle())
              .append(PHOTO)
              .append(PERIOD);
        }
        Utils.findElement(doc, "meta", "name", "description").setAttribute("content", description.toString());

        String html = photoName.replace("jpg", "html");
        context.getFileManager().xmlOutput(doc, new File(albumPath, html));
        System.out.print('#');
      }
      System.out.println();
    }
  }
}
