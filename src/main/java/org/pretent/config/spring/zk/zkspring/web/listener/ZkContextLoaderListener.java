package org.pretent.config.spring.zk.zkspring.web.listener;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.pretent.config.spring.zk.zkspring.ZkWacher;
import org.pretent.config.spring.zk.zkspring.util.StringArrayUtils;
import org.pretent.config.spring.zk.zkspring.web.context.ZkXmlWebApplicationContext;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;

/**
 * 支持spring listener 配置
 *
 * web.xml 配置ContextLoadListener 请配置此listener
 *
 * 继承org.springframework.web.context.ContextLoaderListener
 *
 * 复写ContextLoaderListener.initWebApplicationContext方法
 *
 * 完整配置示例：
 * ---------------------------------
 * <context-param>
        <param-name>contextClass</param-name>
        <param-value>org.pretent.config.spring.zk.zkspring.web.context.ZkXmlWebApplicationContext</param-value>
    </context-param>
    <context-param>
        <param-name>contextConfigLocation</param-name>
        <param-value>zk:/config/applicationContext.xml,zk:/config/app.xml</param-value>
    </context-param>
    <context-param>
        <param-name>skServers</param-name>
        <param-value>127.0.0.1:2181</param-value>
    </context-param>
    <listener>
        <listener-class>
            org.pretent.config.spring.zk.zkspring.web.listener.ZkContextLoaderListener
        </listener-class>
    </listener>
 *
 * ---------------------------------
 * @author root
 *
 */
public class ZkContextLoaderListener extends ContextLoaderListener {


	private static final Logger logger = Logger.getLogger(ZkContextLoaderListener.class);

	/**
	 * 直接调用父类的initWebApplicationContext方法，初始化context后返回当前context，
	 *
	 * 关闭ZkApplicationContext的zkclient
	 *
	 * 监听zookeeper配置变化
	 */
	@Override
	public WebApplicationContext initWebApplicationContext(ServletContext servletContext) {
		logger.debug("--------->开始初始化Web Context");
		ZkXmlWebApplicationContext wac = (ZkXmlWebApplicationContext) super.initWebApplicationContext(servletContext);
		// 关闭zk连接
		wac.getZkClient().close();
		logger.debug("--------->完成初始化Web Context");
		logger.debug("--------->启动zk监听数据变化");
		// 启动zk监听数据变化
		String[] nodes = StringArrayUtils.removeString(servletContext.getInitParameter(CONFIG_LOCATION_PARAM).split(","), "zk:");
		new ZkWacher(ZkXmlWebApplicationContext.getZkServers(), nodes).listen();
		return wac;
	}

}
