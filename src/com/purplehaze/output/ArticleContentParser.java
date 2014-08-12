package com.purplehaze.output;

import com.purplehaze.Context;
import com.purplehaze.Division;
import com.purplehaze.Utils;
import com.purplehaze.input.*;

import org.jdom.Content;
import org.jdom.Element;
import org.jdom.EntityRef;
import org.jdom.Namespace;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse article content to HTML fragment.
 */
class ArticleContentParser {

  private static final Pattern PAGE_PATTERN = Pattern.compile("(\\{!\\S+?!\\})");
  private static final Pattern PARAGRAPH_PATTERN_1 = Pattern
      .compile("(\\n{2,}|(\\{@\\S+\\{.+?\\}@\\}))");
  private static final Pattern PARAGRAPH_PATTERN_2 = Pattern.compile("\\{@(\\S+)\\{(.+)\\}@\\}");
  private static final Pattern PARAGRAPH_PATTERN_3 = Pattern.compile("\\n{2,}");
  private static final Pattern SENTENCE_PATTERN_1 = Pattern.compile("(\\n|(\\{#.+?#\\}))");
  private static final Pattern SENTENCE_PATTERN_2 = Pattern.compile("\\{#(\\S+)\\{(.+)\\}#\\}");
  private static final Pattern SENTENCE_PATTERN_3 = Pattern.compile("(\\s{2,})");
  private static final Set<String> INDENT_TAGS = new HashSet<String>() {
    {
      add("p");
      add("br");
    }
  };
  private final ArticleDataAggregator ada;
  private final PhotoIndexAggregator pia;
  private final Context context;
  private final ArticleContentCache cache;

  public ArticleContentParser(Context context, SiteContent siteContent) {
    this(context, siteContent, ArticleContentCache.getInstance());
  }

  ArticleContentParser(Context context, SiteContent siteContent, ArticleContentCache cache) {
    this.context = context;
    this.cache = cache;
    this.ada = siteContent.getArticleAggregator(context);
    this.pia = siteContent.getPhotoAggregator();
  }

  @SuppressWarnings("unchecked")
  public List<Element> getFormattedPages(String articleId, FormatLevel formatLevel, Namespace ns)
      throws IOException, ClassNotFoundException {
    final ArticleMeta meta = new ArticleMeta(articleId, formatLevel, ada.getDivision());
    if (!cache.cacheHit(meta, ada.getReader(articleId).lastModified())) {
      ArticleContent content = generateArticleContent(articleId, formatLevel, ns, meta);
      return content.getParagraph();
    } else {
      ArticleContent content = null;
      try {
        content = (ArticleContent) cache.get(meta);
      } catch (Exception e) {
        System.err
            .println(e.getMessage() + " However, this error is handled and cache is abandoned.");
      }
      if (content == null) {
        content = generateArticleContent(articleId, formatLevel, ns, meta);
      }
      return content.getParagraph();
    }
  }

  private ArticleContent generateArticleContent(
      String articleId, FormatLevel formatLevel, Namespace ns, ArticleMeta meta)
      throws IOException {
    int workingArticleIdBackup = ada.getCurrentWorkingArticleId();
    ada.setCurrentWorkingArticleId(Integer.parseInt(articleId));
    ArticleContent content = handlePages(articleId, formatLevel, ns);
    ada.setCurrentWorkingArticleId(workingArticleIdBackup);
    cache.update(meta, content);
    return content;
  }

  public List<Element> getPageSubtitles(String articleId, Namespace ns)
      throws IOException, ClassNotFoundException {
    final ArticleMeta meta = new ArticleMeta(articleId, FormatLevel.FULL, ada.getDivision());
    if (!cache.cacheHit(meta, ada.getReader(articleId).lastModified())) {
      getFormattedPages(articleId, FormatLevel.FULL, ns);
    }
    ArticleIndex ai = ((ArticleContent) cache.get(meta)).getIndex();
    return ai.getSubtitles();
  }

