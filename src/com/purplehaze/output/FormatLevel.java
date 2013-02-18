package com.purplehaze.output;

/**
 * Defines different format levels for ArticleContentParser.
 */
enum FormatLevel {
  /**
   * Full format, used in article page.
   */
  FULL,

  /**
   * Snippet format, no embedded image. Used by ArticleSnippet & FeedGenerator.
   */
  SNIPPET,

  /**
   * Raw format, all HTML tags are striped.
   */
  RAW;


  @Override
  public String toString() {
    return super.toString().toLowerCase();
  }
}
