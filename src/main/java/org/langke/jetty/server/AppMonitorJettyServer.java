package org.langke.jetty.server;

import java.io.File;
import java.net.URL;
import java.util.EventListener;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.Servlet;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.FilterMapping;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * 内 嵌 jetty http 服务
 *
 */
public class AppMonitorJettyServer {

	private static final Logger logger = LoggerFactory.getLogger(AppMonitorJettyServer.class);

	//===========set
	private int port = 9009;
	private String contextPath = "/";
	private String webPath;
	private int threadNum = 50;

	private Map<String, Filter> filters;

	private Map<String, Servlet> servlets;
	
	private List<EventListener> listeners;

	//===========
	private Server server;


	private void init() throws Exception {
		server = new Server();

		//连接池
		SelectChannelConnector connector = new SelectChannelConnector();
		connector.setPort(port);
        connector.setMaxIdleTime(30000);
        connector.setRequestHeaderSize(8192);
        QueuedThreadPool threadPool =  new QueuedThreadPool(threadNum);
        threadPool.setName("embed-jetty-http");
        connector.setThreadPool(threadPool);


		server.setConnectors(new Connector[] { connector });
		//Context context = null;
		ServletContextHandler context = null;

		if(webPath != null) {//webapp 可以支持 jsp
			logger.info("load webPath={}", webPath);
			final URL warUrl = new File(webPath).toURI().toURL();
			final String warUrlString = warUrl.toExternalForm();
			context = new WebAppContext(warUrlString, contextPath);
			server.setHandler(context);
		} else {
			context = new ServletContextHandler(server, contextPath);
		}

		//add filter
		if(filters != null) {
			for(Map.Entry<String, Filter> eFilter : filters.entrySet()) {
				logger.info("add filter={}, path={}", eFilter.getValue().getClass(), eFilter.getKey());
				context.addFilter(new FilterHolder(eFilter.getValue()), eFilter.getKey(), FilterMapping.DEFAULT);
			}
		}

		//add servlet
		if(servlets != null) {
			for(Map.Entry<String, Servlet> eServlet : servlets.entrySet()) {
				logger.info("add servlet={}, path={}", eServlet.getValue().getClass(), eServlet.getKey());
				context.addServlet(new ServletHolder(eServlet.getValue()), eServlet.getKey());
			}
		}

		//add listener
		if(listeners != null){
			for(EventListener listener:listeners){
				logger.info("add listener={}",listener.getClass());
				context.addEventListener(listener);
			}
		}
		
		if(webPath == null) {
			context.addServlet(DefaultServlet.class, "/*");
		}
	}


	public void start() throws Exception {
		init();
		server.start();
		logger.info("jetty embed server started, port={}", port);
	}

	public void stop() throws Exception {
		server.stop();
		server.destroy();
	}

	public static void main(String[] args) {
		String contextFile = "classpath:spring-context.xml";
		if (args.length > 0) {
			contextFile = args[0];
		}
		ApplicationContext context = null;
		try {
			context = new FileSystemXmlApplicationContext(contextFile);
		} catch (Exception e) {
			System.out.println("RunMain [spring-conf-file]");
			logger.warn("", e);
		}

		String jettyEmbedServerBeanName = "jettyEmbedServer";
		if (args.length > 1) {
			jettyEmbedServerBeanName = args[1];
		}

		final AppMonitorJettyServer jettyEmbedServer = (AppMonitorJettyServer) context.getBean(jettyEmbedServerBeanName);
		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {
				try {
					jettyEmbedServer.stop();
				} catch (Exception e) {
					logger.error("run main stop error!", e);
				}
			}

		});

		try {
			jettyEmbedServer.start();
			logger.info("server started");
		} catch (Throwable e) {
			logger.warn("has exception!", e);
			System.exit(-1);
		}
		
	}

	public void setPort(int port) {
		this.port = port;
	}
	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	public void setWebPath(String webPath) {
		this.webPath = webPath;
	}

	public void setThreadNum(int threadNum) {
		this.threadNum = threadNum;
	}

	public void setFilters(Map<String, Filter> filters) {
		this.filters = filters;
	}

	public void setServlets(Map<String, Servlet> servlets) {
		this.servlets = servlets;
	}


	public void setListeners(List<EventListener> listeners) {
		this.listeners = listeners;
	}
	
}
