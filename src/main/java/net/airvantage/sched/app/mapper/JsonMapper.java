package net.airvantage.sched.app.mapper;

import java.io.IOException;
import java.io.InputStream;

import net.airvantage.sched.app.exceptions.AppException;
import net.airvantage.sched.model.JobDef;
import net.airvantage.sched.model.JobId;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonMapper {

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
    
    public static JobId jobId(InputStream is) throws AppException {
        JobId res = null;
        
        try {
            res = JACKSON.reader(JobId.class).readValue(is);
        } catch (IOException e) {
            throw new AppException("invalid.json", e);
        }
        
        return res;
    }
    
}
