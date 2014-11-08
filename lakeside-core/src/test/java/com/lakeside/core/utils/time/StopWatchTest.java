package com.lakeside.core.utils.time;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StopWatchTest {

	@Test
	public void testStopWatchSimple() {
		final StopWatch watch = new StopWatch();
		watch.start();
		try {
			Thread.sleep(550);
		} catch (final InterruptedException ex) {
		}
		watch.stop();
		final long time = watch.getTime();
		assertEquals(time, watch.getTime());

		assertTrue(time >= 500);
		assertTrue(time < 700);

		watch.reset();
		assertEquals(0, watch.getTime());
	}
	
	@Test
	public void testSaveStopPoint() {
		final StopWatch watch = StopWatch.newWatch();
		try {
			Thread.sleep(550);
			watch.saveStopPoint();
			Thread.sleep(150);
			watch.saveStopPoint();
			Thread.sleep(1000);
		} catch (final InterruptedException ex) {
		}
		final long time = watch.getTime();
		assertTrue(time >= 1700);
		String info = watch.toString();
		assertTrue(info.split("\t").length==4);
		System.out.println(info);
	}

	@Test
	public void testToString() {
		final StopWatch watch = new StopWatch();
		assertEquals(0, watch.getTime());
		assertEquals("0:00:00.000", watch.toString());

		watch.start();
		try {
			Thread.sleep(500);
		} catch (final InterruptedException ex) {
		}
		assertTrue(watch.getTime() < 2000);
	}

}
