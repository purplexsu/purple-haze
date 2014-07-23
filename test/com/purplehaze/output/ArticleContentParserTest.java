package com.purplehaze.output;

import static com.purplehaze.output.Translations.CHINESE_INDENT;

import com.purplehaze.Context;
import com.purplehaze.Division;
import com.purplehaze.TestUtil;
import com.purplehaze.input.SiteContent;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Date;
import java.util.List;

/**
 * Unittest for ArticleContentParser
 */
public class ArticleContentParserTest extends TestCase {

  private ArticleContentParser acp;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    final FileManager fm = new FileManager(
        new File(TestUtil.getTestFile("com/purplehaze/input/testdata")),
        null);
    final Context context = new Context(fm, Division.BLOG);
    SiteContent siteContent = new SiteContent(context);
    siteContent.loadFromDisk();
    IMocksControl control = EasyMock.createNiceControl();
    ArticleContentCache cache = control.createMock(ArticleContentCache.class);
    EasyMock.expect(cache.cacheHit(EasyMock.<Serializable>anyObject(), EasyMock.<Date>anyObject()))
        .andStubReturn(false);
    acp = new ArticleContentParser(context, siteContent, cache);
    control.replay();
  }

  public void testGetFormattedPages_paragraphTags_full()
      throws IOException, ClassNotFoundException {
    List<Element> pages = acp.getFormattedPages("001", FormatLevel.FULL, null);
    XMLOutputter output = new XMLOutputter(Format.getCompactFormat());
    StringWriter sw = new StringWriter();
    assertEquals(1, pages.size());
    output.output(pages.get(0), sw);
    String expected = "<html>"
        + "<h1>正文标题</h1>"
        + "<p class=\"timestamp\">(2006-1-10 15:52:56)</p>"
        + "<fieldset id=\"ArticleIndex\">"
        + "<ul>"
        + "<li><a href=\"article-001-01.html#t831523682\">标题一1</a></li>"
        + "<ul>"
        + "<li><a href=\"article-001-01.html#t831528023\">标题二2</a></li>"
        + "<li><a href=\"article-001-01.html#t831528023\">标题二2</a></li>"
        + "</ul>"
        + "<li><a href=\"article-001-01.html#t831523683\">标题一2</a></li>"
        + "</ul>"
        + "</fieldset>"
        + "<h3 id=\"t831523682\">标题一1</h3>"
        + "<p>" + CHINESE_INDENT + "<b>正</b>文1</p>"
        + "<h4 id=\"t831528023\">标题二2</h4>"
        + "<p class=\"article_photo\">"
        + "<a href=\"../photo/002/02.html\" rel=\"external\">"
        + "<img src=\"../photo/002/02.jpg\" alt=\"箭扣长城 轻装\" id=\"i00202\" />"
        + "</a>"
        + "</p>"
        + "<p>" + CHINESE_INDENT + "正文<b>2</b></p>"
        + "<p class=\"article_photo\">"
        + "<a href=\"../photo/001/01.html\" rel=\"external\">"
        + "<img src=\"../photo/001/01.jpg\" alt=\"湘西凤凰 旅舍\" id=\"i00101\" />"
        + "</a>"
        + "</p>"
        + "<h4 id=\"t831528023\">标题二2</h4>"
        + "<p class=\"article_photo\">"
        + "<img src=\"../images/image/icon.jpg\" />"
        + "</p>"
        + "<p>" + CHINESE_INDENT + "正文3</p>"
        + "<p class=\"article_photo\"><img src=\"http://example.com/image/icon.jpg\" /></p>"
        + "<h3 id=\"t831523683\">标题一2</h3>"
        + "<p>" + CHINESE_INDENT + "正文4</p>"
        + "<p>" + CHINESE_INDENT + "正文5</p>"
        + "</html>";
    assertEquals(expected, sw.toString());
  }

  public void testGetFormattedPages_paragraphTags_snippet()
      throws IOException, ClassNotFoundException {
    List<Element> pages = acp.getFormattedPages("001", FormatLevel.SNIPPET, null);
    XMLOutputter output = new XMLOutputter(Format.getCompactFormat());
    StringWriter sw = new StringWriter();
    assertEquals(1, pages.size());
    output.output(pages.get(0), sw);
    String expected = "<html>"
        + "标题一1<br />"
        + CHINESE_INDENT + "<b>正</b>文1<br />"
        + "标题二2<br />"
        + "&lt;<a href=\"../photo/002/02.html\">箭扣长城 轻装</a>&gt;<br />"
        + CHINESE_INDENT + "正文<b>2</b><br />"
        + "&lt;<a href=\"../photo/001/01.html\">湘西凤凰 旅舍</a>&gt;<br />"
        + "标题二2<br />"
        + "&lt;图片&gt;<br />"
        + CHINESE_INDENT + "正文3<br />"
        + "&lt;图片&gt;<br />"
        + "标题一2<br />"
        + CHINESE_INDENT + "正文4<br />"
        + CHINESE_INDENT + "正文5<br />"
        + "</html>";
    assertEquals(expected, sw.toString());
  }

  public void testGetFormattedPages_paragraphTags_raw() throws IOException, ClassNotFoundException {
    List<Element> pages = acp.getFormattedPages("001", FormatLevel.RAW, null);
    XMLOutputter output = new XMLOutputter(Format.getCompactFormat());
    StringWriter sw = new StringWriter();
    assertEquals(1, pages.size());
    output.output(pages.get(0), sw);
    String expected = "<html>(标题一1)正文1(标题二2)正文2(标题二2)正文3(标题一2)正文4正文5</html>";
    assertEquals(expected, sw.toString());
  }

  public void testGetFormattedPages_sentenceTags_full() throws IOException, ClassNotFoundException {
    List<Element> pages = acp.getFormattedPages("002", FormatLevel.FULL, null);
    XMLOutputter output = new XMLOutputter(Format.getCompactFormat());
    StringWriter sw = new StringWriter();
    assertEquals(1, pages.size());
    output.output(pages.get(0), sw);
    String expected = "<html>" +
        "<h1>回来喽</h1>" +
        "<p class=\"timestamp\">(2006-2-13 15:36:57)</p>" +
        "<p>" +
        CHINESE_INDENT + "第<b>一</b>段<br />" +
        CHINESE_INDENT + "换行" +
        "<a href=\"../travel/article-001.html\">段尾站内链接</a>" +
        "</p>" +
        "<p>" +
        CHINESE_INDENT + "<b>第</b>二段<br />" +
        CHINESE_INDENT + "换行" +
        "<a rel=\"external\" href=\"http://example.com\">站外链接</a>" +
        "</p>" +
        "</html>";
    assertEquals(expected, sw.toString());
  }

  public void testGetFormattedPages_sentenceTags_snippet()
      throws IOException, ClassNotFoundException {
    List<Element> pages = acp.getFormattedPages("002", FormatLevel.SNIPPET, null);
    XMLOutputter output = new XMLOutputter(Format.getCompactFormat());
    StringWriter sw = new StringWriter();
    assertEquals(1, pages.size());
    output.output(pages.get(0), sw);
    String expected = "<html>"
        + CHINESE_INDENT + "第<b>一</b>段<br />"
        + CHINESE_INDENT + "换行"
        + "<a href=\"../travel/article-001.html\">段尾站内链接</a><br />"
        + CHINESE_INDENT + "<b>第</b>二段<br />"
        + CHINESE_INDENT + "换行"
        + "<a rel=\"external\" href=\"http://example.com\">站外链接</a><br />"
        + "</html>";
    assertEquals(expected, sw.toString());
  }

  public void testGetFormattedPages_sentenceTags_raw() throws IOException, ClassNotFoundException {
    List<Element> pages = acp.getFormattedPages("002", FormatLevel.RAW, null);
    XMLOutputter output = new XMLOutputter(Format.getCompactFormat());
    StringWriter sw = new StringWriter();
    assertEquals(1, pages.size());
    output.output(pages.get(0), sw);
    String expected = "<html>第一段换行段尾站内链接第二段换行站外链接</html>";
    assertEquals(expected, sw.toString());
  }
}
