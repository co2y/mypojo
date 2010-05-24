
package erwins.util.tracker;

import java.util.Date;

import erwins.util.root.DomainObject;

/** 나노초(ns):1/1,000,000,000초
 *  밀리초(ms):1/1,000초
 *  */
public class Tracker implements DomainObject{
    
    private final String className;
    private final String methodName;
    private int count;
    private long maxTime;
    private long minTime;
    private long totalTime;
    private Date lastTime;
    
    public Tracker(String className,String methodName){
    	this.className = className;
    	this.methodName = methodName; 
    }
    
    public void addTime(long time) {
    	count++;
    	totalTime+= time;
    	if(maxTime<time) maxTime = time;
    	if(minTime==0 || minTime>time) minTime = time;
    	lastTime = new Date();
    }
    
	public String getClassName() {
		return className;
	}
	public String getMethodName() {
		return methodName;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public long getMaxTime() {
		return maxTime;
	}
	public long getMaxMs() {
		return maxTime / 1000000;
	}
	public void setMaxTime(long maxTime) {
		this.maxTime = maxTime;
	}
	public long getMinTime() {
		return minTime;
	}
	public long getMinMs() {
		return minTime / 1000000;
	}
	public void setMinTime(long minTime) {
		this.minTime = minTime;
	}
	public long getTotalTime() {
		return totalTime;
	}
	public long getAverageMs() {
		return totalTime / 1000000 / count;
	}
	public void setTotalTime(long totalTime) {
		this.totalTime = totalTime;
	}
	public Date getLastTime() {
		return lastTime;
	}
	public void setLastTime(Date lastTime) {
		this.lastTime = lastTime;
	}
    
    
    
}
