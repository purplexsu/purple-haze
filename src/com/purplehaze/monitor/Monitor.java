package com.purplehaze.monitor;

import com.purplehaze.Utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Log monitor.
 */
public class Monitor implements Runnable {

  private static final Set<Pattern> APPROVED_URL_PATTERNS = new HashSet<Pattern>();
  private static final Set<String> APPROVED_URLS = new HashSet<String>();

  public Monitor(String configDirString) throws IOException {
    File configDir = new File(configDirString);
    Utils.verifyDir(configDir);

    File pattern = new File(configDir, "whitelist_pattern.txt");
    BufferedReader br = new BufferedReader(new FileReader(pattern));
    String line = null;
    while (!Utils.isEmptyString(line = br.readLine())) {
      APPROVED_URL_PATTERNS.add(Pattern.compile(line));
    }

    File url = new File(configDir, "whitelist_url.txt");
    br = new BufferedReader(new FileReader(url));
    line = null;
    while (!Utils.isEmptyString(line = br.readLine())) {
      APPROVED_URLS.add(line);
    }
  }

  public void run() {
    try {
      callServer("http://www.purplexsu.net/cmdcmd.php?cmdcmd=4");
      String log = callServer("http://www.purplexsu.net/purplexsu.txt");
      BufferedReader br = new BufferedReader(new StringReader(log));
      String line = null;
      while ((line = br.readLine()) != null) {
        try {
          String[] array = line.split(" ");
          String requestedFile = array[6].trim();
          int status = Integer.parseInt(array[8].trim());
          String refer = array[10].trim();
          if (refer.startsWith("\"")) {
            refer = refer.substring(1);
          }
          if (refer.endsWith("\"")) {
            refer = refer.substring(0, refer.length() - 1);
          }
          if (requestedFile.endsWith(".jpg")
              && !requestedFile.endsWith("copyright.jpg")
              && !refer.startsWith("http://www.purplexsu.net")
              && (status == 200 || status == 304)) {
            if (!APPROVED_URLS.contains(refer)) {
              boolean bad = true;
              for (Pattern site : APPROVED_URL_PATTERNS) {
                if (site.matcher(refer).matches()) {
                  bad = false;
                  break;
                }
              }
              if (bad) {
                System.out.print("Bad site:" + refer);
                System.out.print("\tstatus:" + status);
                System.out.println("\tSteals:" + requestedFile);
              }
            }
          }
        } catch (NumberFormatException e) {
          System.err.println("Error format:" + line);
        }
      }
      callServer("http://www.purplexsu.net/cmdcmd.php?cmdcmd=5");
    } catch (IOException e) {
      e.printStackTrace();  //TODO: Need further handling
    }
  }


  private String callServer(String url) throws IOException {
    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
    connection.setRequestProperty("User-Agent", "PurpleHaze!");
    System.out.println("Start to connect:" + url);
    connection.connect();
    StringBuilder sb = new StringBuilder();
    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), Utils.DEFAULT_CHARSET));
    String line = null;
    while ((line = br.readLine()) != null) {
      sb.append(line).append("\n");
    }
    connection.disconnect();
    return sb.toString();
  }

  public static void main(String[] args) throws IOException {
    Pattern p = Pattern.compile("^http\\://(\\S+?)\\.soso\\..*");
    boolean result = p.matcher("http://image.soso.com/image.cgi?w=%CC%C1%B9%C1%CD%E2%CC%B2&sc=img&ity=0&scr=&imf=&pid=p.f.zr&ch=s.p.detail.a&ic=one&id=6&pg=1&rnum=1").matches();
    new Monitor(args[0]).run();
  }
}
