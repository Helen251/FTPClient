package com.hlm.ftp;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;

import com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel;

public class FTPMain {

	public static void main(String args[]) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.put("swing.boldMetal", false);
					if (System.getProperty("substancelaf.useDecorations") == null) {
						JFrame.setDefaultLookAndFeelDecorated(true);
						JDialog.setDefaultLookAndFeelDecorated(true);
					}
					System.setProperty("sun.awt.noerasebackground", "true");
					FTPClientFrame client_Frame = new FTPClientFrame();
					client_Frame.setVisible(true);
				} catch (Exception ex) {
					Logger.getLogger(FTPClientFrame.class.getName()).log(
							Level.SEVERE, null, ex);
				}
			}
		});
	}
}
