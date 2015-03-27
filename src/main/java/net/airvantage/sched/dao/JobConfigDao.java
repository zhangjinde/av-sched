package net.airvantage.sched.dao;

import java.sql.SQLException;
import java.util.Map;

import net.airvantage.sched.model.JobConfig;

public interface JobConfigDao {

    public abstract void saveJobConfig(JobConfig config) throws SQLException;

    public abstract void removeJobConfig(String jobId) throws SQLException;

    public abstract JobConfig findJobConfig(String id) throws SQLException;

    public abstract Map<String, JobConfig> jobConfigsById() throws SQLException;

    public abstract void removeAll() throws SQLException;

}
