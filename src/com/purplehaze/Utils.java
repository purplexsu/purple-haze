package com.purplehaze;

import com.purplehaze.input.Media;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;

/**
 * Util class.
 */
public class Utils {

  private static final Pattern htmlTagPattern = Pattern.compile("</?[a-zA-Z][^>]*>");
  private static final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
  public static final String DEFAULT_CHARSET = "UTF-8";
  private static XMLOutputter xmlOutputter = new XMLOutputter(Format.getCompactFormat());

  public static String formatInteger(int value, int digit) {
    NumberFormat nf = NumberFormat.getNumberInstance();
    nf.setMaximumIntegerDigits(digit);
    nf.setMinimumIntegerDigits(digit);
    nf.setGroupingUsed(false);
    return nf.format(value);
  }

  public static String commaString(String[] input) {
    StringBuilder sb = new StringBuilder();
    for (String one : input) {
      sb.append(one).append(",");
    }
    sb.deleteCharAt(sb.length() - 1);
    return sb.toString();
  }

  public static String stringInput(String prompt, String defaultValue) throws IOException {
    System.out.println(prompt);
    String line = br.readLine();
    if (!isEmptyString(line)) {
      return line;
    }
    return defaultValue;

  }

  public static boolean booleanInput(String prompt, boolean defaultValue) throws IOException {
    System.out.println(prompt);
    String line = br.readLine();
    if (isEmptyString(line)) {
      return defaultValue;
    } else {
      char input = line.charAt(0);
      switch (input) {
        case 'Y':
        case 'y':
          return true;
        case 'N':
        case 'n':
          return false;
        default:
          return defaultValue;
      }
    }
  }

  public static void verifyDir(File file) throws IOException {
    if (!file.exists() || !file.isDirectory()) {
      throw new IOException("The dir(" + file + ") is wrong!");
    }

  }

  public static boolean isEmptyString(String s) {
    return makeSafe(s).length() == 0;
  }

  private static String makeSafe(String s) {
    return (s == null) ? "" : s;
  }

  public static Element findElement(Document doc, String name, String id) {
    Element root = doc.getRootElement();
    return findElement(root, name, "id", id);
  }

  public static Element findElement(Document doc, String name, String attributeName, String attributeValue) {
    Element root = doc.getRootElement();
    return findElement(root, name, attributeName, attributeValue);
  }

  public static Element findElement(Element base, String name, String attributeName, String attributeValue) {
    if (name.equalsIgnoreCase(base.getName()) && attributeValue.equalsIgnoreCase(base.getAttributeValue(attributeName))) {
      return base;
    }
    for (Object o : base.getChildren()) {
      Element target = findElement(((Element) o), name, attributeName, attributeValue);
      if (target != null) {
        return target;
      }
    }
    return null;
  }

  public static List<Element> findAllElements(Element base, String name, String attributeName, String attributeValue) {
    List<Element> result = new LinkedList<Element>();
    findAllElements(base, name, attributeName, attributeValue, result);
    return result;
  }

  private static void findAllElements(Element base, String name, String attributeName, String attributeValue, List<Element> result) {
    if (equalsIgnoreCase(name, base.getName()) && equalsIgnoreCase(attributeValue, base.getAttributeValue(attributeName))) {
      result.add(base);
    }
    for (Object o : base.getChildren()) {
      findAllElements(((Element) o), name, attributeName, attributeValue, result);
    }
  }

  public static boolean equals(String s1, String s2) {
    return s1 == s2 || s1 != null && s2 != null && s1.equals(s2);
  }

  public static boolean equalsIgnoreCase(String s1, String s2) {
    return s1 == s2 || s1 != null && s2 != null && s1.equalsIgnoreCase(s2);
  }

  public static void fillImgTag(File photo, Element imgE, String alt) throws IOException {
    String photoName = photo.getName();
    fillImgTag(photoName, photo, imgE, alt);
  }

  public static void fillImgTag(String src, File photo, Element imgE, String alt) throws IOException {
    ImageIcon img = new ImageIcon(photo.getCanonicalPath());
    imgE.setAttribute("src", src);
    final int height = img.getIconHeight();
    final int width = img.getIconWidth();
    if (height > 0 && width > 0) {
      imgE.setAttribute("height", String.valueOf(height));
      imgE.setAttribute("width", String.valueOf(width));
    }
    if (alt != null) {
      imgE.setAttribute("alt", alt);
    }
  }

