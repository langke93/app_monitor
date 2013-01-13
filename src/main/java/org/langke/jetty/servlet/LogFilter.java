package org.langke.jetty.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 记日志
 *
 */
public class LogFilter implements Filter {

	private static final Logger logger = LoggerFactory.getLogger(LogFilter.class);

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		logger.info("{} inited", this.getClass());
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		HttpServletRequest req = (HttpServletRequest) request;
		if(req.getPathInfo() != null){
			logger.info("ip={}, prot={}, contextPath={}, servletPath={}, pathInfo={}, params={}", new Object[] {
					req.getLocalAddr(), req.getLocalPort(), req.getContextPath(),
					req.getServletPath(), req.getPathInfo(), req.getParameterMap()
			});
		}

		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
		logger.info("{} destroyed", this.getClass());
	}

}
