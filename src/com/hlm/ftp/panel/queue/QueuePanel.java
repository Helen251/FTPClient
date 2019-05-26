package com.hlm.ftp.panel.queue;

import com.hlm.ftp.FTPClientFrame;
import com.hlm.ftp.utils.FtpClient;
import com.hlm.ftp.utils.FtpFile;
import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.EAST;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.LinkedList;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;


public class QueuePanel extends JPanel implements ActionListener {
	private JTable queueTable = new JTable(); // 显示任务队列的表格组件
	private JScrollPane scrollPane = new JScrollPane();
	private FTPClientFrame frame; // 主窗体的引用对象
	private String[] columns;
	private FtpClient ftpClient; 	// FTP协议的控制类
	private Timer queueTimer; 		// 队列的定时器
	private LinkedList<Object[]> localQueue; 	// 本地面板的上传队列
	private LinkedList<Object[]> ftpQueue; 		// FTP面板的下载队列
	private JToggleButton stopButton;
	private boolean stop = false; // 队列的控制变量

	/**
	 * 默认的构造方法
	 */
	public QueuePanel() {
		initComponent();
	}


	public QueuePanel(FTPClientFrame frame) {
		this.frame = frame;
		// 从主窗体获取本地面板的上传队列
		localQueue = (LinkedList<Object[]>) frame.getLocalPanel().getQueue();
		// 从主窗体获取FTP面板的下载队列
		ftpQueue = (LinkedList<Object[]>) frame.getFtpPanel().getQueue();
		initComponent(); // 初始化窗体界面
		// 创建定时器，每间隔1秒执行队列刷新任务
		queueTimer = new Timer(1000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (localQueue.size() + ftpQueue.size() == queueTable
						.getRowCount()) // 如果队列大小没有改变
					return; // 结束本方法，不做任何操作
				refreshQueue(); // 否则刷新显示队列信息的表格数据
			}

		});
	}

	private void initComponent() {
		BorderLayout cardLayout = new BorderLayout();
		setLayout(cardLayout);
		columns = new String[] { "任务名称", "方向", "主机", "执行状态" };
		queueTable.setModel(new DefaultTableModel(new Object[][] {}, columns));
		queueTable.getTableHeader().setReorderingAllowed(false);
		scrollPane.setViewportView(queueTable);
		cardLayout.layoutContainer(scrollPane);
		add(scrollPane, CENTER);
		JToolBar controlTool = new JToolBar(JToolBar.VERTICAL);
		controlTool.setRollover(true);
		controlTool.setFloatable(false);
		stopButton = new JToggleButton("暂停");
		stopButton.setActionCommand("stop&start");
		stopButton.addActionListener(this);
		JButton delButton = new JButton("删除");
		delButton.setActionCommand("del");
		delButton.addActionListener(this);
		JButton clearButton = new JButton("清空");
		clearButton.setActionCommand("clear");
		clearButton.addActionListener(this);
		controlTool.setLayout(new BoxLayout(controlTool, BoxLayout.Y_AXIS));
		controlTool.add(stopButton);
		controlTool.add(delButton);
		controlTool.add(clearButton);
		add(controlTool, EAST);
	}

	public void startQueue() {
		ftpClient = frame.getFtpClient();
		queueTimer.start();
	}

	/**
	 * 界面上按钮的事件处理方法
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		final String command = e.getActionCommand();
		if (command.equals("stop&start")) {// 处理暂停按钮事件
			if (stopButton.isSelected()) {
				stop = true;
				stopButton.setText("继续");
			} else {
				stop = false;
				stopButton.setText("暂停");
			}
		}

		if (command.equals("del")) {// 处理删除按钮的事件
			int row = queueTable.getSelectedRow(); // 获取显示队列的表格的当前选择行
			if (row < 0)
				return;
			// 获取选择行的第一个表格单元值
			Object valueAt = queueTable.getValueAt(row, 0);
			// 如果选择内容是File类的对象
			if (valueAt instanceof File) {
				File file = (File) valueAt;
				int size = localQueue.size(); // 获取上传队列大小
				for (int i = 0; i < size; i++) { // 遍历上传队列
					if (localQueue.get(i)[0].equals(file))
						localQueue.remove(i); // 从上传队列中删除文件对象
					}
				}
			else if (valueAt instanceof String) { // 如果选择的是字符串对象
				String fileStr = (String) valueAt;
				int size = ftpQueue.size(); // 获取上传队列的大小
				for (int i = 0; i < size; i++) { // 遍历上传队列
					// 获取上传队列中的文件对象
					FtpFile ftpFile = (FtpFile) ftpQueue.get(i)[0];
					if (ftpFile.getAbsolutePath().equals(fileStr)) {
						ftpQueue.remove(i); // 从上传队列中删除该文件对象
					}
				}
			}
			refreshQueue(); // 刷新队列列表
		}
		if (command.equals("clear")) { // 处理清空按钮的事件
			localQueue.clear(); // 调用本地面板的队列的clear()方法
			ftpQueue.clear(); // 调用FTP面板的队列的clear()方法
		}
	}
	/**
	 * 刷新队列的方法
	 */
	private synchronized void refreshQueue() {
		// 创建表格的数据模型对象
		DefaultTableModel model = new DefaultTableModel(columns, 0);
		// 获取本地上传队列中的任务
		Object[] localQueueArray = localQueue.toArray();
		// 遍历本地上传任务
		for (int i = 0; i < localQueueArray.length; i++) {
			Object[] queueValue = (Object[]) localQueueArray[i];
			if (queueValue == null)
				continue;
			File localFile = (File) queueValue[0];
			// 把上传队列的任务添加到表格组件的数据模型中
			model.addRow(new Object[] { localFile.getAbsoluteFile(), "上传",
					ftpClient.getServer(), i == 0 ? "正在上传" : "等待上传" });
		}
		// 获取下载队列的任务
		Object[] ftpQueueArray = ftpQueue.toArray();
		// 遍历下载队列
		for (int i = 0; i < ftpQueueArray.length; i++) {
			Object[] queueValue = (Object[]) ftpQueueArray[i];
			if (queueValue == null)
				continue;
			FtpFile ftpFile = (FtpFile) queueValue[0];
			// 把下载队列的任务添加到表格组件的数据模型中
			model.addRow(new Object[] { ftpFile.getAbsolutePath(), "下载",
					ftpClient.getServer(), i == 0 ? "正在下载" : "等待下载" });
		}
		queueTable.setModel(model); // 设置表格使用本方法的表格数据模型
	}

	public boolean isStop() {
		return stop;
	}
}