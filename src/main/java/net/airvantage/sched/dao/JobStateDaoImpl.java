package net.airvantage.sched.dao;

import org.apache.log4j.Logger;

import net.airvantage.sched.model.JobDef;
import net.airvantage.sched.model.JobState;

public class JobStateDaoImpl implements JobStateDao {

    private static final Logger LOG = Logger.getLogger(JobStateDaoImpl.class);
    
    @Override
    public void saveJobDef(JobDef jobDef) {
        // TODO(pht) implement me
        LOG.debug("TODO(pht) Would try and save job def : " + jobDef);
    }

    @Override
    public JobState findJobState(String id) {
        LOG.debug("TODO(pht) Would try and get job def with id " + id);
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void lockJob(String id) {
        // TODO Auto-generated method stub
        LOG.debug("TODO(pht) Would try and get lock job with id " + id);
        
    }

    @Override
    public void unlockJob(String id) {
        // TODO Auto-generated method stub
        
    }
    

}
