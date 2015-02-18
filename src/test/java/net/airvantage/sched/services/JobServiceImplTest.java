package net.airvantage.sched.services;

import net.airvantage.sched.app.AppException;
import net.airvantage.sched.model.JobSchedulingType;
import net.airvantage.sched.model.jobDef.JobDef;
import net.airvantage.sched.model.jobDef.JobSchedulingDef;
import net.airvantage.sched.services.JobServiceImpl;

import org.junit.Test;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.ScheduleBuilder;
import org.quartz.Trigger;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

public class JobServiceImplTest {

    @Test
    public void testCanGetSchedulingFromCronJobDef() throws Exception {
        JobSchedulingDef schedDef = cronJobSchedulingDef();
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
        Trigger trigger = JobServiceImpl.jobDefToTrigger(cronJobDef());
        assertThat(trigger).isInstanceOf(CronTrigger.class);
    }
    
    // TODO(pht) test that validation raises exceptions on invalid job def
    
    // TODO(pht) maybe, just one change detector test ?
    
    private JobDef cronJobDef() {
        JobDef jobDef = new JobDef();
        jobDef.setId("av-server/timer");
        jobDef.setUrl("http://test/api/test");
        jobDef.setScheduling(cronJobSchedulingDef());
        return jobDef;
    }
    
    private JobSchedulingDef cronJobSchedulingDef() {
        JobSchedulingDef schedDef = new JobSchedulingDef();
        schedDef.setType(JobSchedulingType.CRON);
        schedDef.setValue("0 0 6 1 1/12 ? *");
        schedDef.setTimeout(5000);
        return schedDef;
    }
   
}
