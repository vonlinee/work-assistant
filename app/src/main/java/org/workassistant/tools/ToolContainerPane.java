package org.workassistant.tools;

import javafx.collections.ObservableList;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.reflections.Reflections;
import org.workassistant.util.ClassUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * @see ToolProvider
 */
public class ToolContainerPane extends TabPane {

    static final List<Class<? extends ToolProvider>> providerTypes = new ArrayList<>();

    static {
        // 创建 Reflections 实例
        Reflections reflections = new Reflections(ToolContainerPane.class.getPackage().getName());
        // 获取指定包及其子包下的所有类
        Set<Class<? extends ToolProvider>> providers = reflections.getSubTypesOf(ToolProvider.class);
        // 打印所有类
        providerTypes.addAll(providers);
    }

    public ToolContainerPane() {
        ObservableList<Tab> tabs = getTabs();

        List<ToolProvider> providers = new ArrayList<>();
        for (Class<? extends ToolProvider> providerType : providerTypes) {
            ToolProvider provider = ClassUtils.instantiate(providerType);
            providers.add(provider);
        }

        providers.sort(Comparator.comparing(ToolProvider::getOrder));
        for (ToolProvider provider : providers) {
            Tab tab = new Tab(provider.getLabel(), provider.getRoot());
            tab.setClosable(false);
            tabs.add(tab);
        }
    }
}
