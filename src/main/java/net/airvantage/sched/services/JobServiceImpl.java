package net.airvantage.sched.services;

import java.util.ArrayList;
import java.util.Arrays;

import net.airvantage.sched.app.AppException;
import net.airvantage.sched.dao.JobStateDao;
import net.airvantage.sched.model.JobDef;
import net.airvantage.sched.model.JobSchedulingDef;
import net.airvantage.sched.model.JobSchedulingType;
import net.airvantage.sched.quartz.PostHttpJob;

import org.apache.log4j.Logger;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.ScheduleBuilder;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

public class JobServiceImpl implements JobService {

    private Logger LOG = Logger.getLogger(JobServiceImpl.class);

    private Scheduler scheduler;

    private JobStateDao jobDefDao;

    public JobServiceImpl(final Scheduler scheduler, final JobStateDao jobDefDao) {
        this.scheduler = scheduler;
        this.jobDefDao = jobDefDao;
    }

    @Override
    public void scheduleJob(JobDef jobDef) throws AppException {
        validateJobDef(jobDef);
        scheduleQuarzJob(jobDef);
        saveJobDef(jobDef);
    }

    private void validateJobDef(JobDef jobDef) throws AppException {
        // TODO check id, etc...
        
    }
    
    private void scheduleQuarzJob(JobDef jobDef) throws AppException {
        JobDetail jobDetail = jobDefToJobDetail(jobDef);
        Trigger trigger = jobDefToTrigger(jobDef);
        try {
            this.scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            LOG.error("Unable to schedule job jobDef : " + jobDef.toString(), e);
            throw new AppException("internal.error", new ArrayList<String>(), e);
        }
    }

    protected JobDetail jobDefToJobDetail(JobDef jobDef) {
        JobDetail detail = JobBuilder.newJob(PostHttpJob.class).withIdentity(jobDef.getId()).build();
        return detail;
    }

    protected static Trigger jobDefToTrigger(JobDef jobDef) throws AppException {
        Trigger trigger = TriggerBuilder.newTrigger().withIdentity(jobDef.getId(), jobDef.getId() + "-trigger")
                .withSchedule(scheduleBuilder(jobDef.getScheduling())).build();
        return trigger;
    }

    protected static ScheduleBuilder<? extends Trigger> scheduleBuilder(JobSchedulingDef schedulingDef) throws AppException {
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

    private void saveJobDef(JobDef jobDef) {
        this.jobDefDao.saveJobDef(jobDef);
    }
    
}
