package com.lakeside.http;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HttpPageTest {
    @Test
    public void testDownloadImage() {
        HttpPageLoader http = HttpPageLoader.getDefaultPageLoader();
        assertEquals(200, http.get("https://s3-ap-southeast-1.amazonaws.com/b40164f5454d2404d6e1fcf7f3e47be4" +
                "/5dc8d3a99e40abe7d6a01e8845002f59.jpg").getStatusCode());

        HttpConfig httpConfig = new HttpConfig();
        httpConfig.setMaxDownloadSize(Integer.MAX_VALUE);
        assertEquals(200, http.withHttpConfig(httpConfig).get("https://farm4.staticflickr.com/3926/14601281968_fb56017803_n.jpg").getStatusCode());
    }
}