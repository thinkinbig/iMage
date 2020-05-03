package org.jis.generator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LayoutGalerieTest {
	
	private LayoutGalerie galerieUnderTest = new LayoutGalerie(null, null);

	private final URL path = this.getClass().getResource(File.separator);

	private File resourceFolder;
	private File fromFile;
	private File toFile;
	private String randomString;

	/**
	 * initialise the resource folder to get ready
	 */
	@Before
	public void before() {
		try {
			resourceFolder = new File(path.toURI());
		} catch (URISyntaxException e) {
			fail();
		}
	}

	/**
	 * delete the created files
	 */
	@After
	public void after() {
		if (fromFile.exists()) { fromFile.delete(); }

		if (toFile.exists()) { toFile.delete(); }

	}

	/**
	 * Test method for {@link org.jis.generator.LayoutGalerie#copyFile(File, File)}.
	 */
	@Test
	public final void testCopyFile1() {
		fromFile = new File("from");
		toFile = new File("to");
		byte[] array = new byte[10];
		new Random().nextBytes(array);
		randomString = new String(array);

		try {
			fromFile.createNewFile();
			Path fromPath = FileSystems.getDefault().getPath(fromFile.getPath());
			Files.writeString(fromPath, randomString);

			galerieUnderTest.copyFile(fromFile, toFile);
			 
			assertTrue(toFile.exists());
			 
			Path toPath = FileSystems.getDefault().getPath(toFile.getPath());
			String contents = Files.readString(toPath);
			 		 
			assertEquals(randomString, contents);
		 }
		 catch (IOException e) {
			fail();
		 }
	}

	/**
	 *  Test if the file throw FileNotFoundException when the file not exists
	 */
	@Test(expected = FileNotFoundException.class)
	public final void testCopyFile2() throws IOException {
		fromFile = new File(resourceFolder, "from");
		toFile = new File(resourceFolder, "to");
		galerieUnderTest.copyFile(fromFile, toFile);
	}

	/**
	 *  Test if the file throw FileNotFoundException when the file is a directory
	 */
	@Test(expected = FileNotFoundException.class)
	public final void testCopyFile3() throws IOException {
		fromFile = new File(resourceFolder, "from");
		fromFile.mkdir();
		toFile = new File(resourceFolder, "to");
		galerieUnderTest.copyFile(fromFile, toFile);
	}

}
