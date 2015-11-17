package com.purplehaze.output;

import java.io.Serializable;
import java.util.Date;

/**
 * An interface to represent how to cache the parsed content.
 */
public interface ArticleContentCache {

  boolean cacheHit(Serializable key, Date modifiedTime);

  boolean update(Serializable key, Serializable value);

  Serializable get(Serializable key);
}
