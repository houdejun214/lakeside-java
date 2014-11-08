package com.lakeside.config;

import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.*;

public class PropConfigurationLoaderTest {

    @Test
    public void testLoad() throws Exception {
        Configuration config = PropConfigurationLoader.load("test.properties");
        assertEquals("testvalue",config.get("test"));

        config = PropConfigurationLoader.load("test1.properties");
        assertEquals(0,config.size());
    }

    @Test
    public void testLoad1() throws Exception {
        InputStream input = PropConfigurationLoaderTest.class.getClassLoader().getResourceAsStream("test.properties");
        Configuration config = PropConfigurationLoader.load(input);
        assertEquals("testvalue",config.get("test"));
    }
}