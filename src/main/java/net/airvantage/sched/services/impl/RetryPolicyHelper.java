package net.airvantage.sched.services.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import net.airvantage.sched.app.exceptions.AppException;
import net.airvantage.sched.dao.JobWakeupDao;
import net.airvantage.sched.model.JobScheduling;
import net.airvantage.sched.model.JobSchedulingType;
import net.airvantage.sched.model.JobState;
import net.airvantage.sched.model.JobWakeup;
import net.airvantage.sched.quartz.job.JobResult;
import net.airvantage.sched.quartz.job.JobResult.CallbackStatus;
import net.airvantage.sched.services.JobSchedulingService;
import net.airvantage.sched.services.JobStateService;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A service to apply the retry policy.
 */
public class RetryPolicyHelper {

    private static final Logger LOG = LoggerFactory.getLogger(RetryPolicyHelper.class);

    private static final long DEFAULT_ACK_TIMEOUT_MS = TimeUnit.HOURS.toMillis(4);
    private static final long DEFAULT_ERROR_DELAY_MS = TimeUnit.MINUTES.toMillis(1);
    private static final long DEFAULT_RETRY_DELAY_MS = TimeUnit.SECONDS.toMillis(8);

    private static final int DEFAULT_MAX_NB_ERRORS = 10;
    private static final int DEFAULT_MAX_NB_RETRIES = 100;

    /**
     * Map to record the current number of errors by job key.
     */
    private ConcurrentHashMap<String, AtomicInteger> errors = new ConcurrentHashMap<>();

    /**
     * Map to record the current number of retries by job key.
     */
    private ConcurrentHashMap<String, AtomicInteger> retries = new ConcurrentHashMap<>();

    private JobWakeupDao jobWakeupDao;
    private JobStateService jobStateService;
    private JobSchedulingService jobSchedulingService;

    // ----------------------------------------------- Constructors ---------------------------------------------------

    public RetryPolicyHelper(JobStateService jobStateService, JobSchedulingService jobSchedulingService,
            JobWakeupDao jobWakeupDao) {

        this.jobWakeupDao = jobWakeupDao;
        this.jobStateService = jobStateService;
        this.jobSchedulingService = jobSchedulingService;
    }

    // ----------------------------------------------- Public Methods -------------------------------------------------

    public void cronJobExecuted(JobResult result) throws AppException {
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

    public void wakeupJobExecuted(JobWakeup wakeup, JobResult result){

        long delay = 0;
        if (result.getRetry() > 0) {
            delay = result.getRetry();

        } else if (result.getStatus() == CallbackStatus.FAILURE) {
            delay = DEFAULT_ERROR_DELAY_MS;
        }

        if (delay > 0) {
            wakeup.setWakeupTime(System.currentTimeMillis() + delay);
            jobWakeupDao.persist(wakeup);

        } else {
            jobWakeupDao.delete(wakeup.getId());
        }
    }

    // ----------------------------------------------- Private Methods ------------------------------------------------

    private void callbackSuccess(JobState state, JobResult result) throws AppException {

        // Clean errors counter
        // errors.remove(result.getJobId());
        //
        // boolean ack = result.isAck();
        // if (ack) {
        //
        // // Clean retries counter
        // retries.remove(result.getJobId());
        //
        // this.jobSchedulingService.ackJob(result.getJobId());
        //
        // } else {
        //
        // long retry = result.getRetry();
        // if (retry > 0) {
        //
        // this.retry(state, getRetryDelay(result.getJobId(), retry, state));
        //
        // } else {
        //
        // // Clean retries counter
        // retries.remove(result.getJobId());
        //
        // // Re-schedule trigger for one-shot job waiting acknowledgement
        // if ((state.getScheduling() != null) && (state.getScheduling().getType() == JobSchedulingType.DATE)) {
        //
        // long timeout = DEFAULT_ACK_TIMEOUT_MS;
        // if (state.getConfig().getTimeout() > 0) {
        // timeout = state.getConfig().getTimeout();
        // }
        //
        // this.retry(state, timeout);
        // }
        // }
        // }
    }

    private void callbackFailure(JobState state, JobResult result) throws AppException {

        // this.retry(state, getErrorDelay(result.getJobId(), state));

        // TODO check the retry date is before the next cron trigger fire time
        // TODO for a CRON add the nb of retry limit
    }

    /**
     * Schedule the job one time later.
     */
    private void retry(JobState state, long delay) throws AppException {

        String jobId = state.getConfig().getId();
        JobScheduling conf = state.getScheduling();

        conf.setStartAt(System.currentTimeMillis() + delay);

        this.jobSchedulingService.rescheduleJob(jobId, conf);
    }

    private long getRetryDelay(String key, long expected, JobState state) {

        AtomicInteger count = retries.putIfAbsent(key, new AtomicInteger());
        if (count == null) {
            count = retries.get(key);
        }

        if (count.get() > DEFAULT_MAX_NB_RETRIES) {
            LOG.error("The job {} retried more than {} times !", state.getConfig(), DEFAULT_MAX_NB_RETRIES);
        }

        return expected + (count.getAndIncrement() * DEFAULT_RETRY_DELAY_MS);
    }

    private long getErrorDelay(String key, JobState state) {

        AtomicInteger count = errors.putIfAbsent(key, new AtomicInteger());
        if (count == null) {
            count = errors.get(key);
        }

        long delay = count.incrementAndGet() * DEFAULT_ERROR_DELAY_MS;
        if (count.get() > DEFAULT_MAX_NB_ERRORS) {
            LOG.error("The job {} failed more than {} times !", state.getConfig(), DEFAULT_MAX_NB_ERRORS);
        }

        return delay;
    }

}
