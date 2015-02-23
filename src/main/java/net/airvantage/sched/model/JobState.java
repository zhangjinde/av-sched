package net.airvantage.sched.model;

public class JobState {

    private JobConfig config;
    private JobLock lock;

    public JobConfig getConfig() {
        return config;
    }

    public void setConfig(JobConfig config) {
        this.config = config;
    }

    public JobLock getLock() {
        return lock;
    }

    public void setLock(JobLock lock) {
        this.lock = lock;
    }
}
