package com.purplehaze;

import com.purplehaze.input.ArticleDataAggregatorTest;
import com.purplehaze.output.ArticleContentParserTest;
import com.purplehaze.output.CleanCommentProcessorTest;
import com.purplehaze.output.DivisionPageInfoTest;
import junit.framework.TestFailure;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import java.util.Enumeration;

/**
 * The entry point for unittest.
 */
public class AllTests {

  private static Class[] tests = {
      ArticleContentParserTest.class,
      ArticleDataAggregatorTest.class,
      CleanCommentProcessorTest.class,
      ContextTest.class,
      DivisionPageInfoTest.class
  };

  public static void main(String[] args) {
    final TestSuite testSuite = new TestSuite(tests);
    final TestResult result = new TestResult();
    testSuite.run(result);
    System.out.println(String.format("%d tests are completed!", result.runCount()));
    final int failureCount = result.failureCount();
    if (failureCount > 0) {
      printTestError(result.failures(), failureCount, "%d tests are failed:", "#%d test fail @ %s:\n%s");
    }
    final int errorCount = result.errorCount();
    if (errorCount > 0) {
      printTestError(result.errors(), errorCount, "Error occurs in %d tests:", "#%d error in %s:\n%s");
    }
  }

  private static void printTestError(
      Enumeration<TestFailure> failures,
      int failureCount,
      final String summary,
      final String detail) {
    System.err.println(String.format(summary, failureCount));
    int index = 1;
    while (failures.hasMoreElements()) {
      final TestFailure failure = failures.nextElement();
      System.err.println(
          String.format(
              detail,
              index++,
              failure.failedTest(),
              failure.trace()));
    }
  }
}
