package com.purplehaze.output;

import org.jdom.Comment;
import org.jdom.Content;
import org.jdom.Element;

import java.util.HashSet;
import java.util.Set;

/**
 * Post processor to clean up all HTML comments.
 */
public class CleanCommentProcessor extends HtmlPostProcessor {

  private static final Set<String> WHITELISTED_ELEMENT_NAMES = new HashSet<String>();

  static {
    WHITELISTED_ELEMENT_NAMES.add("script");
    WHITELISTED_ELEMENT_NAMES.add("style");
  }

  @Override
  protected void process(Content content) {
    if (content instanceof Element) {
      Element e = (Element) content;
      if (WHITELISTED_ELEMENT_NAMES.contains(e.getName())) {
        return;
      }
      Set<Comment> toBeDeleted = new HashSet<Comment>();
      for (Object o : e.getContent()) {
        if (o instanceof Comment) {
          final Comment c = (Comment) o;
          if (!c.getText().startsWith("#"))
          // prevent removing SSI directives
          {
            toBeDeleted.add(c);
          }
        }
      }
      for (Comment c : toBeDeleted) {
        c.detach();
      }
    }
  }
}
