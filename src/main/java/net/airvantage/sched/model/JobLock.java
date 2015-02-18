package net.airvantage.sched.model;

public class JobLock {
    private boolean locked;
    private Long expiration;
    
    public JobLock() {
        locked = false;
        expiration = null;
    }
    
    public boolean isLocked() {
        return locked;
    }
    public void setLocked(boolean locked) {
        this.locked = locked;
    }
    public Long getExpiration() {
        return expiration;
    }
    public void setExpiration(Long expiration) {
        this.expiration = expiration;
    }
    
}
