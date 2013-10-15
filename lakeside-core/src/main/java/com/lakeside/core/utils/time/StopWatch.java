package com.lakeside.core.utils.time;

public class StopWatch {
	private long startTime = 0;
    private long stopTime = 0;
    private boolean running = false;
    
    public StopWatch start() {
        this.startTime = System.currentTimeMillis();
        this.running = true;
        return this;
    }
    
    public StopWatch reset() {
        this.startTime = System.currentTimeMillis();
        this.running = true;
        return this;
    }

    public StopWatch stop() {
        this.stopTime = System.currentTimeMillis();
        this.running = false;
        return this;
    }
    
    //elaspsed time in milliseconds
    public long getElapsedTime() {
        long elapsed;
        if (running) {
             elapsed = (System.currentTimeMillis() - startTime);
        }
        else {
            elapsed = (stopTime - startTime);
        }
        return elapsed;
    }
    
    public TimeSpan getTimeSpan(){
    	long elapsedTime = this.getElapsedTime();
    	TimeSpan span = new TimeSpan(elapsedTime);
    	return span;
    }
    
    
    //elaspsed time in seconds
    public long getElapsedTimeSecs() {
        long elapsed;
        if (running) {
            elapsed = ((System.currentTimeMillis() - startTime) / 1000);
        }
        else {
            elapsed = ((stopTime - startTime) / 1000);
        }
        return elapsed;
    }
}
