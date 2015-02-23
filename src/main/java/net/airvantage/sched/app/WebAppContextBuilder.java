package net.airvantage.sched.app;

import org.eclipse.jetty.webapp.WebAppContext;

public interface WebAppContextBuilder {

    public WebAppContext buildContext() throws Exception;
    
}
