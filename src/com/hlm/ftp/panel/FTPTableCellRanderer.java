package com.hlm.ftp.panel;

import java.io.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import javax.swing.table.*;
import com.hlm.ftp.utils.*;

public class FTPTableCellRanderer extends DefaultTableCellRenderer {
	private final ImageIcon folderIcon = new ImageIcon(getClass().getResource(
            "/com/hlm/ftp/res/folderIcon.JPG"));
	private final ImageIcon fileIcon = new ImageIcon(getClass().getResource(
            "/com/hlm/ftp/res/fileIcon.JPG"));
	private static FTPTableCellRanderer instance = null;


	private FTPTableCellRanderer() {
	}

	public static FTPTableCellRanderer getCellRanderer() {
		if (instance == null)
			instance = new FTPTableCellRanderer();
		return instance;
	}

	@Override
	protected void setValue(Object value) {
		if (value instanceof FileInterface) {
			FileInterface file = (FileInterface) value;
			FileSystemView view = FileSystemView.getFileSystemView();
			if (file.isDirectory()) {
				setText(file.toString());
				setIcon(folderIcon);
			} else {
				if (file instanceof File) {
					Icon icon = view.getSystemIcon((File) file);
					setIcon(icon);
				} else if (file instanceof FtpFile) {
					FtpFile ftpfile = (FtpFile) file;
					try {

						File tempFile = File.createTempFile("tempfile_",
								ftpfile.getName());
						Icon icon = view.getSystemIcon(tempFile);
						tempFile.delete();
						setIcon(icon);
					} catch (IOException e) {
						e.printStackTrace();
						setIcon(fileIcon);
					}
				}
				setText(file.toString());
			}
		} else {
			setIcon(folderIcon);
			setText(value.toString());
		}
	}
}
