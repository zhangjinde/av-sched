package net.airvantage.sched.services.impl;

import java.io.IOException;
import java.io.InputStream;

import net.airvantage.sched.app.exceptions.AppException;
import net.airvantage.sched.app.mapper.JsonMapper;
import net.airvantage.sched.model.PostHttpJobResult;
import net.airvantage.sched.quartz.job.JobResult;
import net.airvantage.sched.quartz.job.JobResult.CallbackStatus;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobExecutionHelper {

    private static final Logger LOG = LoggerFactory.getLogger(JobExecutionHelper.class);

    private static final int DEFAULT_REQUEST_TIMEOUT_MS = 60 * 1000;

    private CloseableHttpClient client;
    private JsonMapper jsonMapper;
    private String schedSecret;

    // ------------------------------------------------- Constructors -------------------------------------------------

    public JobExecutionHelper(CloseableHttpClient client, String schedSecret, JsonMapper jsonMapper) {

        this.client = client;
        this.jsonMapper = jsonMapper;
        this.schedSecret = schedSecret;
    }

    // ------------------------------------------------- Public Methods -----------------------------------------------

    public JobResult doHttpPost(String jobId, String url) {
        LOG.debug("doHttpPost : jobId={}, url={}", jobId, url);

        JobResult result = null;
        try {

            HttpPost request = this.buildRequest(url);
            CloseableHttpResponse response = this.client.execute(request);

            try {
                if (response.getStatusLine().getStatusCode() == 200) {
                    result = this.success(jobId, response.getEntity());

                } else {
                    LOG.warn("Post to {} returns HTTP {}.", url, response.getStatusLine().getStatusCode());
                    result = this.failure(jobId);
                }

            } finally {
                response.close();
            }

        } catch (Exception e) {
            LOG.error("Unable to post to url  (job " + jobId + ")", e);
            result = this.failure(jobId);
        }

        return result;
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

    private JobResult success(String jobId, HttpEntity entity) {

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

        } catch (AppException | IllegalStateException | IOException e) {
            LOG.error("Invalid callback response (job " + jobId + "), it will be ignored.", e);
        }

        return result;
    }

    private JobResult failure(String jobId) {

        JobResult result = new JobResult();
        result.setStatus(CallbackStatus.FAILURE);
        result.setJobId(jobId);

        return result;
    }

}