  public static Element fillVideoTag(Namespace ns, Media media, String data, String album, String srcWithoutExtension) {
    Element videoE = new Element("video", ns);
    videoE.setAttribute("id", getAnchorId(album, data))
        .setAttribute("src", srcWithoutExtension + "." + media.getType().toString().toLowerCase())
        .setAttribute("width", String.valueOf(media.getWidth()))
        .setAttribute("height", String.valueOf(media.getHeight()))
        .setAttribute("controls", "controls");
    return videoE;
  }

  public static String getChildrenTextTrim(Element parent) {
    return stripHtmlTags(xmlOutputter.outputString(parent));
  }

  public static String stripHtmlTags(String string) {
    if ((string == null) || "".equals(string)) {
      return string;
    }
    return htmlTagPattern.matcher(string).replaceAll("");
  }

  public static String getAnchorId(String text) {
    return "t" + Math.abs(text.hashCode());
  }

  public static String getAnchorId(String album, String image) {
    return "i" + album + image;
  }

  public static String getArticleFileName(int articleId) {
    return getArticleFileName(articleId, 1);
  }

  public static String getArticleFileName(String articleId) {
    return getArticleFileName(articleId, 1);
  }

  public static String getArticleFileName(int articleId, int pageIndex) {
    return getArticleFileName(formatInteger(articleId, 3), formatInteger(pageIndex, 2));
  }


  public static String getArticleFileName(String articleId, int pageIndex) {
    return getArticleFileName(articleId, formatInteger(pageIndex, 2));
  }

  public static String getArticleFileName(String articleId, String pageIndex) {
    return "article-" + articleId + "-" + pageIndex + ".html";
  }

  public static String getCommentFileName(int articleId) {
    return getCommentFileName(formatInteger(articleId, 3));
  }

  public static String getCommentFileName(String articleId) {
    return "comment-" + articleId + ".html";
  }

  public static String getColumnDataFileName(String columnId) {
    return "column-" + columnId + ".txt";
  }

  public static String getColumnFileName(String columnId) {
    return "column-" + columnId + "-01.html";
  }

  public static String getSnippetFileName(String articleId) {
    return "snippet-" + articleId + ".jpg";
  }

  public static String getCoverPictureFileName(String articleId) {
    return "cover-" + articleId + ".jpg";
  }

  public static List<Integer> getPageNav(int totalPages, int currentPage) {
    final int pagesNearCurrentPage = 2;
    List<Integer> nav = new ArrayList<Integer>();
    nav.add(1);
    if (currentPage - pagesNearCurrentPage - 1 > 1) {
      // need ... between 1 & current page
      nav.add(0);
      for (int i = pagesNearCurrentPage; i >= 1; i--) {
        nav.add(currentPage - i);
      }
    } else {
      // need not ... between 1 & current page
      for (int i = pagesNearCurrentPage; i >= 1; i--) {
        if (currentPage - i > 1) {
          nav.add(currentPage - i);
        }
      }
    }
    if (currentPage != 1 && currentPage != totalPages) {
      nav.add(currentPage);
    }
    if (currentPage + pagesNearCurrentPage + 1 < totalPages) {
      // need ... between current page & last page
      for (int i = 1; i <= pagesNearCurrentPage; i++) {
        nav.add(currentPage + i);
      }
      nav.add(0);
    } else {
      // need not ... between current page & last page
      for (int i = 1; i <= pagesNearCurrentPage; i++) {
        if (currentPage + i < totalPages) {
          nav.add(currentPage + i);
        }
      }
    }
    nav.add(totalPages);
    return nav;
  }

  public static String getTitleText(String divisionText, String customizedText, int pageIndex, int totalPageCount) {
    StringBuilder sb = new StringBuilder();
    sb.append(divisionText).append(" - ").append(customizedText);
    if (totalPageCount > 1) {
      sb.append(" - ").append(pageIndex + 1);
    }
    sb.append(" (Purplexsu's Space)");
    return sb.toString();
  }

  public static void setMenuFocus(Document doc, Division division) {
    final String id;
    if (division != null) {
      id = "menu_" + division;
    } else {
      id = "menu_index";
    }
    final Element element = findElement(doc, "a", id);
    final String className = element.getAttributeValue("class");
    element.setAttribute("class", className + " selected");
  }
}
