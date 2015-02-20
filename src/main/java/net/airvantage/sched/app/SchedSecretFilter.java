package net.airvantage.sched.app;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.airvantage.sched.Constants;

public class SchedSecretFilter implements Filter{

    public SchedSecretFilter() {
    }
    
        
    @Override
    public void init(FilterConfig arg0) throws ServletException {
    }
    
    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException,
            ServletException {
        
        HttpServletRequest httpReq = (HttpServletRequest) req;
        HttpServletResponse httpResp = (HttpServletResponse) resp;
        
        String schedSecretHeader = httpReq.getHeader(Constants.SCHED_SECRET_HEADER_NAME);
        
        // Singleton pattern FTW - this make reloading works fine.
        String schedSecret = ServiceLocator.getInstance().getSchedSecret();
        if (schedSecretHeader != null && schedSecret.equalsIgnoreCase(schedSecretHeader)) {
            chain.doFilter(req, resp);
        } else {
            httpResp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub
    }

}
