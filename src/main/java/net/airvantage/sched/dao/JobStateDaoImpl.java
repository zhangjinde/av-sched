package net.airvantage.sched.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.airvantage.sched.app.exceptions.AppException;
import net.airvantage.sched.app.exceptions.AppExceptions;
import net.airvantage.sched.model.JobConfig;
import net.airvantage.sched.model.JobDef;
import net.airvantage.sched.model.JobLock;
import net.airvantage.sched.model.JobScheduling;
import net.airvantage.sched.model.JobState;

import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobStateDaoImpl implements JobStateDao {

    private static final Logger LOG = LoggerFactory.getLogger(JobStateDaoImpl.class);

    private JobLockDao jobLockDao;

    private JobConfigDao jobConfigDao;

    private JobSchedulingDao jobSchedulingDao;

    public JobStateDaoImpl(JobConfigDao jobConfigDao, JobLockDao jobLockDao, JobSchedulingDao jobSchedulingDao) {
        this.jobLockDao = jobLockDao;
        this.jobConfigDao = jobConfigDao;
        this.jobSchedulingDao = jobSchedulingDao;
    }

    @Override
    public void saveJobDef(JobDef jobDef) throws AppException {

        try {
            JobConfig config = jobDef.getConfig();
            this.jobConfigDao.saveJobConfig(config);

        } catch (SQLException e) {
            LOG.error(String.format("Unable to store jobDef {}", jobDef), e);
            throw AppExceptions.serverError(e);
        }

    }

    @Override
    public void deleteJobDef(String jobId) throws AppException {
        try {
            this.jobConfigDao.removeJobConfig(jobId);
            this.jobLockDao.removeLock(jobId);
        } catch (SQLException e) {
            LOG.error(String.format("Unable to delete job state {}", jobId), e);
            throw AppExceptions.serverError(e);
        }
    }

    @Override
    public JobState findJobState(String id) throws AppException {

        JobConfig config = null;
        JobLock lock = null;
        JobState state = null;
        JobScheduling scheduling = null;
        try {
            config = this.jobConfigDao.findJobConfig(id);
            lock = this.jobLockDao.findJobLock(id);
            scheduling = this.jobSchedulingDao.findJobScheduling(id);
        } catch (SQLException e) {
            LOG.error(String.format("Unable to find job state with id", id), e);
            throw AppExceptions.serverError(e);
        } catch (SchedulerException e) {
            LOG.error(String.format("Unable to find job state with id", id), e);
            throw AppExceptions.serverError(e);
        }
        if (config != null) {
            state = new JobState();
            state.setConfig(config);
            state.setLock(lock);
            state.setScheduling(scheduling);
        }

        return state;
    }

    @Override
    public void lockJob(String id) throws AppException {
        try {
            JobState jobState = findJobState(id);
            if (jobState != null) {
                Long expiresAt = new Date().getTime() + jobState.getConfig().getTimeout();
                LOG.debug("Will save expiration date" + expiresAt);
                this.jobLockDao.saveLock(id, expiresAt);
            }
        } catch (SQLException e) {
            LOG.error(String.format("Unable to lock job state with id", id), e);
            throw AppExceptions.serverError(e);
        }

    }

    @Override
    public void unlockJob(String id) throws AppException {
        try {
            JobState jobState = findJobState(id);
            if (jobState == null) {
                throw AppExceptions.jobNotFound(id);
            } else {
                this.jobLockDao.removeLock(id);
            }
        } catch (SQLException e) {
            LOG.error(String.format("Unable to lock job state with id", id), e);
            throw AppExceptions.serverError(e);
        }
    }

    @Override
    public List<JobState> getJobStates() throws AppException {

        List<JobState> states = new ArrayList<JobState>();

        try {
            Map<String, JobConfig> jobConfigs = this.jobConfigDao.jobConfigsById();
            Map<String, JobLock> jobLocks = this.jobLockDao.jobLocksById();
            Map<String, JobScheduling> jobSchedulings = this.jobSchedulingDao.jobSchedulingsById();
            
            for (Entry<String, JobConfig> entry : jobConfigs.entrySet()) {
                String id = entry.getKey();
                JobConfig jobConfig = entry.getValue();

                JobState state = new JobState();
                state.setConfig(jobConfig);

                if (jobLocks.containsKey(id)) {
                    JobLock jobLock = jobLocks.get(id);
                    state.setLock(jobLock);
                } else {
                    state.setLock(new JobLock());
                }
                
                if (jobSchedulings.containsKey(id)) {
                    JobScheduling jobScheduling = jobSchedulings.get(id);
                    state.setScheduling(jobScheduling);
                } else {
                    state.setScheduling(new JobScheduling());
                }
                
                states.add(state);

                
            }

        } catch (SQLException e) {
            LOG.error(String.format("Unable to find job states"), e);
            throw AppExceptions.serverError(e);
        } catch (SchedulerException e) {
            LOG.error(String.format("Unable to find job states"), e);
            throw AppExceptions.serverError(e);
        }

        return states;

    }

    @Override
    public void removeAll() throws AppException {
        try {
            this.jobLockDao.removeAll();
            this.jobConfigDao.removeAll();
        } catch (SQLException e) {
            throw new AppException("clear.error", e);
        }
    }

}
