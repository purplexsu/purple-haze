package com.purplehaze;


import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.mime.FileBody;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.util.Timeout;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
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
  private final CloseableHttpClient httpclient;
  private final FTPClient ftpClient = new FTPClient();

  public ArticleTransfer(File hazePath) throws IOException {
    this.hazePath = hazePath;
    this.files = new HashSet();
    zipFile = new File(hazePath, "update.zip");
    final FileInputStream fis = new FileInputStream(new File(System.getProperty("user.home"), ".purple-haze"));
    credentials.load(fis);
    fis.close();
    BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
    credsProvider.setCredentials(new AuthScope(new HttpHost("https", "www.purplexsu.net")), new UsernamePasswordCredentials(credentials.getProperty("http.username"), credentials.getProperty("http.password").toCharArray()));
    httpclient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
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
    // Create multipart entity
    MultipartEntityBuilder builder = MultipartEntityBuilder.create();
    builder.addPart("MyFile", new FileBody(zipFile));
    HttpEntity multipartEntity = builder.build();
    HttpPost httpPost = new HttpPost("https://www.purplexsu.net/cmdcmd/cmdcmd.php?cmdcmd=3");
    httpPost.setEntity(multipartEntity);
    prepareHttpMethod(httpPost);

    // Execute the method.
    try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
      HttpEntity entity = response.getEntity();
      if (response.getCode() != HttpStatus.SC_OK) {
        System.err.println("Method failed: " + response.getCode() + " " + response.getReasonPhrase());
      }
      if (entity != null) {
        // Read the response body.
        System.out.println(EntityUtils.toString(entity));
      }
    } catch (ParseException ex) {
      ex.printStackTrace();
    }
  }

  private void ftp() throws IOException {
    FileInputStream is = new FileInputStream(zipFile);
    try {
      ftpClient.connect("www.purplexsu.net");
      ftpClient.login(credentials.getProperty("ftp.username"), credentials.getProperty("ftp.password"));
      ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
      ftpClient.enterLocalPassiveMode();
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

  private void callServer(String url) throws IOException {
    HttpGet httpGet = new HttpGet(url);
    prepareHttpMethod(httpGet);
    try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
      if (response.getCode() != HttpStatus.SC_OK) {
        System.err.println("Method failed: " + response.getCode() + " " + response.getReasonPhrase());
      }
      // Get the response entity
      HttpEntity entity = response.getEntity();
      // Print the response body
      if (entity != null) {
        System.out.println(EntityUtils.toString(entity));
      }
    } catch (ParseException e) {
      e.printStackTrace();
    }
  }

  private void prepareHttpMethod(HttpUriRequestBase request) {
    request.setHeader("User-Agent", "PurpleHaze!");
    RequestConfig requestConfig = RequestConfig.custom()
        .setConnectTimeout(Timeout.ofMilliseconds(5000)) // Connection timeout (5 seconds)
        .setResponseTimeout(Timeout.ofMilliseconds(60000)) // Socket timeout (60 seconds)
        .setConnectionRequestTimeout(Timeout.ofMilliseconds(5000)) // Connection request timeout (5 seconds)
        .build();
    request.setConfig(requestConfig);
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
