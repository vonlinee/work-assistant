package io.devpl.fxui.bridge;

import org.mybatis.generator.api.CompositePlugin;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.PluginConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 每个Context有一个PluginAggregator对象
 * 只会添加MBGPlugin这一个插件，MBGPlugin管理程序中用到的所有插件
 */
public class PluginRegistry extends CompositePlugin {

    private final Map<Class<?>, PluginConfiguration> plugins = new HashMap<>();

    Context context;
    Properties properties;

    @Override
    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    public void registerPlugin(Class<?> pluginClass) {
        if (!contains(pluginClass)) {
            PluginConfiguration pluginConfiguration = new PluginConfiguration();
            pluginConfiguration.addProperty("type", pluginClass.getName());
            pluginConfiguration.setConfigurationType(pluginClass.getName());
            plugins.put(pluginClass, pluginConfiguration);
        }
    }

    public boolean contains(Class<?> clazz) {
        return plugins.containsKey(clazz);
    }

    public PluginConfiguration getPluginConfiguration(Class<?> pluginClass) {
        return plugins.get(pluginClass);
    }
}
