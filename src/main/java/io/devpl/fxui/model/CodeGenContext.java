package io.devpl.fxui.model;

import io.devpl.fxui.bridge.ProjectConfiguration;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * 代码生成参数
 */
@Getter
@Setter
public class CodeGenContext {

    private ProjectConfiguration projectConfiguration;

    private Map<String, TableGeneration> targetedTables;
}
