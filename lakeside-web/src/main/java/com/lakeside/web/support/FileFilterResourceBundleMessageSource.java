package com.lakeside.web.support;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;

import javax.servlet.ServletContext;

import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.util.StringUtils;
import org.springframework.web.context.ServletContextAware;

/**
 * ReloadableResourceBundleMessageSource that checks a directory
 * tree structure for files of a certain filetype, and then attempts 
 * to load bundles with the same filename as the discovered files, 
 * but with .properties or .xml file extensions. 
 * 
 * For example, setting the filetype to ".jsp" and the directory
 * to "/WEB-INF/jsp/" will make the system look for .jsp files in
 * that directory. For every discovered .jsp file, the system will attempt
 * to load a property or XML file with the same filename as the discovered
 * .jsp file. 
 * 
 * In practise, the ReloadableResourceBundleMessageSource class will
 * have a basename property per file found with the specified extension.
 * 
 * Discovered files (for example .jsp's) without messageBundles are 
 * simply ignored.
 * 
 * The system can therefore detect changes in existing resource bundles
 * without a restart, but new files can not be detected automatically.
 * 
 * A new scan can be forced by calling "forceNewScan()". A scan will then
 * be rerun the next time a key is requested.
 * 
 * @author Daniel Henriksson, mail@setomidor.com 
 * 
 */
public class FileFilterResourceBundleMessageSource extends
	ReloadableResourceBundleMessageSource implements ServletContextAware {

    /**
     * False if the system should scan for existing files
     */
    private boolean scanned = false;

    /**
     * Reference to ServletContext
     */
    private ServletContext context;

    /**
     * The directory where the scan starts
     */
    private String directory;

    /**
     * The filetype to look for
     */
    private String filetype;

    public void setDirectory(String directory) {
	this.directory = directory;
    }

    public void setServletContext(ServletContext context) {
	this.context = context;
    }

    public void setFiletype(String filetype) {
	this.filetype = filetype;
    }

    @Override
    protected String resolveCodeWithoutArguments(String code, Locale locale) {
	attemptScan();
	return super.resolveCodeWithoutArguments(code, locale);
    }

    @Override
    protected MessageFormat resolveCode(String code, Locale locale) {
	attemptScan();
	return super.resolveCode(code, locale);
    }

    @Override
    protected PropertiesHolder getMergedProperties(Locale locale) {
	attemptScan();
	return super.getMergedProperties(locale);
    }

    /**
     * Forces a new scan the next time a key is requested.
     */
    protected synchronized void forceNewScan() {
	scanned = false;
    }

    /**
     * Checks if a scan is required.
     */
    private synchronized void attemptScan() {
	if (scanned) {
	    return;
	}
	scanDirectory();
	scanned = true;
    }

    /**
     * Scans the directory specified in this bean.
     */
    private void scanDirectory() {

	if (context == null) {
	    throw new IllegalStateException("ServletContext not set for "
		    + this.getClass().getName());
	}

	Vector<String> baseNames = new Vector<String>();
	traverse(directory, baseNames);

	String[] generatedBasenames = new String[baseNames.size()];
	baseNames.toArray(generatedBasenames);

	if (logger.isDebugEnabled()) {
	    logger.debug("Runnig with bundles:");
	    logger.debug(StringUtils
		    .arrayToCommaDelimitedString(generatedBasenames));
	}

	super.setBasenames(generatedBasenames);
    }

    /**
     * Traverse an url, and all discovered subpaths of this url recursivly.
     */
    private void traverse(String url, Vector<String> baseNames) {

	if (logger.isDebugEnabled()) {
	    logger.debug("Traversing: " + url);
	}

	Set paths = context.getResourcePaths(url);

	if (paths != null) {
	    for (Object path : paths) {
		traverse((String) path, baseNames);
	    }
	}

	addFile(url, baseNames);
    }

    /**
     * Adds a filename (minus suffix) to the list of baseNames,
     * if the file ends with the expected ending.
     */
    private void addFile(String url, Vector<String> baseNames) {
	if (!url.endsWith(filetype)) {
	    return;
	}

	baseNames.add(removeFileType(url));

    }

    /**
     * Removes the file ending
     */
    private String removeFileType(String filename) {
	int index = filename.lastIndexOf('.');
	if (index == -1) {
	    throw new IllegalArgumentException("Filename without '.' "
		    + "character: '" + filename + "'");
	}

	return filename.substring(0, index);
    }
}