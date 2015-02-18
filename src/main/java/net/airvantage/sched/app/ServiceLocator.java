package net.airvantage.sched.app;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;

import net.airvantage.sched.dao.JobDefDao;
import net.airvantage.sched.dao.JobDefDaoImpl;
import net.airvantage.sched.services.JobService;
import net.airvantage.sched.services.JobServiceImpl;

public class ServiceLocator {

    private static JobService jobService;
    private static JobDefDao jobDefDao;
    private static Scheduler scheduler;
    
    public static JobService getJobService() throws SchedulerException {
        if (jobService == null) {
            jobService = new JobServiceImpl(getScheduler(), getJobDefDao());
        }
        return jobService;
    }

    private static JobDefDao getJobDefDao() {
        if (jobDefDao == null) {
            jobDefDao = new JobDefDaoImpl();
        }
        return jobDefDao;
    }

    private static Scheduler getScheduler() throws SchedulerException {
        if (scheduler == null) {
            SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();
            scheduler = schedFact.getScheduler();
            scheduler.start();
        }
        return scheduler;
    }
    
}
