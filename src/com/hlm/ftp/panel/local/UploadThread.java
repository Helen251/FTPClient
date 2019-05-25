
package com.hlm.ftp.panel.local;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Queue;
import com.hlm.ftp.panel.ftp.FtpPanel;
import com.hlm.ftp.utils.FtpClient;
import com.hlm.ftp.utils.FtpFile;
import com.hlm.ftp.utils.ProgressArg;

class UploadThread extends Thread {
	private LocalPanel localPanel;
	String path = "";// 上传文件的本地相对路径
	String selPath;// 选择的本地文件的路径
	private boolean conRun = true; // 线程的控制变量
	private FtpClient ftpClient; // FTP控制类
	private Object[] queueValues; // 队列任务数组

	/**
	 * 创建上传队列线程的构造方法
	 *
	 * @param localPanel
	 *            - 本地资源管理面板
	 * @param server
	 *            - FTP服务器地址
	 * @param port
	 *            - FTP服务器端口号
	 * @param userStr
	 *            - 登录FTP服务器的用户名
	 * @param passStr
	 *            - 登录FTP服务器的密码
	 */
	public UploadThread(LocalPanel localPanel, String server, int port,
						String userStr, String passStr) {
		try {
			ftpClient = new FtpClient(server, port);
			ftpClient.login(userStr, passStr);
			ftpClient.binary();
			path = ftpClient.pwd();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.localPanel = localPanel;
		new Thread() { // 创建保持服务器通讯的线程
			public void run() {
				while (conRun) {
					try {
						Thread.sleep(30000);
						// 定时向服务器发送消息，保持连接
						UploadThread.this.ftpClient.noop();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}

	public void stopThread() { // 停止线程的方法
		conRun = false;
	}

	private void copyFile(File file, FtpFile ftpFile) { // 递归遍历文件夹的方法
		// 判断队列面板是否执行暂停命令
		while (localPanel.frame.getQueuePanel().isStop()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		Object[] args = localPanel.queue.peek();
		// 判断队列顶是不是上一个处理的任务。
		if (queueValues == null || args == null
				|| !queueValues[0].equals(args[0]))
			return;
		try {
			path = file.getParentFile().getPath().replace(selPath, "");
			ftpFile.setName(path.replace("\\", "/"));
			path = ftpFile.getAbsolutePath();
			System.out.println(path);
			if (file.isFile()) {
				String remoteFile = path + "/" + file.getName(); // 远程FTP的文件名绝对路径
				System.out.println("remoteFile:" + remoteFile);
				ProgressArg progressArg = new ProgressArg(
						(int) (file.length() / 1024), 0, 0);
				OutputStream put = ftpClient.put(remoteFile); // 获取服务器文件的输出流
				FileInputStream fis = null; // 本地文件的输入流
				try {
					fis = new FileInputStream(file); // 初始化文件的输入流
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
				int readNum = 0;
				byte[] data = new byte[1024]; // 缓存大小
				while ((readNum = fis.read(data)) > 0) { // 读取本地文件到缓存
					Thread.sleep(0, 30); // 线程休眠
					put.write(data, 0, readNum); // 输出到服务器
					progressArg.setValue(progressArg.getValue() + 1);// 累加进度条
				}
				progressArg.setValue(progressArg.getMax()); // 结束进度条
				fis.close(); // 关闭文件输入流
				put.close(); // 关闭服务器输出流
			} else if (file.isDirectory()) {
				// path = ftpFile.getAbsolutePath();
				path = file.getPath().replace(selPath, "");
				ftpFile.setName(path.replace("\\", "/"));
				ftpClient.sendServer("MKD " + path + "\r\n");
				ftpClient.readServerResponse();
				File[] listFiles = file.listFiles();
				for (File subFile : listFiles) {
					Thread.sleep(0, 50);
					copyFile(subFile, ftpFile);
				}
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			System.exit(0);
			// JOptionPane.showMessageDialog(localPanel, e1.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * 线程的主体方法
	 *
	 * @see java.lang.Thread#run()
	 */
	public void run() { // 线程的主体方法
		while (conRun) {
			try {
				Thread.sleep(1000); // 线程休眠1秒
				Queue<Object[]> queue = localPanel.queue; // 获取本地面板的队列对象
				queueValues = queue.peek(); // 获取队列首的对象
				if (queueValues == null) { // 如果该对象为空
					continue; // 进行下一次循环
				}
				File file = (File) queueValues[0]; // 获取队列中的本队文件对象
				FtpFile ftpFile = (FtpFile) queueValues[1]; // 获取队列中的FTP文件对象
				if (file != null) {
					selPath = file.getParent();
					copyFile(file, ftpFile); // 调用递归方法上传文件
					FtpPanel ftpPanel = localPanel.frame.getFtpPanel();
					ftpPanel.refreshCurrentFolder(); // 刷新FTP面板中的资源
				}
				Object[] args = queue.peek();
				// 判断队列顶是否为处理的上一个任务。
				if (queueValues == null || args == null
						|| !queueValues[0].equals(args[0])) {
					continue;
				}
				queue.remove(); // 移除队列首元素
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}