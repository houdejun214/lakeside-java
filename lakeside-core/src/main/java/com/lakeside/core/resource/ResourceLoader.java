package com.lakeside.core.resource;

import java.io.IOException;

public interface ResourceLoader {

    public ClassLoader getClassLoader();

    public Resource getResource(String resource) throws IOException;
}