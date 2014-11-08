package com.lakeside.core.resource;


import com.lakeside.core.utils.Assert;
import com.lakeside.core.utils.ClassUtils;
import com.lakeside.core.utils.ResourceUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;


public class DefaultResourceLoader implements ResourceLoader{
	private ClassLoader classLoader;

	/**
	 * Create a new DefaultResourceLoader.
	 * <p>ClassLoader access will happen using the thread context class loader
	 * at the time of this ResourceLoader's initialization.
	 * @see Thread#getContextClassLoader()
	 */
	public DefaultResourceLoader() {
		this.classLoader = ClassUtils.getDefaultClassLoader();
	}

	/**
	 * Create a new DefaultResourceLoader.
	 * @param classLoader the ClassLoader to load class path resources with, or <code>null</code>
	 * for using the thread context class loader at the time of actual resource access
	 */
	public DefaultResourceLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	/**
	 * Return the ClassLoader to load class path resources with.
	 * <p>Will get passed to ClassPathResource's constructor for all
	 * ClassPathResource objects created by this resource loader.
	 */
	@Override
    public ClassLoader getClassLoader() {
		return (this.classLoader != null ? this.classLoader : ClassUtils.getDefaultClassLoader());
	}

	public Resource getResource(String location) throws IOException {
        Assert.notNull(location, "Location must not be null");
        if (location.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)) {
            return new ClassPathResource(location.substring(ResourceUtils.CLASSPATH_URL_PREFIX.length()), getClassLoader());
        }
        else {
            try {
                // Try to parse the location as a URL...
                URL url = new URL(location);
                return new UrlResource(url);
            }
            catch (MalformedURLException ex) {
                // No URL -> resolve as resource path.
                throw ex;
            }
        }
	}

}
