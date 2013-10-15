/*
 * (./) FileUtils.java
 * 
 * (cc) copyright@2010-2011
 * 
 * 
 * this library is all rights reserved , but you can used it for free.
 * if you want more support or functions, please contact with us!
 */
package com.lakeside.core.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.SystemUtils;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

// TODO: Auto-generated Javadoc
/**
 * The Class FileUtils.
 * 
 * @author Hou Dejun
 */
public class FileUtils {

	public static final int BUFFER_SIZE = 4096;

	/**
	 * check whether the file is exists.
	 * 
	 * @param path
	 *            the path
	 * @return the boolean
	 */
	public static Boolean exist(String path) {
		if (StringUtils.isEmpty(path)) {
			throw new RuntimeException("file path is empty!");
		}
		File file = new File(path);
		return file.exists();
	}

	/**
	 * deletes the file or directory if it exists.
	 * 
	 * @param path
	 *            the path
	 */
	public static void delete(String path) {
		if (StringUtils.isEmpty(path)) {
			throw new RuntimeException("file path is empty!");
		}

		File file = new File(path);
		if (file.exists()) {
			file.setExecutable(true);
			if (!file.delete()) {
				throw new RuntimeException(" delete file failed!");
			}
		}
	}

	/**
	 * create the directory if the directory is not exists
	 * 
	 * @param path
	 *            the path
	 */
	public static void mkDirectory(String path) {
		if (StringUtils.isEmpty(path)) {
			throw new RuntimeException("directory path is empty!");
		}
		path = PathUtils.getPath(path);
		File dir = new File(path);
		if (!dir.exists() && !isRootDirctory(path)) {
			if (!dir.mkdirs()) {
				throw new RuntimeException("create directory failed");
			}
		}
	}
	
	/**
	 * check whether the path is a root directory on windows system, suchlike
	 * "c:/" is a root directory. on unix system, suchlike "/" is a root
	 * directory.
	 * 
	 * @param path
	 */
	public static Boolean isRootDirctory(String path) {
		if (StringUtils.isEmpty(path)) {
			throw new RuntimeException("directory path is empty!");
		}
		path = PathUtils.getPath(path);
		Boolean isWindows = SystemUtils.IS_OS_WINDOWS;
		if (isWindows) {
			return path.matches("^[a-zA-Z]:[\\\\/]$");
		} else {
			return "/".equals(path);
		}
	}
	
	/**
	 * check whether the path is a root directory on windows system, suchlike
	 * "c:/" is a root directory. on unix system, suchlike "/" is a root
	 * directory.
	 * 
	 * @param path
	 */
	public static Boolean isAbsolutePath(String path) {
		if (StringUtils.isEmpty(path)) {
			throw new RuntimeException("directory path is empty!");
		}
		path = PathUtils.getPath(path);
		Boolean isWindows = SystemUtils.IS_OS_WINDOWS;
		if (isWindows) {
			return path.matches("^[a-zA-Z]:[\\\\/].*$");
		} else {
			return "/".equals(path);
		}
	}

	/**
	 * check whether the directory is a empty directory that doesn't hava files
	 * or sub directorys.
	 * 
	 * @param path
	 * @return
	 */
	public static Boolean isEmptyDirectory(String path) {
		if (StringUtils.isEmpty(path)) {
			throw new RuntimeException("directory path is empty!");
		}
		path = PathUtils.getPath(path);
		File file = new File(path);
		if (!file.exists()) {
			throw new RuntimeException("directory not be found!");
		}
		if (file.isDirectory()) {
			String[] subs = file.list();
			if (subs != null && subs.length > 0) {
				return false;
			}
		}
		return true;
	}

	/**
	 * make sure file's directory is exists.
	 * 
	 * @param path
	 *            the path
	 */
	public static void insureFileDirectory(String path) {
		if (StringUtils.isEmpty(path)) {
			throw new RuntimeException("file path is empty!");
		}
		path = PathUtils.getPath(path);
		if (path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}
		String dir = path;
		int index = path.lastIndexOf("/");
		if (index > -1) {
			dir = path.substring(0, index);
			mkDirectory(dir);
		}
	}

