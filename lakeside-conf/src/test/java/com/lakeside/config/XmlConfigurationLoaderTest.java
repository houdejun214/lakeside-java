package com.lakeside.config;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class XmlConfigurationLoaderTest {
    @Test
    public void testLoad() throws Exception {
        Configuration config = XmlConfigurationLoader.load("test.xml");
        assertEquals("testvalue",config.get("test"));
        assertEquals(1,config.size());
    }
}