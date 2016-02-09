/*
 * (./) PathUtils.java
 * 
 * (cc) copyright@2010-2011
 * 
 * 
 * this library is all rights reserved , but you can used it for free.
 * if you want more support or functions, please contact with us!
 */
package com.lakeside.core.utils;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

// TODO: Auto-generated Javadoc
/**
 * The Class PathUtils.
 * 
 * the code come from 
 */
public class PathUtils {

    private static final String FOLDER_SEPARATOR = "/";

    private static final String WINDOWS_FOLDER_SEPARATOR = "\\";

    private static final String TOP_PATH = "..";

    private static final String CURRENT_PATH = ".";
	
	/**
     * Determines the relative path of a filename from a base directory.
     * This method is useful in building relative links within pages of
     * a web site.  It provides similar functionality to Anakia's
     * <code>$relativePath</code> context variable.  The arguments to
     * this method may contain either forward or backward slashes as
     * file separators.  The relative path returned is formed using
     * forward slashes as it is expected this path is to be used as a
     * link in a web page (again mimicking Anakia's behavior).
     * <p/>
     * This method is thread-safe.
     * <br/>
     * <pre>
     * PathTool.getRelativePath( null, null )                                   = ""
     * PathTool.getRelativePath( null, "/usr/local/java/bin" )                  = ""
     * PathTool.getRelativePath( "/usr/local/", null )                          = ""
     * PathTool.getRelativePath( "/usr/local/", "/usr/local/java/bin" )         = ".."
     * PathTool.getRelativePath( "/usr/local/", "/usr/local/java/bin/java.sh" ) = "../.."
     * PathTool.getRelativePath( "/usr/local/java/bin/java.sh", "/usr/local/" ) = ""
     * </pre>
     *
     * @param basedir The base directory.
     * @param filename The filename that is relative to the base
     * directory.
     * @return The relative path of the filename from the base
     * directory.  This value is not terminated with a forward slash.
     * A zero-length string is returned if: the filename is not relative to
     * the base directory, <code>basedir</code> is null or zero-length,
     * or <code>filename</code> is null or zero-length.
     */
   public static final String getRelativePath( String basedir, String filename )
   {
       basedir = uppercaseDrive(basedir);
       filename = uppercaseDrive(filename);
 
       /*
         * Verify the arguments and make sure the filename is relative
         * to the base directory.
         */
       if ( basedir == null || basedir.length() == 0 || filename == null
           || filename.length() == 0 || !filename.startsWith( basedir ) )
       {
           return "";
       }
 
       /*
         * Normalize the arguments.  First, determine the file separator
         * that is being used, then strip that off the end of both the
         * base directory and filename.
         */
       String separator = determineSeparator( filename );
       basedir = StringUtils.chompLast( basedir, separator );
       filename = StringUtils.chompLast( filename, separator );
 
       /*
         * Remove the base directory from the filename to end up with a
         * relative filename (relative to the base directory).  This
         * filename is then used to determine the relative path.
         */
       String relativeFilename = filename.substring( basedir.length() );
 
       return determineRelativePath( relativeFilename, separator );
   }
 
   /**
     * Determines the relative path of a filename.  This method is
     * useful in building relative links within pages of a web site.  It
     * provides similar functionality to Anakia's
     * <code>$relativePath</code> context variable.  The argument to
     * this method may contain either forward or backward slashes as
     * file separators.  The relative path returned is formed using
     * forward slashes as it is expected this path is to be used as a
     * link in a web page (again mimicking Anakia's behavior).
     * <p/>
     * This method is thread-safe.
     *
     * @param filename The filename to be parsed.
     * @return The relative path of the filename. This value is not
     * terminated with a forward slash.  A zero-length string is
     * returned if: <code>filename</code> is null or zero-length.
     * @see #getRelativeFilePath(String, String)
     */
   public static final String getRelativePath( String filename )
   {
       filename = uppercaseDrive(filename);
 
       if ( filename == null || filename.length() == 0 )
       {
           return "";
       }
 
       /*
         * Normalize the argument.  First, determine the file separator
         * that is being used, then strip that off the end of the
         * filename.  Then, if the filename doesn't begin with a
         * separator, add one.
         */
 
       String separator = determineSeparator( filename );
       filename = StringUtils.chompLast( filename, separator );
       if ( !filename.startsWith( separator ) )
       {
           filename = separator + filename;
       }
 
       return determineRelativePath( filename, separator );
   }
 
