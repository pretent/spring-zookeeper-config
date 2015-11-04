package org.pretent.config.spring.zk.zkspring;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * zookeeper配置节点监听
 * 
 * 之前初始化好的bean 实例，可以继续使用，并且注入的属性依然是刷新之前的数据
 * 
 * 但是新配置中如果没有配置的bean，刷新后是拿不到的，重新获取的bean属性是刷新之后的数据
 * 
 * 实现IZkDataListener，IZkStateListener用来监听数据和状态的改变
 * 
 * @author root
 *
 */
public class ZkWacher implements IZkDataListener {

	/**
	 * zookeeper存放配置的根节点
	 */
	public static String ZOOKEEPER_ROOT_NODE_PREFIX = "";

	// logger
	private static Logger logger = Logger.getLogger(ZkWacher.class);

	static {
		logger.setLevel(Level.DEBUG);
	}

	/**
	 * 数据处理
	 */
	private IZkNodeDataHandler zkNodeDataHandler;

	/**
	 * 连接zookeeper字符串，多个使用，分割
	 */
	private String servers;

	/**
	 * 监听的zookeeper上节点路径
	 */
	private String[] zknodes;

	public ZkWacher(String servers, String[] zknodes) {
		this.servers = servers;
		this.zknodes = zknodes;
	}

	/**
	 * 
	 * @param zkNodeDataHandler
	 * @param servers
	 * @param zknodes
	 */
	public ZkWacher(IZkNodeDataHandler zkNodeDataHandler, String servers,
			String[] zknodes) {
		this(servers, zknodes);
		this.zkNodeDataHandler = zkNodeDataHandler;
	}

	/**
	 * zookeeper客户端
	 */
	private ZkClient zkClient = null;

	/**
	 * 监听zk上得指定节点
	 */
	public void listen() {
		logger.debug("szookeeper listening " + this.zknodes
				+ " data changes...");
		zkClient = new ZkClient(servers);
		if (this.zkNodeDataHandler == null) {
			this.zkNodeDataHandler = new DefaultZkNodeDataHandler();
		}
		initRootNode();
		for (String node : this.zknodes) {
			String zknode = ZOOKEEPER_ROOT_NODE_PREFIX + node;
			// 如果不存在根节点则创建
			if (!zkClient.exists(zknode)) {
				logger.error("szookeeper [" + zknode + "] is not exists!");
				String[] paths = node.split("/");
				StringBuilder sb = new StringBuilder();
				for (int i = 1; i < paths.length; i++) {
					sb.append("/");
					sb.append(paths[i]);
					logger.debug("szookeeper 创建zookeeper配置节点："
							+ ZkWacher.ZOOKEEPER_ROOT_NODE_PREFIX
							+ sb.toString());
					if (this.zkClient
							.exists(ZkWacher.ZOOKEEPER_ROOT_NODE_PREFIX
									+ sb.toString())) {
						continue;
					}
					this.zkClient
							.createPersistent(ZkWacher.ZOOKEEPER_ROOT_NODE_PREFIX
									+ sb.toString());
				}
			}
			logger.info("szookeeper [" + "开始监听[" + zknode + "]");
			zkClient.subscribeDataChanges(zknode, this);
		}
	}

	/**
	 * 建立跟节点
	 */
	private void initRootNode() {
		if (ZOOKEEPER_ROOT_NODE_PREFIX == null
				|| "".equals(ZOOKEEPER_ROOT_NODE_PREFIX.trim())) {
			return;
		}
		if (!zkClient.exists(ZOOKEEPER_ROOT_NODE_PREFIX)) {
			logger.error("szookeeper [" + ZOOKEEPER_ROOT_NODE_PREFIX
					+ "] is not exists!");
			String[] paths = ZOOKEEPER_ROOT_NODE_PREFIX.split("/");
			StringBuilder sb = new StringBuilder();
			for (int i = 1; i < paths.length; i++) {
				sb.append("/");
				sb.append(paths[i]);
				// logger.debug("szookeeper 创建zookeeper配置节点：" + zknode);
				logger.debug("szookeeper 创建zookeeper配置节点：" + sb.toString());
				if (this.zkClient.exists(sb.toString())) {
					continue;
				}
				this.zkClient.createPersistent(sb.toString());
			}
		}
	}

	/**
	 * 数据被改变
	 */
	@Override
	public void handleDataChange(String zknode, Object data) throws Exception {
		logger.warn("szookeeper " + zknode + " 数据被修改...");
		logger.debug("szookeeper " + zknode + " 修改后数据：");
		// 拿到的是反序列化的ZkObject对象
		logger.debug(((ZkObject) data).getData());
		((DefaultZkNodeDataHandler) zkNodeDataHandler)
				.setZkClient(this.zkClient);
		zkNodeDataHandler.handlerData(zknode, data);
	}

	/**
	 * 数据被删除
	 */
	@Override
	public void handleDataDeleted(String zknode) throws Exception {
		logger.warn("szookeeper " + zknode + " 数据被删除...");
	}

	public String getServers() {
		return servers;
	}
}
