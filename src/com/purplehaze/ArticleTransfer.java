package com.purplehaze;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * To transfer files to server via HTTP or FTP.
 */
public class ArticleTransfer {

  private final File hazePath;
  private final Set<File> files;
  private final File zipFile;
  private final Properties credentials = new Properties();
  private final HttpClient httpClient = new HttpClient();
  private final FTPClient ftpClient = new FTPClient();

  public ArticleTransfer(File hazePath) throws IOException {
    this.hazePath = hazePath;
    this.files = new HashSet<File>();
    zipFile = new File(hazePath, "update.zip");
    final FileInputStream fis = new FileInputStream(
        new File(System.getProperty("user.home"), ".purple-haze"));
    credentials.load(fis);
    fis.close();
    httpClient.getState().setCredentials(
        new AuthScope("www.purplexsu.net", 80),
        new UsernamePasswordCredentials(
            credentials.getProperty("http.username"),
            credentials.getProperty("http.password")));
  }

  public void clearFiles() {
    files.clear();
  }

  public void addFile(File file) {
    if (!file.exists() || file.isDirectory()) {
      return;
    }
    files.add(file);
  }

  public void addFiles(Collection<File> files) {
    for (File file : files) {
      addFile(file);
    }
  }

  public void addDirectory(File dir, FilenameFilter filter) {
    if (!dir.exists() || dir.isFile()) {
      return;
    }
    for (File subDir : dir.listFiles(new FileNameFilter(".*", true))) {
      addDirectory(subDir, filter);
    }
    for (File file : dir.listFiles(filter)) {
      addFile(file);
    }
  }

  public void zip() throws IOException {
    if (zipFile.exists() && zipFile.isFile()) {
      zipFile.delete();
    }
    ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));
    for (File file : files) {
      System.out.println("Archieving file:" + file.getAbsolutePath());
      DataInputStream in = new DataInputStream(new FileInputStream(file));
      out.putNextEntry(new ZipEntry(getRelativePath(file)));
      int c;
      while ((c = in.read()) != -1) {
        out.write(c);
      }
      in.close();
      out.closeEntry();
    }
    out.close();
  }

  public void upload() throws IOException {
    String input = Utils.stringInput("Using HTTP or FTP?[h|f]:", "h");
    System.out.println("Start uploading....");
    if (input.toLowerCase().startsWith("f")) {
      ftp();
    } else {
      http();
    }
  }

  private void http() throws IOException {
    final PostMethod method = new PostMethod("http://www.purplexsu.net/cmdcmd/cmdcmd.php?cmdcmd=3");
    prepareHttpMethod(method);

    try {
      Part[] parts = {new FilePart("MyFile", zipFile)};
      MultipartRequestEntity mime = new MultipartRequestEntity(parts, method.getParams());
      method.setRequestEntity(mime);

      // Execute the method.
      int statusCode = httpClient.executeMethod(method);
      if (statusCode != HttpStatus.SC_OK) {
        System.err.println("Method failed: " + method.getStatusLine());
      }

      // Read the response body.
      final BufferedReader br = new BufferedReader(
          new InputStreamReader(method.getResponseBodyAsStream()));

      // Deal with the response.
      // Use caution: ensure correct character encoding and is not binary data
      String response = br.readLine();
      if (!Utils.equals("10", response)) {
        throw new IOException("Upload fail, response is:" + response);
      }
      br.close();
    } finally {
      // Release the connection.
      method.releaseConnection();
    }
  }

  private void ftp() throws IOException {
    FileInputStream is = new FileInputStream(zipFile);
    try {
      ftpClient.connect("www.purplexsu.net");
      ftpClient.login(
          credentials.getProperty("ftp.username"),
          credentials.getProperty("ftp.password"));
      ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
      ftpClient.enterLocalActiveMode();
      ftpClient.changeWorkingDirectory("/www/cmdcmd");
      ftpClient.storeFile(zipFile.getName(), is);
      ftpClient.disconnect();
      System.out.println("Upload complete!");
    } finally {
      is.close();
    }
  }

  public void extract() throws IOException {
    callServer("http://www.purplexsu.net/cmdcmd/cmdcmd.php?cmdcmd=2");
  }

  public void ping() throws IOException {
    callServer("http://www.purplexsu.net/cmdcmd/ping.php");
  }

  private void callServer(String url) throws IOException {
    final GetMethod method = new GetMethod(url);
    prepareHttpMethod(method);
    try {
      // Execute the method.
      int statusCode = httpClient.executeMethod(method);
      if (statusCode != HttpStatus.SC_OK) {
        System.err.println("Method failed: " + method.getStatusLine());
      }

      // Read the response body.
      BufferedReader br = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));

      // Deal with the response.
      // Use caution: ensure correct character encoding and is not binary data
      String response = null;
      while ((response = br.readLine()) != null) {
        System.out.println(response);
      }
      br.close();
    } finally {
      // Release the connection.
      method.releaseConnection();
    }
  }

  private void prepareHttpMethod(HttpMethod method) {
    method.setDoAuthentication(true);
    method.addRequestHeader("User-Agent", "PurpleHaze!");
    method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
    method.getParams().setSoTimeout(60 * 1000);
  }

  private String getRelativePath(File file) {
    String base = hazePath.getAbsolutePath().replace("\\", "/");
    String full = file.getAbsolutePath().replace("\\", "/");
    String result = full.substring(base.length());
    while (result.startsWith("/")) {
      result = result.substring(1);
    }
    return result;
  }
}
