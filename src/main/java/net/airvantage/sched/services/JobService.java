package net.airvantage.sched.services;

import net.airvantage.sched.app.exceptions.AppException;
import net.airvantage.sched.model.JobDef;
import net.airvantage.sched.model.JobId;

public interface JobService {

    public void scheduleJob(JobDef jobDef) throws AppException;

    public boolean unscheduleJob(JobId jobId) throws AppException;

    public void ackJob(String jobId) throws AppException;

    public boolean triggerJob(String jobId) throws AppException;
    
    public void clean() throws AppException;
}
