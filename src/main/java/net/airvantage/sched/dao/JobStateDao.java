package net.airvantage.sched.dao;

import java.util.List;

import net.airvantage.sched.app.exceptions.AppException;
import net.airvantage.sched.model.JobDef;
import net.airvantage.sched.model.JobState;

public interface JobStateDao {

    public void saveJobDef(JobDef jobDef) throws AppException;

    public void deleteJobDef(String jobId) throws AppException;
    
    public JobState findJobState(String id) throws AppException;

    public List<JobState> getJobStates() throws AppException;
    
    public void lockJob(String id) throws AppException;

    public void unlockJob(String id) throws AppException;

    public void removeAll() throws AppException;

}
