package net.airvantage.sched.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import net.airvantage.sched.model.JobConfig;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

public class JobConfigDaoImpl implements JobConfigDao {

    private QueryRunner queryRunner;

    public JobConfigDaoImpl(DataSource dataSource) {
        this.queryRunner = new QueryRunner(dataSource);
    }
    
    @Override
    public void saveJobConfig (JobConfig config) throws SQLException {
        queryRunner.update("delete from sched_job_configs where id=?", config.getId());
        queryRunner.update("insert into sched_job_configs(id,url,timeout) values(?,?,?)", config.getId(),
                config.getUrl(), config.getTimeout());

    }
    
    @Override
    public JobConfig findJobConfig(String id) throws SQLException {
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
    
    @Override
    public Map<String, JobConfig> jobConfigsById() throws SQLException {

        ResultSetHandler<Map<String, JobConfig>> rsh = new ResultSetHandler<Map<String, JobConfig>>() {
            @Override
            public Map<String, JobConfig> handle(ResultSet rs) throws SQLException {

                Map<String, JobConfig> map = new HashMap<String, JobConfig>();

                while (rs.next()) {
                    JobConfig config = new JobConfig();
                    String id = (String) rs.getString(1);
                    config.setId(id);
                    config.setUrl((String) rs.getString(2));
                    config.setTimeout((Long) rs.getLong(3));
                    map.put(id, config);
                }

                return map;
            }
        };
        return queryRunner.query("select id,url,timeout from sched_job_configs", rsh);

    }

}
