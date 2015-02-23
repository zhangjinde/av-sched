package net.airvantage.sched.quartz;

import java.util.Properties;

import org.apache.commons.configuration.Configuration;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

import com.mysql.jdbc.Driver;

public class QuartzClusteredSchedulerFactory {

    public static final Scheduler buildScheduler(Configuration config) throws SchedulerException {

        StdSchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();

        Properties props = new Properties();

        // General
        props.put(StdSchedulerFactory.PROP_SCHED_INSTANCE_NAME, "av_sched_clustered");
        props.put(StdSchedulerFactory.PROP_SCHED_INSTANCE_ID, "AUTO");

        // Thread pooling ?
        props.put("org.quartz.threadPool.class", org.quartz.simpl.SimpleThreadPool.class.getName());
        props.put("org.quartz.threadPool.threadCount", "25");
        props.put("org.quartz.threadPool.threadPriority", "5");

        // JobStore
        props.put("org.quartz.jobStore.misfireThreshold", "60000");

        props.put("org.quartz.jobStore.class", "org.quartz.impl.jdbcjobstore.JobStoreTX");
        props.put("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.StdJDBCDelegate");
        props.put("org.quartz.jobStore.useProperties", false);
        props.put("org.quartz.jobStore.dataSource", "sched");
        props.put("org.quartz.jobStore.tablePrefix", "QRTZ_");

        props.put("org.quartz.jobStore.isClustered", "true");
        props.put("org.quartz.jobStore.clusterCheckinInterval", "20000");

        // DB
        String jdbcUrl = "jdbc:mysql://" + config.getString("av-sched.db.server") + ":"
                + config.getString("av-sched.db.port") + "/av_sched";
        
        props.put("org.quartz.dataSource.sched.driver", Driver.class.getName());
        props.put("org.quartz.dataSource.sched.URL", jdbcUrl);
        props.put("org.quartz.dataSource.sched.user", config.getString("av-sched.db.user"));
        props.put("org.quartz.dataSource.sched.password", config.getString("av-sched.db.password"));
        // ? props.put("org.quartz.jobStore.dataSource.sched.maxConnections", ?);
        // ? props.put("org.quartz.jobStore.dataSource.sched.validationQuery", ?);

        schedFact.initialize(props);
        Scheduler scheduler = schedFact.getScheduler();
        scheduler.start();
        return scheduler;
    }

}
