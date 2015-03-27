package net.airvantage.sched.model;

public class JobState {

    private JobConfig config;
    private JobLock lock;
    private JobScheduling scheduling;

    public JobConfig getConfig() {
        return config;
    }

    public JobScheduling getScheduling() {
        return scheduling;
    }

    public void setScheduling(JobScheduling scheduling) {
        this.scheduling = scheduling;
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
