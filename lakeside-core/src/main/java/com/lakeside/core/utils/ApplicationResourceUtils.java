package com.lakeside.core.utils;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;

/**
 * application resource util.
 * can resolve the resource url(path) related with the current application.
 * @author houdejun
 *
 */
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
	
	public static InputStream getResourceStream(String path){
		ClassLoader defaultClassLoader = ClassUtils.getDefaultClassLoader();
		return defaultClassLoader.getResourceAsStream(path);
	}
	
	/**
	 * get the root directory path for current application.
	 *  will return the root classpath when debug in IDE.
	 *  will return the application directory when launch in real-env.
	 * @return
	 */
	public static String getRoot(){
		return getResourceUrl("/");
	}
	
	/**
	 * return the root classpath
	 * @return
	 */
	public static String getClassRoot(){
		return applicationClassPathRoot;
	}
	
	/**
	 * helper method to check if is windows.
	 * @return
	 */
	private static boolean isWindows() {
		String os = System.getProperty("os.name").toLowerCase();
		// windows
		return (os.indexOf("win") >= 0);
	}
	
	/**
	 * 系统首先从当前application class path 获取文件，如果文件不存在再从baseClass的资源文件中获取文件。最后从当前目录下获取文件。
	 * @param baseClass
	 * @param path
	 * @return
	 */
	public static String getResourceUrl(Class<?> baseClass,String path){
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
			URL resource =  baseClass.getResource("/"+path);
			if(resource!=null && FileUtils.exist(resource.getPath())){
				return resource.getPath();
			}
			String url = PathUtils.getPath(applicationRoot+"/"+path);
			return url;
		}
	}
}
