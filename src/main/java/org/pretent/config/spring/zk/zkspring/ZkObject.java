package org.pretent.config.spring.zk.zkspring;

import java.io.Serializable;

public class ZkObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1337247042875173758L;

	private byte[] bytes;

	private String data;

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public byte[] getBytes() {
		return bytes;
	}

	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}

}
