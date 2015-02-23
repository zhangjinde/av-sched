package net.airvantage.sched.app;

import net.airvantage.sched.db.SchemaMigrator;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchedApp {

    private static final Logger LOG = LoggerFactory.getLogger(SchedApp.class);

    public SchedApp(String[] args) {
    }

    public void start(WebAppContextBuilder contextBuilder) throws Exception {
        ServiceLocator.getInstance().init();

        long start = System.currentTimeMillis();
        JobDefServlet.startupTime = start;

        SchemaMigrator schemaMigrator = ServiceLocator.getInstance().getSchemaMigrator();

        schemaMigrator.migrate();

        int port = ServiceLocator.getInstance().getPort();

        Server server = new Server(port);

        WebAppContext context = contextBuilder.buildContext();

        server.setHandler(context);
        server.start();

        JobDefServlet.startupDuration = System.currentTimeMillis() - start;
        LOG.info("AV-SCHED started (startup duration: {} ms)", JobDefServlet.startupDuration);

    }

}
