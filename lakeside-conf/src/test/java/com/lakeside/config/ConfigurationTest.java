package com.lakeside.config;

import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.*;

public class ConfigurationTest {

    private Configuration config;

    @Before
    public void setup() {
        config = new Configuration();
        config.put("String","string value");
        config.put("Long","12");
        config.put("Int","12");
        config.put("Double","1.2");
        config.put("Float","1.2");
        config.put("BigDecimal","1.2");
    }

    @Test
    public void testGetLong() throws Exception {
        assertEquals(Long.valueOf(12), config.getLong("Long"));
    }
    @Test(expected = NumberFormatException.class)
    public void testGetLongException() throws Exception {
        assertNull(config.getLong("String"));
    }


    @Test
    public void testGetInt() throws Exception {
        assertEquals(Integer.valueOf(12), config.getInt("Int", 0));
    }

    @Test(expected = NumberFormatException.class)
    public void testGetIntException() throws Exception {
        assertNull(config.getInt("String", 0));
    }

    @Test
    public void testGetDouble() throws Exception {
        assertEquals(1.2, config.getDouble("Double", 0.0),0);
    }

    @Test
    public void testGetFloat() throws Exception {
        assertEquals(1.2f, config.getFloat("Float"),0);
    }

    @Test
    public void testGetFloat1() throws Exception {
        assertEquals(1.2f, config.getFloat("Float", 0),0);
    }

    @Test
    public void testGetBigDecimal() throws Exception {
        assertEquals(BigDecimal.valueOf(1.2), config.getBigDecimal("BigDecimal"));
    }

    @Test
    public void testGetDate() throws Exception {
        config = new Configuration();
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        config.put("Date", format.format(date));
        assertEquals(date.getTime() / 1000, config.getDate("Date").getTime() / 1000);
    }

    @Test
    public void testGet() throws Exception {
        assertNotNull(config.get("String"));
        assertNull(config.get("String1"));
    }

    @Test
    public void testSize() throws Exception {
        assertEquals(6, config.size());
    }

    @Test
    public void testClear() throws Exception {
        assertEquals(6, config.size());
        assertNotNull(config.get("String"));
        config.clear();
        assertEquals(0, config.size());
        assertNull(config.get("String"));
    }

    @Test
    public void testCombineWith() throws Exception {
        Configuration other = new Configuration();
        assertEquals(6, config.size());
        assertNull(config.get("test"));
        assertEquals("string value", config.get("String"));
        other.put("test","1");
        other.put("String","other");
        config.combineWith(other);
        assertEquals(7, config.size());
        assertNotNull(config.get("test"));
    }
}