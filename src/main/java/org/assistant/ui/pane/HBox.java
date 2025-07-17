package org.assistant.ui.pane;

import javax.swing.*;
import java.awt.*;

public class HBox extends JPanel {

	public HBox() {
		super();
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	}

	public void addSpacing(int width) {
		add(Box.createRigidArea(new Dimension(width, 0)));
	}

	public void setEmptyBorder(int topLeftBottomRight) {
		this.setBorder(BorderFactory.createEmptyBorder(topLeftBottomRight, topLeftBottomRight, topLeftBottomRight, topLeftBottomRight));
	}
}
