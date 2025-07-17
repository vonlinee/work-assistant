package org.assistant.control.table;

import org.assistant.tools.excel.ExcelHeaderTable;

import javax.swing.*;

public class Demo {

	public static void main(String[] args) {

		JFrame frame = new JFrame();
		frame.setSize(600, 400);

		ExcelHeaderTable table = new ExcelHeaderTable();

		frame.add(table);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

	}
}
