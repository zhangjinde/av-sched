package net.airvantage.sched.model;


public class JobSchedulingDef {
    
    private JobSchedulingType type;
    private String value;
    private long timeout; 
    
    public JobSchedulingType getType() {
        return type;
    }

    public void setType(JobSchedulingType type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
    
    @Override
    public String toString() {
        return "{" + type + "/" + value + " }";
    }

}
