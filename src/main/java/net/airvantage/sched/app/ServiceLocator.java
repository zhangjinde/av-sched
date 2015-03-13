package net.airvantage.sched.app;

import javax.sql.DataSource;

import net.airvantage.sched.conf.ConfigurationManager;
import net.airvantage.sched.conf.Keys;
import net.airvantage.sched.dao.JobStateDao;
import net.airvantage.sched.dao.JobStateDaoImpl;
import net.airvantage.sched.db.SchemaMigrator;
import net.airvantage.sched.quartz.LockTriggerListener;
import net.airvantage.sched.quartz.QuartzClusteredSchedulerFactory;
import net.airvantage.sched.services.JobService;
import net.airvantage.sched.services.JobServiceImpl;

import org.apache.commons.configuration.Configuration;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.TriggerListener;
import org.quartz.impl.StdSchedulerFactory;

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
    private SchemaMigrator schemaMigrator;
    private DataSource dataSource;

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
            DataSource dataSource = getDataSource();
            jobStateDao = new JobStateDaoImpl(new JobConfigDaoImpl(dataSource), new JobLockDaoImpl(dataSource));

            // jobStateDao = new DummyJobStateDao();
        }
        return jobStateDao;
    }

    private DataSource getDataSource() {
        if (dataSource == null) {
            Configuration config = getConfigManager().get();
            com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource ds = new com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource();
            ds.setServerName(config.getString(Keys.Db.SERVER));
            ds.setPortNumber(config.getInt(Keys.Db.PORT));
            ds.setDatabaseName(config.getString(Keys.Db.DB_NAME));
            ds.setUser(config.getString(Keys.Db.USER));
            ds.setPassword(config.getString(Keys.Db.PASSWORD));
            dataSource = ds;
        }
        return dataSource;
    }

    public SchemaMigrator getSchemaMigrator() throws Exception {
        if (schemaMigrator == null) {
            schemaMigrator = new SchemaMigrator(getDataSource());
        }
        return schemaMigrator;
    }

    public Scheduler getScheduler() throws SchedulerException {
        if (scheduler == null) {
            
            scheduler = QuartzClusteredSchedulerFactory.buildScheduler(getConfigManager().get());
           
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
