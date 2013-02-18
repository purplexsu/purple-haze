package com.purplehaze.output;

import junit.framework.TestCase;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.IOException;
import java.io.StringReader;

/**
 * Unittest for CleanCommentProcessor.
 */
public class CleanCommentProcessorTest extends TestCase
{
    private SAXBuilder builder;
    private CleanCommentProcessor processor;
    private XMLOutputter output;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        builder = new SAXBuilder("org.apache.xerces.parsers.SAXParser", false);
        builder.setFeature("http://xml.org/sax/features/namespaces", false);
        builder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        processor = new CleanCommentProcessor();
        output = new XMLOutputter(Format.getCompactFormat());
    }

    public void testProcess() throws JDOMException, IOException
    {
        String html = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<html><!-- InstanceBegin --><head><title>Title</title></head><!-- InstanceEndEditable -->" +
                "<body><script><!-- var i=1; //--></script>" +
                "<div id=\"ssi\"><!--#include file=\"comment.inc\" --></div></body></html>";
        final Document doc = builder.build(new StringReader(html));
        processor.process(doc);
        final String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
                "<html><head><title>Title</title></head><body><script><!-- var i=1; //--></script>" +
                "<div id=\"ssi\"><!--#include file=\"comment.inc\" --></div></body></html>\r\n";
        assertEquals(expected, output.outputString(doc));
    }
}
