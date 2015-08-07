package net.airvantage.sched.quartz;

import net.airvantage.sched.quartz.job.JobResult;
import net.airvantage.sched.services.impl.RetryPolicyHelper;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.listeners.JobListenerSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A listener to apply retry policy.
 */
public class DefaultJobListener extends JobListenerSupport {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultJobListener.class);

    private RetryPolicyHelper retryPolicyHelper;

    @Override
    public String getName() {
        return "DefaultJobListener";
    }

    public DefaultJobListener(RetryPolicyHelper retryPolicyHelper) {

        this.retryPolicyHelper = retryPolicyHelper;
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {

        try {
            if (context.getResult() != null) {
                JobResult result = (JobResult) context.getResult();
                this.retryPolicyHelper.cronJobExecuted(result);
            }

        } catch (Exception aex) {
            LOG.error("Job complete action failed for job " + context.getJobDetail().getKey().getName(), aex);
        }
    }

}
