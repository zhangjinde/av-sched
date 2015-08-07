package net.airvantage.sched.quartz.job;

import net.airvantage.sched.app.ServiceLocator;
import net.airvantage.sched.app.exceptions.AppException;
import net.airvantage.sched.model.JobState;
import net.airvantage.sched.quartz.job.JobResult.CallbackStatus;
import net.airvantage.sched.services.JobStateService;
import net.airvantage.sched.services.impl.JobExecutionHelper;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CronJob implements Job {

    private static final Logger LOG = LoggerFactory.getLogger(CronJob.class);

    private JobStateService jobStateService;
    private JobExecutionHelper jobExecutionHelper;

    // ------------------------------------------------- Constructors -------------------------------------------------

    /**
     * Constructor used by Quartz to load the job.
     */
    public CronJob() {
        this(ServiceLocator.getInstance().getJobStateService(), ServiceLocator.getInstance().getHttpClientService());
    }

    protected CronJob(JobStateService jobStateService, JobExecutionHelper jobExecutionHelper) {

        this.jobStateService = jobStateService;
        this.jobExecutionHelper = jobExecutionHelper;
    }

    // ------------------------------------------------- Public Methods -----------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LOG.debug("execute : context={}", context);

        JobKey key = context.getJobDetail().getKey();
        String jobId = key.getName();
        JobResult result = null;

        try {

            JobState jobState = this.jobStateService.find(jobId);
            if (jobState != null) {

                String url = jobState.getConfig().getUrl();
                result = this.jobExecutionHelper.doHttpPost(jobId, url);

                if (result.getStatus() == CallbackStatus.SUCCESS) {
                    jobStateService.lockJob(jobId);
                }

            } else {
                LOG.warn("Try to executed a job {} without configuration.", jobId);
            }

        } catch (AppException aex) {
            LOG.error("Unable to execute CRON job " + key + ")", aex);
            throw new JobExecutionException("Unable to execute CRON job " + key + ")", aex);
        }

        context.setResult(result);
    }

}
