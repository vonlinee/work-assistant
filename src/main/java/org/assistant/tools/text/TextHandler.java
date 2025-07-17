package org.assistant.tools.text;

import java.util.List;

public interface TextHandler {

    String getLabel();

    String handle(String input);

    default String splitHandle(String input) {
        String[] split = input.split(getLineSeparator());
        for (int i = 0; i < split.length; i++) {
            split[i] = handle(split[i]);
        }
        return String.join(getLineSeparator(), split);
    }

    default List<String> handleBatch(List<String> inputs) {
        inputs.replaceAll(this::handle);
        return inputs;
    }

    default String getLineSeparator() {
        return System.lineSeparator();
    }
}
