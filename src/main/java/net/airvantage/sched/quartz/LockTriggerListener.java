package net.airvantage.sched.quartz;

import net.airvantage.sched.app.AppException;
import net.airvantage.sched.dao.JobStateDao;
import net.airvantage.sched.model.JobLock;
import net.airvantage.sched.model.JobState;

import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.Trigger.CompletedExecutionInstruction;
import org.quartz.TriggerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LockTriggerListener implements TriggerListener {

    private static final Logger LOG = LoggerFactory.getLogger(LockTriggerListener.class);

    private JobStateDao jobStateDao;

    @Override
    public String getName() {
        return "lockTriggerListener";
    }

    public LockTriggerListener(JobStateDao jobStateDao) {
        this.jobStateDao = jobStateDao;
    }

    @Override
    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
        try {
            String jobId = context.getJobDetail().getKey().getName();
            JobState jobState = this.jobStateDao.findJobState(jobId);

            JobLock lock = jobState.getLock();

            if (lock.isLocked()) {
                if (lock.isExpired()) {
                    this.jobStateDao.unlockJob(jobId);
                    return false;
                } else {
                    return true;
                }
            }
        } catch (AppException e) {
            LOG.error("Unable to veto job execution", e);
        }
        return false;
    }

    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext context) {
        // TODO Auto-generated method stub

    }

    @Override
    public void triggerMisfired(Trigger trigger) {
        // TODO Auto-generated method stub

    }

    @Override
    public void triggerComplete(Trigger trigger, JobExecutionContext context,
            CompletedExecutionInstruction triggerInstructionCode) {
        // TODO Auto-generated method stub

    }

}
