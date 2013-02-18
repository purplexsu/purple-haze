package com.purplehaze.output;

import org.jdom.Element;
import org.jdom.Namespace;


/**
 * The article snippet to display in column & division files.
 */
class ArticleSnippet {

  private String title;
  private String snippetImg;
  private String snippetText;
  private String articleFileName;


  ArticleSnippet(String articleFileName, String title, String snippetText, String snippetImg) {
    this.articleFileName = articleFileName;
    this.title = title;
    this.snippetImg = snippetImg;
    this.snippetText = snippetText;
  }


  String getSnippetText() {
    return snippetText;
  }

  String getSnippetImg() {
    return snippetImg;
  }

  String getTitle() {
    return title;
  }

  String getArticleFileName() {
    return articleFileName;
  }

  Element writeCenterAligned(Namespace ns) {
    Element divE = new Element("div", ns).setAttribute("class", "columncell_c snippettext");
    divE.addContent(writeSnippetImg(ns))
        .addContent(new Element("div", ns)
            .setAttribute("class", "snippetheader")
            .setText(this.getTitle()))
        .addContent(writeSnippetText(ns));
    return divE;
  }

  void writeLeftAligned(Element parent, Namespace ns) {
    Element divE1 = new Element("div", ns).setAttribute("class", "snippetheader");
    if (snippetImg != null) {
      divE1.addContent(writeSnippetImg(ns).setAttribute("class", "leftsnippetimg"));
    }
    divE1.addContent(title);

    Element divE2 = new Element("div", ns).setAttribute("class", "snippettext");
    divE2.addContent(writeSnippetText(ns));
    parent.addContent(divE1).addContent(divE2);
  }

  private Element writeSnippetImg(Namespace ns) {
    Element imgE = new Element("img", ns);
    imgE.setAttribute("src", this.getSnippetImg());
    imgE.setAttribute("width", "90");
    imgE.setAttribute("height", "60");
    imgE.setAttribute("alt", title);
    imgE.setAttribute("title", title);
    return imgE;
  }

  private Element writeSnippetText(Namespace ns) {
    Element aE = new Element("a", ns);
    aE.setAttribute("href", this.getArticleFileName());
    aE.setText(this.getSnippetText());
    return aE;
  }
}
