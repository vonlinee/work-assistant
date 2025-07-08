package org.workassistant.ui.bridge;

import java.util.List;

/**
 * @see TargetFile
 */
public interface FileGenerator {

    void initialize(GenerationTarget target);

    List<GeneratedFile> getGeneratedFiles();
}
