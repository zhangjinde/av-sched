package net.airvantage.sched.services;

import net.airvantage.sched.app.AppException;
import net.airvantage.sched.model.jobDef.JobDef;

public interface JobService {

    public void scheduleJob(JobDef jobDef) throws AppException;
    
}
