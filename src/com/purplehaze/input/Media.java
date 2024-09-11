package com.purplehaze.input;

public class Media {

  private final Type type = Type.PHOTO;
  private int width;
  private int height;
  private final String rawString;

  enum Type {
    PHOTO,
    MOV,
    MP4;
  }

  public Media(String rawString) {
    this.rawString = rawString;
  }

  public int getHeight() {
    return height;
  }

  public int getWidth() {
    return width;
  }

  public Type getType() {
    return type;
  }

  public String getTags() {
    return rawString;
  }
}
