package org.assistant.jcef;

import org.assistant.editor.RSyntaxCodeEditor;
import org.assistant.editor.jcef.JcefMonacoEditor;
import org.assistant.ui.controls.Button;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Test extends JFrame {

	public static void main(String[] args) {
		Test test = new Test();
		test.setLayout(new BorderLayout());

		JPanel jPanel = new JPanel();
		Button btn = new Button("获取文本");
		Button btn1 = new Button("设置文本");
		jPanel.add(btn);
		jPanel.add(btn1);
		test.add(jPanel, BorderLayout.NORTH);

		RSyntaxCodeEditor editor = new RSyntaxCodeEditor();
		editor.setSize(500, 400);
		test.add(editor);

		btn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				super.mouseClicked(e);
				System.out.println(">>> " + editor.getText());
			}
		});

		btn1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				super.mouseClicked(e);
				editor.setText("1111111111111111");
			}
		});


		test.setSize(500, 400);
		test.setVisible(true);
	}
}
