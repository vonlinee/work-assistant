package org.assistant.tools;

import org.reflections.Reflections;
import org.springframework.beans.BeanUtils;
import org.assistant.ui.pane.TabPane;

import java.util.Set;

public class ToolCollectionPane extends TabPane {

	public ToolCollectionPane() {
		Reflections reflections = new Reflections(getClass().getPackageName());
		Set<Class<? extends ToolProvider>> toolProviderTypes = reflections.getSubTypesOf(ToolProvider.class);
		for (Class<? extends ToolProvider> toolProviderType : toolProviderTypes) {
			ToolProvider provider = BeanUtils.instantiateClass(toolProviderType);
			addTool(provider);
		}
	}

	public void addTool(ToolProvider provider) {
		addTab(provider.getLabel(), provider.getView());
	}
}
