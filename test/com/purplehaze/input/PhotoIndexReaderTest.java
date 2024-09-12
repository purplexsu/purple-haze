package com.purplehaze.input;

import com.purplehaze.TestUtil;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;

import static com.purplehaze.input.Media.Type.MP4;
import static com.purplehaze.input.Media.Type.PHOTO;

/**
 *
 */
public class PhotoIndexReaderTest extends TestCase {

  public void testRead() throws IOException {
    PhotoIndexReader reader =
        new PhotoIndexReader(new File(TestUtil.getTestFile("com/purplehaze/input/testdata/data/photo/011.txt")));
    reader.read();
    assertEquals(9, reader.getNumOfMedias());
    Media media = reader.getMedia(3);
    assertEquals(PHOTO, media.getType());
    assertEquals("\u65B0\u52A0\u5761\u4E2A\u4EBA\u65C5\u6E38\u7535\u5B50\u7B7E\u8BC1", media.getTags());
    assertEquals(0, media.getWidth());
    assertEquals(0, media.getHeight());
  }

  public void testReadMp4() throws IOException {
    PhotoIndexReader reader =
        new PhotoIndexReader(new File(TestUtil.getTestFile("com/purplehaze/input/testdata/data/photo/007.txt")));
    reader.read();
    assertEquals(4, reader.getNumOfMedias());
    Media media = reader.getMedia(0);
    assertEquals(MP4, media.getType());
    assertEquals("\u5858\u6CBD\u5916\u6EE9", media.getTags());
    assertEquals(760, media.getWidth());
    assertEquals(480, media.getHeight());
  }
}
