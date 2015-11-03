package org.pretent.config.spring.zk.zkspring.web.context;

/**
 * 用来保存Context对象
 * 
 * @author root
 *
 */
public class ZkApplicationContextUtils {

	private ZkApplicationContextUtils() {
	}

	private static ZkXmlWebApplicationContext context = null;

	public static ZkXmlWebApplicationContext getContext() {
		return context;
	}

	public static void setContext(ZkXmlWebApplicationContext context) {
		ZkApplicationContextUtils.context = context;
	}
}
