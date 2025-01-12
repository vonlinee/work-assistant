package io.devpl.fxui.view;

import java.util.ArrayList;
import java.util.List;

/**
 * 生成器选项
 */
public class GeneratorItem {

    private String name;

    private String description;

    private final List<Option> options = new ArrayList<>();

    public GeneratorItem(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void addOption(Option option) {
        options.add(option);
    }

    public List<Option> getOptions() {
        return options;
    }
}
