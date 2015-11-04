package org.pretent.config.spring.zk.zkspring.util;

import org.springframework.context.ApplicationContext;

/**
 * 保存ApplicationContext对象
 * 
 * 保存监听的xml文件和资源文件
 * 
 * @author root
 *
 */
public class ApplicationContextUtils {
	
	// 当前运行的spring context
	private static ApplicationContext context;

	// 用来保存资源文件的名称
	private static String[] properties;

	// 用来保存xml文件的名称
	private static String[] xmls;
	
	private static String servers = null;
	
	public static String getServers() {
		return servers;
	}

	public static void setServers(String servers) {
		ApplicationContextUtils.servers = servers;
	}

	public static ApplicationContext getApplicationContext() {
		return context;
	}

	public static void setContext(ApplicationContext ctx) {
		context = ctx;
	}

	public String[] getProperties() {
		return properties;
	}

	public static void setProperties(String[] props) {
		properties = props;
	}

	public static String[] getXmls() {
		return xmls;
	}

	public static void setXmls(String[] xms) {
		xmls = xms;
	}

}
