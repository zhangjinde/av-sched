package net.airvantage.sched.services.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import net.airvantage.sched.app.exceptions.AppException;
import net.airvantage.sched.app.exceptions.AppExceptions;
import net.airvantage.sched.dao.JobConfigDao;
import net.airvantage.sched.dao.JobLockDao;
import net.airvantage.sched.dao.JobSchedulingDao;
import net.airvantage.sched.model.JobConfig;
import net.airvantage.sched.model.JobDef;
import net.airvantage.sched.model.JobLock;
import net.airvantage.sched.model.JobScheduling;
import net.airvantage.sched.model.JobSchedulingType;
import net.airvantage.sched.model.JobState;
import net.airvantage.sched.quartz.job.PostHttpJob;
import net.airvantage.sched.services.JobSchedulingService;
import net.airvantage.sched.services.JobStateService;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A service to manage the jobs scheduling.
 */
public class JobSchedulingServiceImpl implements JobSchedulingService {

    private Logger LOG = LoggerFactory.getLogger(JobSchedulingServiceImpl.class);

    private Scheduler scheduler;
    private JobStateService jobStateService;

    private JobLockDao jobLockDao;
    private JobConfigDao jobConfigDao;
    private JobSchedulingDao jobSchedulingDao;

    // ------------------------------------------------ Constructors --------------------------------------------------

    public JobSchedulingServiceImpl(Scheduler scheduler, JobStateService jobStateService, JobConfigDao jobConfigDao,
            JobLockDao jobLockDao, JobSchedulingDao jobSchedulingDao) {

        this.scheduler = scheduler;
        this.jobStateService = jobStateService;

        this.jobLockDao = jobLockDao;
        this.jobConfigDao = jobConfigDao;
        this.jobSchedulingDao = jobSchedulingDao;
    }

    // ------------------------------------------ JobSchedulingService Methods ----------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public void scheduleJob(JobDef jobDef) throws AppException {
        LOG.debug("scheduleJob : jobDef={}", jobDef);

        validateJobDef(jobDef);
        scheduleQuarzJob(jobDef);