   /**
     * Determines the directory component of a filename.  This is useful
     * within DVSL templates when used in conjunction with the DVSL's
     * <code>$context.getAppValue("infilename")</code> to get the
     * current directory that is currently being processed.
     * <p/>
     * This method is thread-safe.
     * <br/>
     * <pre>
     * PathTool.getDirectoryComponent( null )                                   = ""
     * PathTool.getDirectoryComponent( "/usr/local/java/bin" )                  = "/usr/local/java"
     * PathTool.getDirectoryComponent( "/usr/local/java/bin/" )                 = "/usr/local/java/bin"
     * PathTool.getDirectoryComponent( "/usr/local/java/bin/java.sh" )          = "/usr/local/java/bin"
     * </pre>
     *
     * @param filename The filename to be parsed.
     * @return The directory portion of the <code>filename</code>.  If
     * the filename does not contain a directory component, "." is
     * returned.
     */
   public static final String getDirectoryComponent( String filename )
   {
       if ( filename == null || filename.length() == 0 )
       {
           return "";
       }
 
       String separator = determineSeparator( filename );
       String directory = StringUtils.chomp( filename, separator );
 
       if ( filename.equals( directory ) )
       {
           return ".";
       }
 
       return directory;
   }
 
   /**
    * Calculates the appropriate link given the preferred link and the relativePath of the document.
    * <br/>
    * <pre>
    * PathTool.calculateLink( "/index.html", "../.." )                                        = "../../index.html"
    * PathTool.calculateLink( "http://plexus.codehaus.org/plexus-utils/index.html", "../.." ) = "http://plexus.codehaus.org/plexus-utils/index.html"
    * PathTool.calculateLink( "/usr/local/java/bin/java.sh", "../.." )                        = "../../usr/local/java/bin/java.sh"
    * PathTool.calculateLink( "../index.html", "/usr/local/java/bin" )                        = "/usr/local/java/bin/../index.html"
    * PathTool.calculateLink( "../index.html", "http://plexus.codehaus.org/plexus-utils" )    = "http://plexus.codehaus.org/plexus-utils/../index.html"
    * </pre>
    *
    * @param link the link
    * @param relativePath the relative path
    * @return String
    */
   public static final String calculateLink(String link, String relativePath)
   {
       //This must be some historical feature
       if (link.startsWith("/site/"))
       {
           return link.substring(5);
       }
 
       //Allows absolute links in nav-bars etc
       if (link.startsWith("/absolute/"))
       {
           return link.substring(10);
       }
 
       // This traps urls like http://
       if (link.indexOf(":") >= 0)
       {
           return link;
       }
 
       //If relativepath is current directory, just pass the link through
       if (relativePath.equals("."))
       {
           if (link.startsWith("/"))
           {
               return link.substring(1);
           }
 
           return link;
       }
 
       //If we don't do this, you can end up with ..//bob.html rather than ../bob.html
       if (relativePath.endsWith("/") && link.startsWith("/"))
       {
           return relativePath + "." + link.substring(1);
       }
 
       if (relativePath.endsWith("/") || link.startsWith("/"))
       {
           return relativePath + link;
       }
 
       return relativePath + "/" + link;
   }
 
   /**
    * This method can calculate the relative path between two pathes on a web site.
    * <br/>
    * <pre>
    * PathTool.getRelativeWebPath( null, null )                                          = ""
    * PathTool.getRelativeWebPath( null, "http://plexus.codehaus.org/" )                 = ""
    * PathTool.getRelativeWebPath( "http://plexus.codehaus.org/", null )                 = ""
    * PathTool.getRelativeWebPath( "http://plexus.codehaus.org/",
    * "http://plexus.codehaus.org/plexus-utils/index.html" )        = "plexus-utils/index.html"
    * PathTool.getRelativeWebPath( "http://plexus.codehaus.org/plexus-utils/index.html",
    * "http://plexus.codehaus.org/"                                 = "../../"
    * </pre>
    *
    * @param oldPath the old path
    * @param newPath the new path
    * @return a relative web path from <code>oldPath</code>.
    */
   public static final String getRelativeWebPath( final String oldPath, final String newPath )
   {
       if ( StringUtils.isEmpty( oldPath ) || StringUtils.isEmpty( newPath ) )
       {
           return "";
       }
 
       String resultPath = buildRelativePath( newPath, oldPath, '/' );
 
       if ( newPath.endsWith( "/" ) && !resultPath.endsWith( "/" ) )
       {
           return resultPath + "/";
       }
 
       return resultPath;
   }
 
