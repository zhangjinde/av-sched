package net.airvantage.sched.app;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.airvantage.sched.app.mapper.JsonMapper;
import net.airvantage.sched.model.JobId;
import net.airvantage.sched.services.JobService;

import org.apache.log4j.Logger;
import org.quartz.SchedulerException;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JobActionServlet extends HttpServlet {

    public static final Logger LOG = Logger.getLogger(JobActionServlet.class);
    
    private static final ObjectMapper JACKSON = new ObjectMapper();
    
    private JobService jobService;
    
    @Override
    public void init() throws ServletException {
        super.init();
        
        try {
            jobService = ServiceLocator.getJobService();
        } catch (SchedulerException e) {
            throw new ServletException(e);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getServletPath().endsWith("ack")) {
            ackJob(req, resp);
        }
    }

    protected void ackJob(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, Object> res = new HashMap<String, Object>();
        try {
            JobId jobId = JsonMapper.jobId(req.getInputStream());
            jobService.ackJob(jobId.getId());
        } catch (AppException e) {
            LOG.debug("Exception while acknowledging job", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            res = e.asMap();
        }
        resp.setContentType("application/json");
        resp.getWriter().println(JACKSON.writeValueAsString(res));
    }
}
