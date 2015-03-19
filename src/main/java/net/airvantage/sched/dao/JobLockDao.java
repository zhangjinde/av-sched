package net.airvantage.sched.dao;

import java.sql.SQLException;
import java.util.Map;

import net.airvantage.sched.model.JobLock;

public interface JobLockDao {

    public abstract JobLock findJobLock(String id) throws SQLException;

    public abstract Map<String, JobLock> jobLocksById() throws SQLException;

    public abstract void saveLock(String id, Long expiresAt) throws SQLException;

    public abstract void removeLock(String id) throws SQLException;

    public abstract void removeAll() throws SQLException;

}