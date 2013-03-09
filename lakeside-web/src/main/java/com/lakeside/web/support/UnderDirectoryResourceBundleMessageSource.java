package com.lakeside.web.support;

import java.io.File;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ResourceBundleMessageSource;

import com.lakeside.core.utils.ApplicationResourceUtils;
import com.lakeside.core.utils.PathUtils;

public class UnderDirectoryResourceBundleMessageSource extends ResourceBundleMessageSource {
	
	protected final Logger log = LoggerFactory.getLogger(this.getClass());
	
    public void setMessageDirectory(String directoryPath) {
    	
        File rootDir = new File( ApplicationResourceUtils.getResourceUrl(directoryPath) );
        ArrayList<String> baseNames = new ArrayList<String>();
        String classRoot = ApplicationResourceUtils.getClassRoot();
        iterateScanDirectoryAndAddBaseNames(baseNames, rootDir, classRoot);
        setBasenames(baseNames.toArray(new String[baseNames.size()]));
    }

    private void iterateScanDirectoryAndAddBaseNames(ArrayList<String> baseNames, File directory,String classPath) {
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                iterateScanDirectoryAndAddBaseNames(baseNames, file, classPath);
            } else {
                if (file.getName().endsWith(".properties")) {
                    String filePath = file.getAbsolutePath().replaceAll("\\\\", "/").replaceAll(".properties$", "");
                    filePath = PathUtils.getRelativeFilePath(classPath,filePath);;
                    baseNames.add(filePath);
                }
            }
        }
    }
	
}
