package com.purplehaze.output;

import com.purplehaze.Context;
import com.purplehaze.Division;
import com.purplehaze.Utils;
import com.purplehaze.input.ArticleDataAggregator;
import com.purplehaze.input.ArticleDataReader;
import com.purplehaze.input.SiteContent;
import org.jdom.CDATA;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.XMLOutputter;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Generate ATOM feed.
 */
public class FeedGenerator {

  private static final int MAX_ARTICLE = 30;
  private static final SimpleDateFormat SDF_IN_DATA = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  private static final SimpleDateFormat SDF_IN_FEED = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+08:00");
  private static final String SITE_URL = "http://www.purplexsu.net/";
  private static final String SITE_TITLE = "Purplexsu's Space";
  private final SiteContent siteContent;
  private static final Pattern ATOM_URL_ADJUST_PATTERN = Pattern.compile("href=\"\\.{2}/{1,2}");
  private final Context context;

  public FeedGenerator(Context context, SiteContent siteContent) {
    this.context = context;
    this.siteContent = siteContent;
  }

  public void feeds() throws IOException, ParseException, ClassNotFoundException {
    Document doc = initFeed();
    Map<ArticleDataAggregator, Integer> adas = new HashMap<ArticleDataAggregator, Integer>();
    for (Division d : Division.values()) {
      if (d != Division.PHOTO) {
        ArticleDataAggregator ada = siteContent.getArticleAggregator(d);
        adas.put(ada, ada.size());
      }
    }
    Set<Division> handled = new HashSet<Division>();
    System.out.println();
    System.out.print("Writing ATOM:");
    int articleCount = 0;
    while (handled.size() < Division.values().length - 1) {
      ArticleDataAggregator latest = null;
      for (ArticleDataAggregator ada : adas.keySet()) {
        if (handled.contains(ada.getDivision())) {
          continue;
        }
        if (later(adas, ada, latest)) {
          latest = ada;
        }
      }
      Integer index = adas.get(latest);
      latest.setCurrentWorkingArticleId(index);
      appendToFeed(doc, latest);
      if (++articleCount > MAX_ARTICLE) {
        break;
      }
      index--;
      if (index == 0) {
        handled.add(latest.getDivision());
      } else {
        adas.put(latest, index);
      }
    }
    context.getFileManager().xmlOutput(doc, new File(context.getHazePath(), "atom.xml"));
  }

  private void appendToFeed(Document doc, ArticleDataAggregator ada) throws IOException, ParseException, ClassNotFoundException {
    System.out.print("#");
    ArticleDataReader reader = ada.getCurrentWorkingReader();
    Namespace ns = doc.getRootElement().getNamespace();
    String[] summaries = null;
    XMLOutputter xmlOutputter = new XMLOutputter();
    final String articleId = reader.getArticleId();
    final String divisionUrl = SITE_URL + ada.getDivision() + "/";
    if (Utils.isEmptyString(reader.getRawContent())) {
      summaries = new String[]{reader.getSnippet()};
    } else {
      final Context context = new Context(this.context, ada.getDivision());
      List<Element> rawPages = new ArticleContentParser(context, siteContent)
          .getFormatedPages(articleId, FormatLevel.SNIPPET, null);
      summaries = new String[rawPages.size()];
      for (int i = 0; i < summaries.length; i++) {
        StringWriter sw = new StringWriter();
        if (i == 0) {
          File divisionPath = new File(context.getHazePath(), ada.getDivision().toString());
          File coverPicture = new File(divisionPath, Utils.getCoverPictureFileName(articleId));
          if (coverPicture.exists() && coverPicture.canRead()) {
            ImageIcon img = new ImageIcon(coverPicture.getCanonicalPath());
            sw.write("<a target=\"_blank\" href=\"");
            sw.write(divisionUrl + Utils.getArticleFileName(articleId, 1));
            sw.write("\"><img src=\"");
            sw.write(divisionUrl);
            sw.write(coverPicture.getName());
            sw.write("\" height=\"");
            sw.write(String.valueOf(img.getIconHeight()));
            sw.write("\" width=\"");
            sw.write(String.valueOf(img.getIconWidth()));
            sw.write("\" /></a><br />");
          }
        }
        xmlOutputter.outputElementContent(rawPages.get(i), sw);
        summaries[i] = sw.toString().replaceAll("xmlns=\".*?\"", "");
      }
    }
    String updateTime = reader.getTime();
    Date updateDate = SDF_IN_DATA.parse(updateTime);
    String updateTimeString = SDF_IN_FEED.format(updateDate);
    String publishTimeString = SDF_IN_FEED.format(new Date(updateDate.getTime() - 1000));
    final String commentUrl = divisionUrl + Utils.getCommentFileName(articleId);
    for (int i = 0; i < summaries.length; i++) {
      String articleUrl = divisionUrl + Utils.getArticleFileName(articleId, i + 1);
      StringBuilder sb = new StringBuilder(summaries[i]);
      addLineBreak(sb);
      if (summaries.length > 1) {
        if (i > 0) {
          addLink(sb, Translations.PREVIOIUS_PAGE, divisionUrl + Utils.getArticleFileName(articleId, i));
          addWhiteSpace(sb);
        }
        if (i < summaries.length - 1) {
          addLink(sb, Translations.NEXT_PAGE, divisionUrl + Utils.getArticleFileName(articleId, i + 2));
          addWhiteSpace(sb);
        }
      }
      addLink(sb, Translations.WRITE_COMMENT, commentUrl);

      Element entryE = new Element("entry", ns);
      String fullTitle = reader.getFullTitle();
      if (summaries.length > 1) {
        fullTitle = fullTitle + " - " + (i + 1);
      }
      entryE.addContent(new Element("title", ns).setText(fullTitle));
      entryE.addContent(new Element("updated", ns).setText(updateTimeString));
      entryE.addContent(new Element("published", ns).setText(publishTimeString));
      entryE.addContent(new Element("id", ns).setText(articleUrl));
      entryE.addContent(new Element("link", ns)
          .setAttribute("rel", "alternate")
          .setAttribute("type", "text/html")
          .setAttribute("href", articleUrl));
      String summary = ATOM_URL_ADJUST_PATTERN.matcher(sb).replaceAll("href=\"" + SITE_URL);
      entryE.addContent(new Element("content", ns).setAttribute("type", "html").addContent(new CDATA(summary)));
      doc.getRootElement().addContent(entryE);
    }
  }

