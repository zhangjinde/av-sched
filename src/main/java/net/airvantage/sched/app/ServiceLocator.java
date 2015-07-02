package net.airvantage.sched.app;

import javax.sql.DataSource;

import net.airvantage.sched.app.mapper.JsonMapper;
import net.airvantage.sched.conf.ConfigurationManager;
import net.airvantage.sched.conf.Keys;
import net.airvantage.sched.dao.JobConfigDao;
import net.airvantage.sched.dao.JobLockDao;
import net.airvantage.sched.dao.JobSchedulingDao;
import net.airvantage.sched.db.SchemaMigrator;
import net.airvantage.sched.quartz.LockTriggerListener;
import net.airvantage.sched.quartz.QuartzClusteredSchedulerFactory;
import net.airvantage.sched.quartz.RetryJobListener;
import net.airvantage.sched.services.JobSchedulingService;
import net.airvantage.sched.services.JobStateService;
import net.airvantage.sched.services.RetryPolicyService;
import net.airvantage.sched.services.impl.JobSchedulingServiceImpl;
import net.airvantage.sched.services.impl.JobStateServiceImpl;
import net.airvantage.sched.services.impl.RetryPolicyServiceImpl;

import org.apache.commons.configuration.Configuration;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerListener;

public class ServiceLocator {

    // Do not use everywhere, only in things like quartz jobs & servlets.
    private static ServiceLocator instance;

    public static ServiceLocator getInstance() {
        if (instance == null) {
            instance = new ServiceLocator();
        }
        return instance;
    }

    private ConfigurationManager configManager;
    private Scheduler scheduler;
    private SchemaMigrator schemaMigrator;
    private DataSource dataSource;
    private JsonMapper jsonMapper;
    private CloseableHttpClient httpClient;

    private JobStateService jobStateSerice;
    private JobSchedulingService jobService;
    private RetryPolicyService retryPolicyService;

    private JobSchedulingDao jobSchedulingDao;
    private JobConfigDao jobConfigDao;
    private JobLockDao jobLockDao;

    public void init() {
        instance = this;
        configManager = new ConfigurationManager();
    }

    public JsonMapper getJsonMapper() {

        if (jsonMapper == null) {
            jsonMapper = new JsonMapper();
        }

        return jsonMapper;
    }

    public JobSchedulingService getJobService() throws SchedulerException {

        if (jobService == null) {
            jobService = new JobSchedulingServiceImpl(getScheduler(), getJobStateService(), getJobConfigDao(),
                    getJobLockDao(), getJobSchedulingDao());
        }
        return jobService;
    }

    public RetryPolicyService getRetryPolicyService() throws SchedulerException {

        if (retryPolicyService == null) {
            retryPolicyService = new RetryPolicyServiceImpl(getJobStateService(), getJobService());
        }
        return retryPolicyService;
    }

    public ConfigurationManager getConfigManager() {
        return configManager;
    }

    public JobStateService getJobStateService() throws SchedulerException {
        if (jobStateSerice == null) {
            jobStateSerice = new JobStateServiceImpl(getJobConfigDao(), getJobLockDao(), getJobSchedulingDao());

        }
        return jobStateSerice;
    }

    public JobSchedulingDao getJobSchedulingDao() throws SchedulerException {
        if (jobSchedulingDao == null) {
            jobSchedulingDao = new JobSchedulingDao(getScheduler());

        }
        return jobSchedulingDao;
    }

    public JobLockDao getJobLockDao() throws SchedulerException {
        if (jobLockDao == null) {
            jobLockDao = new JobLockDao(getDataSource());

        }
        return jobLockDao;
    }

    public JobConfigDao getJobConfigDao() throws SchedulerException {
        if (jobConfigDao == null) {
            jobConfigDao = new JobConfigDao(getDataSource());

        }
        return jobConfigDao;
    }

    public CloseableHttpClient getHttpClient() {
        if (httpClient == null) {
            httpClient = HttpClientBuilder.create().disableContentCompression().setMaxConnPerRoute(10)
                    .setMaxConnTotal(25).evictExpiredConnections().build();
        }
        return httpClient;
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
            // Attempt to fix https://github.com/AirVantage/av-sched/issues/6
            ds.setAutoReconnect(true);

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
            scheduler.getListenerManager().addJobListener(getRetryJobListener());
        }
        return scheduler;
    }

    private TriggerListener getLockTriggerListener() throws SchedulerException {
        return new LockTriggerListener(getJobStateService());
    }

    private JobListener getRetryJobListener() throws SchedulerException {
        return new RetryJobListener(getRetryPolicyService());
    }

    public String getSchedSecret() {
        return getConfigManager().get().getString("av-sched.secret");
    }

    public int getPort() {
        return getConfigManager().get().getInt("av-sched.port");
    }
}
