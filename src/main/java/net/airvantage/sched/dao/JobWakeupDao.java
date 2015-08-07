package net.airvantage.sched.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import javax.sql.DataSource;

import net.airvantage.sched.app.exceptions.DaoRuntimeException;
import net.airvantage.sched.model.JobWakeup;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DAO to manage the {@link JobWakeup} object model.
 */
public class JobWakeupDao {

    private final static Logger LOG = LoggerFactory.getLogger(JobWakeupDao.class);

    private QueryRunner queryRunner;

    public JobWakeupDao(DataSource dataSource) throws DaoRuntimeException {
        this.queryRunner = new QueryRunner(dataSource);
    }

    /**
     * Persists the given wake-up and update it if already exists (same id).
     */
    public void persist(JobWakeup wakeup) throws DaoRuntimeException {
        LOG.debug("persist : wakeup={}", wakeup);

        try {
            if (this.find(wakeup.getId()) == null) {
                queryRunner.update("insert into sched_job_wakeups(id,wakeup_time,callback) values(?,?,?)",
                        wakeup.getId(), new Timestamp(wakeup.getWakeupTime()), wakeup.getCallback());

            } else {
                queryRunner.update("update sched_job_wakeups set wakeup_time=?,callback=? where id=?", new Timestamp(
                        wakeup.getWakeupTime()), wakeup.getCallback(), wakeup.getId());
            }

        } catch (SQLException ex) {
            // Hack to manage concurrent calls (if this wake-up already exists just ignore it)
            if (!ex.getMessage().contains("Duplicate entry")) {
                throw new DaoRuntimeException(ex);
            }
        }
    }

    /**
     * Deletes the wake-up with the given identifier.
     */
    public void delete(String wakeupId) throws DaoRuntimeException {
        LOG.debug("delete : wakeupId={}", wakeupId);

        try {
            queryRunner.update("delete from sched_job_wakeups where id=?", wakeupId);

        } catch (SQLException ex) {
            throw new DaoRuntimeException(ex);
        }
    }

    /**
     * Delete all the existing wake-ups.
     */
    public void deleteAll() throws DaoRuntimeException {

        try {
            queryRunner.update("delete from sched_job_wakeups");

        } catch (SQLException ex) {
            throw new DaoRuntimeException(ex);
        }
    }

    /**
     * Returns the wake-up with the given identifier.
     */
    public JobWakeup find(String wakeupId) throws DaoRuntimeException {
        LOG.debug("find : wakeupId={}", wakeupId);

        ResultSetHandler<JobWakeup> rsh = (ResultSet rs) -> {

            if (!rs.next()) {
                return null;
            }

            JobWakeup wakeup = new JobWakeup();
            wakeup.setId(rs.getString(1));
            wakeup.setWakeupTime(rs.getTimestamp(2).getTime());
            wakeup.setCallback(rs.getString(3));

            return wakeup;
        };

        try {
            return queryRunner.query("select id, wakeup_time, callback from sched_job_wakeups where id=?", rsh,
                    wakeupId);

        } catch (SQLException ex) {
            throw new DaoRuntimeException(ex);
        }
    }

    /**
     * Iterate through the wake-ups matching the given temporal window.
     */
    public void iterate(long from, long to, WakeupJobHandler handler) throws DaoRuntimeException {
        LOG.debug("iterate : from={}, to={}", from, to);

        ResultSetHandler<Boolean> rsh = (ResultSet rs) -> {

            boolean processing = true;
            boolean next = rs.next();
            
            while (next && processing) {

                JobWakeup wakeup = new JobWakeup();
                wakeup.setId(rs.getString(1));
                wakeup.setWakeupTime(rs.getTimestamp(2).getTime());
                wakeup.setCallback(rs.getString(3));

                processing = handler.handle(wakeup);
                next = rs.next();
            }

            return next;
        };

        try {
            queryRunner.query("select id, wakeup_time, callback from sched_job_wakeups "
                    + "where wakeup_time >= ? and wakeup_time <= ?", rsh, new Timestamp(from), new Timestamp(to));

        } catch (SQLException ex) {
            throw new DaoRuntimeException(ex);
        }
    }

    public interface WakeupJobHandler {
        public boolean handle(JobWakeup wakeup);
    }

}
