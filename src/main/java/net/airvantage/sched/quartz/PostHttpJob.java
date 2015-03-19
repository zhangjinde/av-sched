package net.airvantage.sched.quartz;

import net.airvantage.sched.app.ServiceLocator;
import net.airvantage.sched.dao.JobStateDao;
import net.airvantage.sched.model.JobState;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostHttpJob implements Job {

    private static final Logger LOG = LoggerFactory.getLogger(PostHttpJob.class);
    
    private HttpClient http;
    private JobStateDao jobStateDao;
    private String schedSecret;

    
    public PostHttpJob() {
        this(ServiceLocator.getInstance().getHttpClient(),
                ServiceLocator.getInstance().getJobStateDao(),
                ServiceLocator.getInstance().getSchedSecret());
    }
    
    protected PostHttpJob(HttpClient httpClient, JobStateDao jobStateDao, String schedSecret) {
        this.http = httpClient;
        this.jobStateDao = jobStateDao;
        this.schedSecret = schedSecret;
    }
    
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String jobId = context.getJobDetail().getKey().getName();
        doJob(jobId);
    }

    protected void doJob(String jobId) throws JobExecutionException {
        try {
            JobState jobState = this.jobStateDao.findJobState(jobId);
            String url = jobState.getConfig().getUrl();
            LOG.debug("Will post to url", url);
            HttpPost request = new HttpPost(url);
            request.setHeader("X-Sched-secret", schedSecret);
            HttpResponse response = this.http.execute(request);
            if (response.getStatusLine().getStatusCode() == 200) {
                jobStateDao.lockJob(jobId);
            } else {
                // TODO(pht) notify logical "logging" system
                LOG.error("TODO(pht) What do we want, again ?");
            }
        } catch (Exception e) {
            // The host might just be closed, that will happen a lot in tests...
            LOG.error("Unable to post to url", e);
            // TODO(pht) notify "logging" logical system
        }
    }
    
    
    
}
