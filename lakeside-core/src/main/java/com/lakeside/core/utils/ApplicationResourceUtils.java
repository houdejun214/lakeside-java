package com.lakeside.core.utils;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;

public class ApplicationResourceUtils {
	
	private static String applicationRoot;
	
	private static String applicationClassPathRoot="";
	
	static {
		// get the root directory of current application
		File current = new File(".");
		String dir = current.getAbsolutePath();
		applicationRoot = PathUtils.getParentPath(dir);
		// get the root path of classpath;
		ClassLoader defaultClassLoader = ClassUtils.getDefaultClassLoader();
		URL resource = defaultClassLoader.getResource("");
		if(resource!=null){
			applicationClassPathRoot = URLDecoder.decode(resource.getPath());
			if(isWindows()&& applicationClassPathRoot.startsWith("/")){
				applicationClassPathRoot = applicationClassPathRoot.substring(1);
			}
		}
	}
	
	/**
	 * get the resource file url that is relative to the current application directory if it is a relative path
	 * 
	 * @param path
	 * @return
	 */
	public static String getResourceUrl(String path){
		File file=new File(path);
		if(file.isAbsolute()){
			return path;
		}else{
			if(!StringUtils.isEmpty(applicationClassPathRoot)){
				String url = PathUtils.getPath(applicationClassPathRoot+"/"+path);
				if(FileUtils.exist(url)){
					return url;
				}
			}
			String url = PathUtils.getPath(applicationRoot+"/"+path);
			return url;
		}
	}
	
	public static String getClassRoot(){
		return applicationClassPathRoot;
	}
	
	public static boolean isWindows() {
		String os = System.getProperty("os.name").toLowerCase();
		// windows
		return (os.indexOf("win") >= 0);
 
	}
}
