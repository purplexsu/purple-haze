package com.purplehaze;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Generates Google site map file.
 */
public class SiteMapGenerator {

  private static final Map<Division, String> CHANGE_FREQ_MAP = new HashMap<Division, String>() {
    {
      put(Division.PHOTO, "weekly");
      put(Division.APPRAISAL, "weekly");
      put(Division.MOVIE, "weekly");
      put(Division.MUSIC, "weekly");
      put(Division.TRAVEL, "daily");
      put(Division.BLOG, "weekly");
    }
  };
  private static final Map<Division, String> PRIORITY_MAP = new HashMap<Division, String>() {
    {
      put(Division.PHOTO, "0.5");
      put(Division.APPRAISAL, "0.6");
      put(Division.MOVIE, "0.7");
      put(Division.MUSIC, "0.6");
      put(Division.TRAVEL, "1.0");
      put(Division.BLOG, "0.6");
    }

  };
  private final List<String> DEFAULT_BLACK_LIST =
      Arrays.asList("_sample", "_notes", "images", "data", "raw", "Templates", "Materials");
  private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");
  private Set<File> visited;
  private FilenameFilter htmlFilter;
  private static final String BASE_URL = "http://www.purplexsu.net";
  private static final Namespace SITEMAP_NS = Namespace.getNamespace("http://www.google.com/schemas/sitemap/0.84");
  private final Context context;

  public SiteMapGenerator(Context context) {
    this.context = context;
    visited = new HashSet<File>();
    htmlFilter = new FileExtFilter("html");
  }

  public void articleSiteMap() throws IOException {
    String siteMapFile = "article.xml";
    EnumSet<Division> set = EnumSet.of(Division.PHOTO);
    Set<String> blackList = new HashSet<String>();
    for (Division d : set) {
      blackList.add(d.toString());
    }
    siteMap(siteMapFile, blackList);
  }

  public void photoSiteMap() throws IOException {
    String siteMapFile = "photo.xml";
    EnumSet<Division> set = EnumSet.allOf(Division.class);
    set.remove(Division.PHOTO);
    Set<String> blackList = new HashSet<String>();
    for (Division d : set) {
      blackList.add(d.toString());
    }
    siteMap(siteMapFile, blackList);
  }

  private void siteMap(String siteMapFile, Set<String> blackList) throws IOException {
    Element root = new Element("urlset", SITEMAP_NS);
    Document doc = new Document(root);
    Stack<File> stack = new Stack<File>();
    stack.push(context.getHazePath());
    while (!stack.isEmpty()) {
      File current = stack.peek();
      File[] files = current.listFiles();
      Arrays.sort(files);
      boolean isDir = false;
      for (int i = 0; i < files.length && !isDir; i++) {
        isDir = isCandidate(files[i], blackList);
      }
      if (isDir) {
        for (File file : files) {
          if (isCandidate(file, blackList)) {
            stack.push(file);
          }
        }
      } else {
        visited.add(stack.pop());
        for (File file : current.listFiles(htmlFilter)) {
          if (!file.getName().contains("comment")) {
            String relativeName = file.getCanonicalPath().substring(context.getHazePath().getCanonicalPath().length()).replaceAll("\\\\", "/");
            Element locE = new Element("loc", SITEMAP_NS).setText(BASE_URL + relativeName);
            Element lastmodE = new Element("lastmod", SITEMAP_NS).setText(SDF.format(new Date(file.lastModified())));
            Element changefreqE = new Element("changefreq", SITEMAP_NS).setText(getChangeFreq(file));
            Element priorityE = new Element("priority", SITEMAP_NS).setText(getPriority(file));
            Element urlE = new Element("url", SITEMAP_NS).addContent(locE).addContent(lastmodE).addContent(changefreqE).addContent(priorityE);
            root.addContent(urlE);
          }
        }
      }
    }
    context.getFileManager().xmlOutput(doc, new File(context.getHazePath(), siteMapFile));
  }

  private String getChangeFreq(File file) {
    if (file.getName().contains("index")) {
      return "daily";
    }
    for (Division d : Division.values()) {
      if (file.getAbsolutePath().contains(d.toString())) {
        return CHANGE_FREQ_MAP.get(d);
      }
    }
    return "weekly";
  }

  private String getPriority(File file) {
    for (Division d : Division.values()) {
      if (file.getAbsolutePath().contains(d.toString())) {
        return PRIORITY_MAP.get(d);
      }
    }
    return "0.6";
  }

  private boolean isCandidate(File file, Set<String> blackList) throws IOException {
    Set<String> fullBlackList = new HashSet<String>(blackList);
    fullBlackList.addAll(DEFAULT_BLACK_LIST);
    for (String keyword : fullBlackList) {
      if (file.getCanonicalPath().contains(keyword)) {
        return false;
      }
    }
    return file.isDirectory() && !visited.contains(file);
  }
}
