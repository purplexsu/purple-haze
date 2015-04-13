package com.purplehaze.output;

import org.jdom.Element;
import org.jdom.Namespace;

/**
 *
 */
public class PhotoSnippet {

  private final String photoDirName;
  private final String snippetText;
  private final int photoCount;

  public PhotoSnippet(String photoDirName, String snippetText, int photoCount) {
    super();
    this.photoDirName = photoDirName;
    this.snippetText = snippetText;
    this.photoCount = photoCount;
  }

  public void write(Element parent, Namespace ns) {
    Element cellDivE = new Element("div", ns).setAttribute("class", "photo_snippet");
    Element aE = new Element("a", ns)
        .setAttribute("href", photoDirName + "/index.html");
    cellDivE.addContent(aE);
    aE.addContent(new Element("img", ns)
        .setAttribute("height", "60")
        .setAttribute("width", "90")
        .setAttribute("src", photoDirName + "/snippet.jpg")
        .setAttribute("alt", snippetText));
    aE.addContent(new Element("br", ns));
    aE.addContent(snippetText);
    aE.addContent(new Element("br", ns));
    aE.addContent("(" + photoCount + ")");
    parent.addContent(cellDivE);
  }
}
