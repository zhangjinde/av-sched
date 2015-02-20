package net.airvantage.sched.app;

import net.airvantage.sched.conf.ConfigurationManager;
import net.airvantage.sched.dao.DummyJobStateDao;
import net.airvantage.sched.dao.JobStateDao;
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

    // ---------- Singleton pattern ----------
    // Do not use everywhere, only in things like quartz jobs & servlets.
    private static ServiceLocator instance;

    public static ServiceLocator getInstance() {
        if (instance == null) {
            instance = new ServiceLocator();
        }
        return instance;
    }

    // ---------- Singleton pattern ----------

    private ConfigurationManager configManager;
    private JobService jobService;
    private JobStateDao jobStateDao;
    private Scheduler scheduler;

    public void init() {
        instance = this;
        configManager = new ConfigurationManager();
    }

    public JobService getJobService() throws SchedulerException {
        if (jobService == null) {
            jobService = new JobServiceImpl(getScheduler(), getJobStateDao());
        }
        return jobService;
    }

    public ConfigurationManager getConfigManager() {
        return configManager;
    }

    public JobStateDao getJobStateDao() {
        if (jobStateDao == null) {
            // jobStateDao = new JobStateDaoImpl();
            jobStateDao = new DummyJobStateDao();
        }
        return jobStateDao;
    }

    public Scheduler getScheduler() throws SchedulerException {
        if (scheduler == null) {
            SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();
            scheduler = schedFact.getScheduler();
            scheduler.start();
            scheduler.getListenerManager().addTriggerListener(getLockTriggerListener());
        }
        return scheduler;
    }

    private TriggerListener getLockTriggerListener() {
        return new LockTriggerListener(getJobStateDao());
    }

    public HttpClient getHttpClient() {
        // TODO(pht) Is it safe to reuse the same httpClient everytime ?
        return HttpClients.createDefault();
    }

    public String getSchedSecret() {
        return getConfigManager().get().getString("av-sched.secret");
    }

    public int getPort() {
        return getConfigManager().get().getInt("av-sched.port");
    }
}
