package net.airvantage.sched.model;

public class JobDef {

    private JobConfig config;
    private JobScheduling scheduling;

    public JobConfig getConfig() {
        return config;
    }

    public void setConfig(JobConfig config) {
        this.config = config;
    }

    public JobScheduling getScheduling() {
        return scheduling;
    }

    public void setScheduling(JobScheduling scheduling) {
        this.scheduling = scheduling;
    }
    
}
