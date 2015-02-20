package net.airvantage.sched.quartz;

import java.util.List;

import net.airvantage.sched.TestUtils;
import net.airvantage.sched.dao.JobStateDao;
import net.airvantage.sched.model.JobState;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static com.google.common.truth.Truth.assertThat;

public class PostHttpJobTest {

    @Mock
    HttpClient http;
    
    @Mock
    JobStateDao jobStateDao;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void testPostToUrlFromJobDef() throws Exception {
    
        PostHttpJob job = new PostHttpJob(http, jobStateDao, "secret");
        
        JobState jobState = TestUtils.cronJobState("av-server/foo");
        Mockito.when(jobStateDao.findJobState("av-server/foo")).thenReturn(jobState);
        
        HttpResponse response = prepareResponse();
        Mockito.when(http.execute(Mockito.any(HttpPost.class))).thenReturn(response);
        
        job.doJob("av-server/foo");
        
        Mockito.verify(http).execute(Mockito.any(HttpPost.class));
        Mockito.verify(jobStateDao).lockJob("av-server/foo");
    }

    @Test
    public void testPostWithSecretInHeader() throws Exception {

        PostHttpJob job = new PostHttpJob(http, jobStateDao, "secret");
        
        JobState jobState = TestUtils.cronJobState("av-server/foo");
        Mockito.when(jobStateDao.findJobState("av-server/foo")).thenReturn(jobState);
        
        HttpResponse response = prepareResponse();
        Mockito.when(http.execute(Mockito.any(HttpPost.class))).thenReturn(response);
        
        ArgumentCaptor<HttpPost> captor = ArgumentCaptor.forClass(HttpPost.class);
        
        job.doJob("av-server/foo");

        Mockito.verify(http).execute(captor.capture());
        
        List<HttpPost> posts = captor.getAllValues();
        HttpPost post = posts.get(0);
        assertThat(post.getHeaders("X-Sched-Secret")[0].getValue()).isEqualTo("secret");
    }
  
    private HttpResponse prepareResponse() {
        StatusLine sl = new BasicStatusLine(new ProtocolVersion("http", 1, 1), 200, null);
        HttpResponse response = new BasicHttpResponse(sl);
        return response;
    }
    
}
