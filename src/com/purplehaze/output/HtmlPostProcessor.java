package com.purplehaze.output;

import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;

/**
 * Process the generated HTML document.
 */
public abstract class HtmlPostProcessor {

  public void process(Document document) {
    final Element root = document.getRootElement();
    loop(root);
  }

  private void loop(Content content) {
    process(content);
    if (content instanceof Element) {
      for (Object o : ((Element) content).getContent()) {
        loop((Content) o);
      }
    }
  }

  protected abstract void process(Content content);
}
