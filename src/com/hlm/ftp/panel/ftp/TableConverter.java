package com.hlm.ftp.panel.ftp;

import com.hlm.ftp.utils.FileInterface;
import javax.swing.table.TableModel;
import javax.swing.table.TableStringConverter;

public class TableConverter extends TableStringConverter {

	@Override
	public String toString(TableModel model, int row, int column) {
		Object value = model.getValueAt(row, column);
		if (value instanceof FileInterface) {
			FileInterface file = (FileInterface) value;
			if (file.isDirectory())
				return "!" + file.toString();
			else
				return "Z" + file.toString();
		}
		if (value.equals(".") || value.equals(".."))
			return "!!";
		return value.toString();
	}

}
