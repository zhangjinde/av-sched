package net.airvantage.sched.dao;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.airvantage.sched.model.JobDef;
import net.airvantage.sched.model.JobLock;
import net.airvantage.sched.model.JobState;

public class DummyJobStateDao implements JobStateDao {

    private Map<String, JobState> states = new HashMap<String, JobState>();
    
    @Override
    public void saveJobDef(JobDef jobDef) {
        
        // TODO(pht) move to fromDef ?
        JobState st = new JobState();
        st.setId(jobDef.getId());
        st.setUrl(jobDef.getUrl());
        st.setScheduling(jobDef.getScheduling());
        st.setLock(new JobLock());
        
        states.put(jobDef.getId(), st);
    }

    @Override
    public JobState findJobState(String id) {
        return states.get(id);
    }

    @Override
    public void lockJob(String id) {
        
        // TODO(pht) move to a "lock" method ?
        JobState st = findJobState(id);
        JobLock lock = new JobLock();
        lock.setLocked(true);
        lock.setExpiration(new Date().getTime() + st.getScheduling().getTimeout());
        st.setLock(lock);
        
        states.put(id, st);
    }
    
    @Override
    public void unlockJob(String id) {
     // TODO(pht) move to a "lock" method ?
        JobState st = findJobState(id);
        JobLock lock = new JobLock();
        lock.setLocked(false);
        lock.setExpiration(null);
        st.setLock(lock);
        
        states.put(id, st);
    }

}
