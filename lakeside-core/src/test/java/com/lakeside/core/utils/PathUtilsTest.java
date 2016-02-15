package com.lakeside.core.utils;

import org.junit.Test;

import static com.lakeside.core.utils.PathUtils.getExtension;
import static com.lakeside.core.utils.PathUtils.getParentPath;
import static com.lakeside.core.utils.PathUtils.join;
import static org.junit.Assert.*;

public class PathUtilsTest {

    @Test
    public void testJoin() throws Exception {
        assertEquals("/root/test.js", join("/root/","test.js"));
        assertEquals("/root/test.js", join("/root","test.js"));
    }

    @Test
    public void testGetParentPath() {
        assertEquals("/root", getParentPath("/root/test.js"));
        assertEquals("/root/dir", getParentPath("/root/dir/test.js"));
    }

    @Test
    public void testGetExtension() {
        assertEquals("js", getExtension("/root/test.js"));
    }

}