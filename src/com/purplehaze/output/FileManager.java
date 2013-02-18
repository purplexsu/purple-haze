package com.purplehaze.output;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * File operation manager.
 */
public class FileManager {

  private final File hazePath;
  private final File templatePath;
  private final XMLOutputter xmlOutputter;
  private final SAXBuilder nonValidatedBuilder;
  private final Set<File> updated;
  private final List<HtmlPostProcessor> processors;

  public FileManager(File hazePath, File templatePath) {
    this.hazePath = hazePath;
    this.templatePath = templatePath;
    xmlOutputter = new XMLOutputter(Format.getCompactFormat());
    nonValidatedBuilder = new SAXBuilder("org.apache.xerces.parsers.SAXParser", false);
    nonValidatedBuilder.setFeature("http://xml.org/sax/features/namespaces", false);
    nonValidatedBuilder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
    updated = new HashSet<File>();
    processors = new ArrayList<HtmlPostProcessor>();
    processors.add(new CleanCommentProcessor());
  }

  public File getHazePath() {
    return hazePath;
  }

  public File getTemplatePath() {
    return templatePath;
  }

  public Document nonValidatedBuild(File file) throws IOException, JDOMException {
    return nonValidatedBuilder.build(file);
  }

  public void xmlOutput(Document doc, File file) throws IOException {
    for (HtmlPostProcessor p : processors) {
      p.process(doc);
    }
    xmlOutputter.output(doc, new FileOutputStream(file));
    updated.add(file);
  }

  public Set<File> getUpdatedFiles() {
    return Collections.unmodifiableSet(updated);
  }
}
