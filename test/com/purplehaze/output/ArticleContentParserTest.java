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

  public void testGetFormattedPages_paragraphTags() throws IOException, ClassNotFoundException {

  }

  public void testGetFormattedPages_sentenceTags() throws IOException, ClassNotFoundException {
    List<Element> pages = acp.getFormattedPages("002", FormatLevel.FULL, null);
    XMLOutputter output = new XMLOutputter(Format.getCompactFormat());
    StringWriter sw = new StringWriter();
    output.output(pages.get(0), sw);
    String expected =
        "<html>" +
            "<h1>回来喽</h1>" +
            "<p class=\"timestamp\">(2006-2-13 15:36:57)</p>" +
            "<p>" +
            Translations.CHINESE_INDENT + "第<b>一</b>段<br />" +
            Translations.CHINESE_INDENT + "换行" +
            "<a href=\"../travel/article-001.html\">段尾站内链接</a>" +
            "</p>" +
            "<p>" +
            Translations.CHINESE_INDENT + "<b>第</b>二段<br />" +
            Translations.CHINESE_INDENT + "换行" +
            "<a rel=\"external\" href=\"http://example.com\">站外链接</a>" +
            "</p>" +
            "</html>";
    assertEquals(expected, sw.toString());
  }
}
