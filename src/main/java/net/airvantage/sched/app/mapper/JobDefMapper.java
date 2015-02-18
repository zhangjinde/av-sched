package net.airvantage.sched.app.mapper;

import java.io.IOException;
import java.io.InputStream;

import net.airvantage.sched.app.AppException;
import net.airvantage.sched.model.jobDef.JobDef;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JobDefMapper {

    private static final ObjectMapper JACKSON = new ObjectMapper();
    
    public static JobDef jobDef(InputStream is) throws AppException {
        
        JobDef res = null;
        try {
            res = JACKSON.reader(JobDef.class).readValue(is);
        } catch (IOException e) {
            throw new AppException("invalid.json", e);
        }
        
        return res;
    }
    
}