  private ArticleContent handlePages(String articleId, FormatLevel formatLevel, Namespace ns)
      throws IOException {
    ArticleContent articleContent = new ArticleContent(ns);
    ArticleDataReader adr = ada.getReader(articleId);
    String content = adr.getRawContent();
    //{!page!} currently
    Matcher m = PAGE_PATTERN.matcher(content);
    int start = 0;
    int index = 0;
    while (m.find()) {
      handleOnePage(articleContent, content.substring(start, m.start()), index++, formatLevel, ns);
      start = m.end();
    }
    handleOnePage(articleContent, content.substring(start), index, formatLevel, ns);
    if (formatLevel == FormatLevel.FULL) {
      ArticleIndex ai = articleContent.getIndex();
      if (ai.getItemCount() == 0 || adr.disableArticleIndex()) {
        ai.detach();
      }
    }
    return articleContent;
  }

  private void handleOnePage(ArticleContent content, String pageRawContent, int pageIndex,
      FormatLevel formatLevel, Namespace ns) throws IOException {
    Element pageE = new Element("html", ns);
    content.addElement(pageE);
    ArticleIndex articleIndex = content.getIndex();
    ArticleDataReader adr = ada.getCurrentWorkingReader();
    if (pageIndex == 0 && formatLevel == FormatLevel.FULL) {
      //title in the first page
      pageE.addContent(new Element("h1", ns).setText(adr.getTitle()));
      if (adr.getSubtitle() != null) {
        pageE.addContent(new Element("h2", ns).setText("--" + adr.getSubtitle()));
      }
      pageE.addContent(new Element("p", ns).setAttribute("class", "timestamp")
          .setText("(" + adr.getDisplayTime() + ")"));
      articleIndex.attach(pageE);
    }
    Matcher m = PARAGRAPH_PATTERN_1.matcher(pageRawContent);
    int start = 0;
    while (m.find()) {
      handleOneParagraph(pageE, pageRawContent.substring(start, m.start()), formatLevel, ns);
      handleParagraphTag(articleIndex, pageE, m.group(1), pageIndex, formatLevel, ns);
      start = m.end();
    }
    handleOneParagraph(pageE, pageRawContent.substring(start), formatLevel, ns);
  }

