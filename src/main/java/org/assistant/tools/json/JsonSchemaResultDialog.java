package org.assistant.tools.json;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;

public class JsonSchemaResultDialog extends JDialog {

    public JsonSchemaResultDialog(Frame owner, String generatedSchema) {
        super(owner, "Generated JSON Schema", true);

        RSyntaxTextArea textArea = new RSyntaxTextArea(30, 80);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
        textArea.setCodeFoldingEnabled(true);
        textArea.setText(generatedSchema);
        textArea.setCaretPosition(0);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton copyBtn = new JButton("Copy to Clipboard");
        JButton closeBtn = new JButton("Close");

        copyBtn.addActionListener(e -> {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(generatedSchema), null);
            JOptionPane.showMessageDialog(this, "Copied to clipboard!", "Success", JOptionPane.INFORMATION_MESSAGE);
        });

        closeBtn.addActionListener(e -> dispose());

        buttonPanel.add(copyBtn);
        buttonPanel.add(closeBtn);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(new RTextScrollPane(textArea), BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
    }
}
