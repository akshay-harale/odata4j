package org.odata4j.test.integration.producer.custom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.core.Response.Status;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityKey;
import org.odata4j.core.OProperties;
import org.odata4j.exceptions.ODataProducerException;

/**
 * The Class StreamingTest contain test cases for streaming media resources.
 */
public class StreamingTest extends StreamingBaseTest {

  public StreamingTest(RuntimeFacadeType type) {
    super(type);
  }

  @Before
  public void setUp() {
    super.setUp(20);
  }

  @Test
  public void testMLE() throws InterruptedException {

    ODataConsumer consumer = this.rtFacade.createODataConsumer(endpointUri, null);

    OEntity mle = consumer.getEntity("MediaResourceEntity", 1).execute();

    if (mle.getMediaLinkStream() != null) {
      long fileLength = saveStream(mle.getMediaLinkStream());
      Assert.assertTrue(fileLength != 0);
    } else {
      fail("Media link is not present");
    }

  }

  @Test
  public void testCreateAndDeleteMLE() throws InterruptedException {

    ODataConsumer consumer = this.rtFacade.createODataConsumer(endpointUri, null);

    InputStream inputStream;
    try {
      inputStream = new FileInputStream(".\\test\\resources\\Penguins_small.jpg");
      String desc = "Entity created on " + new java.util.Date().toString();

      OEntity category = consumer
          .createEntity("MediaResourceEntity")
          .properties(
              OProperties.string("Description", desc))
          .properties(OProperties.inputStream("MediaResource", inputStream)).execute();

      Assert.assertNotNull(category.getProperty("MediaResourceID").getValue());
      assertEquals(category.getProperty("Description").getValue().toString(), desc);

      OEntityKey key = OEntityKey.create("MediaResourceID", category.getProperty("MediaResourceID").getValue());

      category = consumer.getEntity("MediaResourceEntity", key).execute();

      if (category.getMediaLinkStream() != null) {
        long fileLength = saveStream(category.getMediaLinkStream());
        Assert.assertTrue(fileLength != 0);
      } else {
        fail("Media link is not present");
      }

      consumer.deleteEntity("MediaResourceEntity", key).execute();

      try {
        consumer.getEntity("MediaResourceEntity", key).execute();
        fail("Not found exception expected but not thrown");
      } catch (ODataProducerException ex) {
        assertEquals(ex.getHttpStatus().getStatusCode(), Status.NOT_FOUND.getStatusCode());
      }

      category = consumer
          .createEntity("MediaResourceEntity")
          .properties(
              OProperties.string("Description", "Test create without stream")).execute();

      Assert.assertNotNull(category.getProperty("MediaResourceID").getValue());
      assertEquals(category.getProperty("Description").getValue().toString(), "Test create without stream");

    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

  }

  @Test
  public void testUpdateMLE() {

    ODataConsumer consumer = this.rtFacade.createODataConsumer(endpointUri, null);

    OEntity category = consumer.getEntity("MediaResourceEntity", 1).execute();

    InputStream inputStream;
    try {
      inputStream = new FileInputStream(".\\test\\resources\\Tulips_small.jpg");
      String desc = "Entity updated with stream on " + new java.util.Date().toString();
      consumer
          .updateEntity(category)
          .properties(
              OProperties.string("Description", desc))
          .properties(OProperties.inputStream("MediaResource", inputStream)).execute();

      category = consumer.getEntity("MediaResourceEntity", 1).execute();

      assertEquals(category.getProperty("Description").getValue().toString(), desc);
      assertEquals(category.getProperty("MediaResourceID").getValue().toString(), "1");

      if (category.getMediaLinkStream() != null) {
        long fileLength = saveStream(category.getMediaLinkStream());
        Assert.assertTrue(fileLength != 0);
      } else {
        fail("Media link is not present");
      }

    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  private long saveStream(InputStream mediaLinkStream) {
    BufferedInputStream bis = null;
    BufferedOutputStream bos = null;
    File file = null;
    try {
      File currentDir = new File(".");
      String originDirectory;

      originDirectory = currentDir.getCanonicalPath();

      file = new File(originDirectory + "/Image.Jpeg");
      if (file.exists()) {
        file.delete();
      }
      file.createNewFile();
      bos = new BufferedOutputStream(new FileOutputStream(file));

      // save the stream 
      bis = new BufferedInputStream(mediaLinkStream);
      byte[] array = new byte[1024];
      while (bis.available() != 0) {
        bis.read(array);
        bos.write(array);
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        bis.close();
        bos.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return file != null ? file.length() : 0;
  }

}
