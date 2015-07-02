package net.airvantage.sched.services.impl;

import java.util.concurrent.TimeUnit;

import net.airvantage.sched.app.exceptions.AppException;
import net.airvantage.sched.model.JobDef;
import net.airvantage.sched.model.JobSchedulingType;
import net.airvantage.sched.model.JobState;
import net.airvantage.sched.quartz.job.JobResult;
import net.airvantage.sched.quartz.job.JobResult.CallbackStatus;
import net.airvantage.sched.services.JobSchedulingService;
import net.airvantage.sched.services.JobStateService;
import net.airvantage.sched.services.RetryPolicyService;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A service to apply the retry policy.
 */
public class RetryPolicyServiceImpl implements RetryPolicyService {

    private static final Logger LOG = LoggerFactory.getLogger(RetryPolicyServiceImpl.class);

    private static final long DEFAULT_ACK_TIMEOUT_MS = TimeUnit.HOURS.toMillis(4);
    private static final long DEFAULT_RETRY_DELAY_MS = TimeUnit.MINUTES.toMillis(1);
    private static final int DEFAULT_MAX_NB_RETRY = 10;

    private JobStateService jobStateService;
    private JobSchedulingService jobSchedulingService;

    // ----------------------------------------------- Constructors ---------------------------------------------------

    public RetryPolicyServiceImpl(JobStateService jobStateService, JobSchedulingService jobSchedulingService) {

        this.jobStateService = jobStateService;
        this.jobSchedulingService = jobSchedulingService;
    }

    // ----------------------------------------------- Public Methods -------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public void jobExecuted(JobResult result) throws AppException {
        LOG.debug("jobExecuted : result={}", result);

        Validate.notNull(result);
        Validate.notNull(result.getJobId());

        JobState state = this.jobStateService.find(result.getJobId());
        if (state != null) {

            if (result.getStatus() == CallbackStatus.FAILURE) {
                this.callbackFailure(state, result);

            } else {
                this.callbackSuccess(state, result);
            }
        }
    }

    // ----------------------------------------------- Private Methods ------------------------------------------------

    /**
     * <ul>
     * <li>A job is auto-ack if requested ( ack param = true )</li>
     * <li>A job is re-scheduled later if requested ( retry param > 0 )</li>
     * <li>A DATE job is re-scheduled later to wait ack ( ack param = false )</li>
     * </ul>
     */
    private void callbackSuccess(JobState state, JobResult result) throws AppException {

        boolean ack = result.isAck();
        if (ack) {
            this.jobSchedulingService.ackJob(result.getJobId());

        } else {

            long retry = result.getRetry();
            if (retry > 0) {
                this.retry(state, retry);

            } else {

                // Re-schedule trigger for one-shot job
                if ((state.getScheduling() != null) && (state.getScheduling().getType() == JobSchedulingType.DATE)) {

                    if (state.getConfig().getTimeout() > 0) {
                        this.retry(state, state.getConfig().getTimeout());

                    } else {
                        this.retry(state, DEFAULT_ACK_TIMEOUT_MS);
                    }
                }
            }
        }
    }

    /**
     * A DATE job is re-scheduled on failure 5 times max.
     */
    private void callbackFailure(JobState state, JobResult result) throws AppException {

        // if one-shot job execution failed then retry
        if ((state.getScheduling() != null) && (state.getScheduling().getType() == JobSchedulingType.DATE)) {

            this.retry(state, result.getNbErrors() * DEFAULT_RETRY_DELAY_MS);
            if (result.getNbErrors() > DEFAULT_MAX_NB_RETRY) {
                LOG.error("The job {} failed more than {} times !", state.getConfig(), DEFAULT_MAX_NB_RETRY);
            }
        }
    }

    /**
     * Schedule the job one time later.
     */
    private void retry(JobState state, long delay) throws AppException {

        JobDef job = new JobDef();
        job.setConfig(state.getConfig());
        job.setScheduling(state.getScheduling());

        job.getScheduling().setStartAt(System.currentTimeMillis() + delay);

        this.jobSchedulingService.scheduleJob(job);
    }

}
