package org.assistant.control;

import org.assistant.ui.controls.Button;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class JTreeExample extends JFrame {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JTreeExample frame = new JTreeExample();
		frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
		frame.pack();
		frame.setSize(400, 400);
		frame.setLocationRelativeTo(null);
		JTree tree = new JTree();

		Button btn = new Button("Add");

		btn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				super.mouseClicked(e);
			}
		});

		frame.add(btn);

		frame.add(tree);
		frame.setVisible(true);
	}
}
