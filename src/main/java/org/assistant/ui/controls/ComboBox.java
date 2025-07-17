package org.assistant.ui.controls;

import javax.swing.*;
import java.awt.*;

public class ComboBox<E> extends JComboBox<E> {

	public void setPreferredSize(int w, int h) {
		setPreferredSize(new Dimension(w, h));
	}
}
