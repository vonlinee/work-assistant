package org.assistant;

import com.formdev.flatlaf.FlatLightLaf;
import org.assistant.tools.ToolCollectionPane;
import org.assistant.util.Messages;

import javax.swing.*;

public class MainFrame extends JFrame {

	static {
		FlatLightLaf.setup();
	}

	public static void main(String[] args) {
		MainFrame app = new MainFrame();
		app.setSize(600, 500);
		app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// 窗体居中
		app.setLocationRelativeTo(null);

		ToolCollectionPane tabPane = new ToolCollectionPane();
		app.add(tabPane);
		app.setTitle(Messages.getString("app.title"));
		app.setVisible(true);
	}
}
