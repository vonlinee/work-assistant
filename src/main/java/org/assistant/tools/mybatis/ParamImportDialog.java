package org.assistant.tools.mybatis;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ParamImportDialog extends JDialog {

    private final RSyntaxTextArea inputArea;
    private final Consumer<Map<String, String>> onImport;

    public ParamImportDialog(Frame owner, Consumer<Map<String, String>> onImport) {
        super(owner, "Import Parameters", true);
        this.onImport = onImport;

        inputArea = new RSyntaxTextArea(15, 60);
        inputArea.setToolTipText("Paste a URL query string or a JSON object here...");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton importBtn = new JButton("Parse & Import");
        JButton cancelBtn = new JButton("Cancel");

        importBtn.addActionListener(e -> parseAndImport());
        cancelBtn.addActionListener(e -> dispose());

        buttonPanel.add(importBtn);
        buttonPanel.add(cancelBtn);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JLabel hintLabel = new JLabel("Paste JSON or a URL string containing query parameters:");
        hintLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

        contentPanel.add(hintLabel, BorderLayout.NORTH);
        contentPanel.add(new RTextScrollPane(inputArea), BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        getContentPane().add(contentPanel);
        pack();
        setLocationRelativeTo(owner);
    }

    private void parseAndImport() {
        String content = inputArea.getText().trim();
        if (content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter JSON or a URL string.", "Empty Input",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Map<String, String> parsedParams = new HashMap<>();

        try {
            if (content.startsWith("{") || content.startsWith("[")) {
                // Try JSON parsing
                JsonElement element = JsonParser.parseString(content);
                if (element.isJsonObject()) {
                    JsonObject obj = element.getAsJsonObject();
                    for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                        extractJsonValues(entry.getKey(), entry.getValue(), parsedParams);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Root JSON must be an object to map to parameters.",
                            "Invalid JSON", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else {
                // Try URL/Query String parsing
                String queryString = content;
                if (content.contains("?")) {
                    queryString = content.substring(content.indexOf("?") + 1);
                }

                String[] pairs = queryString.split("&");
                for (String pair : pairs) {
                    int idx = pair.indexOf("=");
                    if (idx > 0) {
                        String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                        String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                        parsedParams.put(key, value);
                    } else if (pair.length() > 0) {
                        // key with no value
                        parsedParams.put(URLDecoder.decode(pair, StandardCharsets.UTF_8), "");
                    }
                }
            }

            if (parsedParams.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No parameters could be parsed from the input.", "Parse Result",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                onImport.accept(parsedParams);
                dispose();
            }

        } catch (JsonSyntaxException jse) {
            JOptionPane.showMessageDialog(this, "Failed to parse JSON:\n" + jse.getMessage(), "Parse Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to parse input:\n" + ex.getMessage(), "Parse Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void extractJsonValues(String prefixKey, JsonElement element, Map<String, String> params) {
        if (element.isJsonNull()) {
            params.put(prefixKey, "");
        } else if (element.isJsonPrimitive()) {
            params.put(prefixKey, element.getAsString());
        } else if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                extractJsonValues(prefixKey + "." + entry.getKey(), entry.getValue(), params);
            }
        } else if (element.isJsonArray()) {
            // Arrays are usually joined or passed as JSON string in simple param contexts.
            // For now, flattening the array as a single string (like "1,2,3") or just JSON
            // string.
            // Using a simple JSON dump for arrays is safest.
            params.put(prefixKey, element.toString());
        }
    }
}
