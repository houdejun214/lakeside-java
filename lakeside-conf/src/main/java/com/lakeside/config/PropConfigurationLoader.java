package com.lakeside.config;


import com.lakeside.core.utils.ApplicationResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * properties files base configuration loader
 */
public class PropConfigurationLoader {

    private static Logger logger = LoggerFactory.getLogger(PropConfigurationLoader.class);

    /**
     * load configuration from a path,
     * the path can be a relative path or a absolute path.
     * @param path
     * @return
     */
    public static Configuration load(String path) {
        String absolutePath = ApplicationResourceUtils.getResourceUrl(path);
        // load xml config file
        File file = new File(absolutePath);
        InputStream stream = null;
        try {
            if (!file.exists()) {
                stream = ApplicationResourceUtils.getResourceStream(path);
            } else {
                stream = new FileInputStream(file);
            }
            return load(stream);
        } catch(Exception e){
            // exception raised when parse the config files
            throw new RuntimeException(e);
        }finally {
            if(stream!=null){
                try {
                    stream.close();
                } catch (IOException e) {
                }
            }
        }


    }

    /**
     * load configuration from input stream
     * @param inputStream
     * @return
     */
    public static  Configuration load(InputStream inputStream) {
        Configuration config = new Configuration();
        try {
            if(inputStream!=null) {
                Properties prop = new Properties();
                prop.load(inputStream);
                for (Object key : prop.keySet()) {
                    config.put(key.toString(), prop.get(key).toString());
                }
            }
        } catch (Exception e) {
            logger.error("load config file {} exception", e);
        }
        return config;
    }
}
