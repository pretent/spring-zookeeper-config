package org.pretent.config.spring.zk.zkspring;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import lombok.Getter;
import lombok.Setter;

import org.I0Itec.zkclient.ZkClient;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

@Setter
@Getter
public class ZkPropertyConfigurer extends PropertyPlaceholderConfigurer {

	/**
	 * 要监听zookeeper的节点的前缀，在资源文件中得key
	 */
	private static final String ZOOKEEPER_CONFIG_PREFIX_KEY = "zookeeper_config_prefix";
	/**
	 * 本地存储配置文件的目录，在资源文件中的key
	 */
	private static final String LOCAL_CONFIG_PATH_KEY = "local_config_path";

	private static Logger logger = Logger.getLogger(ZkPropertyConfigurer.class);

	static {
		logger.setLevel(Level.DEBUG);
	}

	// zk 服务器地址，多个用，分割（192.1.1.1:2181,192.2.2.2:2181...）
	private String servers;

	// zk client
	private ZkClient zkClient;

	// 资源文件的名称
	private String[] properties;

	/**
	 * 从PropertyPlaceholderConfigurer实现
	 */
	@Override
	protected void processProperties(
			ConfigurableListableBeanFactory beanFactoryToProcess,
			Properties props) throws BeansException {
		for (String property : properties) {
			logger.debug("szookeeper " + property);
		}
		try {
			zkClient = new ZkClient(servers);
			config(props);
			initProperties(props);
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.setProperties(props);
		logger.debug("szookeeper 总配置：" + props);
		super.processProperties(beanFactoryToProcess, props);
		new ZkWacher(this.servers, properties).listen();
		this.zkClient.close();
	}

	/**
	 * 监听的zookeeper跟节点设置，本地存储路径设置
	 * 
	 * @param props
	 */
	private void config(Properties props) {
		// 判断本地配置存储路径设置
		if (StringUtils.isNotEmpty(props.getProperty(LOCAL_CONFIG_PATH_KEY))) {
			DefaultZkNodeDataHandler.DEFAULT_CONFIG_PATH = props.getProperty(
					LOCAL_CONFIG_PATH_KEY).trim();
			logger.debug("szookeeper 设置本地存储配置路径："
					+ DefaultZkNodeDataHandler.DEFAULT_CONFIG_PATH);
		}
		// 判断设置本地的存储路径是否存在，不存在则建立
		File file = new File(DefaultZkNodeDataHandler.DEFAULT_CONFIG_PATH);
		if (!file.exists() || !file.isDirectory()) {
			file.mkdirs();
			logger.debug("szookeeper 创建本地存储配置路径："
					+ DefaultZkNodeDataHandler.DEFAULT_CONFIG_PATH);
		}
		// 判断zookeeper监听根节点是否设置
		if (StringUtils.isNotEmpty(props
				.getProperty(ZOOKEEPER_CONFIG_PREFIX_KEY))) {
			ZkWacher.ZOOKEEPER_ROOT_NODE_PREFIX = props.getProperty(
					ZOOKEEPER_CONFIG_PREFIX_KEY).trim();
			logger.debug("szookeeper 设置zookeeper监听配置根节点："
					+ ZkWacher.ZOOKEEPER_ROOT_NODE_PREFIX);
		}
		// 判断zook监听的节点是否存在，不存则创建永久节点
		if (!this.zkClient.exists(ZkWacher.ZOOKEEPER_ROOT_NODE_PREFIX)) {
			logger.debug("szookeeper 创建zookeeper配置节点："
					+ ZkWacher.ZOOKEEPER_ROOT_NODE_PREFIX);
			String[] paths = ZkWacher.ZOOKEEPER_ROOT_NODE_PREFIX.split("/");
			StringBuilder sb = new StringBuilder();
			for (int i = 1; i < paths.length; i++) {
				sb.append("/");
				sb.append(paths[i]);
				if (this.zkClient.exists(sb.toString())) {
					continue;
				}
				this.zkClient.createPersistent(sb.toString());
			}
		}
	}

	/**
	 * 初始化资源
	 * 
	 * @param props
	 * @throws Exception
	 */
	private void initProperties(Properties props) throws Exception {
		// 从zookeeper上读取配置的所有资源文件（properties）
		for (String property : properties) {
			byte[] data = getData(property);
			fillProperties(props, data);
		}
	}

	/**
	 * 给Properties填充属性
	 * 
	 * @param props
	 * @param data
	 * @throws IOException
	 */

	private void fillProperties(Properties props, byte[] data)
			throws IOException {
		BufferedReader bre = null;
		String str = null;
		if (data == null) {
			return;
		}
		bre = new BufferedReader(new InputStreamReader(
				new ByteArrayInputStream(data)));
		try {
			logger.debug("szookeeper 从zk上读取的配置...");
			while ((str = bre.readLine()) != null) {
				if (StringUtils.isNotBlank(str)) {
					// 完整的应该还需要处理：多条配置、value中包含=、忽略#号开头
					if (str.startsWith("#")) {
						continue;
					}
					String[] line = StringUtils.split(str, "=");
					props.put(line[0].trim(), line[1].trim());
					logger.debug("szookeeper 配置:" + line[0] + "=" + line[1]);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 从zookeeper上读取指定节点配置
	 * 
	 * @param properties
	 * @return
	 * @throws Exception
	 */
	private byte[] getData(String properties) {
		String zknode = ZkWacher.ZOOKEEPER_ROOT_NODE_PREFIX + properties;
		if (!zkClient.exists(zknode)) {
			logger.error("zknode [" + zknode + "] is not exists!");
			return null;
		}
		ZkObject obj = zkClient.readData(zknode);
		return obj == null ? null : obj.getData().trim().getBytes();
	}
}