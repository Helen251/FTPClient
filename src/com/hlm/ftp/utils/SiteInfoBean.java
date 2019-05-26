package com.hlm.ftp.utils;


public class SiteInfoBean {
	private String siteName; //
	private String server;
	private String userName;
	private int port; //
	private String id; //


	public SiteInfoBean(String siteName, String server, int port,
			String userName) {
		id = System.currentTimeMillis() + "";
		this.siteName = siteName;
		this.server = server;
		this.port = port;
		this.userName = userName;
	}


	public SiteInfoBean(String id, String info) {
		this.id = id;
		String[] infos = info.split(",");
		this.siteName = infos[0];
		this.server = infos[1];
		this.port = Integer.valueOf(infos[2]);
		this.userName = infos[3];
	}


	public String getSiteName() {
		return siteName;
	}


	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}


	@Override
	public String toString() {
		return siteName;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
