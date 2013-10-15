package com.lakeside.data.compress;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;

public class CompressUtil {
	public static void createArchive(String outputFilename, String inputFile)
			throws IOException {
		GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(
				outputFilename));
		byte[] buf = new byte[1024];

		File file = new File(inputFile);

		FileInputStream in = new FileInputStream(file);

		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	}

	/**
	 * Creates a tar.gz file at the specified path with the contents of the
	 * specified directory.
	 * 
	 * @param dirPath
	 *            The path to the directory to create an archive of
	 * @param archivePath
	 *            The path to the archive to create
	 * 
	 * @throws IOException
	 *             If anything goes wrong
	 */
	public static void createTarGzOfDirectory(String directoryPath,
			String tarGzPath) throws IOException {
		FileOutputStream fOut = null;
		BufferedOutputStream bOut = null;
		GzipCompressorOutputStream gzOut = null;
		TarArchiveOutputStream tOut = null;

		try {
			fOut = new FileOutputStream(new File(tarGzPath));
			bOut = new BufferedOutputStream(fOut);
			gzOut = new GzipCompressorOutputStream(bOut);
			tOut = new TarArchiveOutputStream(gzOut);

			addFileToTarGz(tOut, directoryPath, "");
		} finally {
			if(tOut!=null){
				tOut.finish();
				tOut.close();
			}
			if(gzOut!=null)
				gzOut.close();
			if(bOut!=null)
				bOut.close();
			if(fOut!=null)
				fOut.close();
		}
	}

	/**
	 * Creates a tar entry for the path specified with a name built from the
	 * base passed in and the file/directory name. If the path is a directory, a
	 * recursive call is made such that the full directory is added to the tar.
	 * 
	 * @param tOut
	 *            The tar file's output stream
	 * @param path
	 *            The filesystem path of the file/directory being added
	 * @param base
	 *            The base prefix to for the name of the tar file entry
	 * 
	 * @throws IOException
	 *             If anything goes wrong
	 */
	private static void addFileToTarGz(TarArchiveOutputStream tOut,
			String path, String base) throws IOException {
		File f = new File(path);
		String entryName = base + f.getName();
		TarArchiveEntry tarEntry = new TarArchiveEntry(f, entryName);

		tOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
		tOut.putArchiveEntry(tarEntry);

		if (f.isFile()) {
			IOUtils.copy(new FileInputStream(f), tOut);

			tOut.closeArchiveEntry();
		} else {
			tOut.closeArchiveEntry();

			File[] children = f.listFiles();

			if (children != null) {
				for (File child : children) {
					addFileToTarGz(tOut, child.getAbsolutePath(), entryName
							+ "/");
					tOut.flush();
				}
			}
		}
	}
}