   /**
    * This method can calculate the relative path between two pathes on a file system.
    * <br/>
    * <pre>
    * PathTool.getRelativeFilePath( null, null )                                   = ""
    * PathTool.getRelativeFilePath( null, "/usr/local/java/bin" )                  = ""
    * PathTool.getRelativeFilePath( "/usr/local", null )                           = ""
    * PathTool.getRelativeFilePath( "/usr/local", "/usr/local/java/bin" )          = "java/bin"
    * PathTool.getRelativeFilePath( "/usr/local/java/bin", "/usr/local/" )         = "../.."
    * PathTool.getRelativeFilePath( "/usr/local/", "/usr/local/java/bin/java.sh" ) = "java/bin/java.sh"
    * PathTool.getRelativeFilePath( "/usr/local/java/bin/java.sh", "/usr/local/" ) = "../../.."
    * PathTool.getRelativeFilePath( "/usr/local/", "/bin" )                        = "../../bin"
    * PathTool.getRelativeFilePath( "/bin", "/usr/local/" )                        = "../usr/local"
    * </pre>
    * Note: On Windows based system, the <code>/</code> character should be replaced by <code>\</code> character.
    *
    * @param oldPath the old path
    * @param newPath the new path
    * @return a relative file path from <code>oldPath</code>.
    */
   public static final String getRelativeFilePath( final String oldPath, final String newPath )
   {
       if ( StringUtils.isEmpty( oldPath ) || StringUtils.isEmpty( newPath ) )
       {
           return "";
       }
 
       // normalise the path delimiters
       String fromPath = new File( oldPath ).getPath();
       String toPath = new File( newPath ).getPath();
 
       // strip any leading slashes if its a windows path
       if ( toPath.matches( "^\\[a-zA-Z]:" ) )
       {
           toPath = toPath.substring( 1 );
       }
       if ( fromPath.matches( "^\\[a-zA-Z]:" ) )
       {
           fromPath = fromPath.substring( 1 );
       }
 
       // lowercase windows drive letters.
       if ( fromPath.startsWith( ":", 1 ) )
       {
           fromPath = Character.toLowerCase( fromPath.charAt( 0 ) ) + fromPath.substring( 1 );
       }
       if ( toPath.startsWith( ":", 1 ) )
       {
           toPath = Character.toLowerCase( toPath.charAt( 0 ) ) + toPath.substring( 1 );
       }
 
       // check for the presence of windows drives. No relative way of
       // traversing from one to the other.
       if ( ( toPath.startsWith( ":", 1 ) && fromPath.startsWith( ":", 1 ) )
                       && ( !toPath.substring( 0, 1 ).equals( fromPath.substring( 0, 1 ) ) ) )
       {
           // they both have drive path element but they dont match, no
           // relative path
           return null;
       }
 
       if ( ( toPath.startsWith( ":", 1 ) && !fromPath.startsWith( ":", 1 ) )
                       || ( !toPath.startsWith( ":", 1 ) && fromPath.startsWith( ":", 1 ) ) )
       {
           // one has a drive path element and the other doesnt, no relative
           // path.
           return null;
       }
 
       String resultPath = buildRelativePath( toPath, fromPath, File.separatorChar );
 
       if ( newPath.endsWith( File.separator ) && !resultPath.endsWith( File.separator ) )
       {
           return resultPath + File.separator;
       }
 
       return resultPath;
   }
 
   // ----------------------------------------------------------------------
   // Private methods
   // ----------------------------------------------------------------------
 
   /**
     * Determines the relative path of a filename.  For each separator
     * within the filename (except the leading if present), append the
     * "../" string to the return value.
     *
     * @param filename The filename to parse.
     * @param separator The separator used within the filename.
     * @return The relative path of the filename.  This value is not
     * terminated with a forward slash.  A zero-length string is
     * returned if: the filename is zero-length.
     */
   private static final String determineRelativePath( String filename,
                                                      String separator )
   {
       if ( filename.length() == 0 )
       {
           return "";
       }
 
 
       /*
         * Count the slashes in the relative filename, but exclude the
         * leading slash.  If the path has no slashes, then the filename
         * is relative to the current directory.
         */
       int slashCount = StringUtils.countMatches( filename, separator ) - 1;
       if ( slashCount <= 0 )
       {
           return ".";
       }
 
       /*
         * The relative filename contains one or more slashes indicating
         * that the file is within one or more directories.  Thus, each
         * slash represents a "../" in the relative path.
         */
       StringBuffer sb = new StringBuffer();
       for ( int i = 0; i < slashCount; i++ )
       {
           sb.append( "../" );
       }
 
       /*
         * Finally, return the relative path but strip the trailing
         * slash to mimic Anakia's behavior.
         */
       return StringUtils.chop( sb.toString() );
   }
 
