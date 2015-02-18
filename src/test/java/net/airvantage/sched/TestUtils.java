package net.airvantage.sched;

import net.airvantage.sched.model.JobDef;
import net.airvantage.sched.model.JobLock;
import net.airvantage.sched.model.JobSchedulingDef;
import net.airvantage.sched.model.JobSchedulingType;
import net.airvantage.sched.model.JobState;

public class TestUtils {

    public  static JobState cronJobState(String id) {
        JobState jobDef = new JobState();
        jobDef.setId(id);
        jobDef.setUrl("http://test/api/test");
        jobDef.setScheduling(cronJobSchedulingDef());
        jobDef.setLock(new JobLock());
        return jobDef;
    }
    
    
    public  static JobDef cronJobDef(String id) {
        JobDef jobDef = new JobDef();
        jobDef.setId(id);
        jobDef.setUrl("http://test/api/test");
        jobDef.setScheduling(cronJobSchedulingDef());
        return jobDef;
    }
    
    public static JobSchedulingDef cronJobSchedulingDef() {
        JobSchedulingDef schedDef = new JobSchedulingDef();
        schedDef.setType(JobSchedulingType.CRON);
        schedDef.setValue("0 0 6 1 1/12 ? *");
        schedDef.setTimeout(5000);
        return schedDef;
    }
    
}
