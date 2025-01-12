package io.devpl.fxui.plugins;

import org.mybatis.generator.api.PluginAdapter;

import java.util.List;

/**
 * Swagger支持插件
 */
public class SwaggerSupportPlugin extends PluginAdapter {

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }
}
