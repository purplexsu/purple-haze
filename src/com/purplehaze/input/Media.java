package com.purplehaze.input;

public class Media {

  private Type type = Type.PHOTO;
  private int width;
  private int height;
  private final String tags;

  enum Type {
    PHOTO,
    MOV,
    MP4
  }

  public Media(String rawString) {
    String [] parts = rawString.split("\\|");
    this.tags = parts[0];
    if (parts.length > 1) {
      this.type = Type.valueOf(parts[1].toUpperCase());
    }
    if (parts.length > 2) {
      this.width = Integer.parseInt(parts[2]);
    }
    if (parts.length > 3) {
      this.height = Integer.parseInt(parts[3]);
    }
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
    return tags;
  }
}
