package com.purplehaze;

/**
 * Util class for unittest.
 */
public class TestUtil {

  public static String getTestFile(String path) {
    return TestUtil.class.getClassLoader().getResource(path).getFile();
  }
}
