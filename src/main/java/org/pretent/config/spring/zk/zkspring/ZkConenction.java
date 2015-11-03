package org.pretent.config.spring.zk.zkspring;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.I0Itec.zkclient.ZkClient;

public class ZkConenction {
	public static final String ZOOKEEPER_NODE_PATH = "/config";

	public static void main(String[] args) throws Exception {
		// 连接zookeeper服务器
		ZkClient zkClient = new ZkClient("127.0.0.1:2181");
		// 读取本地spring配置
		String filePath = "/Users/user/app.xml";
		// zookeeper保存spring配置的znode
		String zkPath = ZOOKEEPER_NODE_PATH + "/app.xml";
		ZkObject object = new ZkObject();
		object.setData(readFile(filePath).trim());
		System.out.println(object.getData());
		// 上传配置
		zkClient.writeData(zkPath, object);
		zkClient.close();
		System.out.println("配置上传完成");

	}

	/**
	 * 读取本地配置
	 * 
	 * @param path
	 * @return
	 * @throws Exception
	 */
	public static String readFile(String path) throws Exception {
		InputStream in = new FileInputStream(new File(path));
		byte[] buf = new byte[1024];
		StringBuffer sbf = new StringBuffer();
		int len = -1;
		while ((len = in.read(buf)) != -1) {
			sbf.append(new String(buf), 0, len);
		}
		in.close();
		return sbf.toString();
	}
}
