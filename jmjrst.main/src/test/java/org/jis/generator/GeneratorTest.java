package org.jis.generator;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class GeneratorTest {
  /**
   * Class under test.
   */
  private Generator generator;

  private int imageHeight, imageWidth;
  private static final File TEST_DIR = new File("target/test");
  private static final String IMAGE_FILE = "/image.jpg";
  private String imageName;

  /**
   * Input for test cases
   */
  private BufferedImage testImage;
  /**
   * Metadata for saving the image
   */
  private IIOMetadata imeta;
  /**
   * output from test cases
   */
  private BufferedImage rotatedImageTestResult;

  private final URL testClasses = this.getClass().getResource("");

  /**
   * Sicherstellen, dass das Ausgabeverzeichnis existiert und leer ist.
   */
  @BeforeClass
  public static void beforeClass() {
    if (TEST_DIR.exists()) {
      for (File f : TEST_DIR.listFiles()) {
        f.delete();
      }
    } else {
      TEST_DIR.mkdirs();
    }
  }

  @Before
  public void setUp() {
    this.generator = new Generator(null, 0);

    this.testImage = null;
    this.imeta = null;
    this.rotatedImageTestResult = null;

    final URL imageResource = this.getClass().getResource(IMAGE_FILE);

    imageName = extractFileNameWithoutExtension(new File(imageResource.getFile()));
   
    try (ImageInputStream iis = ImageIO.createImageInputStream(imageResource.openStream())) {
      ImageReader reader = ImageIO.getImageReadersByFormatName("jpg").next();
      reader.setInput(iis, true);
      ImageReadParam params = reader.getDefaultReadParam();
      this.testImage = reader.read(0, params);
      this.imageHeight = this.testImage.getHeight();
      this.imageWidth = this.testImage.getWidth();
      this.imeta = reader.getImageMetadata(0);
      reader.dispose();
    } catch (IOException e) {
      fail(e.getMessage());
    }
  }

  private String extractFileNameWithoutExtension(File file) {
    String fileName = file.getName();
    if (fileName.indexOf(".") > 0) {
      return fileName.substring(0, fileName.lastIndexOf("."));
    } else {
      return fileName;
    }
  }

  /**
   * Automatisches Speichern von testImage.
   */
  @After
  public void tearDown() {
    SimpleDateFormat sdf = new SimpleDateFormat("MM-dd_HH.mm.ss.SSS");
    String time = sdf.format(new Date());

    File outputFile = new File(
        MessageFormat.format("{0}/{1}_rotated_{2}.jpg", TEST_DIR, imageName, time));


    if (this.rotatedImageTestResult != null) {
      try (FileOutputStream fos = new FileOutputStream(outputFile);
           ImageOutputStream ios = ImageIO.createImageOutputStream(fos)) {
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
        writer.setOutput(ios);

        ImageWriteParam iwparam = new JPEGImageWriteParam(Locale.getDefault());
        iwparam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT); // mode explicit necessary

        // set JPEG Quality
        iwparam.setCompressionQuality(1f);
        writer.write(this.imeta, new IIOImage(this.rotatedImageTestResult, null, null), iwparam);
        writer.dispose();
      } catch (IOException e) {
        fail();
      }
    } else {
      // clean all non rotating files
      for (File item : TEST_DIR.listFiles()) {
        final Pattern pattern = Pattern.compile(imageName + "_rotated_.*");
        if (!pattern.matcher(extractFileNameWithoutExtension(item)).matches()) {
          item.delete();
        }
      }
    }
  }

  @Test
  public void testRotateImage_RotateImage0() {
    this.rotatedImageTestResult = this.generator.rotateImage(this.testImage, 0);

    assertTrue(imageEquals(this.testImage, this.rotatedImageTestResult));
  }

  @Test
  public void testRotateImage_RotateNull0() {
    this.rotatedImageTestResult = this.generator.rotateImage(null, 0);

    assertNull(this.rotatedImageTestResult);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRotateImage_Rotate042() {
    this.generator.rotateImage(this.testImage, 0.42);
  }

  @Test
  public void testRotateImage_Rotate90() {
    this.rotatedImageTestResult = this.generator.rotateImage(this.testImage, Generator.ROTATE_90);

    assertEquals(this.testImage.getHeight(), this.rotatedImageTestResult.getWidth());
    assertEquals(this.testImage.getWidth(), this.rotatedImageTestResult.getHeight());

    for (int i = 0; i < this.imageHeight; i++) {
      for (int j = 0; j < this.imageWidth; j++) {
        assertEquals(this.testImage.getRGB(j, i), this.rotatedImageTestResult.getRGB(this.imageHeight - 1 - i, j));
      }
    }
  }

  @Test
  public void testRotateImage_Rotate270() {
    this.rotatedImageTestResult = this.generator.rotateImage(this.testImage, Generator.ROTATE_270);

    assertEquals(this.testImage.getHeight(), this.rotatedImageTestResult.getWidth());
    assertEquals(this.testImage.getWidth(), this.rotatedImageTestResult.getHeight());

    for (int i = 0; i < this.imageHeight; i++) {
      for (int j = 0; j < this.imageWidth; j++) {
        assertEquals(this.testImage.getRGB(j, i), this.rotatedImageTestResult.getRGB(i, this.imageWidth - 1 - j));
      }
    }
  }

  @Test
  public void testRotateImage_RotateM90() {
    this.rotatedImageTestResult = this.generator.rotateImage(this.testImage, Math.toRadians(-90));

    assertEquals(this.testImage.getHeight(), this.rotatedImageTestResult.getWidth());
    assertEquals(this.testImage.getWidth(), this.rotatedImageTestResult.getHeight());

    for (int i = 0; i < this.imageHeight; i++) {
      for (int j = 0; j < this.imageWidth; j++) {
        assertEquals(this.testImage.getRGB(j, i), this.rotatedImageTestResult.getRGB(i, this.imageWidth - 1 - j));
      }
    }
  }

  @Test
  public void testRotateImage_RotateM270() {
    this.rotatedImageTestResult = this.generator.rotateImage(this.testImage, Math.toRadians(-270));

    assertEquals(this.testImage.getHeight(), this.rotatedImageTestResult.getWidth());
    assertEquals(this.testImage.getWidth(), this.rotatedImageTestResult.getHeight());

    for (int i = 0; i < this.imageHeight; i++) {
      for (int j = 0; j < this.imageWidth; j++) {
        assertEquals(this.testImage.getRGB(j, i), this.rotatedImageTestResult.getRGB(this.imageHeight - 1 - i, j));
      }
    }
  }

  /**
   *  Test if generator can create zip
   */
  @Test
  public final void testCreateZip() {
    File zipName = new File(TEST_DIR, "test.zip");
    assertFalse(zipName.exists());
    Vector<File> selected = new Vector<>();
    File test = new File("test");
    try {
      test.createNewFile();
    } catch (IOException e) {
      fail();
    }
    selected.add(test);
    generator.createZip(zipName, selected);
    assertTrue(zipName.exists());
  }

  @Test
  public final void testGenerate() {
  }


  /**
   * Check if two images are identical - pixel wise.
   * 
   * @param expected
   *          the expected image
   * @param actual
   *          the actual image
   * @return true if images are equal, false otherwise.
   */
  protected static boolean imageEquals(BufferedImage expected, BufferedImage actual) {
    if (expected == null || actual == null) {
      return false;
    }

    if (expected.getHeight() != actual.getHeight()) {
      return false;
    }

    if (expected.getWidth() != actual.getWidth()) {
      return false;
    }

    for (int i = 0; i < expected.getHeight(); i++) {
      for (int j = 0; j < expected.getWidth(); j++) {
        if (expected.getRGB(j, i) != actual.getRGB(j, i)) {
          return false;
        }
      }
    }

    return true;
  }

}
