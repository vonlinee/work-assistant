package org.assistant;

import com.formdev.flatlaf.FlatLightLaf;
import org.assistant.tools.ToolCollectionPane;
import org.assistant.util.Messages;
import org.assistant.util.SwingUtils;

import javax.swing.*;

public class MainFrame extends JFrame {

	static {
		FlatLightLaf.setup();
	}

	public static void main(String[] args) {
		MainFrame app = new MainFrame();

		SwingUtils.setScreenRatioSize(app, 0.6);
		app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// 窗体居中
		SwingUtils.alignToCenter(app);

		ToolCollectionPane tabPane = new ToolCollectionPane();
		app.add(tabPane);
		app.setTitle(Messages.getString("app.title"));
		app.setVisible(true);
	}
}