  private void handleParagraphTag(ArticleIndex articleIndex, Element pageE, String tag,
      int pageIndex, FormatLevel formatLevel, Namespace ns) throws IOException {
    if (!PARAGRAPH_PATTERN_3.matcher(tag).matches()) {
      Matcher m = PARAGRAPH_PATTERN_2.matcher(tag);
      if (!m.find()) {
        throw new IllegalArgumentException("Wrong data format for tag:" + tag);
      }
      String type = m.group(1);
      String data = m.group(2);
      if ("t2".equalsIgnoreCase(type) || "t3".equalsIgnoreCase(type)) {
        String fullTitle, shortTitle;
        if (data.contains("|")) {
          String[] titles = data.split("\\|", 2);
          fullTitle = titles[0];
          shortTitle = titles[1];
        } else {
          fullTitle = data;
          shortTitle = data;
        }
        if ("t2".equalsIgnoreCase(type)) {
          //{@t2{����(2003.4.21)}@}
          switch (formatLevel) {
            case FULL: {
              pageE.addContent(new Element("h3", ns)
                  .setAttribute("id", Utils.getAnchorId(fullTitle))
                  .setText(fullTitle));
              articleIndex.addItem(ada.getCurrentWorkingArticleId(), fullTitle, shortTitle, type,
                  pageIndex);
              break;
            }
            case SNIPPET: {
              pageE.addContent(fullTitle).addContent(new Element("br", ns));
              break;
            }
            case RAW: {
              pageE.addContent("(" + fullTitle + ")");
              break;
            }
          }
        } else if ("t3".equalsIgnoreCase(type)) {
          //{@t3{����(2003.4.21)}@}
          switch (formatLevel) {
            case FULL: {
              pageE.addContent(new Element("h4", ns)
                  .setAttribute("id", Utils.getAnchorId(fullTitle))
                  .setText(fullTitle));
              articleIndex.addItem(ada.getCurrentWorkingArticleId(), fullTitle, shortTitle, type,
                  pageIndex);
              break;
            }
            case SNIPPET: {
              pageE.addContent(fullTitle).addContent(new Element("br", ns));
              break;
            }
            case RAW: {
              pageE.addContent("(" + fullTitle + ")");
              break;
            }
          }
        }
      } else if ("img".equalsIgnoreCase(type)) {
        String alt = null;
        String src;
        String link = null;
        Element pE = new Element("p", ns).setAttribute("class", "article_photo");
        Element imgE = new Element("img", ns);
        if (data.startsWith("http")) {
          //{@img{http://www.google.cn/pic.jpg}@}
          src = data;
          if (formatLevel == FormatLevel.FULL) {
            pE.addContent(imgE);
          }
        } else if (data.startsWith("/")) {
          //{@img{/2pigs.gif}@}
          src = "../images" + data;
          if (formatLevel == FormatLevel.FULL) {
            File imgFile = new File(context.getDivisionPath(), src);
            Utils.fillImgTag(imgFile, imgE, null);
            pE.addContent(imgE);
          }
        } else {
          //{@img{01}@} or {@img{001-01}@}
          String album = ada.getCurrentWorkingReader().getPhoto();
          if (album == null || data.contains("-")) {
            album = data.split("-")[0];
            data = data.split("-")[1];
          }
          link = "../" + Division.PHOTO + "/" + album + "/" + data + ".html";
          src = "../" + Division.PHOTO + "/" + album + "/" + data + ".jpg";
          PhotoIndexReader pir = pia.getReader(album);
          alt = pir.getTags().get(Integer.parseInt(data) - 1).replace('/', ' ');

          if (formatLevel == FormatLevel.FULL) {
            File imgFile = new File(context.getDivisionPath(), src);
            Utils.fillImgTag(imgFile, imgE, alt);
            imgE.setAttribute("id", Utils.getAnchorId(album, data));
            Element aE = new Element("a", ns)
                .setAttribute("href", link)
                .setAttribute("rel", "external");
            pE.addContent(aE.addContent(imgE));
          }
        }
        switch (formatLevel) {
          case FULL: {
            imgE.setAttribute("src", src);
            pageE.addContent(pE);
            break;
          }
          case SNIPPET: {
            if (alt == null) {
              pageE.addContent("<" + Division.PHOTO.getChinese() + ">")
                  .addContent(new Element("br", ns));
            } else {
              pageE.addContent("<")
                  .addContent(new Element("a", ns)
                      .setAttribute("href", link)
                      .setText(alt))
                  .addContent(">")
                  .addContent(new Element("br", ns));
            }
            break;
          }
        }
      } else if ("youku".equalsIgnoreCase(type)) {
        //{@youku{kadfh}@}
        String id = data;
        String width = "480";
        String height = "400";
        if (data.contains("|")) {
          String[] parts = data.split("\\|", 3);
          id = parts[0];
          width = parts[1];
          height = parts[2];
        }
        switch (formatLevel) {
          case FULL: {
            pageE.addContent(new Element("p", ns).setAttribute("class", "article_photo")
                .addContent(new Element("embed", ns)
                        .setAttribute("src",
                            "http://player.youku.com/player.php/sid/" + id + "/v.swf")
                        .setAttribute("allowFullScreen", "true")
                        .setAttribute("quality", "high")
                        .setAttribute("width", width)
                        .setAttribute("height", height)
                        .setAttribute("align", "middle")
                        .setAttribute("allowScriptAccess", "always")
                        .setAttribute("type", "application/x-shockwave-flash")
                ));
            break;
          }
          case SNIPPET: {
            final String link = "http://v.youku.com/v_show/id_" + id + ".html";
            pageE.addContent("<")
                .addContent(new Element("a", ns)
                    .setAttribute("href", link)
                    .setText(Translations.VIDEO))
                .addContent(">")
                .addContent(new Element("br", ns));
            break;
          }
        }
      }
    }
  }

  private void handleOneParagraph(Element pageE, String paragraph, FormatLevel formatLevel,
      Namespace ns) {
    paragraph = paragraph.trim();
    if (Utils.isEmptyString(paragraph)) {
      return;
    }
    Element pE = new Element("p", ns);
    Matcher m = SENTENCE_PATTERN_1.matcher(paragraph);
    int start = 0;
    while (m.find()) {
      handleOneSentence(pE, paragraph.substring(start, m.start()), formatLevel);
      handleSentenceTag(pE, m.group(1), formatLevel, ns);
      start = m.end();
    }
    handleOneSentence(pE, paragraph.substring(start), formatLevel);
    if (pE.getChildren().size() > 0 || !Utils.isEmptyString(pE.getTextTrim())) {
      switch (formatLevel) {
        case FULL: {
          pageE.addContent(pE);
          break;
        }
        case SNIPPET: {
          pageE.addContent(pE.removeContent()).addContent(new Element("br", ns));
          break;
        }
        case RAW: {
          pageE.addContent(pE.getTextTrim());
          break;
        }
      }
    }
  }

