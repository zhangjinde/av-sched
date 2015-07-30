package net.airvantage.sched.quartz.job;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import net.airvantage.sched.TestUtils;
import net.airvantage.sched.app.mapper.JsonMapper;
import net.airvantage.sched.model.JobState;
import net.airvantage.sched.model.PostHttpJobResult;
import net.airvantage.sched.services.JobStateService;
import net.airvantage.sched.services.impl.JobExecutionHelper;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;

@Ignore
public class HttpClientServiceTest {

    private JobExecutionHelper service;

    @Mock
    private CloseableHttpClient httpClient;

    @Mock
    private JobStateService jobStateDao;

    @Mock
    private JsonMapper jsonMapper;

    @Before
    public void setUp() {

        MockitoAnnotations.initMocks(this);
        service = new JobExecutionHelper(httpClient, "secret", jsonMapper);
    }

    @Test
    public void testPostToUrl_success() throws Exception {

        // INPUT

        String jobId = "jobid";

        // MOCK

        JobExecutionContext context = Mockito.mock(JobExecutionContext.class);
        JobDetail detail = Mockito.mock(JobDetail.class);
        JobKey key = new JobKey(jobId);

        Mockito.when(context.getJobDetail()).thenReturn(detail);
        Mockito.when(detail.getKey()).thenReturn(key);

        JobState jobState = TestUtils.cronJobState(jobId);
        Mockito.when(jobStateDao.find(jobId)).thenReturn(jobState);

        CloseableHttpResponse response = Mockito.mock(CloseableHttpResponse.class);
        StatusLine statusLine = Mockito.mock(StatusLine.class);

        Mockito.when(httpClient.execute(Mockito.any(HttpPost.class))).thenReturn(response);
        Mockito.when(response.getStatusLine()).thenReturn(statusLine);
        Mockito.when(statusLine.getStatusCode()).thenReturn(200);

        // RUN

        // service.executeConJob(jobId);

        // VERIFY

        ArgumentCaptor<HttpPost> captor = ArgumentCaptor.forClass(HttpPost.class);
        Mockito.verify(httpClient).execute(captor.capture());
        List<HttpPost> posts = captor.getAllValues();
        HttpPost post = posts.get(0);
        assertThat(post.getHeaders("X-Sched-Secret")[0].getValue()).isEqualTo("secret");

        Mockito.verify(jobStateDao).lockJob(Mockito.eq(jobId));

        ArgumentCaptor<JobResult> resultCaptor = ArgumentCaptor.forClass(JobResult.class);
        Mockito.verify(context).setResult(resultCaptor.capture());

        JobResult result = resultCaptor.getValue();
        Assert.assertEquals(JobResult.CallbackStatus.SUCCESS, result.getStatus());
        Assert.assertEquals(jobId, result.getJobId());
    }

    @Test
    public void testPostToUrl_withDirective() throws Exception {

        // INPUT

        String jobId = "jobid";

        PostHttpJobResult callbackResult = new PostHttpJobResult();
        callbackResult.setAck(true);
        callbackResult.setRetry(123456789l);

        // MOCK

        JobExecutionContext context = Mockito.mock(JobExecutionContext.class);
        JobDetail detail = Mockito.mock(JobDetail.class);
        JobKey key = new JobKey(jobId);
        JobDataMap datamap = Mockito.mock(JobDataMap.class);

        Mockito.when(context.getJobDetail()).thenReturn(detail);
        Mockito.when(detail.getKey()).thenReturn(key);
        Mockito.when(detail.getJobDataMap()).thenReturn(datamap);

        JobState jobState = TestUtils.cronJobState(jobId);
        Mockito.when(jobStateDao.find(jobId)).thenReturn(jobState);

        CloseableHttpResponse response = Mockito.mock(CloseableHttpResponse.class);
        StatusLine statusLine = Mockito.mock(StatusLine.class);
        HttpEntity httpEntity = Mockito.mock(HttpEntity.class);

        Mockito.when(httpClient.execute(Mockito.any(HttpPost.class))).thenReturn(response);
        Mockito.when(response.getStatusLine()).thenReturn(statusLine);
        Mockito.when(response.getEntity()).thenReturn(httpEntity);
        Mockito.when(statusLine.getStatusCode()).thenReturn(200);
        Mockito.when(httpEntity.getContent()).thenReturn(Mockito.mock(InputStream.class));

        Mockito.when(jsonMapper.postHttpJobResult(Mockito.any(InputStream.class))).thenReturn(callbackResult);

        // RUN

        // service.executeConJob(jobId);

        // VERIFY

        ArgumentCaptor<HttpPost> captor = ArgumentCaptor.forClass(HttpPost.class);
        Mockito.verify(httpClient).execute(captor.capture());
        List<HttpPost> posts = captor.getAllValues();
        HttpPost post = posts.get(0);
        assertThat(post.getHeaders("X-Sched-Secret")[0].getValue()).isEqualTo("secret");

        Mockito.verify(jobStateDao).lockJob(Mockito.eq(jobId));

        ArgumentCaptor<JobResult> resultCaptor = ArgumentCaptor.forClass(JobResult.class);
        Mockito.verify(context).setResult(resultCaptor.capture());

        JobResult result = resultCaptor.getValue();
        Assert.assertEquals(JobResult.CallbackStatus.SUCCESS, result.getStatus());
        Assert.assertEquals(jobId, result.getJobId());
        Assert.assertEquals(callbackResult.getAck(), result.isAck());
        Assert.assertEquals(callbackResult.getRetry().longValue(), result.getRetry());
    }