   /**
     * Helper method to determine the file separator (forward or
     * backward slash) used in a filename.  The slash that occurs more
     * often is returned as the separator.
     *
     * @param filename The filename parsed to determine the file
     * separator.
     * @return The file separator used within <code>filename</code>.
     * This value is either a forward or backward slash.
     */
   private static final String determineSeparator( String filename )
   {
       int forwardCount = StringUtils.countMatches( filename, "/" );
       int backwardCount = StringUtils.countMatches( filename, "\\" );
 
       return forwardCount >= backwardCount ? "/" : "\\";
   }
 
   /**
    * Cygwin prefers lowercase drive letters, but other parts of maven use uppercase.
    *
    * @param path the path
    * @return String
    */
   static final String uppercaseDrive(String path)
   {
       if (path == null)
       {
           return null;
       }
       if (path.length() >= 2 && path.charAt(1) == ':')
       {
           path = Character.toUpperCase( path.charAt( 0 ) ) + path.substring( 1 );
       }
       return path;
   }
 
   /**
    * Builds the relative path.
    *
    * @param toPath the to path
    * @param fromPath the from path
    * @param separatorChar the separator char
    * @return the string
    */
   private static final String buildRelativePath( String toPath,  String fromPath, final char separatorChar )
   {
       // use tokeniser to traverse paths and for lazy checking
       StringTokenizer toTokeniser = new StringTokenizer( toPath, String.valueOf( separatorChar ) );
       StringTokenizer fromTokeniser = new StringTokenizer( fromPath, String.valueOf( separatorChar ) );
 
       int count = 0;
 
       // walk along the to path looking for divergence from the from path
       while ( toTokeniser.hasMoreTokens() && fromTokeniser.hasMoreTokens() )
       {
           if ( separatorChar == '\\' )
           {
               if ( !fromTokeniser.nextToken().equalsIgnoreCase( toTokeniser.nextToken() ) )
               {
                   break;
               }
           }
           else
           {
               if ( !fromTokeniser.nextToken().equals( toTokeniser.nextToken() ) )
               {
                   break;
               }
           }
 
           count++;
       }
 
       // reinitialise the tokenisers to count positions to retrieve the
       // gobbled token
 
       toTokeniser = new StringTokenizer( toPath, String.valueOf( separatorChar ) );
       fromTokeniser = new StringTokenizer( fromPath, String.valueOf( separatorChar ) );
 
       while ( count-- > 0 )
       {
           fromTokeniser.nextToken();
           toTokeniser.nextToken();
       }

       StringBuilder relativePath = new StringBuilder("");

       // add back refs for the rest of from location.
       while ( fromTokeniser.hasMoreTokens() )
       {
           fromTokeniser.nextToken();
 
           relativePath.append("..");
 
           if ( fromTokeniser.hasMoreTokens() )
           {
               relativePath.append(separatorChar);
           }
       }
 
       if ( relativePath.length() != 0 && toTokeniser.hasMoreTokens() )
       {
           relativePath.append(separatorChar);
       }
 
       // add fwd fills for whatevers left of newPath.
       while ( toTokeniser.hasMoreTokens() )
       {
           relativePath.append(toTokeniser.nextToken());
 
           if ( toTokeniser.hasMoreTokens() )
           {
               relativePath.append(separatorChar);
           }
       }
       return relativePath.toString();
   }

   /**
	 * get the path string, the result would be a reasonable path string 
	 * which be in accord with current system
	 * @param path
	 * @return
	 */
	public static String getPath(String path){
		if(StringUtils.isEmpty(path)){
			return "";
		}
		return Pattern.compile("(\\\\|\\/)+").matcher(path).replaceAll("/");
	}
	
	/**
	 * get the file's name of the specify path
	 * @param path
	 * @return
	 */
	public static String getFileName(String path){
		path =getPath(path);
		int lastDirPos = path.lastIndexOf("/");
		if(lastDirPos ==-1 ){
			return path;
		}
		int end = path.length();
		int nameLast = path.lastIndexOf("?");
		if(nameLast != -1){
			end = nameLast;
		}
		return path.substring(lastDirPos+1,end);
	}

	/**
	 *  get the specify path file's name which not contain extension name
	 * @param path
	 * @return
	 */
	public static String getFileNameWithoutExtension(String path){
		String fileName=getFileName(path);
		int extPos=fileName.lastIndexOf(".");
		if(extPos<=0){
			return fileName;
		}
		return fileName.substring(0,extPos);
	}
	