        try {

            // Persist the job configuration
            this.jobConfigDao.persist(jobDef.getConfig());

        } catch (Exception ex) {
            LOG.error(String.format("Unable to schedule job %s", jobDef), ex);
            throw new AppException("can.not.persist.configuration", Arrays.asList(jobDef.getConfig().getId()), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean unscheduleJob(String jobId) throws AppException {
        LOG.debug("unscheduleJob : jobId={}", jobId);

        boolean res = false;
        try {

            res = unscheduleQuartzJob(jobId);

            this.jobConfigDao.delete(jobId);
            this.jobLockDao.delete(jobId);

        } catch (SQLException ex) {
            LOG.error(String.format("Unable to unschedule job {}", jobId), ex);
            throw AppExceptions.serverError(ex);
        }

        return res;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void ackJob(String jobId) throws AppException {
        LOG.debug("ackJob : jobId={}", jobId);

        try {

            JobConfig config = this.jobConfigDao.find(jobId);
            if (config == null) {
                throw new AppException("job.not.found", Arrays.asList(jobId));
            }

            JobScheduling schedConf = this.jobSchedulingDao.find(jobId);
            if ((schedConf == null) || (schedConf.getType() == JobSchedulingType.DATE)) {

                // Remove job configuration when a job is complete
                this.unscheduleJob(jobId);

            } else {
                this.jobStateService.unlockJob(jobId);
            }

        } catch (SchedulerException | SQLException sex) {
            LOG.error(String.format("Unable to acknowledge job {}", jobId), sex);
            throw new AppException("ack.job.failure", Arrays.asList(jobId), sex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean triggerJob(String jobId) throws AppException {
        LOG.debug("triggerJob : jobId={}", jobId);

        boolean res = false;
        try {

            JobState jobState = this.jobStateService.find(jobId);
            if (jobState == null) {
                throw AppExceptions.jobNotFound(jobId);
            }

            JobLock lock = jobState.getLock();

            // Currently locked jobs should not be re-triggered
            if (lock.isLocked() && !lock.isExpired()) {
                res = false;

            } else {
                this.scheduler.triggerJob(this.buildJobKey(jobId));
                res = true;
            }

        } catch (SchedulerException ex) {
            LOG.error(String.format("Unable to trigger job {}", jobId), ex);
            throw new AppException("can.not.trigger", Arrays.asList(jobId), ex);
        }

        return res;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteJob() throws AppException {
        LOG.debug("deleteJob");

        try {
            // Delete configuration
            this.jobLockDao.deleteAll();
            this.jobConfigDao.deleteAll();

            // Delete scheduling
            this.scheduler.clear();

        } catch (SchedulerException | SQLException ex) {
            LOG.error("Unable to cleard jobd", ex);
            throw new AppException("clear.error", ex);
        }
    }

    // ----------------------------------------------- Private Methods ------------------------------------------------

    private void validateJobDef(JobDef jobDef) throws AppException {

        Validate.notNull(jobDef);
        Validate.notNull(jobDef.getConfig());
        Validate.notNull(jobDef.getScheduling());

        if (StringUtils.isEmpty(jobDef.getConfig().getId())) {
            jobDef.getConfig().setId(UUID.randomUUID().toString());
        }

        if (StringUtils.isEmpty(jobDef.getConfig().getUrl())) {
            throw new AppException("missing.config.url");
        }

        if (jobDef.getConfig().getUrl().length() > 255) {
            throw new AppException("too.long.url");
        }

        if (jobDef.getScheduling().getType() == null) {
            throw new AppException("missing.scheduling.type");
        }

        if (jobDef.getScheduling().getType() == JobSchedulingType.CRON) {
            if (StringUtils.isEmpty(jobDef.getScheduling().getValue()))
                throw new AppException("missing.scheduling.value");
        }

        if (jobDef.getScheduling().getType() == JobSchedulingType.DATE) {
            if (jobDef.getScheduling().getStartAt() == 0)
                throw new AppException("missing.scheduling.date");
        }
    }

    private void scheduleQuarzJob(JobDef jobDef) throws AppException {

        JobDetail job = this.buildJob(jobDef);
        Trigger trigger = this.buildTrigger(jobDef, job.getKey());

        try {

            // Add a new job or replace an existing one
            this.scheduler.addJob(job, true);

            try {
                if (this.scheduler.checkExists(trigger.getKey())) {
                    this.scheduler.rescheduleJob(trigger.getKey(), trigger);

                } else {
                    this.scheduler.scheduleJob(trigger);
                }

            } catch (ObjectAlreadyExistsException e) {
                LOG.warn("Trigger already exists with key {}, try to replace it.", trigger.getKey());

                // To manage concurrent calls
                this.scheduler.rescheduleJob(trigger.getKey(), trigger);
            }

        } catch (SchedulerException e) {
            LOG.error("Unable to schedule job jobDef : " + jobDef.toString(), e);
            throw new AppException("internal.error", new ArrayList<String>(), e);
        }
    }

    /**
     * Try unscheduling the job.
     * 
     * @return true if the job existed, and was unscheduled.
     */
    private boolean unscheduleQuartzJob(String jobId) throws AppException {

        try {
            return this.scheduler.deleteJob(this.buildJobKey(jobId));

        } catch (SchedulerException e) {
            LOG.error("Unable to unschedule job with id : " + jobId, e);
            throw new AppException("internal.error", new ArrayList<String>(), e);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Trigger buildTrigger(JobDef jobDef, JobKey job) throws AppException {

        JobScheduling conf = jobDef.getScheduling();
        TriggerKey key = this.buildTiggerKey(jobDef.getConfig().getId());
        TriggerBuilder trigger = TriggerBuilder.newTrigger().withIdentity(key);

        try {
            switch (jobDef.getScheduling().getType()) {
            case CRON:

                trigger.withSchedule(CronScheduleBuilder.cronSchedule(conf.getValue()));

            case DATE:
            default:

                if (conf.getStartAt() > 0) {
                    trigger.startAt(new Date(conf.getStartAt()));
                }
                break;
            }

        } catch (RuntimeException e) {
            throw new AppException("invalid.schedule.value", Arrays.asList(conf.getValue()), e);
        }

        if (job != null) {
            trigger.forJob(job);
        }

        return trigger.build();
    }

    private JobDetail buildJob(JobDef jobDef) throws AppException {

        JobKey key = this.buildJobKey(jobDef.getConfig().getId());
        JobBuilder job = JobBuilder.newJob(PostHttpJob.class).withIdentity(key).storeDurably();

        return job.build();
    }

    private JobKey buildJobKey(String confId) {
        return new JobKey(confId);
    }

    private TriggerKey buildTiggerKey(String confId) {
        return new TriggerKey(confId, confId + "-trigger");
    }

}
