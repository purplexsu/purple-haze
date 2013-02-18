package com.purplehaze.output;

import org.jdom.Element;
import org.jdom.Namespace;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * A wrapper of article content.
 */
class ArticleContent implements Serializable {

  private ArticleIndex index;
  private List<Element> paragraph;

  public ArticleContent(Namespace ns) {
    index = new ArticleIndex(ns);
    paragraph = new LinkedList<Element>();
  }

  public ArticleIndex getIndex() {
    return index;
  }

  public List<Element> getParagraph() {
    return paragraph;
  }

  public void addElement(Element element) {
    paragraph.add(element);
  }
}
