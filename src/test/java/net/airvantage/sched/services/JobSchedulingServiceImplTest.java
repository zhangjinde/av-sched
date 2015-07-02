package net.airvantage.sched.services;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import net.airvantage.sched.TestUtils;
import net.airvantage.sched.app.exceptions.AppException;
import net.airvantage.sched.dao.JobConfigDao;
import net.airvantage.sched.dao.JobLockDao;
import net.airvantage.sched.dao.JobSchedulingDao;
import net.airvantage.sched.model.JobConfig;
import net.airvantage.sched.model.JobDef;
import net.airvantage.sched.model.JobScheduling;
import net.airvantage.sched.quartz.job.PostHttpJob;
import net.airvantage.sched.services.impl.JobSchedulingServiceImpl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerKey;

public class JobSchedulingServiceImplTest {

    private JobSchedulingServiceImpl service;

    @Mock
    private Scheduler scheduler;

    @Mock
    private JobStateService jobStateService;

    @Mock
    private JobConfigDao jobConfigDao;

    @Mock
    private JobLockDao jobLockDao;

    @Mock
    private JobSchedulingDao jobSchedulingDao;

    @Before
    public void setUp() {

        MockitoAnnotations.initMocks(this);
        service = new JobSchedulingServiceImpl(scheduler, jobStateService, jobConfigDao, jobLockDao, jobSchedulingDao);
    }

    @Test
    public void scheduleJob_withCron() throws Exception {

        // INPUT

        String jobId = "jobid";
        JobDef jobDef = TestUtils.cronJobDef(jobId, "0 0 6 1 1/12 ? *");

        // RUN

        service.scheduleJob(jobDef);

        // VERIFY

        Mockito.verify(jobConfigDao).persist(Mockito.eq(jobDef.getConfig()));

        ArgumentCaptor<JobDetail> detailCaptor = ArgumentCaptor.forClass(JobDetail.class);
        Mockito.verify(this.scheduler).addJob(detailCaptor.capture(), Mockito.eq(true));

        ArgumentCaptor<Trigger> triggerCaptor = ArgumentCaptor.forClass(Trigger.class);
        Mockito.verify(this.scheduler).scheduleJob(triggerCaptor.capture());

        JobDetail detail = detailCaptor.getValue();
        Assert.assertEquals(PostHttpJob.class, detail.getJobClass());
        Assert.assertEquals(jobId, detail.getKey().getName());

        Trigger trigger = triggerCaptor.getValue();
        Assert.assertEquals(CronScheduleBuilder.class, trigger.getScheduleBuilder().getClass());
        Assert.assertEquals(detail.getKey(), trigger.getJobKey());
    }

    @Test
    public void scheduleJob_withDate() throws Exception {

        // INPUT

        String jobId = "jobid";
        long startAt = System.currentTimeMillis();

        JobDef jobDef = TestUtils.dateJobDef(jobId, startAt);

        // RUN

        service.scheduleJob(jobDef);

        // VERIFY

        Mockito.verify(jobConfigDao).persist(Mockito.eq(jobDef.getConfig()));

        ArgumentCaptor<JobDetail> detailCaptor = ArgumentCaptor.forClass(JobDetail.class);
        Mockito.verify(this.scheduler).addJob(detailCaptor.capture(), Mockito.eq(true));
        
        ArgumentCaptor<Trigger> triggerCaptor = ArgumentCaptor.forClass(Trigger.class);
        Mockito.verify(this.scheduler).scheduleJob(triggerCaptor.capture());

        JobDetail detail = detailCaptor.getValue();
        Assert.assertEquals(PostHttpJob.class, detail.getJobClass());
        Assert.assertEquals(jobId, detail.getKey().getName());

        Trigger trigger = triggerCaptor.getValue();
        Assert.assertEquals(SimpleScheduleBuilder.class, trigger.getScheduleBuilder().getClass());
        Assert.assertEquals(startAt, trigger.getStartTime().getTime());
        Assert.assertEquals(detail.getKey(), trigger.getJobKey());
    }

    @Test
    public void scheduleJob_alreadyExists() throws Exception {

        // INPUT

        String jobId = "jobid";
        long startAt = System.currentTimeMillis();

        JobDef jobDef = TestUtils.dateJobDef(jobId, startAt);
        
        // MOCK
        
        Mockito.when(scheduler.checkExists(Mockito.any(TriggerKey.class))).thenReturn(true);

        // RUN

        service.scheduleJob(jobDef);

        // VERIFY

        Mockito.verify(jobConfigDao).persist(Mockito.eq(jobDef.getConfig()));

        ArgumentCaptor<JobDetail> detailCaptor = ArgumentCaptor.forClass(JobDetail.class);
        Mockito.verify(this.scheduler).addJob(detailCaptor.capture(), Mockito.eq(true));
        
        ArgumentCaptor<Trigger> triggerCaptor = ArgumentCaptor.forClass(Trigger.class);
        Mockito.verify(this.scheduler).rescheduleJob(Mockito.any(TriggerKey.class), triggerCaptor.capture());

        JobDetail detail = detailCaptor.getValue();
        Assert.assertEquals(PostHttpJob.class, detail.getJobClass());
        Assert.assertEquals(jobId, detail.getKey().getName());

        Trigger trigger = triggerCaptor.getValue();
        Assert.assertEquals(SimpleScheduleBuilder.class, trigger.getScheduleBuilder().getClass());
        Assert.assertEquals(startAt, trigger.getStartTime().getTime());
        Assert.assertEquals(detail.getKey(), trigger.getJobKey());
    }

    @Test
    public void scheduleJob_withInvalidCron() throws Exception {

        // INPUT

        String jobId = "jobid";
        JobDef jobDef = TestUtils.cronJobDef(jobId, "foo");

        // RUN

        try {
            service.scheduleJob(jobDef);
            fail("Cron expression should have been refused");

        } catch (AppException e) {
            assertThat(e.getError()).is("invalid.schedule.value");
            assertThat(e.getParams().get(0)).is("foo");
        }
    }

    // TODO(pht) test that validation raises exceptions on invalid job def
    // TODO(pht) maybe, just one change detector test for schedule Job ?

    @Test
    public void ackJob_cronJob() throws Exception {

        // INPUT

        String jobId = "jobid";

        // MOCK

        JobConfig config = Mockito.mock(JobConfig.class);
        JobScheduling schedConf = Mockito.mock(JobScheduling.class);

        Mockito.when(jobConfigDao.find(Mockito.eq(jobId))).thenReturn(config);
        Mockito.when(jobSchedulingDao.find(Mockito.eq(jobId))).thenReturn(schedConf);

        // RUN

        service.ackJob(jobId);

        // VERFIY

        Mockito.verify(jobStateService).unlockJob(jobId);
    }

    @Test
    public void ackJob_dateJob() throws Exception {

        // INPUT

        String jobId = "jobid";

        // MOCK

        JobConfig config = Mockito.mock(JobConfig.class);

        Mockito.when(jobConfigDao.find(Mockito.eq(jobId))).thenReturn(config);
        Mockito.when(jobSchedulingDao.find(Mockito.eq(jobId))).thenReturn(null);

        // RUN

        service.ackJob(jobId);

        // VERFIY

        Mockito.verify(jobConfigDao).delete(jobId);
        Mockito.verify(jobLockDao).delete(jobId);
    }

}
