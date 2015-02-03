package com.jk.common.redis.filter;

import com.jk.common.spring.ApplicationContextHolder;
import com.jk.common.redis.httpsession.RedisSessionManager;
import com.jk.common.redis.httpsession.RequestEventSubject;
import com.jk.common.redis.httpsession.SessionHttpServletRequestWrapper;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;

/**
 * Filter dynamic request, create wrapper for the original http request, replece
 * http session with redis http session
 * 
 * @author leslie
 *
 */
public class RedisSessionFilter implements Filter {
    // ignore static resource request, only filter the dynamic request
    public static final String[] IGNORE_SUFFIX = { ".png", ".jpg", ".jpeg", ".gif", ".css", ".js", ".html", ".htm", "swf" };
    private RedisSessionManager sessionManager;

    public void init(FilterConfig filterConfig) throws ServletException {
        sessionManager = ApplicationContextHolder.getBean("redisSessionManager");
    }

    public void doFilter(ServletRequest servletRequest,
            ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;

        if (!(ifFilter(request))) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        HttpServletResponse response = (HttpServletResponse) servletResponse;
        RequestEventSubject eventSubject = new RequestEventSubject();
        // create http request wrapper, replace origin http session with redis session
        SessionHttpServletRequestWrapper requestWrapper = new SessionHttpServletRequestWrapper(
                request, response, this.sessionManager, eventSubject);
        try {
            filterChain.doFilter(requestWrapper, servletResponse);
        } finally {
            // save the newest session info.
            eventSubject.completed(request, response);
        }
    }

    /**
     * check if the request uri need to be filtered
     * 
     * @param request
     * @return
     */
    private boolean ifFilter(HttpServletRequest request) {
        for (String suffix : IGNORE_SUFFIX) {
            if (StringUtils.endsWithIgnoreCase(request.getRequestURI(), suffix)) {
                return false;
            }
        }
        return true;
    }

    public void destroy() {

    }
}