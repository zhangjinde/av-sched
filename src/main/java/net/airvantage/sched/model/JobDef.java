package net.airvantage.sched.model;

public class JobDef {

    private String id;
    private String url;
    private JobSchedulingDef scheduling;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public JobSchedulingDef getScheduling() {
        return scheduling;
    }

    public void setScheduling(JobSchedulingDef scheduling) {
        this.scheduling = scheduling;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{ id :" + id);
        builder.append(", url :" + url);
        builder.append(", sched :" + this.scheduling.toString());
        builder.append(" }");
        return builder.toString();
    }
    
}
