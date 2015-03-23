package net.airvantage.sched.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.airvantage.sched.model.JobScheduling;
import net.airvantage.sched.model.JobSchedulingType;

import org.quartz.JobDetail;
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
    public Map<String, JobScheduling> jobSchedulingsById() throws SchedulerException {

        HashMap<String, JobScheduling> jobSchedulings = new HashMap<String, JobScheduling>();

        Set<TriggerKey> triggerKeys = scheduler.getTriggerKeys(GroupMatcher.anyTriggerGroup());
        for (TriggerKey key : triggerKeys) {

            Trigger trigger = scheduler.getTrigger(key);

            if (trigger instanceof CronTriggerImpl) {

                JobScheduling sched = new JobScheduling();
                sched.setType(JobSchedulingType.CRON);

                CronTriggerImpl cronTrigger = (CronTriggerImpl) trigger;
                sched.setValue(cronTrigger.getCronExpression());

                String jobId = cronTrigger.getJobName();
                jobSchedulings.put(jobId, sched);
            }

        }

        return jobSchedulings;

    }

}
