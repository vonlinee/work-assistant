package org.assistant.ui.pane;

import javax.swing.*;
import java.awt.*;

public class VBox extends JPanel {

	public VBox() {
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	}

	public void addSpacing(int height) {
		add(Box.createRigidArea(new Dimension(0, height)));
	}

	public void setEmptyBorder(int topLeftBottomRight) {
		this.setBorder(BorderFactory.createEmptyBorder(topLeftBottomRight, topLeftBottomRight, topLeftBottomRight, topLeftBottomRight));
	}
}
