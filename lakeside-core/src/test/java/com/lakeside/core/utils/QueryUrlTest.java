package com.lakeside.core.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class QueryUrlTest {

    @Test
    public void testToString() throws Exception {
        QueryUrl queryUrl = new QueryUrl("http:google.com?test=123&test1=12?ajax=true");
        assertEquals("true",queryUrl.getOneParameter("ajax"));
    }

    @Test
    public void testRemoveDuplicated() {
        QueryUrl queryUrl = new QueryUrl("http:google.com?test=123&test1=12?ajax=true&test=1111&test=final&ajax=true");
        queryUrl.removeDuplicated();
        assertEquals(3,queryUrl.getParameterByName("test").size());
        assertEquals(1,queryUrl.getParameterByName("ajax").size());
    }
}