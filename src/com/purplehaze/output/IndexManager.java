package com.purplehaze.output;

import com.purplehaze.Context;
import com.purplehaze.Division;
import com.purplehaze.Utils;
import com.purplehaze.input.*;
import org.jdom.*;

import java.io.IOException;

/**
 * The output manager for index page.
 */
public class IndexManager {

  protected Context context;
  protected SiteContent siteContent;

  public IndexManager(Context context, SiteContent siteContent) {
    this.context = context;
    this.siteContent = siteContent;
  }

  public static IndexManager getInstance(Context context, SiteContent siteContent) {
    return new IndexManager(context, siteContent);
  }

  public void updateIndex() throws IOException, JDOMException, ClassNotFoundException {
    Document doc = context.getFileManager().nonValidatedBuild(context.getRootIndexTemplateFile());
    Utils.setMenuFocus(doc, null);
    for (Division d : Division.values()) {
      Context cellContext = new Context(context, d);
      switch (d) {
        case PHOTO:
          updatePhotoCell(siteContent, cellContext, doc);
          break;
        case TRAVEL:
          updateTravelCell(siteContent, cellContext, doc);
          break;
        default:
          updateDivisionCell(siteContent, cellContext, doc);
          break;
      }
    }
    context.getFileManager().xmlOutput(doc, context.getRootIndexFile());
  }

  private void updateDivisionCell(SiteContent siteContent, Context context, Document doc)
      throws IOException, ClassNotFoundException {
    ArticleDataAggregator ada = siteContent.getArticleAggregator(context);
    final Division division = ada.getDivision();
    Element divE = Utils.findElement(doc, "div", division + "Update");
    divE.removeContent();
    Namespace ns = divE.getNamespace();
    ArticleDataReader lastOne = ada.getReaders().get(ada.getReaders().size() - 1);
    String articleFileName = division + "/" + Utils.getArticleFileName(lastOne.getArticleId());
    String snippetText = lastOne.getSnippet();
    if (Utils.isEmptyString(snippetText)) {
      snippetText = new ArticleContentParser(context, siteContent)
          .getFormatedPages(lastOne.getArticleId(), FormatLevel.RAW, ns).get(0).getTextTrim();
      int snippetLength = Math.min(74, snippetText.length());
      snippetText = snippetText.substring(0, snippetLength) + Translations.SUSPENSION_POINTS;
    }
    ArticleSnippet as = new ArticleSnippet(articleFileName, lastOne.getFullTitle(), snippetText, null);
    as.writeLeftAligned(divE, ns);
    writeMoreLink(doc, division, ada.size());
  }

  protected void writeMoreLink(Document doc, Division division, int size) {
    Element divE = Utils.findElement(doc, "div", division + "Count");
    divE.getChild("a", divE.getNamespace()).setText(Translations.MORE + division.getChinese() + "(" + size + ")");
  }


  private void updateTravelCell(SiteContent siteContent, Context context, Document doc) throws IOException, JDOMException {
    Element divE = Utils.findElement(doc, "div", "travelUpdate");
    Namespace ns = divE.getNamespace();
    divE.removeContent();
    ArticleDataAggregator ada = siteContent.getArticleAggregator(context);
    writeOneTravelSnippet(divE, ada, context, ada.getReaders().size() - 1);
    divE.addContent(new Element("div", ns).setAttribute("class", "verticalspace").addContent(new EntityRef("nbsp")));
    writeOneTravelSnippet(divE, ada, context, ada.getReaders().size() - 2);
    writeMoreLink(doc, context.getDivision(), ada.size());
  }

  private void writeOneTravelSnippet(Element divE, ArticleDataAggregator ada, Context context, int articleId) {
    Namespace ns = divE.getNamespace();
    ArticleDataReader lastOne = ada.getReaders().get(articleId);
    String articleFileName = "travel/" + Utils.getArticleFileName(lastOne.getArticleId());
    String img = lastOne.getImgSnippetPath(context.getDivisionPath());
    // TODO: the logic is hacky
    if (img.startsWith("../")) {
      img = img.substring(3);
    } else {
      img = context.getDivision() + "/" + img;
    }
    ArticleSnippet as = new ArticleSnippet(articleFileName, lastOne.getFullTitle(), lastOne.getSnippet(), img);
    as.writeLeftAligned(divE, ns);
  }


  private void updatePhotoCell(SiteContent siteContent, Context context, Document doc) throws IOException, JDOMException {
    Element divE = Utils.findElement(doc, "div", "photoUpdate");
    Namespace ns = divE.getNamespace();
    divE.removeContent();
    int i = 0;
    PhotoIndexAggregator pia = siteContent.getPhotoAggregator();
    for (; i > -3; i--) {
      writeOneAlbumSnippet(divE, ns, pia, pia.size() + i);
    }
    final int currentAlbumId = pia.getCurrentWorkingAlbumId();
    if (currentAlbumId == 0 // update article not photo
        || (currentAlbumId <= pia.size() && currentAlbumId >= pia.size() - 3)) // update one of recent 4 albums
    {
      writeOneAlbumSnippet(divE, ns, pia, pia.size() + i);
    } else {
      writeOneAlbumSnippet(divE, ns, pia, currentAlbumId);
    }
    writeMoreLink(doc, Division.PHOTO, pia.size());
  }

  private void writeOneAlbumSnippet(Element divE, Namespace ns, PhotoIndexAggregator pia, int albumId) {
    PhotoIndexReader lastOne = pia.getReader(albumId);
    String title = lastOne.getTitle() + "(" + lastOne.getTags().size() + ")";
    String snippetImg = "photo/" + lastOne.getAlbumId() + "/snippet.jpg";
    String link = "photo/" + lastOne.getAlbumId() + "/index.html";

    Element snippetDiv = new Element("div", ns).setAttribute("class", "snippetunit");
    snippetDiv.addContent(new Element("img", ns)
        .setAttribute("width", "90")
        .setAttribute("height", "60")
        .setAttribute("alt", title)
        .setAttribute("title", title)
        .setAttribute("src", snippetImg));
    snippetDiv.addContent(new Element("div", ns)
        .setAttribute("class", "caption")
        .addContent(new Element("a", ns)
            .setText(title)
            .setAttribute("href", link))
        .addContent(new Element("br", ns)));
    divE.addContent(snippetDiv);
  }

}
