package com.lakeside.core.utils.time;

import org.joda.time.DateTime;
import org.joda.time.DurationFieldType;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

public class DateTimeUtilsTest {

    @Test
    public void testFormat() throws Exception {
        DateTime time = new DateTime(2014,11,11,23,0,0);
        assertEquals("2014-11-11 23:00:00",DateTimeUtils.format(time.toDate(),"YYYY-MM-dd HH:mm:ss"));
    }

    @Test
    public void testFormatGMT() throws Exception {
        DateTime time = new DateTime(2014,11,11,23,0,0);
        assertEquals("2014-11-11 23:00:00",DateTimeUtils.formatGMT(time.toDate(), "YYYY-MM-dd HH:mm:ss"));
    }

    @Test
    public void testParse() throws Exception {
        DateTime time = new DateTime(2014,11,11,23,0,0);
        assertEquals(time.toDate(),DateTimeUtils.parse("2014-11-11 23:00:00","YYYY-MM-dd HH:mm:ss"));
    }

    @Test
    public void testBetween() throws Exception {

    }

    @Test
    public void testCompareDate() throws Exception {

    }

    @Test
    public void testAdd() throws Exception {
        assertEquals(new DateTime(2014,11,12,23,0,0).toDate(),
                DateTimeUtils.add(new DateTime(2014,11,11,23,0,0).toDate(), DurationFieldType.days(),1));
        assertEquals(new DateTime(2014,12,1,23,0,0).toDate(),
                DateTimeUtils.add(new DateTime(2014,11,30,23,0,0).toDate(), DurationFieldType.days(),1));
        assertEquals(new DateTime(2014,11,29,23,0,0).toDate(),
                DateTimeUtils.add(new DateTime(2014,11,30,23,0,0).toDate(), DurationFieldType.days(),-1));
        assertEquals(new DateTime(2014,10,31,23,0,0).toDate(),
                DateTimeUtils.add(new DateTime(2014,11,1,23,0,0).toDate(), DurationFieldType.days(),-1));
        assertEquals(new DateTime(2014,9,30,23,0,0).toDate(),
                DateTimeUtils.add(new DateTime(2014,10,1,23,0,0).toDate(), DurationFieldType.days(),-1));
    }

    @Test
    public void testGetBetweenTime() throws Exception {

    }

    @Test
    public void testGetTimeFromUnixTime() throws Exception {

    }

    @Test
    public void testGetTimeFromUnixTime1() throws Exception {

    }

    @Test
    public void testGetEndOfDay() throws Exception {
        assertEquals(new DateTime(2014,11,11,23,59,59).toDate(),
                DateTimeUtils.getEndOfDay(new DateTime(2014, 11, 11, 23, 0, 0).toDate()));
    }

    @Test
    public void testGetBeginOfDay() throws Exception {
        assertEquals(new DateTime(2014,11,11,00,00,00).toDate(),
                DateTimeUtils.getBeginOfDay(new DateTime(2014,11,11,23,0,0).toDate()));
    }

    @Test
    public void testGetUnixTime() throws Exception {

    }

    @Test
    public void testGetFirstDayOfWeek() throws Exception {

    }

    @Test
    public void testGetLastDayOfWeek() throws Exception {

    }

    @Test
    public void testGetFirstDayOfMonth() throws Exception {

    }

    @Test
    public void testGetFirstDayOfMonth1() throws Exception {

    }

    @Test
    public void testGetLastDayOfMonth() throws Exception {

    }

    @Test
    public void testGetFirstDayOfQuarter() throws Exception {

    }

    @Test
    public void testGetLastDayOfQuarter() throws Exception {

    }

    @Test
    public void testGetFirstDayOfYear() throws Exception {

    }

    @Test
    public void testGetLastDayOfYear() throws Exception {

    }
}