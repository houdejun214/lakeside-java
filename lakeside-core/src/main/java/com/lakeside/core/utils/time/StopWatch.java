package com.lakeside.core.utils.time;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DurationFormatUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * <code>StopWatch</code> provides a convenient API for timings/code step watcher util.
 * </p>
 * 
 * This class is not thread-safe
 * @author houdejun
 *
 */
public class StopWatch {
	private long startTime = 0;
	private long stopTime = 0;
    private boolean running = false;
    private List<Long> stops = new ArrayList<Long>(3);
    
    public StopWatch() {
		this.startTime = System.currentTimeMillis();
		this.stopTime = System.currentTimeMillis();
		this.running = false;
	}

	/**
     * start stopwatch
     * @return
     */
    public StopWatch start() {
        this.startTime = System.currentTimeMillis();
        this.running = true;
        return this;
    }
    
    /**
     * reset stopwatch
     * @return
     */
    public StopWatch reset() {
        this.startTime = System.currentTimeMillis();
        this.running = true;
        return this;
    }
    
    /**
     * stop stopwatch
     * @return
     */
    public StopWatch stop() {
        this.stopTime = System.currentTimeMillis();
        this.running = false;
        return this;
    }
    
    /**
     * save a stop-point for watcher
     * @return
     */
    public StopWatch saveStopPoint(){
    	this.stops.add(System.currentTimeMillis());
    	return this;
    }
    
    @Deprecated
    public TimeSpan getTimeSpan(){
    	long elapsedTime = this.getElapsedTime();
    	TimeSpan span = new TimeSpan(elapsedTime);
    	return span;
    }
    
    /**
     * get elaspsed time in milliseconds
     * @return
     */
    public long getElapsedTime() {
        long elapsed = getTime();
        return elapsed;
    }
    
    /**
     * get elaspsed time in seconds
     * @return
     */
    public long getElapsedTimeSecs() {
        long elapsed = this.getElapsedTime() / 1000;
        return elapsed;
    }
    
    /**
     * get the duration time (Millis);
     * @return
     */
    public long getTime() {
        if (this.running) {
        	return System.currentTimeMillis() - this.startTime;
        } else {
        	long lag = this.stopTime - this.startTime;
        	return lag >0 ? lag : 0;
        }
    }
    
    /**
     * get the duration time (Millis);
     * @return
     */
    private long getTime(long currentTimeMillis) {
        if (this.running) {
        	return currentTimeMillis - this.startTime;
        } else {
        	long lag = this.stopTime - this.startTime;
        	return lag >0 ? lag : 0;
        }
    }
    
    /**
     * create a new StepWatch instance.
     * It will start the watcher automatic.
     * @return
     */
    public static StopWatch newWatch(){
    	StopWatch watcher = new StopWatch().start();
    	return watcher;
    }
    
    /**
     * output as:
     * 551			150			1000		0:00:01.701
     * step1(ms)	step2(ms)	step3(ms)	total duration
     */
    @Override
    public String toString() {
    	long currentTimeMillis = System.currentTimeMillis();
    	if(stops!=null && stops.size()>0){
    		List<Long> durationList = new ArrayList<Long>();
    		long last = this.startTime;
    		for(int i=0;i<stops.size();i++){
    			long cur = stops.get(i);
				durationList.add(cur - last);
				last = cur;
    		}
    		if (this.running) {
				durationList.add(currentTimeMillis - last);
	        } else {
	        	durationList.add(this.stopTime - last);
	        }
    		
    		return StringUtils.join(durationList, "\t")+"\t"+DurationFormatUtils.formatDurationHMS(getTime(currentTimeMillis));
    	}else{
    		return DurationFormatUtils.formatDurationHMS(getTime(currentTimeMillis));
    	}
    }
}
