package org.assistant.tools.doc.parser;

import lombok.Data;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Metadata about a parsed project.
 */
@Data
public class ProjectInfo {

    public enum BuildTool {
        MAVEN, GRADLE, UNKNOWN
    }

    private String projectName;
    private String version;
    private String description;
    private BuildTool buildTool = BuildTool.UNKNOWN;
    private Path projectRoot;
    private List<Path> sourceRoots = new ArrayList<>();
    private String contextPath;

    public void addSourceRoot(Path sourceRoot) {
        this.sourceRoots.add(sourceRoot);
    }
}
