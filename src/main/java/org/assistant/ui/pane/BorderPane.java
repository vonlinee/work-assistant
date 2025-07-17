package org.assistant.ui.pane;

import javax.swing.*;
import java.awt.*;

public class BorderPane extends JPanel {

	public BorderPane() {
		super(new BorderLayout());
	}

	public void setTop(JComponent component) {
		add(component, BorderLayout.NORTH);
	}

	public void setCenter(JComponent component) {
		add(component, BorderLayout.CENTER);
	}

	public void setBottom(JComponent component) {
		add(component, BorderLayout.SOUTH);
	}

	public void setLeft(JComponent component) {
		add(component, BorderLayout.WEST);
	}

	public void setRight(JComponent component) {
		add(component, BorderLayout.EAST);
	}
}
