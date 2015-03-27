package net.airvantage.sched.services;

import java.util.ArrayList;
import java.util.Arrays;

import net.airvantage.sched.app.exceptions.AppException;
import net.airvantage.sched.app.exceptions.AppExceptions;
import net.airvantage.sched.dao.JobStateDao;
import net.airvantage.sched.model.JobDef;
import net.airvantage.sched.model.JobId;
import net.airvantage.sched.model.JobLock;
import net.airvantage.sched.model.JobScheduling;
import net.airvantage.sched.model.JobSchedulingType;
import net.airvantage.sched.model.JobState;
import net.airvantage.sched.quartz.PostHttpJob;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.ScheduleBuilder;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobServiceImpl implements JobService {

    private Logger LOG = LoggerFactory.getLogger(JobServiceImpl.class);

    private Scheduler scheduler;

    private JobStateDao jobStateDao;

    public JobServiceImpl(final Scheduler scheduler, final JobStateDao jobStateDao) {
        this.scheduler = scheduler;
        this.jobStateDao = jobStateDao;
    }

    @Override
    public void scheduleJob(JobDef jobDef) throws AppException {
        validateJobDef(jobDef);
        scheduleQuarzJob(jobDef);
        saveJobDef(jobDef);
    }

    @Override
    public void unscheduleJob(JobId jobId) throws AppException {
        unscheduleQuartzJob(jobId);
        deleteJobDef(jobId);
    }

    @Override
    public void ackJob(String jobId) throws AppException {
        this.jobStateDao.unlockJob(jobId);
    }

    @Override
    public boolean triggerJob(String jobId) throws AppException {
        boolean res = false;
        try {
            JobState jobState = this.jobStateDao.findJobState(jobId);
            if (jobState == null) {
                throw AppExceptions.jobNotFound(jobId);
            }
            JobLock lock = jobState.getLock();
            // Currently locked jobs should not be retriggered
            if (lock.isLocked() && !lock.isExpired()) {
                res = false;
            } else {
                this.scheduler.triggerJob(new JobKey(jobId));
                res = true;
            }
        } catch (SchedulerException e) {
            throw new AppException("can.not.trigger", Arrays.asList(jobId), e);
        }
        return res;
    }

    @Override
    public void clean() throws AppException {
        try {
            this.jobStateDao.removeAll();
            this.scheduler.clear();
        } catch (SchedulerException e) {
            throw new AppException("clear.error", e);
        }
    }

    private void validateJobDef(JobDef jobDef) throws AppException {
        // TODO check id, etc...

    }

    private void scheduleQuarzJob(JobDef jobDef) throws AppException {
        JobDetail jobDetail = jobDefToJobDetail(jobDef);
        Trigger trigger = jobDefToTrigger(jobDef);
        try {
            // Replace potentially existing job with the same key.
            JobKey key = jobDetail.getKey();
            if (this.scheduler.checkExists(key)) {
                this.scheduler.deleteJob(key);
            }
            this.scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            LOG.error("Unable to schedule job jobDef : " + jobDef.toString(), e);
            throw new AppException("internal.error", new ArrayList<String>(), e);
        }
    }

    private void unscheduleQuartzJob(JobId jobId) throws AppException {
        try {
            JobKey key = new JobKey(jobId.getId());
            if (this.scheduler.checkExists(key)) {
                this.scheduler.deleteJob(key);
            }
        } catch (SchedulerException e) {
            LOG.error("Unable to unschedule job with id : " + jobId.getId(), e);
            throw new AppException("internal.error", new ArrayList<String>(), e);
        }
    }

    protected JobDetail jobDefToJobDetail(JobDef jobDef) {
        JobDetail detail = JobBuilder.newJob(PostHttpJob.class).withIdentity(jobDef.getConfig().getId()).build();
        return detail;
    }

    protected static Trigger jobDefToTrigger(JobDef jobDef) throws AppException {
        String id = jobDef.getConfig().getId();
        Trigger trigger = TriggerBuilder.newTrigger().withIdentity(id, id + "-trigger")
                .withSchedule(scheduleBuilder(jobDef.getScheduling())).build();

        return trigger;
    }

    protected static ScheduleBuilder<? extends Trigger> scheduleBuilder(JobScheduling schedulingDef)
            throws AppException {
        ScheduleBuilder<? extends Trigger> res = null;
        if (schedulingDef.getType() == JobSchedulingType.CRON) {
            try {
                res = CronScheduleBuilder.cronSchedule(schedulingDef.getValue());
            } catch (RuntimeException e) {
                throw new AppException("invalid.schedule.value", Arrays.asList(schedulingDef.getValue()), e);
            }
        }

        return res;
    }

    private void saveJobDef(JobDef jobDef) throws AppException {
        this.jobStateDao.saveJobDef(jobDef);
    }

    private void deleteJobDef(JobId jobId) throws AppException {
        this.jobStateDao.deleteJobDef(jobId.getId());
    }

}
