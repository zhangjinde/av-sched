package net.airvantage.sched;

import net.airvantage.sched.model.JobConfig;
import net.airvantage.sched.model.JobDef;
import net.airvantage.sched.model.JobLock;
import net.airvantage.sched.model.JobSchedulingDef;
import net.airvantage.sched.model.JobSchedulingType;
import net.airvantage.sched.model.JobState;

public class TestUtils {

    public static JobState cronJobState(String id) {
        JobConfig config = new JobConfig();
        config.setId(id);
        config.setUrl("http://test/api/test");
        config.setTimeout(5000);

        JobState jobState = new JobState();
        jobState.setConfig(config);
        jobState.setLock(new JobLock());
        return jobState;
    }

    public static JobDef cronJobDef(String id) {
        JobConfig config = new JobConfig();
        config.setId(id);
        config.setUrl("http://test/api/test");
        config.setTimeout(5000);
        
        JobDef jobDef = new JobDef();
        jobDef.setConfig(config);
        jobDef.setScheduling(cronJobSchedulingDef());
        return jobDef;
    }

    public static JobSchedulingDef cronJobSchedulingDef() {
        JobSchedulingDef schedDef = new JobSchedulingDef();
        schedDef.setType(JobSchedulingType.CRON);
        schedDef.setValue("0 0 6 1 1/12 ? *");
        return schedDef;
    }

}
