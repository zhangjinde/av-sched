package net.airvantage.sched.app;

import java.net.URL;

import org.eclipse.jetty.util.log.Slf4jLog;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Main that launches the application with an embedded Jetty.
 */
public class SchedJarLauncher {

    public static void main(String[] args) throws Exception {

        SchedApp app = new SchedApp(args);
        app.start(new WebAppContextBuilder() {

            @Override
            public WebAppContext buildContext() throws Exception {
                WebAppContext context = new WebAppContext();

                URL location = SchedJarLauncher.class.getProtectionDomain().getCodeSource().getLocation();
                context.setResourceBase("jar:" + location.toExternalForm() + "!/");
                context.setContextPath("/sched/");
                context.setLogger(new Slf4jLog());
                return context;
            }
        });

    }
}
