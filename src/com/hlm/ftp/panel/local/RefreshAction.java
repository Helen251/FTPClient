package com.hlm.ftp.panel.local;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Icon;

class RefreshAction extends AbstractAction {
	private LocalPanel localPanel;


	public RefreshAction(LocalPanel localPanel, String name, Icon icon) {
		super(name, icon);
		this.localPanel = localPanel;
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		this.localPanel.refreshCurrentFolder();
	}
}