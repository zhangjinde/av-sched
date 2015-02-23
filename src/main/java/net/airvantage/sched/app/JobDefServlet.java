package net.airvantage.sched.app;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.airvantage.sched.app.mapper.JsonMapper;
import net.airvantage.sched.model.JobDef;
import net.airvantage.sched.services.JobService;

import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JobDefServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(JobDefServlet.class);
    
    private static final ObjectMapper JACKSON = new ObjectMapper();

    public static long startupTime;
    public static long startupDuration;

    
    private JobService jobService = null;

    @Override
    public void init() throws ServletException {
        super.init();

        JACKSON.enable(SerializationFeature.INDENT_OUTPUT);
        
        try {
            jobService = ServiceLocator.getInstance().getJobService();
        } catch (SchedulerException e) {
            throw new ServletException("Unable to create jobService from ServiceLocator", e);
        }

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, Object> res = new HashMap<String, Object>();
        
        try {
            JobDef jobDef = JsonMapper.jobDef(req.getInputStream());
            jobService.scheduleJob(jobDef);
            // TODO(pht) or something better ?
            res.put("id", jobDef.getConfig().getId());
        } catch (AppException e) {
            LOG.debug("Exception while scheduling job", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            res = e.asMap();
        }
        resp.setContentType("application/json");
        resp.getWriter().println(JACKSON.writeValueAsString(res));
    }

}
