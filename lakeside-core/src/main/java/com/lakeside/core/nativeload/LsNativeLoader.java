package com.lakeside.core.nativeload;

import java.io.File;
import java.io.IOException;

public class LsNativeLoader {
	 private static JniExtractor jniExtractor = new LsJniExtractor();

	    /**
	     * Extract the given library from a jar, and load it.
	     * <p>
	     * The default jni extractor expects libraries to be in META-INF/jni, with their platform-dependent name.
	     *
	     * @param libname   platform-independent library name (as would be passed to System.loadLibrary)
	     *
	     * @throws IOException if there's a problem extracting the jni library
	     * @throws SecurityException  if a security manager exists and its
	     *             <code>checkLink</code> method doesn't allow
	     *             loading of the specified dynamic library
	     *
	     */
	    public static void loadLibrary(String libname) throws IOException {
	        File lib = jniExtractor.extractJni(libname);
	        System.load(lib.getAbsolutePath());
	    }

	    /**
	     * @return the jniExtractor
	     */
	    public static JniExtractor getJniExtractor() {
	        return jniExtractor;
	    }

	    /**
	     * @param jniExtractor the jniExtractor to set
	     */
	    public static void setJniExtractor(JniExtractor jniExtractor) {
	        LsNativeLoader.jniExtractor = jniExtractor;
	    }
}
