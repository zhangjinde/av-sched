package net.airvantage.sched.services;

import net.airvantage.sched.TestUtils;
import net.airvantage.sched.app.exceptions.AppException;
import net.airvantage.sched.dao.JobWakeupDao;
import net.airvantage.sched.model.JobDef;
import net.airvantage.sched.model.JobScheduling;
import net.airvantage.sched.model.JobState;
import net.airvantage.sched.quartz.job.JobResult;
import net.airvantage.sched.quartz.job.JobResult.CallbackStatus;
import net.airvantage.sched.services.impl.RetryPolicyHelper;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

@Ignore
public class RetryPolicyServiceImplTest {

    private RetryPolicyHelper service;

    @Mock
    private JobWakeupDao jobWakeupDao;

    @Mock
    private JobStateService jobStateService;

    @Mock
    private JobSchedulingService jobSchedulingService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        service = new RetryPolicyHelper(jobStateService, jobSchedulingService, jobWakeupDao);
    }

    @Test
    public void jobExecuted_cronJobSuccess() throws AppException {

        // INPUT

        String jobId = "jobid";
        JobState state = TestUtils.cronJobState(jobId);

        // MOCK

        JobResult result = Mockito.mock(JobResult.class);

        Mockito.when(result.getStatus()).thenReturn(CallbackStatus.SUCCESS);
        Mockito.when(result.getJobId()).thenReturn(jobId);
        Mockito.when(result.isAck()).thenReturn(false);
        Mockito.when(result.getRetry()).thenReturn(0l);

        Mockito.when(jobStateService.find(jobId)).thenReturn(state);

        // RUN

        // service.jobExecuted(result);

        // VERIFY

        Mockito.verifyNoMoreInteractions(jobSchedulingService);
    }

    @Test
    public void jobExecuted_dateJobSuccess() throws AppException {

        // INPUT

        String jobId = "jobid";
        JobState state = TestUtils.wakeupJobState(jobId);

        // MOCK

        JobResult result = Mockito.mock(JobResult.class);

        Mockito.when(result.getStatus()).thenReturn(CallbackStatus.SUCCESS);
        Mockito.when(result.getJobId()).thenReturn(jobId);
        Mockito.when(result.isAck()).thenReturn(false);
        Mockito.when(result.getRetry()).thenReturn(0l);

        Mockito.when(jobStateService.find(jobId)).thenReturn(state);

        // RUN

        // service.jobExecuted(result);

        // VERIFY

        ArgumentCaptor<JobScheduling> captor = ArgumentCaptor.forClass(JobScheduling.class);
        Mockito.verify(jobSchedulingService).rescheduleJob(Mockito.eq(jobId), captor.capture());

        JobScheduling conf = captor.getValue();
        Assert.assertTrue(System.currentTimeMillis() < conf.getStartAt());
    }

    @Test
    public void jobExecuted_jobAutoAck() throws AppException {

        // INPUT

        String jobId = "jobid";
        JobState state = TestUtils.wakeupJobState(jobId);

        // MOCK

        JobResult result = Mockito.mock(JobResult.class);

        Mockito.when(result.getStatus()).thenReturn(CallbackStatus.SUCCESS);
        Mockito.when(result.getJobId()).thenReturn(jobId);
        Mockito.when(result.isAck()).thenReturn(true);
        Mockito.when(result.getRetry()).thenReturn(0l);

        Mockito.when(jobStateService.find(jobId)).thenReturn(state);

        // RUN

        // service.jobExecuted(result);

        // VERIFY

        Mockito.verify(jobSchedulingService).ackJob(jobId);
    }

    @Test
    public void jobExecuted_jobRetryPeriod() throws AppException {

        // INPUT

        String jobId = "jobid";
        JobState state = TestUtils.wakeupJobState(jobId);

        long now = System.currentTimeMillis();
        long delay = 600_000l;

        // MOCK

        JobResult result = Mockito.mock(JobResult.class);

        Mockito.when(result.getStatus()).thenReturn(CallbackStatus.SUCCESS);
        Mockito.when(result.getJobId()).thenReturn(jobId);
        Mockito.when(result.isAck()).thenReturn(false);
        Mockito.when(result.getRetry()).thenReturn(delay);

        Mockito.when(jobStateService.find(jobId)).thenReturn(state);

        // RUN

        // service.jobExecuted(result);

        // VERIFY

        ArgumentCaptor<JobScheduling> captor = ArgumentCaptor.forClass(JobScheduling.class);
        Mockito.verify(jobSchedulingService).rescheduleJob(Mockito.eq(jobId), captor.capture());

        JobScheduling conf = captor.getValue();
        Assert.assertTrue((now + delay) <= conf.getStartAt());
    }

    @Test
    public void triggerComplete_cronJobFailed() throws AppException {

        // INPUT

        String jobId = "jobid";
        JobState state = TestUtils.cronJobState(jobId);

        // MOCK

        JobResult result = Mockito.mock(JobResult.class);

        Mockito.when(result.getStatus()).thenReturn(CallbackStatus.FAILURE);
        Mockito.when(result.getJobId()).thenReturn(jobId);
        Mockito.when(result.isAck()).thenReturn(false);
        Mockito.when(result.getRetry()).thenReturn(0l);

        Mockito.when(jobStateService.find(jobId)).thenReturn(state);

        // RUN

        // service.jobExecuted(result);

        // VERIFY

        Mockito.verifyNoMoreInteractions(jobSchedulingService);
    }

    @Test
    public void triggerComplete_dateJobFailed() throws AppException {

        // INPUT

        String jobId = "jobid";
        JobState state = TestUtils.wakeupJobState(jobId);

        // MOCK

        JobResult result = Mockito.mock(JobResult.class);

        Mockito.when(result.getStatus()).thenReturn(CallbackStatus.FAILURE);
        Mockito.when(result.getJobId()).thenReturn(jobId);
        Mockito.when(result.isAck()).thenReturn(false);
        Mockito.when(result.getRetry()).thenReturn(0l);

        Mockito.when(jobStateService.find(jobId)).thenReturn(state);

        // RUN

        // service.jobExecuted(result);

        // VERIFY

        ArgumentCaptor<JobScheduling> captor = ArgumentCaptor.forClass(JobScheduling.class);
        Mockito.verify(jobSchedulingService).rescheduleJob(Mockito.eq(jobId), captor.capture());

        JobScheduling conf = captor.getValue();
        Assert.assertTrue(System.currentTimeMillis() < conf.getStartAt());
    }

}
