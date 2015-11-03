package org.pretent.config.spring.zk.zkspring;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

import org.I0Itec.zkclient.ZkClient;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.pretent.config.spring.zk.zkspring.util.ApplicationContextUtils;
import org.pretent.config.spring.zk.zkspring.util.MD5Util;
import org.pretent.config.spring.zk.zkspring.util.StringArrayUtils;
import org.pretent.config.spring.zk.zkspring.web.context.ZkApplicationContextUtils;
import org.pretent.config.spring.zk.zkspring.web.context.ZkXmlWebApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractRefreshableApplicationContext;
import org.springframework.context.support.AbstractRefreshableConfigApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.web.context.ContextLoader;

/**
 * 缓存数据
 * 
 * context重新读取
 * 
 * @author root
 *
 */
public class DefaultZkNodeDataHandler implements IZkNodeDataHandler {

	/**
	 * 文件默认存放根目录
	 */
	public static String DEFAULT_CONFIG_PATH = "/Users/user/zpringconfig";

	private SimpleDateFormat sdf = new SimpleDateFormat(".yyyyMMdd.hhmmss");

	// logger
	private static Logger logger = Logger
			.getLogger(DefaultZkNodeDataHandler.class);

	@Setter
	@Getter
	private ZkClient zkClient = null;

	static {
		logger.setLevel(Level.DEBUG);
	}

