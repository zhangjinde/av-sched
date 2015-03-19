package net.airvantage.sched.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.airvantage.sched.app.AppException;
import net.airvantage.sched.model.JobDef;
import net.airvantage.sched.model.JobLock;
import net.airvantage.sched.model.JobState;

public class DummyJobStateDao implements JobStateDao {

    private Map<String, JobState> states = new HashMap<String, JobState>();
    
    @Override
    public void saveJobDef(JobDef jobDef) {
        JobState st = new JobState();
        st.setConfig(jobDef.getConfig());
        states.put(jobDef.getConfig().getId(), st);
    }

    @Override
    public void deleteJobDef(String jobId) throws AppException {
        if (states.containsKey(jobId)) {
            states.remove(jobId);
        }
    }
    
    @Override
    public JobState findJobState(String id) {
        return states.get(id);
    }

    @Override
    public void removeAll() throws AppException {
        states.clear();
    }
    
    @Override
    public void lockJob(String id) {
        
        // TODO(pht) move to a "lock" method ?
        JobState st = findJobState(id);
        JobLock lock = new JobLock();
        lock.setLocked(true);
        lock.setExpiresAt(new Date().getTime() + st.getConfig().getTimeout());
        st.setLock(lock);
        
        states.put(id, st);
    }
    
    @Override
    public void unlockJob(String id) {
     // TODO(pht) move to a "lock" method ?
        JobState st = findJobState(id);
        JobLock lock = new JobLock();
        lock.setLocked(false);
        lock.setExpiresAt(null);
        st.setLock(lock);
        
        states.put(id, st);
    }

    @Override
    public List<JobState> getJobStates() throws AppException {
        List<JobState> res = new ArrayList<JobState>();
        res.addAll(states.values());
        return res;
    }
    
}
