package net.airvantage.sched.app;

import java.net.URLClassLoader;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.log.Slf4jLog;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchedMain {

    private static final Logger LOG = LoggerFactory.getLogger(SchedMain.class);

    
    public static void main(String[] args) throws Exception {

        long start = System.currentTimeMillis();
        JobsServlet.startupTime = start;

        URLClassLoader cl = (URLClassLoader) Thread.currentThread().getContextClassLoader();

        int port = 8086;
        Server server = new Server(port);
        WebAppContext context = new WebAppContext();
        context.setDescriptor("src/main/webapp/WEB-INF/web.xml");
        context.setResourceBase("src/main/webapp");
        context.setContextPath("/sched");
        context.setParentLoaderPriority(true);
        context.setLogger(new Slf4jLog());

        ProjectClassLoader myCl = new ProjectClassLoader(context, cl.getURLs());
        context.setClassLoader(myCl);
        server.setHandler(context);
        server.start();

        JobsServlet.startupDuration = System.currentTimeMillis() - start;
        LOG.info("AV-SCHED started (startup duration: {} ms)", JobsServlet.startupDuration);
        
    }
    
    
}
