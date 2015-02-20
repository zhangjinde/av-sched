package net.airvantage.sched.quartz;

import net.airvantage.sched.dao.JobStateDao;
import net.airvantage.sched.model.JobLock;
import net.airvantage.sched.model.JobState;

import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.Trigger.CompletedExecutionInstruction;
import org.quartz.TriggerListener;

public class LockTriggerListener implements TriggerListener {

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
