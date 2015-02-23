package net.airvantage.sched.model;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobLock {
    
    private static final Logger LOG = LoggerFactory.getLogger(JobLock.class);
    
    private boolean locked;
    private Long expiresAt;
    
    public JobLock() {
        locked = false;
        expiresAt = null;
    }
    
    public boolean isLocked() {
        return locked;
    }
    public void setLocked(boolean locked) {
        this.locked = locked;
    }
    
    public Long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Long expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isExpired() {
        return isExpired(new Date());
    }
    
    public boolean isExpired(Date date) {
        LOG.debug("Checking if expired : " + this.expiresAt + " vs " + date.getTime());
        boolean expired = (this.expiresAt != null && this.expiresAt < date.getTime());
        LOG.debug("Is it expired ? " + expired);
        return expired;
    }
    
}
