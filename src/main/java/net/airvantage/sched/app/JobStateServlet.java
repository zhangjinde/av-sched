package net.airvantage.sched.app;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.airvantage.sched.app.exceptions.AppException;
import net.airvantage.sched.dao.JobStateDao;
import net.airvantage.sched.model.JobState;

import org.quartz.SchedulerException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JobStateServlet extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final ObjectMapper JACKSON = new ObjectMapper();

    private JobStateDao jobStateDao;

    @Override
    public void init() throws ServletException {
        JACKSON.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            jobStateDao = ServiceLocator.getInstance().getJobStateDao();
        } catch (SchedulerException e) {
            throw new ServletException(e);
        }

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        try {
            List<JobState> jobStates = null;
            String jobId = req.getParameter("jobId");
            if (jobId != null) {
                jobStates = Arrays.asList(jobStateDao.findJobState(jobId));
            } else {
                jobStates = jobStateDao.getJobStates();
            }

            resp.setContentType("application/json");

            String content = JACKSON.writeValueAsString(jobStates);
            resp.getWriter().write(content);

        } catch (AppException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