  private void addWhiteSpace(StringBuilder sb) {
    sb.append("&nbsp;&nbsp;");
  }

  private void addLineBreak(StringBuilder sb) {
    sb.append("<br />");
  }

  private void addLink(StringBuilder source, String text, String url) {
    source.append("<a target=\"_blank\" href=\"")
        .append(url)
        .append("\">")
        .append(text)
        .append("</a>");
  }

  private Document initFeed() {
    Namespace ns = Namespace.getNamespace("http://www.w3.org/2005/Atom");
    Element root = new Element("feed", ns);
    Date date = new Date();
    root.addContent(new Element("id", ns).setText(SITE_URL))
        .addContent(new Element("title", ns).setText(SITE_TITLE))
        .addContent(new Element("link", ns).setAttribute("href", SITE_URL + "atom.xml").setAttribute("rel", "self"))
        .addContent(new Element("link", ns).setAttribute("href", SITE_URL).setAttribute("rel", "alternate"))
        .addContent(new Element("subtitle", ns).setAttribute("type", "html").setText(Translations.MY_BLOG_PHOTO_AND_MUSIC))
        .addContent(new Element("rights", ns).setText("Copyright " +
            Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai")).get(Calendar.YEAR) +
            ", Purplexsu"))
        .addContent(new Element("updated", ns).setText(SDF_IN_FEED.format(date)))
        .addContent(new Element("author", ns)
            .addContent(new Element("name", ns).setText("Purplexsu"))
            .addContent(new Element("uri", ns).setText(SITE_URL))
            .addContent(new Element("email", ns).setText("pur@purplexsu.net")));
    return new Document(root);
  }


  private boolean later(Map<ArticleDataAggregator, Integer> adas, ArticleDataAggregator ada1, ArticleDataAggregator ada2) {
    if (ada1 == null) {
      return false;
    }
    if (ada2 == null) {
      return true;
    }
    ArticleDataReader adr1 = ada1.getReader(adas.get(ada1));
    ArticleDataReader adr2 = ada2.getReader(adas.get(ada2));
    try {
      String time1 = adr1.getTime();
      Date date1 = SDF_IN_DATA.parse(time1);
      String time2 = adr2.getTime();
      Date date2 = SDF_IN_DATA.parse(time2);
      return date1.getTime() >= date2.getTime();
    } catch (ParseException e) {
      e.printStackTrace();  //TODO: Need further handling
      return false;
    }
  }
}
