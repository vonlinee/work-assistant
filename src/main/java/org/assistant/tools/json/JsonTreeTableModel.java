package org.assistant.tools.json;

import org.jdesktop.swingx.treetable.DefaultTreeTableModel;

import java.util.Arrays;

public class JsonTreeTableModel extends DefaultTreeTableModel {

    public JsonTreeTableModel() {
        super(null, Arrays.asList("Key", "Value", "Type"));
    }

    @Override
    public Object getValueAt(Object node, int column) {
        if (node instanceof JsonNode jsonNode) {
            return switch (column) {
                case 0 -> jsonNode.getKey(); // Handled natively by Swing Tree Rendering logic usually, but defined for
                                             // strictness
                case 1 -> jsonNode.getDisplayValue();
                case 2 -> jsonNode.getDisplayType();
                default -> super.getValueAt(node, column);
            };
        }
        return super.getValueAt(node, column);
    }

    @Override
    public boolean isCellEditable(Object node, int column) {
        if (column == 1) {
            if (node instanceof JsonNode jsonNode) {
                if (jsonNode.isLeaf()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void setValueAt(Object value, Object node, int column) {
        if (column == 1 && node instanceof JsonNode jsonNode && jsonNode.isLeaf()) {
            String newValue = value != null ? value.toString() : "";
            com.google.gson.JsonElement newElement;

            try {
                newElement = com.google.gson.JsonParser.parseString(newValue);
                // If it parses to an object or array, but it's a leaf, treat it as a string to be safe.
                if (!newElement.isJsonPrimitive() && !newElement.isJsonNull()) {
                    newElement = new com.google.gson.JsonPrimitive(newValue);
                }
            } catch (Exception e) {
                // Not valid JSON, just store as String
                newElement = new com.google.gson.JsonPrimitive(newValue);
            }

            org.jdesktop.swingx.treetable.TreeTableNode parent = jsonNode.getParent();
            if (parent instanceof JsonNode parentNode && parentNode.getJsonElement() != null) {
                com.google.gson.JsonElement parentEl = parentNode.getJsonElement();
                if (parentEl.isJsonObject()) {
                    parentEl.getAsJsonObject().add(jsonNode.getKey(), newElement);
                } else if (parentEl.isJsonArray()) {
                    String keyStr = jsonNode.getKey();
                    if (keyStr.startsWith("[") && keyStr.endsWith("]")) {
                        try {
                            int idx = Integer.parseInt(keyStr.substring(1, keyStr.length() - 1));
                            parentEl.getAsJsonArray().set(idx, newElement);
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
            jsonNode.updateNodeData(newElement);
        }
    }

    public void fireNodeStructureChanged(Object node) {
        if (node instanceof org.jdesktop.swingx.treetable.TreeTableNode) {
            modelSupport.fireTreeStructureChanged(new javax.swing.tree.TreePath(getPathToRoot((org.jdesktop.swingx.treetable.TreeTableNode) node)));
        }
    }

    public void fireNodeChanged(Object node) {
        if (node instanceof org.jdesktop.swingx.treetable.TreeTableNode) {
            modelSupport.firePathChanged(new javax.swing.tree.TreePath(getPathToRoot((org.jdesktop.swingx.treetable.TreeTableNode) node)));
        }
    }
}
