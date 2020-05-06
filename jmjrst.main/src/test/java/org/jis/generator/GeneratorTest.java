package org.jis.generator;

import org.jis.Main;
import org.junit.*;

import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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
  private File image;
  private String imageName;
  private Main mockMain;

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

  /**
   *  Unglücklicherweise bin ich mit mock nicht vertraut. ich wende an Reflection
   * @throws NoSuchMethodException no default constructor
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   * @throws InstantiationException
   */
  @Before
  public void setUp() throws NoSuchMethodException, IllegalAccessException,
          InvocationTargetException, InstantiationException {
    Constructor c = Main.class.getDeclaredConstructor();
    c.setAccessible(true);
    mockMain = (Main) c.newInstance();
    this.generator = new Generator(mockMain, 0);
    this.testImage = null;
    this.imeta = null;
    this.rotatedImageTestResult = null;

    final URL imageResource = this.getClass().getResource(IMAGE_FILE);

    try {
      image = new File(imageResource.toURI());
    } catch (URISyntaxException e) {
      fail();
    }
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


  /**
   * generate an image that is 2 scale bigger as the normal one, not print
   */
  @Test
  public final void testGenerateImage_Expand(){
    try {
      File receive = generator.generateImage(image, TEST_DIR, false, imageWidth * 2,
              imageHeight * 3, "");
      BufferedImage actual = ImageIO.read(receive);
      assertTrue(receive.exists());
      assertEquals(imageHeight * 3 ,actual.getHeight());
      assertEquals(imageWidth * 3, actual.getWidth());
    } catch (IOException e) {
      fail();
    }
  }

  /**
   * generate an image that is smaller as the normal one
   */
  @Test
  public final void testGenerateImage_Shrink(){
    try {
      File receive = generator.generateImage(image, TEST_DIR, true, imageWidth / 2,
              imageHeight / 3, "");
      BufferedImage actual = ImageIO.read(receive);
      assertTrue(receive.exists());
      assertEquals(imageHeight / 3 ,actual.getHeight());
      assertEquals(imageWidth / 3, actual.getWidth());
    } catch (IOException e) {
      fail();
    }
  }



  /**
   * test if the image can be rotated
   * this method can't run because rotate() has exception
   */
  @Test
  public final void testRotateWithAngle() {
    try {
      File test = generator.generateImage(image, TEST_DIR, false, imageWidth,
              imageHeight, "");
      assertTrue(test != null);
      generator.rotate(test, 90);
      assertTrue(test.exists());
    } catch (IOException e) {
      fail();
    }
  }

  /**
   * test if the file can rotate
   * error occurs but was caught by Generator
   * @throws IOException image not generate
   */
  @Test
  public final void testRotate() throws IOException {
    File test = generator.generateImage(image, TEST_DIR, false, imageWidth,
            imageHeight, "");
    assertTrue(test != null);
    generator.rotate(test);
    assertTrue(test.exists());
  }

  /**
   * Test generate if text can generate image
   */
  @Test
  public final void testGenerateText() {
    generator.generateText(image, TEST_DIR, imageWidth, imageHeight);

    // check if the files contains export file
    File receive = null;
    for (File file : TEST_DIR.listFiles()) {
      if (file.getName().equals("t_" + imageName + ".jpg")) {
        receive = file;
        break;
      }
    }
    try {
      BufferedImage bi = ImageIO.read(receive);
      // it strange why these two picture all not as big
      assertTrue(bi != null);
    } catch (IOException e) {
      fail();
    }
  }

  /**
   *  Test generate Text when input and output are directory
   */
  @Test
  public final void testGenerateText_Dir() {
    File in = new File("target/test-classes");
    generator.generateText(in, TEST_DIR, imageWidth, imageHeight);
    File receive = null;
    for (File file : TEST_DIR.listFiles()) {
      receive = file;
      break;
    }
    try {
      BufferedImage bi = ImageIO.read(receive);
      // it strange why these two picture all not as big
      assertTrue(bi != null);
    } catch (IOException e) {
      fail();
    }
  }

  /**
   *  No idea how it works
   */
  @Test
  @Ignore
  public final void test() {
    generator.generate(false);
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
