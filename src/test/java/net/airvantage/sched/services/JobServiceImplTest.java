package net.airvantage.sched.services;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import net.airvantage.sched.TestUtils;
import net.airvantage.sched.app.AppException;
import net.airvantage.sched.dao.JobStateDao;
import net.airvantage.sched.model.JobSchedulingDef;
import net.airvantage.sched.model.JobSchedulingType;

import org.junit.Test;
import org.mockito.Mockito;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.ScheduleBuilder;
import org.quartz.Scheduler;
import org.quartz.Trigger;

public class JobServiceImplTest {

    @Test
    public void testCanGetSchedulingFromCronJobDef() throws Exception {
        JobSchedulingDef schedDef = TestUtils.cronJobSchedulingDef();
        ScheduleBuilder<? extends Trigger> builder = JobServiceImpl.scheduleBuilder(schedDef);
        assertThat(builder).isInstanceOf(CronScheduleBuilder.class);
    }

    @Test
    public void testThrowsWhenCronExpressionIsInvalid() throws Exception {
        JobSchedulingDef schedDef = new JobSchedulingDef();
        schedDef.setType(JobSchedulingType.CRON);
        schedDef.setValue("foo");
        try {
            JobServiceImpl.scheduleBuilder(schedDef);
            fail("Cron expression should have been refused");
        } catch (AppException e) {
            assertThat(e.getError()).is("invalid.schedule.value");
            assertThat(e.getParams().get(0)).is("foo");
        }
    }
    
    @Test
    public void testCreatesTriggerFromDef() throws Exception {
        Trigger trigger = JobServiceImpl.jobDefToTrigger(TestUtils.cronJobDef("av-server/timers"));
        assertThat(trigger).isInstanceOf(CronTrigger.class);
    }
    
    // TODO(pht) test that validation raises exceptions on invalid job def
    
    // TODO(pht) maybe, just one change detector test for schedule Job ?
    
   
    // Is this a "change-detector" test ?
    @Test
    public void tetUnlocksLockedJobAtScheduling() throws Exception {
        
        Scheduler sched = Mockito.mock(Scheduler.class);
        JobStateDao dao = Mockito.mock(JobStateDao.class);
        
        JobServiceImpl service = new JobServiceImpl(sched, dao);
        
        service.ackJob("av-server/timers");
        
        Mockito.verify(dao).unlockJob("av-server/timers");
    }
   
}
