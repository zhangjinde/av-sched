package net.airvantage.sched.dao;

import java.util.Map;

import net.airvantage.sched.model.JobScheduling;

import org.quartz.SchedulerException;

public interface JobSchedulingDao {

//    public abstract JobScheduling findJobScheduling(String id) throws SchedulerException;

    public abstract Map<String, JobScheduling> jobSchedulingsById() throws SchedulerException;
    
}
