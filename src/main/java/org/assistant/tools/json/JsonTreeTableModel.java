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
        // Core JSON nodes are inherently read-only structure viewers
			if (column == 1) {
				if (node instanceof JsonNode) {
					if (((JsonNode) node).isLeaf()) {
						return true;
					}
				}
			}
        return false;
    }
}
