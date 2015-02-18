package net.airvantage.sched.quartz;

import net.airvantage.sched.TestUtils;
import net.airvantage.sched.dao.JobStateDao;
import net.airvantage.sched.model.JobState;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicStatusLine;
import org.junit.Test;
import org.mockito.Mockito;

public class PostHttpJobTest {

    @Test
    public void testPostToUrlFromJobDef() throws Exception {
        
        HttpClient http = Mockito.mock(HttpClient.class);
        JobStateDao jobStateDao = Mockito.mock(JobStateDao.class);
        
        JobState jobState = TestUtils.cronJobState("av-server/foo");
        Mockito.when(jobStateDao.findJobState("av-server/foo")).thenReturn(jobState);
        
        StatusLine sl = new BasicStatusLine(new ProtocolVersion("http", 1, 1), 200, null);
        HttpResponse response = Mockito.mock(HttpResponse.class);
        Mockito.when(response.getStatusLine()).thenReturn(sl);
        
        Mockito.when(http.execute(Mockito.any(HttpPost.class))).thenReturn(response);
        
        PostHttpJob job = new PostHttpJob(http, jobStateDao);
        
        job.doJob("av-server/foo");
        
        Mockito.verify(http).execute(Mockito.any(HttpPost.class));
        Mockito.verify(jobStateDao).lockJob("av-server/foo");
    }
    
}
