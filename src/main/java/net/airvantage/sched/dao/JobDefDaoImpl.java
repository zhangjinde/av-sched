package net.airvantage.sched.dao;

import org.apache.log4j.Logger;

import net.airvantage.sched.model.jobDef.JobDef;

public class JobDefDaoImpl implements JobDefDao {

    private static final Logger LOG = Logger.getLogger(JobDefDaoImpl.class);
    
    @Override
    public void saveJobDef(JobDef jobDef) {
        // TODO(pht) implement me
        LOG.debug("TODO(pht) Would try and save job def : " + jobDef);
    }

}
