package com.purplehaze.output;

import com.purplehaze.Utils;
import org.jdom.Element;
import org.jdom.Namespace;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Constructs article index from article paragraph titles.
 */
class ArticleIndex implements Serializable {

  private static final String T2 = "t2";
  private static final String T3 = "t3";

  private Element articleIndexElement;
  private int itemCount;
  private String currentType;
  private Stack<Element> ulElements;
  private List<Element> subtitles;

  ArticleIndex(Namespace ns) {
    ulElements = new Stack<Element>();
    subtitles = new ArrayList<Element>();
    Element currentUlElement = new Element("ul", ns);
    ulElements.push(currentUlElement);
    articleIndexElement = new Element("fieldset", ns)
        .setAttribute("id", "ArticleIndex")
        .addContent(currentUlElement);
    itemCount = 0;
    currentType = T2;
  }

  void attach(Element element) {
    element.addContent(articleIndexElement);
  }

  void detach() {
    articleIndexElement.detach();
  }

  int getItemCount() {
    return itemCount;
  }

  void addItem(int articleId, String fullTitle, String shortTitle, String type, int pageIndex) {
    Namespace ns = this.articleIndexElement.getNamespace();
    if (!Utils.equals(currentType, type)) {
      if (Utils.equals(type, T3) && Utils.equals(currentType, T2)) {
        // currentType is T2 and added type is T3, push a new <ul> into stack
        Element ulE = new Element("ul", ns);
        ulElements.peek().addContent(ulE);
        ulElements.push(ulE);
      } else {
        // currentType is T3 and added type is T2, pop a <ul> out
        ulElements.pop();
      }
      currentType = type;
    }
    Element aE = new Element("a", ns).setText(shortTitle).setAttribute(
        "href", Utils.getArticleFileName(articleId, pageIndex + 1) + "#" + Utils.getAnchorId(fullTitle));
    ulElements.peek().addContent(new Element("li", ns).addContent(aE));
    subtitles.add(aE);
    itemCount++;
  }

  List<Element> getSubtitles() {
    List<Element> result = new ArrayList<Element>(subtitles.size());
    for (Element subtitle : subtitles) {
      result.add((Element) subtitle.clone());
    }
    return result;
  }
}
