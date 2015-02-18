package net.airvantage.sched.model;

public class JobState extends JobDef {
    private JobLock lock;
    
    public JobLock getLock() {
        return lock;
    }
    
    public void setLock(JobLock lock) {
        this.lock = lock;
    }
}
