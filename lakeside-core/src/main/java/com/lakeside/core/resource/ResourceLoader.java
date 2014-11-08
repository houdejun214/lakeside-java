package com.lakeside.core.resource;

import java.io.IOException;
import java.io.InputStream;

public interface ResourceLoader {

    public ClassLoader getClassLoader();

    public Resource getResource(String resource) throws IOException;
}