	/**
	 * make sure the file is exist,create a new file if it does't exist
	 * 
	 * @param path
	 */
	public static void insureFileExist(String path) {
		insureFileDirectory(path);
		File file = new File(path);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * empty the directory
	 * 
	 * @param path
	 */
	public static void emptyDir(String path) {
		if (StringUtils.isEmpty(path)) {
			throw new RuntimeException("file path is empty!");
		}
		File file = new File(path);
		if (file.exists()) {
			File[] children = file.listFiles();
			if (children != null) {
				for (int i = 0; i < children.length; i++) {
					deleteRecursively(children[i]);
				}
			}
		}
	}

	/**
	 * Delete the supplied {@link File} - for directories, recursively delete
	 * any nested directories or files as well.
	 * 
	 * @param root
	 *            the root <code>File</code> to delete
	 * @return <code>true</code> if the <code>File</code> was deleted, otherwise
	 *         <code>false</code>
	 */
	public static boolean deleteRecursively(File root) {
		if (root != null && root.exists()) {
			if (root.isDirectory()) {
				File[] children = root.listFiles();
				if (children != null) {
					for (int i = 0; i < children.length; i++) {
						deleteRecursively(children[i]);
					}
				}
			}
			return root.delete();
		}
		return false;
	}

	/**
	 * Recursively copy the contents of the <code>src</code> file/directory to
	 * the <code>dest</code> file/directory.
	 * 
	 * @param src
	 *            the source directory
	 * @param dest
	 *            the destination directory
	 * @throws IOException
	 *             in the case of I/O errors
	 */
	public static void copyRecursively(File src, File dest) throws IOException {
		Assert.isTrue(src != null && (src.isDirectory() || src.isFile()),
				"Source File must denote a directory or file");
		Assert.notNull(dest, "Destination File must not be null");
		doCopyRecursively(src, dest);
	}

	/**
	 * Actually copy the contents of the <code>src</code> file/directory to the
	 * <code>dest</code> file/directory.
	 * 
	 * @param src
	 *            the source directory
	 * @param dest
	 *            the destination directory
	 * @throws IOException
	 *             in the case of I/O errors
	 */
	private static void doCopyRecursively(File src, File dest)
			throws IOException {
		if (src.isDirectory()) {
			if (!dest.mkdir()) {
				throw new RuntimeException("create directory failed");
			}
			File[] entries = src.listFiles();
			if (entries == null) {
				throw new IOException("Could not list files in directory: "
						+ src);
			}
			for (int i = 0; i < entries.length; i++) {
				File file = entries[i];
				doCopyRecursively(file, new File(dest, file.getName()));
			}
		} else if (src.isFile()) {
			try {
				dest.createNewFile();
			} catch (IOException ex) {
				IOException ioex = new IOException("Failed to create file: "
						+ dest);
				ioex.initCause(ex);
				throw ioex;
			}
			copy(src, dest);
		} else {
			// Special File handle: neither a file not a directory.
			// Simply skip it when contained in nested directory...
		}
	}

	/**
	 * Copy the contents of the given input File to the given output File.
	 * 
	 * @param in
	 *            the file to copy from
	 * @param out
	 *            the file to copy to
	 * @return the number of bytes copied
	 * @throws IOException
	 *             in case of I/O errors
	 */
	public static int copy(File in, File out) throws IOException {
		Assert.notNull(in, "No input File specified");
		Assert.notNull(out, "No output File specified");
		return copy(new BufferedInputStream(new FileInputStream(in)),
				new BufferedOutputStream(new FileOutputStream(out)));
	}

	
	public static int copy(String in, String out) throws IOException {
		Assert.notNull(in, "No input File specified");
		Assert.notNull(out, "No output File specified");
		return copy(new BufferedInputStream(new FileInputStream(new File(in))),
				new BufferedOutputStream(new FileOutputStream(new File(out))));
	}
	
	
	/**
	 * Copy the contents of the given byte array to the given output File.
	 * 
	 * @param in
	 *            the byte array to copy from
	 * @param out
	 *            the file to copy to
	 * @throws IOException
	 *             in case of I/O errors
	 */
	public static void copy(byte[] in, File out) throws IOException {
		Assert.notNull(in, "No input byte array specified");
		Assert.notNull(out, "No output File specified");
		ByteArrayInputStream inStream = new ByteArrayInputStream(in);
		OutputStream outStream = new BufferedOutputStream(new FileOutputStream(
				out));
		copy(inStream, outStream);
	}

	/**
	 * Copy the contents of the given InputStream to the given OutputStream.
	 * Closes both streams when done.
	 * 
	 * @param in
	 *            the stream to copy from
	 * @param out
	 *            the stream to copy to
	 * @return the number of bytes copied
	 * @throws IOException
	 *             in case of I/O errors
	 */
	public static int copy(InputStream in, OutputStream out) throws IOException {
		Assert.notNull(in, "No InputStream specified");
		Assert.notNull(out, "No OutputStream specified");
		try {
			int byteCount = 0;
			byte[] buffer = new byte[BUFFER_SIZE];
			int bytesRead = -1;
			while ((bytesRead = in.read(buffer)) != -1) {
				out.write(buffer, 0, bytesRead);
				byteCount += bytesRead;
			}
			out.flush();
			return byteCount;
		} finally {
			try {
				in.close();
			} catch (IOException ex) {
			}
			try {
				out.close();
			} catch (IOException ex) {
			}
		}
	}

	/**
	 * java get the file size
	 * 
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public static double getFileSize(File file) {
		double size = 0;
		try {
			if (file.exists()) {
				FileInputStream fis = null;
				fis = new FileInputStream(file);
				size = fis.available();
				fis.close();
			} else {
				return 0;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return size;
	}

	/**
	 * java get the file size
	 * 
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public static double getFileSize(String filePath) {
		File file = new File(filePath);
		return getFileSize(file);
	}
	/**
     * Opens a {@link FileInputStream} for the specified file, providing better
     * error messages than simply calling <code>new FileInputStream(file)</code>.
     * <p>
     * At the end of the method either the stream will be successfully opened,
     * or an exception will have been thrown.
     * <p>
     * An exception is thrown if the file does not exist.
     * An exception is thrown if the file object exists but is a directory.
     * An exception is thrown if the file exists but cannot be read.
     * 
     * @param file  the file to open for input, must not be <code>null</code>
     * @return a new {@link FileInputStream} for the specified file
     * @throws FileNotFoundException if the file does not exist
     * @throws IOException if the file object is a directory
     * @throws IOException if the file cannot be read
     */
	public static FileInputStream openInputStream(File file) throws IOException {
		if (file.exists()) {
			if (file.isDirectory()) {
				throw new IOException("File '" + file
						+ "' exists but is a directory");
			}
			if (file.canRead() == false) {
				throw new IOException("File '" + file + "' cannot be read");
			}
		} else {
			throw new FileNotFoundException("File '" + file
					+ "' does not exist");
		}
		return new FileInputStream(file);
	}

	// -----------------------------------------------------------------------
	/**
	 * Reads the contents of a file into a String. The file is always closed.
	 * 
	 * @param file
	 *            the file to read, must not be <code>null</code>
	 * @param encoding
	 *            the encoding to use, <code>null</code> means platform default
	 * @return the file contents, never <code>null</code>
	 * @throws IOException
	 *             in case of an I/O error
	 * @throws java.io.UnsupportedEncodingException
	 *             if the encoding is not supported by the VM
	 */
	public static String readFileToString(File file, String encoding)
			throws IOException {
		Charset set = Charsets.UTF_8;
		if(!StringUtils.isEmpty(encoding)){
			set = Charset.forName(encoding);
		}
		return Files.toString(file, set);
	}
	
	/**
	 * Get the contents of an <code>InputStream</code> as a list of Strings,
     * one entry per line, using the specified character encoding.
     * 
	 * @param file
	 * @param encoding
	 * @return
	 * @throws IOException
	 */
    public static List<String> readLines(File file, String encoding) throws IOException {
    	InputStreamReader reader = null;
        try {
        	reader = new InputStreamReader(openInputStream(file), encoding);
            return readLines(reader);
        } finally {
        	reader.close();
        }
    }
	
    public static List<String> readLines(Reader input) throws IOException {
        BufferedReader reader = new BufferedReader(input);
        List<String> list = new ArrayList<String>();
        String line = reader.readLine();
        while (line != null) {
            list.add(line);
            line = reader.readLine();
        }
        return list;
    }

	/**
	 * Reads the contents of a file into a String using the default encoding for
	 * the VM. The file is always closed.
	 * 
	 * @param file
	 *            the file to read, must not be <code>null</code>
	 * @return the file contents, never <code>null</code>
	 * @throws IOException
	 *             in case of an I/O error
	 * @since Commons IO 1.3.1
	 */
	public static String readFileToString(File file) throws IOException {
		return readFileToString(file, null);
	}
	
	public static String readFileToString(String file) throws IOException {
		return readFileToString(new File(file), null);
	}

	public static boolean fileExists(String path) {
		return new File(path).exists();
	}
}
