package org.pretent.config.spring.zk.zkspring.web.setvlet;

import org.pretent.config.spring.zk.zkspring.ZkWacher;
import org.pretent.config.spring.zk.zkspring.util.StringArrayUtils;
import org.pretent.config.spring.zk.zkspring.web.context.ZkApplicationContextUtils;
import org.pretent.config.spring.zk.zkspring.web.context.ZkXmlWebApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * 
 * 支持spring mvc 配置
 * 
 * web.xml 配置DispatcherServelt 请配置此Servlet
 * 
 * 完整配置示例：
 * -------------------------------
 * <servlet>
        <servlet-name>spring</servlet-name>
        <servlet-class>org.pretent.config.spring.zk.zkspring.web.setvlet.ZkDispatcherServlet</servlet-class>
        <init-param>
            <param-name>contextConfigLocation</param-name>
            <param-value>zk:/config/applicationContext.xml,zk:/config/app.xml</param-value>
        </init-param>
        <init-param>
            <param-name>contextClass</param-name>
            <param-value>org.pretent.config.spring.zk.zkspring.web.context.ZkXmlWebApplicationContext</param-value>
        </init-param>
		<init-param>
			<param-name>skServers</param-name>
			<param-value>127.0.0.1:2181</param-value>
		</init-param>
        <load-on-startup>1</load-on-startup>
    </servlet>
    <servlet-mapping>
        <servlet-name>spring</servlet-name>
        <url-pattern>/</url-pattern>
    </servlet-mapping>
 * -------------------------------
 * @author root
 *
 */
public class ZkDispatcherServlet extends DispatcherServlet {

	private Class<?> calzz = ZkXmlWebApplicationContext.class;

	/**
	 * 
	 */
	private static final long serialVersionUID = 6716794867197368237L;

	@Override
	public Class<?> getContextClass() {
		String contextClazz = getServletConfig().getInitParameter("contextClass");
		if (contextClazz != null) {
			try {
				return Class.forName(contextClazz);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		return calzz;
	}

	@Override
	protected WebApplicationContext initWebApplicationContext() {
		logger.debug("--------->开始初始化Web Context");
		ZkXmlWebApplicationContext wac = (ZkXmlWebApplicationContext) super.initWebApplicationContext();
		ZkApplicationContextUtils.setContext(wac);
		// 关闭zk连接
		wac.getZkClient().close();
		logger.debug("--------->完成初始化Web Context");
		logger.debug("--------->启动zk监听数据变化");
		// 启动zk监听数据变化
		String[] nodes = StringArrayUtils.removeString(getInitParameter("contextConfigLocation").split(","), "zk:");
		new ZkWacher(ZkXmlWebApplicationContext.getZkServers(), nodes).listen();
		return wac;
	}
	
}
