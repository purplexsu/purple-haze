package com.purplehaze.output;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class InMemoryContentCache implements ArticleContentCache {

  private Map<Serializable, Serializable> cache = new HashMap<>();

  @Override
  public boolean cacheHit(Serializable key, Date modifiedTime) {
    return cache.containsKey(key);
  }

  @Override
  public boolean update(Serializable key, Serializable value) {
    cache.put(key, value);
    return true;
  }

  @Override
  public Serializable get(Serializable key) {
    return cache.get(key);
  }
}
