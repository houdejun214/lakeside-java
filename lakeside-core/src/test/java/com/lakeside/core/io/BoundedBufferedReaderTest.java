package com.lakeside.core.io;

import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;

import static org.junit.Assert.*;

public class BoundedBufferedReaderTest {

    @Test
    public void testReadLine() throws Exception {

        InputStream resourceAsStream = BoundedBufferedReaderTest.class.getClassLoader().getResourceAsStream("test_file_bounded_buffer_reader.txt");
        BoundedBufferedReader reader = new BoundedBufferedReader(new InputStreamReader(resourceAsStream));
        String line;
        while ((line = reader.readLine())!=null){
            System.out.println(line);
        }
        reader.close();
    }

    @Test
    public void testReadLineMaxLength() throws Exception {

        InputStream resourceAsStream = BoundedBufferedReaderTest.class.getClassLoader().getResourceAsStream("test_file_bounded_buffer_reader.txt");
        BoundedBufferedReader reader = new BoundedBufferedReader(new InputStreamReader(resourceAsStream),20);
        String line;
        while ((line = reader.readLine())!=null){
            assertTrue(line.length()<=20);
            System.out.println(line);
        }
        reader.close();
    }
}