package net.airvantage.sched.services;

import net.airvantage.sched.app.exceptions.AppException;
import net.airvantage.sched.quartz.job.JobResult;

/**
 * A service to apply the retry policy.
 */
public interface RetryPolicyService {

    /**
     * Call when a scheduled job was executed.
     */
    void jobExecuted(JobResult jobResult) throws AppException;

}