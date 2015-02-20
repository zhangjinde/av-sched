package net.airvantage.sched.app;

import net.airvantage.sched.dao.DummyJobStateDao;
import net.airvantage.sched.dao.JobStateDao;
import net.airvantage.sched.dao.JobStateDaoImpl;
import net.airvantage.sched.quartz.LockTriggerListener;
import net.airvantage.sched.services.JobService;
import net.airvantage.sched.services.JobServiceImpl;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.TriggerListener;

public class ServiceLocator {

    private static JobService jobService;
    private static JobStateDao jobStateDao;
    private static Scheduler scheduler;
    
    public static JobService getJobService() throws SchedulerException {
        if (jobService == null) {
            jobService = new JobServiceImpl(getScheduler(), getJobStateDao());
        }
        return jobService;
    }

    public static JobStateDao getJobStateDao() {
        if (jobStateDao == null) {
            // jobStateDao = new JobStateDaoImpl();
            jobStateDao = new DummyJobStateDao();
        }
        return jobStateDao;
    }

    public static Scheduler getScheduler() throws SchedulerException {
        if (scheduler == null) {
            SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();
            scheduler = schedFact.getScheduler();
            scheduler.start();
            scheduler.getListenerManager().addTriggerListener(getLockTriggerListener());
        }
        return scheduler;
    }
    
    private static TriggerListener getLockTriggerListener() {
        return new LockTriggerListener(getJobStateDao());
    }

    public static HttpClient getHttpClient() {
        // TODO(pht) Is it safe to reuse the same httpClient everytime ?
        return HttpClients.createDefault();
    }
}
