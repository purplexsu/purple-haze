package com.purplehaze.output;

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
        + "<p class=\"article_text\"><span><b>正</b>文1</span></p>"
        + "<h4 id=\"t831528023\">标题二2</h4>"
        + "<p class=\"article_photo\">"
        + "<a href=\"../photo/002/02.html\" rel=\"external\">"
        + "<img src=\"../photo/002/02.jpg\" alt=\"箭扣长城 轻装\" id=\"i00202\" />"
        + "</a>"
        + "</p>"
        + "<p class=\"article_text\"><span>正文<b>2</b></span></p>"
        + "<p class=\"article_photo\">"
        + "<a href=\"../photo/001/01.html\" rel=\"external\">"
        + "<img src=\"../photo/001/01.jpg\" alt=\"湘西凤凰 旅舍\" id=\"i00101\" />"
        + "</a>"
        + "</p>"
        + "<h4 id=\"t831528023\">标题二2</h4>"
        + "<p class=\"article_photo\">"
        + "<img src=\"../images/image/icon.jpg\" />"
        + "</p>"
        + "<p class=\"article_text\"><span>正文3</span></p>"
        + "<p class=\"article_photo\"><img src=\"http://example.com/image/icon.jpg\" /></p>"
        + "<h3 id=\"t831523683\">标题一2</h3>"
        + "<p class=\"article_text\"><span>正文4</span></p>"
        + "<p class=\"article_text\"><span>正文5</span></p>"
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
        + "<span><b>正</b>文1</span><br />"
        + "标题二2<br />"
        + "&lt;<a href=\"../photo/002/02.html\">箭扣长城 轻装</a>&gt;<br />"
        + "<span>正文<b>2</b></span><br />"
        + "&lt;<a href=\"../photo/001/01.html\">湘西凤凰 旅舍</a>&gt;<br />"
        + "标题二2<br />"
        + "&lt;图片&gt;<br />"
        + "<span>正文3</span><br />"
        + "&lt;图片&gt;<br />"
        + "标题一2<br />"
        + "<span>正文4</span><br />"
        + "<span>正文5</span><br />"
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
        "<p class=\"article_text\">" +
        "<span>第<b>一</b>段</span>" +
        "<span>换行" +
        "<a href=\"../travel/article-001.html\">段尾站内链接</a></span>" +
        "<span><span class=\"capital\">首</span>字母加大</span>" +
        "</p>" +
        "<p class=\"article_text\">" +
        "<span><b>第</b>二段</span>" +
        "<span>换行" +
        "<a rel=\"external\" href=\"http://example.com\">站外链接</a></span>" +
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
        + "<span>第<b>一</b>段</span><br />"
        + "<span>换行"
        + "<a href=\"../travel/article-001.html\">段尾站内链接</a></span><br />"
        + "<span>首字母加大</span><br />"
        + "<span><b>第</b>二段</span><br />"
        + "<span>换行"
        + "<a rel=\"external\" href=\"http://example.com\">站外链接</a></span><br />"
        + "</html>";
    assertEquals(expected, sw.toString());
  }

  public void testGetFormattedPages_sentenceTags_raw() throws IOException, ClassNotFoundException {
    List<Element> pages = acp.getFormattedPages("002", FormatLevel.RAW, null);
    XMLOutputter output = new XMLOutputter(Format.getCompactFormat());
    StringWriter sw = new StringWriter();
    assertEquals(1, pages.size());
    output.output(pages.get(0), sw);
    String expected = "<html>第一段换行段尾站内链接首字母加大第二段换行站外链接</html>";
    assertEquals(expected, sw.toString());
  }

  public void testGetFormattedPages_albumMediaTags_full() throws IOException, ClassNotFoundException {
    List<Element> pages = acp.getFormattedPages("003", FormatLevel.FULL, null);
    XMLOutputter output = new XMLOutputter(Format.getCompactFormat());
    StringWriter sw = new StringWriter();
    assertEquals(1, pages.size());
    output.output(pages.get(0), sw);
    String expected = "<html><h1>终于搞定了新疆的游记</h1>" +
        "<p class=\"timestamp\">(2006-2-14 10:07:58)</p>" +
        "<p class=\"article_text\"><span>http://cloud.withu.com/purplexsu/013</span>" +
        "<span>Test media tags: images</span></p>" +
        "<p class=\"article_photo\"><a href=\"../photo/007/02.html\" rel=\"external\">" +
        "<img src=\"../photo/007/02.jpg\" alt=\"塘沽外滩\" id=\"i00702\" /></a></p>" +
        "<p class=\"article_text\"><span>MP4</span></p>" +
        "<p class=\"article_photo\"><video id=\"i00701\" src=\"../photo/007/01.mp4\" width=\"760\" height=\"480\" " +
        "controls=\"controls\" /></p>" +
        "<p class=\"article_text\"><span>MOV</span></p>" +
        "<p class=\"article_photo\"><video id=\"i00703\" src=\"../photo/007/03.mov\" width=\"640\" height=\"480\" " +
        "controls=\"controls\" /></p>" +
        "</html>";
    assertEquals(expected, sw.toString());
  }

}
