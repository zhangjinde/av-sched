package net.airvantage.sched.app;

import java.net.URLClassLoader;

import org.eclipse.jetty.util.log.Slf4jLog;
import org.eclipse.jetty.webapp.WebAppContext;

public class SchedMain {

    public static void main(String[] args) throws Exception {

        SchedApp app = new SchedApp(args);
        app.start(new WebAppContextBuilder() {
            
            @Override
            public WebAppContext buildContext() throws Exception {
                WebAppContext context = new WebAppContext();
                context.setDescriptor("src/main/webapp/WEB-INF/web.xml");
                context.setResourceBase("src/main/webapp");
                context.setContextPath("/sched");
                context.setParentLoaderPriority(true);
                context.setLogger(new Slf4jLog());

                URLClassLoader cl = (URLClassLoader) Thread.currentThread().getContextClassLoader();
                ProjectClassLoader myCl = new ProjectClassLoader(context, cl.getURLs());
                context.setClassLoader(myCl);
                return context;
            }
        });
        
    }
    
    
}
