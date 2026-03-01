package org.assistant.tools.doc.parser;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Metadata about a parsed project.
 */
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

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BuildTool getBuildTool() {
        return buildTool;
    }

    public void setBuildTool(BuildTool buildTool) {
        this.buildTool = buildTool;
    }

    public Path getProjectRoot() {
        return projectRoot;
    }

    public void setProjectRoot(Path projectRoot) {
        this.projectRoot = projectRoot;
    }

    public List<Path> getSourceRoots() {
        return sourceRoots;
    }

    public void setSourceRoots(List<Path> sourceRoots) {
        this.sourceRoots = sourceRoots;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public void addSourceRoot(Path sourceRoot) {
        this.sourceRoots.add(sourceRoot);
    }
}
