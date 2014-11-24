package com.lakeside.core;

import org.junit.Test;

import java.io.PrintStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ArgOptionsTest {

    @Test
    public void test() {
        String[] args = new String[]{"-ip=123", "--id=213", "-ic", "sdfd", "-have"};
        Options options = ArgOptionsBuilder.create().parse(args);
        assertEquals(options.get("ip", ""), "123");
        assertEquals(options.get("id", ""), "213");
        assertEquals(options.get("ic", ""), "sdfd");
        assertTrue(options.haveArg("have"));
    }

    @Test
    public void testAddArgument() {
        String[] args = new String[]{"-ip=123", "--id=213", "-ic", "sdfd", "-have"};
        ArgOptions options = new ArgOptions();
        options.addArgument("ip", true, "ip address");
        options.addArgument("id", false, "id of the object");
        options.addArgument("ic", false, "ic of the object");
        options.addArgument("have", false, "have the value in object");
        options.parse(args);
        assertTrue(options.haveArg("have"));
    }

    @Test(expected = RuntimeException.class)
    public void testAddArgumentLackParameter() {
        String[] args = new String[]{"-ip=123", "--id=213", "-ic", "sdfd", "-have"};
        ArgOptions options = ArgOptionsBuilder.create()
                .addArgument("ip", true, "ip address")
                .addArgument("id", false, "id of the object")
                .addArgument("ic", false, "ic of the object")
                .addArgument("have", false, "have the value in object")
                .addArgument("uid", true, "uid of the object")
                .parse(args);
    }

    @Test
    public void testAliasParameter() {
        String[] args = new String[]{"-ip=123", "--userid=213", "-ic", "sdfd", "-have"};
        ArgOptions options = ArgOptionsBuilder.create()
                .addArgument("ip", true, "ip address")
                .addArgument("id", false, "id of the object")
                .addArgument("ic", false, "ic of the object")
                .addArgument("have", false, "have the value in object")
                .addArgument("userid", "uid", true, "user id of the object")
                .parse(args);
        assertEquals("213", options.get("userid"));
        assertEquals("213", options.get("uid"));
    }

    @Test
    public void testAliasParameter1() {
        String[] args = new String[]{"-ip=123", "--uid=213", "-ic", "sdfd", "-have"};
        ArgOptions options = ArgOptionsBuilder.create()
                .addArgument("ip", true, "ip address")
                .addArgument("id", false, "id of the object")
                .addArgument("ic", false, "ic of the object")
                .addArgument("have", false, "have the value in object")
                .addArgument("userid", "uid", true, "user id of the object")
                .parse(args);
        assertEquals("213", options.get("userid"));
        assertEquals("213", options.get("uid"));
    }

    @Test
    public void testNoNameParameter() {
        String[] args = new String[]{"-ip=123", "--userid=213", "-ic", "sdfd", "file.jpg"};
        ArgOptions options = ArgOptionsBuilder.create()
                .addArgument("ip", true, "ip address")
                .addArgument("id", false, "id of the object")
                .addArgument("ic", false, "ic of the object")
                .addArgument("have", false, "have the value in object")
                .addArgument("userid", "uid", true, "user id of the object")
                .parse(args);
        assertEquals("file.jpg", options.get());
    }

    @Test
    public void testPrintParameter() {
        ArgOptions options = ArgOptionsBuilder.create()
                .addArgument("ip", true, "ip address")
                .addArgument("id", false, "id of the object")
                .addArgument("ic", false, "ic of the object")
                .addArgument("have", false, "have the value in object")
                .addArgument("userid", "uid", true, "user id of the object")
                .parse();
        StringOutputStream buffer = new StringOutputStream();
        options.printHelp(new PrintStream(buffer));
        assertTrue(buffer.toString().contains("--userid,--uid"));
    }
}
