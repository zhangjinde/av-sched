package net.airvantage.sched.model;

import java.util.Date;

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

    public boolean isExpired() {
        return isExpired(new Date());
    }
    
    public boolean isExpired(Date date) {
        return (this.expiration != null && this.expiration < date.getTime());
    }
    
}
