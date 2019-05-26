package com.hlm.ftp.utils;


public class FtpFile implements FileInterface {
	private String name = "";
	private String path = "";
	protected boolean directory;
	private boolean file;
	private String lastDate;
	private String size;
	private long longSize;
	private final int GB = (int) Math.pow(1024, 3);
	private final int MB = (int) Math.pow(1024, 2);
	private final int KB = 1024;


	public FtpFile() {
	}

	/**
	 *
	 * 
	 * @param name
	 *
	 * @param path
	 *
	 * @param directory
	 */
	public FtpFile(String name, String path, boolean directory) {
		this.name = name;
		this.path = path;
		this.directory = directory;
	}

	public String getSize() {
		return size;
	}


	public void setSize(String nsize) {
		if (nsize.indexOf("DIR") != -1) {
			this.size = "<DIR>";
			directory = true;
			file = false;
		} else { // ����
			file = true;
			directory = false;
			this.size = nsize.trim();
			longSize = Long.parseLong(size);
			if (longSize > GB) {
				size = longSize / GB + "G ";
			}
			if (longSize > MB) {
				size = longSize / MB + "M ";
			}
			if (longSize > KB) {
				size = longSize / KB + "K ";
			}
		}
	}

	public String getLastDate() {
		return lastDate;
	}

	public void setLastDate(String lastDate) {
		this.lastDate = lastDate;
	}

	public boolean isFile() {
		return file;
	}

	public boolean isDirectory() {
		return directory;
	}


	public String getAbsolutePath() {
		if (path.lastIndexOf('/') == path.length() - 1)
			return path + name;
		else
			return path + "/" + name;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	@Override
	public String toString() {
		return name;
	}

	public long getLongSize() {
		return longSize;
	}
}
