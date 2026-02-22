package org.assistant.tools.doc;

import org.assistant.tools.ToolProvider;

import javax.swing.*;

/**
 * Registers the API Doc tool in the tool collection pane.
 * <p>
 * Auto-discovered via {@link org.reflections.Reflections}.
 * </p>
 */
class ApiDocToolProvider implements ToolProvider {

    @Override
    public String getLabel() {
        return "API Doc";
    }

    @Override
    public JComponent getView() {
        return new ApiDocToolPane();
    }

    @Override
    public int getOrder() {
        return 10;
    }
}
