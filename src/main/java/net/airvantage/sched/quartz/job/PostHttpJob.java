package net.airvantage.sched.quartz.job;

import java.io.IOException;
import java.io.InputStream;

import net.airvantage.sched.app.ServiceLocator;
import net.airvantage.sched.app.exceptions.AppException;
import net.airvantage.sched.app.mapper.JsonMapper;
import net.airvantage.sched.model.JobState;
import net.airvantage.sched.quartz.job.JobResult.CallbackStatus;
import net.airvantage.sched.services.JobStateService;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DisallowConcurrentExecution
@PersistJobDataAfterExecution
public class PostHttpJob implements Job {

    private static final Logger LOG = LoggerFactory.getLogger(PostHttpJob.class);

    private static final int DEFAULT_REQUEST_TIMEOUT_MS = 60 * 1000;
    private static final String DATAMAP_NB_ERROR_PARAM_NAME = "NB_ERRORS";

    private JobStateService jobStateDao;
    private CloseableHttpClient client;
    private JsonMapper jsonMapper;
    private String schedSecret;

    // ------------------------------------------------- Constructors -------------------------------------------------

    /**
     * Constructor used by Quartz to load the job.
     */
    public PostHttpJob() throws SchedulerException {
        this(ServiceLocator.getInstance().getJobStateService(), ServiceLocator.getInstance().getHttpClient(),
                ServiceLocator.getInstance().getSchedSecret(), ServiceLocator.getInstance().getJsonMapper());
    }

    protected PostHttpJob(JobStateService jobStateDao, CloseableHttpClient client, String schedSecret,
            JsonMapper jsonMapper) {

        this.client = client;
        this.jsonMapper = jsonMapper;
        this.schedSecret = schedSecret;
        this.jobStateDao = jobStateDao;
    }

    // ------------------------------------------------- Public Methods -----------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LOG.debug("execute : context={}", context);

        String jobId = context.getJobDetail().getKey().getName();
        JobDataMap datamap = context.getJobDetail().getJobDataMap();
        try {

            JobState jobState = this.jobStateDao.find(jobId);
            if (jobState != null) {

                String url = jobState.getConfig().getUrl();

                HttpPost request = this.buildRequest(url);
                CloseableHttpResponse response = this.client.execute(request);

                try {
                    if (response.getStatusLine().getStatusCode() == 200) {

                        context.setResult(this.success(jobId, response.getEntity(), datamap));
                        jobStateDao.lockJob(jobId);

                    } else {
                        LOG.warn("Post to {} returns HTTP {}.", url, response.getStatusLine().getStatusCode());
                        context.setResult(this.failure(jobId, datamap));
                    }

                } finally {
                    response.close();
                }

            } else {
                LOG.warn("Try to executed a job {} without configuration.", jobId);
            }

        } catch (Exception e) {
            LOG.error("Unable to post to url  (job " + jobId + ")", e);
            context.setResult(this.failure(jobId, datamap));
        }
    }

    // ------------------------------------------------- Private Methods ----------------------------------------------

    private HttpPost buildRequest(String url) {
        LOG.debug("Will post to url", url);

        HttpPost request = new HttpPost(url);
        request.setHeader("X-Sched-secret", schedSecret);

        RequestConfig rqCfg = RequestConfig.custom().setConnectTimeout(DEFAULT_REQUEST_TIMEOUT_MS)
                .setConnectionRequestTimeout(DEFAULT_REQUEST_TIMEOUT_MS).build();

        request.setConfig(rqCfg);

        return request;
    }

    private JobResult success(String jobId, HttpEntity entity, JobDataMap datamap) {

        JobResult result = new JobResult();
        result.setStatus(CallbackStatus.SUCCESS);
        result.setJobId(jobId);

        try {
            if (entity != null) {
                InputStream stream = entity.getContent();

                if (stream != null) {
                    PostHttpJobResult content = jsonMapper.postHttpJobResult(stream);

                    if (content != null) {
                        if (content.getAck() != null) {
                            result.setAck(content.getAck());
                        }
                        if (content.getRetry() != null) {
                            result.setRetry(content.getRetry());
                        }
                    }
                }
            }
            
            // Clean previous errors
            datamap.remove(DATAMAP_NB_ERROR_PARAM_NAME);

        } catch (AppException | IllegalStateException | IOException e) {
            LOG.error("Invalid callback response (job " + jobId + "), it will be ignored.", e);
        }

        return result;
    }

    private JobResult failure(String jobId, JobDataMap datamap) {

        JobResult result = new JobResult();
        result.setStatus(CallbackStatus.FAILURE);
        result.setJobId(jobId);

        if (!datamap.containsKey(DATAMAP_NB_ERROR_PARAM_NAME)) {
            datamap.put(DATAMAP_NB_ERROR_PARAM_NAME, 1);
            result.setNbErrors(1);

        } else {
            int errors = datamap.getInt(DATAMAP_NB_ERROR_PARAM_NAME);

            result.setNbErrors(errors + 1);
            datamap.put(DATAMAP_NB_ERROR_PARAM_NAME, errors + 1);
        }

        return result;
    }

}