	public void handlerData(String zknode, Object data) {
		ZkObject obj = (ZkObject) data;
		try {
			this.refresh((ZkObject) data);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (true) {
			return;
		}
		// 对比数据
		if (!compare(zknode, obj)) {
			// 备份原数据
			this.backup(zknode);
			// 缓存新数据
			this.writeData(zknode, obj.getData().trim().getBytes());
			// 取得新配置
			this.readXmlConfig();
			String[] configLocations = getConfigLocations();
			// 刷新数据
			this.refresh(configLocations);
		} else {
			logger.debug("szookeeper " + zknode
					+ " [文件内容相同]数据新数据内容与原来数据相同，不需备份，不需刷新...");
		}
	}

	/**
	 * 新数据和原数据对比
	 * 
	 * @param zknode
	 * @param data
	 * @return
	 */
	private boolean compare(String zknode, ZkObject data) {
		boolean flag = false;
		try {
			// 拿到的是反序列化的ZkObject对象
			String newMd5 = MD5Util.getMD5String(data.getData().trim()
					.getBytes());
			File file = new File(DEFAULT_CONFIG_PATH + zknode);
			if (!file.exists()) {
				return false;
			}
			String orgMd5 = MD5Util.getFileMD5String(file);
			logger.debug("szookeeper 原数据MD5：" + newMd5);
			logger.debug("szookeeper 新数据MD5：" + orgMd5);
			if (newMd5.equals(orgMd5)) {
				logger.debug("szookeeper " + zknode
						+ " 新数据内容与原来数据MD5相同，不需备份，不需刷新...");
				return true;
			} else {
				return org.pretent.config.spring.zk.zkspring.util.FileUtils
						.compare(new ByteArrayInputStream(data.getData()
								.getBytes()), new FileInputStream(file));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return flag;
	}

	/**
	 * 取得重新加载的配置文件路径
	 * 
	 * @return
	 */
	private String[] getConfigLocations() {
		String[] configLocations = StringArrayUtils.insertString(
				ApplicationContextUtils.getXmls(), "file:"
						+ DEFAULT_CONFIG_PATH
						+ ZkWacher.ZOOKEEPER_ROOT_NODE_PREFIX);
		for (String str : configLocations) {
			logger.debug("szookeeper 新配置文件：" + str);
		}
		return configLocations;
	}

	/**
	 * 如果新的文件需要保存先备份原来的文件
	 * 
	 * 文件保存位置 ${DEFAULT_STORAGE_FILE_PREFIX}/versions/${znode}.yyyyMMdd.hhmmss
	 * 
	 * @param zknode
	 */
	public void backup(String zknode) {
		logger.debug("szookeeper 开始备份数据.....");
		File file = new File(DEFAULT_CONFIG_PATH + zknode);
		File versionsFile = new File(DEFAULT_CONFIG_PATH + File.separator
				+ "versions" + zknode + sdf.format(new Date()));
		if (!versionsFile.getParentFile().exists()) {
			versionsFile.getParentFile().mkdirs();
		}
		// 文件复制
		try {
			// FileUtils.copyFile(file, versionsFile);
			throw new IOException("");
		} catch (IOException e) {
			logger.debug("szookeeper 备份数据异常:" + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * 缓存zookeeper上数据到本地文件系统
	 * 
	 * @param zknode
	 * @param data
	 */
	private void writeData(String zknode, byte[] data) {
		OutputStream oos = null;
		String cachePath = DEFAULT_CONFIG_PATH + zknode;
		try {
			File cache = new File(cachePath);
			if (!cache.getParentFile().exists()) {
				cache.getParentFile().mkdirs();
			}
			if (!cache.exists()) {
				cache.createNewFile();
			}
			oos = new FileOutputStream(cache);
			oos.write(data);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (oos != null) {
				try {
					oos.flush();
					oos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		logger.debug("szookeeper " + zknode + " > [" + cachePath
				+ "] 数据缓存完成...");
	}

	/**
	 * 如果本地没有远程xml上得配置，将远程xml配置下载至本地，以备spring context重新加载
	 */
	private void readXmlConfig() {
		String[] xmls = ApplicationContextUtils.getXmls();
		for (String xml : xmls) {
			String zkPath = ZkWacher.ZOOKEEPER_ROOT_NODE_PREFIX + xml;
			ZkObject obj = this.zkClient.readData(zkPath);
			String cachePath = DEFAULT_CONFIG_PATH + zkPath;
			File file = new File(cachePath);
			// 如果文件不存在并且
			if (!file.exists()) {
				logger.debug("szookeeper 开始写入数据[" + zkPath + "]");
				writeData(zkPath, obj.getData().toString().getBytes());
			}
		}
	}

	/**
	 * 刷新spring context
	 */
	private void refresh(String... filePath) {
		// 取得当前spring context
		ApplicationContext context = ContextLoader
				.getCurrentWebApplicationContext();
		ApplicationContext ctxt = ApplicationContextUtils
				.getApplicationContext();
		if (context == null) {
			context = ctxt;
		}
		logger.debug("szookeeper 开始刷新Context...");
		// BeanTest beana = context.getBean(BeanTest.class);
		logger.debug("szookeeper ----------------");
		// beana.saySelf();
		logger.debug("szookeeper ----------------");
		// TODO 设置配置文件路径
		// XmlWebApplicationContext 和 ClassPathXmlApplicationContext
		// 都是AbstractRefreshableConfigApplicationContext的子类
		// 都具有setConfigLocation()和refresh()方法
		// org.springframework.context.support.AbstractRefreshableConfigApplicationContext
		AbstractRefreshableConfigApplicationContext ctx = (AbstractRefreshableConfigApplicationContext) context;
		// ctx.setConfigLocation("file:/Users/user/applicationContext.xml");
		// ctx.setConfigLocation("file:"+filePath);
		ctx.setConfigLocations(filePath);
		// 刷新重新加载Spring Context
		if (ctx.getParent() != null) {
			((AbstractRefreshableApplicationContext) ctx.getParent()).refresh();
		}
		// ((AbstractRefreshableApplicationContext) context).refresh();
		// ctx.close();
		ctx.refresh();
		// ctx.start();
		logger.debug("szookeeper ----------------");
		logger.debug("szookeeper 刷新完成Context...");
	}

	/**
	 * 直接从内存里读取配置信息
	 * 
	 * @param object
	 * @throws IOException
	 */
	@SuppressWarnings("all")
	private void refresh(ZkObject object) throws IOException {
		// 只有配置了ContextLoaderListener才能通过此方法得到context对象
		ZkXmlWebApplicationContext context = (ZkXmlWebApplicationContext) ContextLoader
				.getCurrentWebApplicationContext();
		// 只用DispatcherServlet才能通过此方法取到
		if (context == null) {
			context = ZkApplicationContextUtils.getContext();
		}
		System.out
				.println("WebApplicationContextUtils.getWebApplicationContext(sc)---->"
						+ context);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bos.write(object.getData().getBytes());
		Resource[] res = new Resource[] { new ByteArrayResource(
				bos.toByteArray()) };
		bos.close();
		if (context.getParent() != null) {
			((AbstractRefreshableApplicationContext) context.getParent())
					.close();
			((AbstractRefreshableApplicationContext) context.getParent())
					.refresh();
		}
		context.close();
		context.setResources(res);
		context.refresh();
	}
}
