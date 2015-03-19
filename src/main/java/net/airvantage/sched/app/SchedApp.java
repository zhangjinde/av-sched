package net.airvantage.sched.app;

import net.airvantage.sched.db.SchemaMigrator;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchedApp {

    private static final Logger LOG = LoggerFactory.getLogger(SchedApp.class);

    private boolean clean = false;
    
    public SchedApp(String[] args) {
        if (args.length > 0 && "--clear".equals(args[0])) {
            this.clean = true;
        }
        
    }

    public void start(WebAppContextBuilder contextBuilder) throws Exception {
        ServiceLocator.getInstance().init();

        if (this.clean) {
            ServiceLocator.getInstance().getJobService().clean();
        }
        
        long start = System.currentTimeMillis();
        JobDefServlet.startupTime = start;

        SchemaMigrator schemaMigrator = ServiceLocator.getInstance().getSchemaMigrator();

        schemaMigrator.migrate();

        int port = ServiceLocator.getInstance().getPort();

        Server server = new Server(port);

        WebAppContext webAppContext = contextBuilder.buildContext();

        /* K
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(false);
        resourceHandler.setWelcomeFiles(new String[]{ "public/index.html" });

        HandlerList handlerList = new HandlerList();
        handlerList.setHandlers(new Handler[] { resourceHandler, webAppContext});
        
        server.setHandler(handlerList);
        */
        
        server.setHandler(webAppContext);
        server.start();

        JobDefServlet.startupDuration = System.currentTimeMillis() - start;
        LOG.info("AV-SCHED started (startup duration: {} ms)", JobDefServlet.startupDuration);

    }

}
