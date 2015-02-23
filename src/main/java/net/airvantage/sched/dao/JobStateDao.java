package net.airvantage.sched.dao;

import net.airvantage.sched.app.AppException;
import net.airvantage.sched.model.JobDef;
import net.airvantage.sched.model.JobState;

public interface JobStateDao {

    public void saveJobDef(JobDef jobDef) throws AppException;

    public JobState findJobState(String id) throws AppException;

    public void lockJob(String id) throws AppException;

    public void unlockJob(String id) throws AppException;
}