  private void handleSentenceTag(Element pE, String tag, FormatLevel formatLevel, Namespace ns) {
    if ("\n".equalsIgnoreCase(tag)) {
      if (formatLevel != FormatLevel.RAW) {
        pE.addContent(new Element("br", ns));
      }
      return;
    }
    Matcher m = SENTENCE_PATTERN_2.matcher(tag);
    if (!m.find()) {
      throw new IllegalArgumentException("Wrong data format for tag:" + tag);
    }
    String type = m.group(1);
    String data = m.group(2);
    if ("a".equalsIgnoreCase(type)) {
      String link, text;
      if (data.contains("|")) {
        //{#a{http://www.google.cn|Ѭ��ζ}#}
        link = data.split("\\|")[0];
        text = data.split("\\|")[1];
      } else {
        //{#a{http://www.google.cn}#}
        link = data;
        text = data;
      }
      switch (formatLevel) {
        case FULL:
        case SNIPPET: {
          boolean external = link.startsWith("http");
          Element aE = new Element("a", ns);
          pE.addContent(aE);
          if (external) {
            aE.setAttribute("rel", "external").setAttribute("href", link).setText(text);
          } else {
            aE.setAttribute("href", "../" + link).setText(text);
          }
          break;
        }
        case RAW: {
          pE.addContent(text);
          break;
        }
      }
      return;
    }
    if ("b".equalsIgnoreCase(type)) {
      switch (formatLevel) {
        case FULL:
        case SNIPPET: {
          pE.addContent(new Element("b", ns).setText(data));
          break;
        }
        case RAW: {
          pE.addContent(data);
          break;
        }
      }
      return;
    }
    if ("c".equalsIgnoreCase(type)) {
      switch (formatLevel) {
        case FULL: {
          pE.addContent(new Element("span", ns).setAttribute("class", "capital").setText(data));
          break;
        }
        case SNIPPET:
        case RAW: {
          pE.addContent(data);
          break;
        }
      }
    }
  }

  private void handleOneSentence(Element pE, String sentence, FormatLevel formatLevel) {
    boolean indent = false;
    if (pE.getContent() == null || pE.getContent().size() == 0) {
      indent = true;
    } else {
      List content = pE.getContent();
      Content c = (Content) content.get(content.size() - 1);
      if (c instanceof Element) {
        Element e = (Element) c;
        if (INDENT_TAGS.contains(e.getName())) {
          indent = true;
        }
      }
    }
    if (indent && formatLevel != FormatLevel.RAW) {
      pE.addContent(Translations.CHINESE_INDENT);
    }
    Matcher m = SENTENCE_PATTERN_3.matcher(sentence);

    int start = 0;
    while (m.find()) {
      // replace white space with &nbsp;
      pE.addContent(sentence.substring(start, m.start()));
      if (formatLevel != FormatLevel.RAW) {
        for (int i = 0; i < m.group(1).length(); i++) {
          pE.addContent(new EntityRef("nbsp"));
        }
      }
      start = m.end();
    }
    pE.addContent(sentence.substring(start));
  }

  private class ArticleMeta implements Serializable {

    private String articleId;
    private FormatLevel formatLevel;
    private Division division;

    public ArticleMeta(String articleId, FormatLevel formatLevel, Division division) {
      this.articleId = articleId;
      this.formatLevel = formatLevel;
      this.division = division;
    }

    @Override
    public int hashCode() {
      return articleId.hashCode() * formatLevel.hashCode() * division.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof ArticleMeta) {
        ArticleMeta k = (ArticleMeta) obj;
        return k.articleId.equals(articleId) && k.formatLevel == formatLevel
            && k.division == division;
      }
      return false;
    }

    @Override
    public String toString() {
      return new StringBuilder()
          .append(division)
          .append("/")
          .append(articleId)
          .append("_")
          .append(formatLevel)
          .append(".cache")
          .toString();
    }
  }

}
