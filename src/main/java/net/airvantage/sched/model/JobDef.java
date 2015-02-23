package net.airvantage.sched.model;

public class JobDef {

    private JobConfig config;
    private JobSchedulingDef scheduling;

    public JobConfig getConfig() {
        return config;
    }

    public void setConfig(JobConfig config) {
        this.config = config;
    }

    public JobSchedulingDef getScheduling() {
        return scheduling;
    }

    public void setScheduling(JobSchedulingDef scheduling) {
        this.scheduling = scheduling;
    }

//    
//    @Override
//    public String toString() {
//        StringBuilder builder = new StringBuilder();
//        builder.append("{ id :" + id);
//        builder.append(", url :" + url);
//        builder.append(", timeout :" + timeout);
//        builder.append(", sched :" + this.scheduling.toString());
//        builder.append(" }");
//        return builder.toString();
//    }
    
}
