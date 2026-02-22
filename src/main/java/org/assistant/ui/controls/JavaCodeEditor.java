package org.assistant.ui.controls;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.*;

public class JavaCodeEditor extends JFrame {

    public JavaCodeEditor() {
        JPanel cp = new JPanel(new BorderLayout());

        // 1. Create the text area
        RSyntaxTextArea textArea = new RSyntaxTextArea(20, 60);

        // 2. Set the language (SyntaxConstants has Java, Python, C, etc.)
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);

        // 3. Enable anti-aliasing and code folding
        textArea.setCodeFoldingEnabled(true);
        textArea.setAntiAliasingEnabled(true);

        // 4. Wrap it in an RTextScrollPane (adds line numbers)
        RTextScrollPane sp = new RTextScrollPane(textArea);
        cp.add(sp);

        setContentPane(cp);
        setTitle("Java Syntax Highlighter");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        // Run on the Event Dispatch Thread (Swing best practice)
        SwingUtilities.invokeLater(() -> {
            new JavaCodeEditor().setVisible(true);
        });
    }
}