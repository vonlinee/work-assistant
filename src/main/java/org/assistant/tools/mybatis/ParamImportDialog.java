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
import java.util.function.BiConsumer;

public class ParamImportDialog extends JDialog {

    private final RSyntaxTextArea inputArea;
    private final ParamNode rootNode;
    private final BiConsumer<Map<String, String>, Boolean> onImport;
    private JComboBox<String> strategyCombo;
    private JComboBox<String> formatCombo;

    public ParamImportDialog(Frame owner, ParamNode rootNode, BiConsumer<Map<String, String>, Boolean> onImport) {
        super(owner, "Import Parameters", true);
        this.rootNode = rootNode;
        this.onImport = onImport;

        inputArea = new RSyntaxTextArea(15, 60);
        inputArea.setToolTipText("Paste a URL query string or a JSON object here...");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        formatCombo = new JComboBox<>(new String[] { "JSON", "URL Query" });
        JButton mockBtn = new JButton("🎲 Generate Mock");
        strategyCombo = new JComboBox<>(new String[] { "Merge", "Override" });
        JButton importBtn = new JButton("Parse & Import");
        JButton cancelBtn = new JButton("Cancel");

        mockBtn.addActionListener(e -> {
            if ("JSON".equals(formatCombo.getSelectedItem())) {
                generateMockJson();
            } else {
                generateMockUrl();
            }
        });
        importBtn.addActionListener(e -> parseAndImport());
        cancelBtn.addActionListener(e -> dispose());

        buttonPanel.add(formatCombo);
        buttonPanel.add(mockBtn);
        buttonPanel.add(new JLabel("Strategy: "));
        buttonPanel.add(strategyCombo);
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
                boolean isOverride = "Override".equals(strategyCombo.getSelectedItem());
                onImport.accept(parsedParams, isOverride);
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
            params.put(prefixKey, element.toString());
        }
    }

    private void generateMockJson() {
        if (rootNode == null || rootNode.getChildCount() == 0) {
            JOptionPane.showMessageDialog(this, "No parameters available to mock.", "Empty Params",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JsonObject rootJson = new JsonObject();
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            ParamNode child = (ParamNode) rootNode.getChildAt(i);
            buildMockJson(rootJson, child);
        }

        com.google.gson.Gson gson = new com.google.gson.GsonBuilder().setPrettyPrinting().create();
        inputArea.setText(gson.toJson(rootJson));
        inputArea.setCaretPosition(0);
    }

    private void buildMockJson(JsonObject parent, ParamNode node) {
        if (node.getChildCount() > 0) {
            JsonObject obj = new JsonObject();
            for (int i = 0; i < node.getChildCount(); i++) {
                buildMockJson(obj, (ParamNode) node.getChildAt(i));
            }
            parent.add(node.getKey(), obj);
        } else {
            String type = node.getDataType() != null ? node.getDataType().toUpperCase() : "STRING";
            if (type.equals("NUMERIC")) {
                parent.addProperty(node.getKey(), 1);
            } else if (type.equals("BOOLEAN")) {
                parent.addProperty(node.getKey(), true);
            } else {
                parent.addProperty(node.getKey(), "mock_" + node.getKey().replace("[", "").replace("]", ""));
            }
        }
    }

    private void generateMockUrl() {
        if (rootNode == null || rootNode.getChildCount() == 0) {
            JOptionPane.showMessageDialog(this, "No parameters available to mock.", "Empty Params",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            ParamNode child = (ParamNode) rootNode.getChildAt(i);
            buildMockUrl(sb, child.getKey(), child);
        }

        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '&') {
            sb.deleteCharAt(sb.length() - 1);
        }

        inputArea.setText(sb.toString());
        inputArea.setCaretPosition(0);
    }

    private void buildMockUrl(StringBuilder sb, String currentPath, ParamNode node) {
        if (node.getChildCount() > 0) {
            for (int i = 0; i < node.getChildCount(); i++) {
                ParamNode child = (ParamNode) node.getChildAt(i);
                String childPath = child.getKey();
                String newPath;
                if (childPath.startsWith("[")) {
                    newPath = currentPath + childPath;
                } else {
                    newPath = currentPath + "." + childPath;
                }
                buildMockUrl(sb, newPath, child);
            }
        } else {
            String type = node.getDataType() != null ? node.getDataType().toUpperCase() : "STRING";
            Object val;
            if (type.equals("NUMERIC")) {
                val = 1;
            } else if (type.equals("BOOLEAN")) {
                val = true;
            } else {
                val = "mock_" + node.getKey().replace("[", "").replace("]", "");
            }

            try {
                sb.append(java.net.URLEncoder.encode(currentPath, "UTF-8"))
                        .append("=")
                        .append(java.net.URLEncoder.encode(String.valueOf(val), "UTF-8"))
                        .append("&");
            } catch (Exception e) {
                sb.append(currentPath).append("=").append(val).append("&");
            }
        }
    }
}
