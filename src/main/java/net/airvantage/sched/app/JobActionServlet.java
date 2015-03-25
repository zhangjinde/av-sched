package net.airvantage.sched.app;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.airvantage.sched.app.exceptions.AppException;
import net.airvantage.sched.app.mapper.JsonMapper;
import net.airvantage.sched.model.JobId;
import net.airvantage.sched.services.JobService;

import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JobActionServlet extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public static final Logger LOG = LoggerFactory.getLogger(JobActionServlet.class);

    private static final ObjectMapper JACKSON = new ObjectMapper();

    private JobService jobService;

    @Override
    public void init() throws ServletException {
        super.init();

        try {
            jobService = ServiceLocator.getInstance().getJobService();
        } catch (SchedulerException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getPathInfo().endsWith("ack")) {
            ackJob(req, resp);
        } else if (req.getPathInfo().endsWith("trigger")) {
            triggerJob(req, resp);
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
        writeJSON(resp, res);
    }

    protected void triggerJob(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, Object> res = new HashMap<String, Object>();
        try {
            JobId jobId = JsonMapper.jobId(req.getInputStream());
            boolean triggered = jobService.triggerJob(jobId.getId());
            res.put("triggered", triggered);
        } catch (AppException e) {
            LOG.debug("Exception while triggering job", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            res = e.asMap();
        }
        writeJSON(resp, res);
    }

    private void writeJSON(HttpServletResponse resp, Map<String, Object> res) throws IOException,
            JsonProcessingException {
        resp.setContentType("application/json");
        resp.getWriter().println(JACKSON.writeValueAsString(res));
    }

}