    @Test
    public void testPostToUrl_failure() throws Exception {

        // INPUT

        String jobId = "jobid";

        // MOCK

        JobExecutionContext context = Mockito.mock(JobExecutionContext.class);
        JobDetail detail = Mockito.mock(JobDetail.class);
        JobKey key = new JobKey(jobId);

        Mockito.when(context.getJobDetail()).thenReturn(detail);
        ;
        Mockito.when(detail.getKey()).thenReturn(key);

        JobState jobState = TestUtils.cronJobState(jobId);
        Mockito.when(jobStateDao.find(jobId)).thenReturn(jobState);

        CloseableHttpResponse response = Mockito.mock(CloseableHttpResponse.class);
        StatusLine statusLine = Mockito.mock(StatusLine.class);

        Mockito.when(httpClient.execute(Mockito.any(HttpPost.class))).thenReturn(response);
        Mockito.when(response.getStatusLine()).thenReturn(statusLine);
        Mockito.when(statusLine.getStatusCode()).thenReturn(500);

        // RUN

        // service.executeConJob(jobId);

        // VERIFY

        ArgumentCaptor<HttpPost> captor = ArgumentCaptor.forClass(HttpPost.class);
        Mockito.verify(httpClient).execute(captor.capture());
        List<HttpPost> posts = captor.getAllValues();
        HttpPost post = posts.get(0);
        assertThat(post.getHeaders("X-Sched-Secret")[0].getValue()).isEqualTo("secret");

        ArgumentCaptor<JobResult> resultCaptor = ArgumentCaptor.forClass(JobResult.class);
        Mockito.verify(context).setResult(resultCaptor.capture());

        JobResult result = resultCaptor.getValue();
        Assert.assertEquals(JobResult.CallbackStatus.FAILURE, result.getStatus());
        Assert.assertEquals(jobId, result.getJobId());
    }

    @Test
    public void testPostToUrl_connRefuse() throws Exception {

        // INPUT

        String jobId = "jobid";

        // MOCK

        JobExecutionContext context = Mockito.mock(JobExecutionContext.class);
        JobDataMap datamap = Mockito.mock(JobDataMap.class);
        JobDetail detail = Mockito.mock(JobDetail.class);
        JobKey key = new JobKey(jobId);

        Mockito.when(context.getJobDetail()).thenReturn(detail);
        Mockito.when(detail.getJobDataMap()).thenReturn(datamap);
        Mockito.when(detail.getKey()).thenReturn(key);

        JobState jobState = TestUtils.cronJobState(jobId);
        Mockito.when(jobStateDao.find(jobId)).thenReturn(jobState);

        Mockito.when(httpClient.execute(Mockito.any(HttpPost.class))).thenThrow(new IOException("connection refuse"));

        // RUN

        // service.executeConJob(jobId);

        // VERIFY

        ArgumentCaptor<HttpPost> captor = ArgumentCaptor.forClass(HttpPost.class);
        Mockito.verify(httpClient).execute(captor.capture());
        List<HttpPost> posts = captor.getAllValues();
        HttpPost post = posts.get(0);
        assertThat(post.getHeaders("X-Sched-Secret")[0].getValue()).isEqualTo("secret");

        ArgumentCaptor<JobResult> resultCaptor = ArgumentCaptor.forClass(JobResult.class);
        Mockito.verify(context).setResult(resultCaptor.capture());

        JobResult result = resultCaptor.getValue();
        Assert.assertEquals(JobResult.CallbackStatus.FAILURE, result.getStatus());
        Assert.assertEquals(jobId, result.getJobId());
    }

}
