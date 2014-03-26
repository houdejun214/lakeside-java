package com.lakeside.core;

import org.junit.Assert;
import org.junit.Test;

public class StateEnumTest {

	@Test
	public void test() {
		Assert.assertEquals(0,RunState.Init.getValue());
		Assert.assertEquals(1,RunState.Start.getValue());
		Assert.assertEquals(2,RunState.End.getValue());
	}

	@Test
	public void testParse() {
		Assert.assertEquals(RunState.Init,RunState.getState(RunState.class, 0));
		Assert.assertEquals(RunState.Start,RunState.getState(RunState.class, 1));
		Assert.assertEquals(RunState.End,RunState.getState(RunState.class, 2));
		Assert.assertNotEquals(RunState.End,RunState.getState(RunState.class, 1));
	}

	@Test
	public void testParseName() {
		Assert.assertEquals(RunState.Init,RunState.getState(RunState.class, "init"));
		Assert.assertEquals(RunState.Start,RunState.getState(RunState.class, "start"));
		Assert.assertEquals(RunState.End,RunState.getState(RunState.class, "end"));
		Assert.assertNotEquals(RunState.End,RunState.getState(RunState.class, "start"));
	}
	
	private static class RunState extends StateEnum {
		static RunState Init = new RunState("init",0);
		static RunState Start = new RunState("start",1);
		static RunState End = new RunState("end",2);
		protected RunState(String name, int value) {
			super(name, value);
		}
	}
}
