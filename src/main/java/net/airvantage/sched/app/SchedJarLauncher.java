package net.airvantage.sched.app;

import java.net.URL;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.log.Slf4jLog;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main that launches the application with an embedded Jetty.
 */
public class SchedJarLauncher {

    private static final Logger LOG = LoggerFactory.getLogger(SchedJarLauncher.class);
    
    public static void main(String[] args) throws Exception {

        /*
        long start = System.currentTimeMillis();
        StatusServlet.startupTime = start;

        ServiceLocator loc = new ServiceLocator();
        loc.init();

        LegacyServiceLocator lloc = new LegacyServiceLocator(loc);
        lloc.init();

        new CoreServiceLocator();
        */

        Server server = new Server(8085);
        WebAppContext context = new WebAppContext();

        URL location = SchedJarLauncher.class.getProtectionDomain().getCodeSource().getLocation();
        context.setResourceBase("jar:" + location.toExternalForm() + "!/");
        context.setContextPath("/sched/");
        context.setLogger(new Slf4jLog());

        server.setHandler(context);
        server.start();

        /*
        StatusServlet.startupDuration = System.currentTimeMillis() - start;
        */
        // LOG.info("AV-DATA started (startup duration: {} ms)", StatusServlet.startupDuration);
        
    }
}
