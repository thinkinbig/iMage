package org.jis.generator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.*;
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
	public void setUP() {
		try {
			resourceFolder = new File(path.toURI());
			fromFile = new File(resourceFolder, "from");
			toFile = new File(resourceFolder, "to");
		} catch (URISyntaxException e) {
			fail();
		}
	}

	/**
	 * delete the created files
	 */
	@After
	public void tearDown() {
		if (fromFile.exists()) { fromFile.delete(); }

		if (toFile.exists()) { toFile.delete(); }

	}

	/**
	 * Test the method with random input bytes
	 * Test method for {@link org.jis.generator.LayoutGalerie#copyFile(File, File)}.
	 */
	@Test
	public final void testCopyFile1() {
		byte[] array = new byte[10];
		new Random().nextBytes(array);
		randomString = new String(array);

		try {
			if (!toFile.exists()) { fromFile.createNewFile(); }
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
	 *  Test method for {@link org.jis.generator.LayoutGalerie#copyFile(File, File)}.
	 */
	@Test(expected = FileNotFoundException.class)
	public final void testCopyFile2() throws IOException {
		galerieUnderTest.copyFile(fromFile, toFile);
	}

	/**
	 *  Test if the file throw FileNotFoundException when the file is a directory
	 *  Test method for {@link org.jis.generator.LayoutGalerie#copyFile(File, File)}.
	 */
	@Test(expected = FileNotFoundException.class)
	public final void testCopyFile3() throws IOException {
		fromFile.mkdir();
		galerieUnderTest.copyFile(fromFile, toFile);
	}

	/**
	 *	Test the case when the zieldatei file is duplicated
	 *  Test method for {@link org.jis.generator.LayoutGalerie#copyFile(File, File)}.
	 */
	@Test
	public final void testCopyFile4() {
		try {

			Path fromPath = FileSystems.getDefault().getPath(fromFile.getPath());
			Path toPath = FileSystems.getDefault().getPath(toFile.getPath());

			// make new file if not exists
			if (!toFile.exists()) {
				toFile.createNewFile();
				Files.writeString(toPath, "file is not overwritten\n");
			}

			// write fromFile
			fromFile.createNewFile();
			Files.writeString(fromPath, "file is overwritten");

			// copy fromFile to toFile
			galerieUnderTest.copyFile(fromFile, toFile);

			// read from toFile
			String contents = Files.readString(toPath);


			assertEquals("file is overwritten", contents);

		} catch (IOException e) {
			fail();
		}
	}


	/**
	 *  Test if the copyFile() proceeds when the fromFile is not readable
	 *  my OS runs with file-lock somehow not as expected, no matter with
	 *  tryLock() or lock()
	 *  @throws IOException expected
	 */
	@Test(expected = IOException.class)
	public final void testCopyFile5() throws IOException {

		Path fromPath = FileSystems.getDefault().getPath(fromFile.getPath());

		// make new file if  not exists
		if (!fromFile.exists()) { fromFile.createNewFile(); }

		fromFile.setReadable(false);
		Files.writeString(fromPath, "file is not readable\n");

		// make new file if not exists
		if (!toFile.exists()) { toFile.createNewFile(); }

		galerieUnderTest.copyFile(fromFile, toFile);

	}


	/**
	 *  Test if the copyFile() will run if the toFile is not granted writing authorize
	 * @throws IOException expected
	 */
	@Test(expected = IOException.class)
	public final void testCopyFile6() throws IOException {
		Path fromPath = FileSystems.getDefault().getPath(fromFile.getPath());

		// make new file if  not exists
		if (!fromFile.exists()) { fromFile.createNewFile(); }

		Files.writeString(fromPath, "file is not readable\n");

		// make new file if not exists
		if (!toFile.exists()) { toFile.createNewFile(); }

		toFile.setWritable(false);

		galerieUnderTest.copyFile(fromFile, toFile);
	}


}
