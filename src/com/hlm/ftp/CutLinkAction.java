package com.hlm.ftp;

import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.Icon;

class CutLinkAction extends AbstractAction {
	private FTPClientFrame frame; // ����������ö���

	/**
	 * ���췽��
	 * 
	 * @param client_Frame
	 *            �����������
	 * @param string
	 *
	 * @param icon
	 *
	 */
	public CutLinkAction(FTPClientFrame client_Frame, String string, Icon icon) {
		super(string, icon);
		frame = client_Frame;
		setEnabled(false);
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			frame.ftpPanel.stopDownThread();
			frame.localPanel.stopUploadThread();
			frame.getFtpPanel().getQueue().clear();
			frame.getFtpPanel().clearTable();
			frame.getLocalPanel().getQueue().clear();
			if (frame.ftpClient != null && frame.ftpClient.serverIsOpen()) {
				frame.ftpClient.sendServer("quit\r\n");
				frame.ftpClient.readServerResponse();
				frame.ftpClient = null;
			}
			frame.localPanel.getActionMap().get("uploadAction").setEnabled(
					false);
			frame.ftpPanel.getActionMap().get("downAction").setEnabled(false);
			setEnabled(false);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}