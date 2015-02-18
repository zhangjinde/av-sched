package net.airvantage.sched.dao;

import net.airvantage.sched.model.JobDef;
import net.airvantage.sched.model.JobState;

public interface JobStateDao {

    public void saveJobDef(JobDef jobDef);
    
    public JobState findJobState(String id);
    
    public void lockJob(String id);
}
