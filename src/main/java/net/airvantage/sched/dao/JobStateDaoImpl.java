package net.airvantage.sched.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import javax.sql.DataSource;

import net.airvantage.sched.app.AppException;
import net.airvantage.sched.model.JobConfig;
import net.airvantage.sched.model.JobDef;
import net.airvantage.sched.model.JobLock;
import net.airvantage.sched.model.JobState;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobStateDaoImpl implements JobStateDao {

    private static final Logger LOG = LoggerFactory.getLogger(JobStateDaoImpl.class);

    private QueryRunner queryRunner;

    public JobStateDaoImpl(DataSource dataSource) {
        this.queryRunner = new QueryRunner(dataSource);
    }

    @Override
    public void saveJobDef(JobDef jobDef) throws AppException {

        try {
            JobConfig config = jobDef.getConfig();
            queryRunner.update("delete from sched_job_configs where id=?", config.getId());
            queryRunner.update("insert into sched_job_configs(id,url,timeout) values(?,?,?)", config.getId(),
                    config.getUrl(), 
                    config.getTimeout());

        } catch (SQLException e) {
            LOG.error(String.format("Unable to store jobDef {}", jobDef), e);
            throw AppException.serverError(e);
        }

    }

    @Override
    public JobState findJobState(String id) throws AppException {

        JobConfig config = null;
        JobLock lock= null;
        JobState state = null;
        try {
            config = findJobConfig(id);
            lock = findJobLock(id);
        } catch (SQLException e) {
            LOG.error(String.format("Unable to find job state with id", id), e);
            throw AppException.serverError(e);
        }
        if (config != null) {
            state = new JobState();
            state.setConfig(config);
            state.setLock(lock);
        }
        
        return state;
    }

    @Override
    public void lockJob(String id) throws AppException {
        try {
            JobState jobState = findJobState(id);
            if (jobState != null) {
                Long expiresAt = new Date().getTime() + jobState.getConfig().getTimeout();
                LOG.debug("Will save expiration date" + expiresAt);
                Timestamp ts = new Timestamp(expiresAt);
                queryRunner.update("insert into sched_job_locks(id, expires_at) values(?,?)", id, ts);
            }
        } catch (SQLException e) {
            LOG.error(String.format("Unable to lock job state with id", id), e);
            throw AppException.serverError(e);
        }

    }

    @Override
    public void unlockJob(String id) throws AppException {
        try {
            JobState jobState = findJobState(id);
            if (jobState != null) {
                queryRunner.update("delete from sched_job_locks where id=?", id);
            }
        } catch (SQLException e) {
            LOG.error(String.format("Unable to lock job state with id", id), e);
            throw AppException.serverError(e);
        }
    }
    
    private JobConfig findJobConfig(String id) throws SQLException {
        ResultSetHandler<JobConfig> rsh = new ResultSetHandler<JobConfig>() {
            @Override
            public JobConfig handle(ResultSet rs) throws SQLException {
                if (!rs.next()) {
                    return null;
                }
                JobConfig config = new JobConfig();
                config.setId((String) rs.getString(1));
                config.setUrl((String) rs.getString(2));
                config.setTimeout((Long) rs.getLong(3));
                return config;

            }
        };
        return queryRunner.query("select id,url,timeout from sched_job_configs where id=?", rsh, id);
    }

    private JobLock findJobLock(String id) throws SQLException {
        ResultSetHandler<JobLock> rsh = new ResultSetHandler<JobLock>() {
            @Override
            public JobLock handle(ResultSet rs) throws SQLException {
                if (!rs.next()) {
                    return new JobLock();
                } else {
                    JobLock lock = new JobLock();
                    lock.setLocked(true);
                    Timestamp expiresAt = rs.getTimestamp(2);
                    lock.setExpiresAt(expiresAt.getTime());
                    return lock;
                }
            }
        };
        return queryRunner.query("select id,expires_at from sched_job_locks where id=?", rsh, id);
   
    }

}
