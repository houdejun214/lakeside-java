package com.lakeside.core;

import org.junit.Test;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import static org.junit.Assert.*;

public class ThreadPoolTaskExecutorTest {

    @Test
    public void testSetCorePoolSize() throws Exception {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.initialize(10);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        assertEquals(10, executor.getExecutor().getCorePoolSize());
        executor.shutdown();
    }

    @Test
    public void testSetMaxPoolSize() throws Exception {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setMaxPoolSize(1);
        executor.initialize(10);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        assertEquals(1, executor.getExecutor().getMaximumPoolSize());
        for (int i = 0; i < 11; i++) {
            executor.execute(new PrintTask(i));
        }
        executor.shutdown();
    }

    @Test(expected = RejectedExecutionException.class)
    public void testSetQueueCapacity() throws Exception {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(10);
        executor.initialize(10);
        for (int i = 1; i <= 21; i++) {
            executor.execute(new SleepTask(100));
        }
        executor.shutdown();
    }

    @Test
    public void testSetQueueCapacityWithRejectHandler() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(10);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize(10);
        for (int i = 1; i <= 21; i++) {
            executor.execute(new SleepTask(100));
        }
    }

    @Test
    public void testSetQueueCapacityWithBlock() throws Exception {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.initialize(10,true);
        for (int i = 1; i <= 11; i++) {
            executor.execute(new SleepTask(100));
        }
    }


    @Test
    public void testSetRejectedExecutionHandler() throws Exception {

    }

    @Test
    public void testGetPoolSize() throws Exception {

    }

    private static class PrintTask implements Runnable {
        private final int counter;

        private PrintTask(int counter) {
            this.counter = counter;
        }

        @Override
        public void run() {
            System.out.println(counter);
        }
    }

    private static class SleepTask implements Runnable {
        private final int time;

        private SleepTask(int time) {
            this.time = time;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}