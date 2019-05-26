/**
 *
 */
package com.hlm.ftp.panel.ftp;

import java.io.*;

import javax.swing.*;
import com.hlm.ftp.utils.FtpClient;
import com.hlm.ftp.utils.FtpFile;
import com.hlm.ftp.utils.ProgressArg;
import sun.net.*;

public class DownThread extends Thread {
	private final FtpPanel ftpPanel; // FTP资源管理面板
	private final FtpClient ftpClient; // FTP控制类
	private boolean conRun = true; // 线程的控制变量
	private String path; // FTP的路径信息
	private Object[] queueValues; // 下载任务的数组

	/**
	 * 构造方法
	 *
	 * @param ftpPanel
	 *            - FTP资源管理面板
	 */
	public DownThread(FtpPanel ftpPanel) {
		this.ftpPanel = ftpPanel;
		ftpClient = new FtpClient(); // 创建新的FTP控制对象
		FtpClient ftp = ftpPanel.ftpClient;
		try {
			// 连接到FTP服务器
			ftpClient.openServer(ftp.getServer(), ftp.getPort());
			ftpClient.login(ftp.getName(), ftp.getPass()); // 登录服务器
			ftpClient.binary(); // 使用二进制传输
			ftpClient.noop();
		} catch (IOException e) {
			e.printStackTrace();
		}
		new Thread() { // 创建保持服务器通讯的线程
			public void run() {
				while (conRun) {
					try {
						Thread.sleep(30000);
						ftpClient.noop(); // 定时向服务器发送消息，保持连接
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}

	public void stopThread() {// 停止线程的方法
		conRun = false;
	}

	/**
	 * 下载线程的递归方法，用户探索FTP下载文件夹的所有子文件夹和内容
	 *
	 * @param file
	 *            - FTP文件对象
	 * @param localFolder
	 *            - 本地文件夹对象
	 */
	private void downFile(FtpFile file, File localFolder) {
		// 判断队列面板是否执行暂停命令
		while (ftpPanel.frame.getQueuePanel().isStop()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		Object[] args = ftpPanel.queue.peek();
		// 判断队列顶是否为处理的上一个任务。
		if (queueValues == null || args == null
				|| !queueValues[0].equals(args[0]))
			return;
		try {
			String ftpFileStr = file.getAbsolutePath().replaceFirst(path + "/",
					"");
			if (file.isFile()) {
				// 获取服务器指定文件的输入流
				TelnetInputStream ftpIs = ftpClient.get(file.getName());
				if (ftpIs == null) {
					JOptionPane.showMessageDialog(this.ftpPanel, file.getName()
							+ "无法下载");
					return;
				}
				// 创建本地文件对象
				File downFile = new File(localFolder, ftpFileStr);
				// 创建本地文件的输出流
				FileOutputStream fout = new FileOutputStream(downFile, true);
				ProgressArg progressArg = new ProgressArg((int) (file
						.getLongSize() / 1024), 0, 0);
				byte[] data = new byte[1024]; // 定义缓存
				int read = -1;
				while ((read = ftpIs.read(data)) > 0) { // 读取FTP文件内容到缓存
					Thread.sleep(0, 30); // 线程休眠
					fout.write(data, 0, read); // 将缓存数据写入本地文件
					// 累加进度条
					progressArg.setValue(progressArg.getValue() + 1);
				}
				progressArg.setValue(progressArg.getMax());// 结束进度条
				fout.close(); // 关闭文件输出流
				ftpIs.close(); // 关闭FTP文件输入流
			} else if (file.isDirectory()) { // 如果下载的是文件夹
				// 创建本地文件夹对象
				File directory = new File(localFolder, ftpFileStr);
				directory.mkdirs(); // 创建本地的文件夹
				ftpClient.cd(file.getName()); // 改变FTP服务器的当前路径
				// 获取FTP服务器的文件列表信息
				InputStreamReader list = new InputStreamReader(ftpClient.list());
				BufferedReader br = new BufferedReader(list);
				String nameStr = null;
				while ((nameStr = br.readLine()) != null) {
					Thread.sleep(0, 50);
					String name = nameStr.substring(39);
					String size = nameStr.substring(18, 39);
					FtpFile ftpFile = new FtpFile(); // 创建FTP文件对象
					ftpFile.setName(name); // 设置文件名
					ftpFile.setPath(file.getAbsolutePath());// 设置文件路径
					ftpFile.setSize(size); // 设置文件大小
					downFile(ftpFile, localFolder); // 递归执行子文件夹的下载
				}
				list.close();
				br.close();
				ftpClient.cdUp(); // 返回FTP上级路径
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void run() { // 线程业务方法
		while (conRun) {
			try {
				Thread.sleep(1000);
				ftpClient.noop();

				queueValues = ftpPanel.queue.peek();
				if (queueValues == null) {
					continue;
				}
				FtpFile file = (FtpFile) queueValues[0];
				File localFolder = (File) queueValues[1];
				if (file != null) {
					path = file.getPath();
					ftpClient.cd(path);
					downFile(file, localFolder);
					path = null;
					ftpPanel.frame.getLocalPanel().refreshCurrentFolder();
				}
				Object[] args = ftpPanel.queue.peek();
				// 判断队列顶是否为处理的上一个任务。
				if (queueValues == null || args == null
						|| !queueValues[0].equals(args[0]))
					continue;
				ftpPanel.queue.poll();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("thread is UnAlive!");
	}
}