	/**
	 * change the file path by replacing the extension with new
	 * @param path
	 * @param newExtension
	 * @return
	 */
	public static String changeFileExtension(String path,String newExtension){
		if(path==null){
			return "";
		}
        if(StringUtils.isEmpty(newExtension)){
            return path;
        }
		int extPos=path.lastIndexOf(".");
		if(extPos<0){
			return path+"."+newExtension;
		}
		return path.substring(0,extPos)+"."+newExtension;
	}
	
	/**
	 * append a suffix string to the file name
	 * 
	 * for: name.txt, append _en ==> name_en.txt
	 *  
	 * 
	 * @param path
	 * @param suffix
	 * @return
	 */
	public static String appendFileNameSuffix(String path,String suffix){
		if(path==null){
			return "";
		}
		int extPos=path.lastIndexOf(".");
		if(extPos<0){
			return path+suffix;
		}
		return path.substring(0,extPos)+suffix+path.substring(extPos);
	}
	
	/**
	 * @param path
	 * @return
	 */
	public static String getExtension(String path){
		String fileName=getFileName(path);
		int extPos=fileName.lastIndexOf(".");
		if(extPos<0){
			return "";
		}
		return fileName.substring(extPos+1);
	}
   
   /**
    * get the parent path of current path which maybe is a file path or a directory path
    * @param path
    */
   public static String getParentPath(String path){
	   if(path==null || path.trim().equals("")){
		   return "";
	   }
	   path = getPath(path);
	   if(path.endsWith("/")){
		   path = path.substring(0,path.length()-1);
	   }
	   int lastindex=path.lastIndexOf("/");
	   if(lastindex!=-1){
		   return path.substring(0,lastindex);
	   }
	   return path;
   }

    /**
     * Normalize the path by suppressing sequences like "path/.." and
     * inner simple dots.
     * <p>The result is convenient for path comparison. For other uses,
     * notice that Windows separators ("\") are replaced by simple slashes.
     * @param path the original path
     * @return the normalized path
     */
    public static String cleanPath(String path) {
        if (path == null) {
            return null;
        }
        String pathToUse = StringUtils.replace(path, WINDOWS_FOLDER_SEPARATOR, FOLDER_SEPARATOR);

        // Strip prefix from path to analyze, to not treat it as part of the
        // first path element. This is necessary to correctly parse paths like
        // "file:core/../core/io/Resource.class", where the ".." should just
        // strip the first "core" directory while keeping the "file:" prefix.
        int prefixIndex = pathToUse.indexOf(":");
        String prefix = "";
        if (prefixIndex != -1) {
            prefix = pathToUse.substring(0, prefixIndex + 1);
            pathToUse = pathToUse.substring(prefixIndex + 1);
        }
        if (pathToUse.startsWith(FOLDER_SEPARATOR)) {
            prefix = prefix + FOLDER_SEPARATOR;
            pathToUse = pathToUse.substring(1);
        }

        String[] pathArray = StringUtils.tokenizeToStringArray(pathToUse, FOLDER_SEPARATOR);
        List<String> pathElements = new LinkedList<String>();
        int tops = 0;

        for (int i = pathArray.length - 1; i >= 0; i--) {
            String element = pathArray[i];
            if (CURRENT_PATH.equals(element)) {
                // Points to current directory - drop it.
            }
            else if (TOP_PATH.equals(element)) {
                // Registering top path found.
                tops++;
            }
            else {
                if (tops > 0) {
                    // Merging path element with element corresponding to top path.
                    tops--;
                }
                else {
                    // Normal path element found.
                    pathElements.add(0, element);
                }
            }
        }
        // Remaining top paths need to be retained.
        for (int i = 0; i < tops; i++) {
            pathElements.add(0, TOP_PATH);
        }
        return prefix + StringUtils.join(pathElements, FOLDER_SEPARATOR);
    }

    /**
     * Apply the given relative path to the given path,
     * assuming standard Java folder separation (i.e. "/" separators).
     * @param path the path to start from (usually a full file path)
     * @param relativePath the relative path to apply
     * (relative to the full file path above)
     * @return the full file path that results from applying the relative path
     */
    public static String applyRelativePath(String path, String relativePath) {
        int separatorIndex = path.lastIndexOf(FOLDER_SEPARATOR);
        if (separatorIndex != -1) {
            String newPath = path.substring(0, separatorIndex);
            if (!relativePath.startsWith(FOLDER_SEPARATOR)) {
                newPath += FOLDER_SEPARATOR;
            }
            return newPath + relativePath;
        }
        else {
            return relativePath;
        }
    }

}
