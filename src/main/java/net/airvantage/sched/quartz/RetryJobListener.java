package net.airvantage.sched.quartz;

import net.airvantage.sched.quartz.job.JobResult;
import net.airvantage.sched.services.RetryPolicyService;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.listeners.JobListenerSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A listener to apply retry policy.
 */
public class RetryJobListener extends JobListenerSupport {

    private static final Logger LOG = LoggerFactory.getLogger(RetryJobListener.class);

    private RetryPolicyService retryPolicyService;

    @Override
    public String getName() {
        return "RetryJobListener";
    }

    public RetryJobListener(RetryPolicyService retryPolicyService) {

        this.retryPolicyService = retryPolicyService;
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {

        try {
            if (context.getResult() != null) {
                JobResult result = (JobResult) context.getResult();
                this.retryPolicyService.jobExecuted(result);
            }

        } catch (Exception aex) {
            LOG.error("Job complete action failed for job " + context.getJobDetail().getKey().getName(), aex);
        }
    }

}
