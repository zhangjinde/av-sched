package net.airvantage.sched.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.airvantage.sched.model.JobScheduling;
import net.airvantage.sched.model.JobSchedulingType;

import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.impl.triggers.CronTriggerImpl;

public class JobSchedulingDaoImpl implements JobSchedulingDao {

    private Scheduler scheduler;

    public JobSchedulingDaoImpl(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public JobScheduling findJobScheduling(String jobId) throws SchedulerException {
        JobScheduling jobScheduling = null;
        // K Set<TriggerKey> triggerKeys = scheduler.getTriggerKeys(GroupMatcher.groupEquals(jobId));
        List<? extends Trigger> triggersOfJob = scheduler.getTriggersOfJob(new JobKey(jobId));
        if (triggersOfJob != null && !triggersOfJob.isEmpty()) {
            Trigger trigger = triggersOfJob.get(0);
            JobSchedulingWithId sched = fromTrigger(trigger);
            if (sched != null) {
                jobScheduling = sched.jobScheduling;
            }
        }

        return jobScheduling;
    }

    @Override
    public Map<String, JobScheduling> jobSchedulingsById() throws SchedulerException {
        HashMap<String, JobScheduling> jobSchedulings = new HashMap<String, JobScheduling>();

        Set<TriggerKey> triggerKeys = scheduler.getTriggerKeys(GroupMatcher.anyTriggerGroup());
        for (TriggerKey key : triggerKeys) {
            Trigger trigger = scheduler.getTrigger(key);
            JobSchedulingWithId sched = fromTrigger(trigger);
            if (sched != null) {
                jobSchedulings.put(sched.jobId, sched.jobScheduling);
            }
        }

        return jobSchedulings;
    }

    private class JobSchedulingWithId {
        public String jobId;
        public JobScheduling jobScheduling;

        public JobSchedulingWithId(String jobId, JobScheduling jobScheduling) {
            super();
            this.jobId = jobId;
            this.jobScheduling = jobScheduling;
        }

    }

    private JobSchedulingWithId fromTrigger(Trigger trigger) {

        if (trigger instanceof CronTriggerImpl) {

            JobScheduling sched = new JobScheduling();
            sched.setType(JobSchedulingType.CRON);

            CronTriggerImpl cronTrigger = (CronTriggerImpl) trigger;
            sched.setValue(cronTrigger.getCronExpression());

            String jobId = cronTrigger.getJobName();

            return new JobSchedulingWithId(jobId, sched);

        }
        return null;

    }

}
