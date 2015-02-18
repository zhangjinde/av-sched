package net.airvantage.sched.quartz;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class PostHttpJob implements Job {

    private static final Logger LOG = Logger.getLogger(PostHttpJob.class);
    
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LOG.debug("Would try and run job execution context" + context.getJobDetail().getKey().getName());
    }

}
