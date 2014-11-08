package com.lakeside.config;

import com.lakeside.core.utils.ApplicationResourceUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * xml files base configuration loader
 */
public class XmlConfigurationLoader {
	
	private static Map<Integer,Configuration> configs = new HashMap<Integer,Configuration>();

	public static Configuration load(String path){
		int code = path.hashCode();
		Configuration conf = configs.get(code);
		if(conf!=null){
			return conf;
		}
		synchronized(configs){
		  if(!configs.containsKey(code)){
			conf = new Configuration();
			try {
				String absolutePath = ApplicationResourceUtils.getResourceUrl(path);
				// load xml config file 
				SAXReader reader = new SAXReader();
				File file = new File(absolutePath);
				Document document = null;
				if(!file.exists()){
					InputStream stream = ApplicationResourceUtils.getResourceStream(path);
					document = reader.read(stream);
				}else{
					document = reader.read(file);
				}
		        Element root = document.getRootElement();
		        Iterator<?> iterator = root.elementIterator();
		        while(iterator.hasNext()){
		        	//property
		        	Element next = (Element)iterator.next();
		        	// add site specified conf
		        	String name="";
		        	String value="";
		        	Element nameEl = next.element("name");
		    		if(name!=null){
		    			name = (nameEl.getTextTrim());
		    		}
		    		Element valueEl = next.element("value");
		    		if(value!=null){
		    			value = (valueEl.getTextTrim());
		    		}
		    		conf.put(name, value);
		        }
		        configs.put(code, conf);
			} catch (DocumentException e) {
				throw new RuntimeException(e);
			}
		  }
		}
		return conf;
	}
}
