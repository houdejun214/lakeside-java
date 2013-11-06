package com.lakeside.core.nativeload;

import java.io.File;
import java.io.IOException;

public interface JniExtractor {

	/**
	 * extract a JNI library to a temporary file
	 * @param libname
	 * @return
	 * @throws IOException
	 */
	public File extractJni(String libname) throws IOException;
}
