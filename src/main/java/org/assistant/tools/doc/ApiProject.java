package org.assistant.tools.doc;

import java.util.ArrayList;
import java.util.List;

/**
 * Top-level container representing all APIs discovered in a project.
 */

public class ApiProject {

    /** Project name (from pom.xml or build.gradle) */
    private String projectName;

    /** Project version */
    private String version;

    /** Project description */
    private String description;

    /** Global base path (e.g. servlet context-path) */
    private String basePath;

    /** API groups, typically one per controller class */
    private List<ApiGroup> groups = new ArrayList<>();

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

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public List<ApiGroup> getGroups() {
        return groups;
    }

    public void setGroups(List<ApiGroup> groups) {
        this.groups = groups;
    }

    public void addGroup(ApiGroup group) {
        this.groups.add(group);
    }

    /** Flatten all endpoints across all groups */
    public List<WebApiInfo> getAllApis() {
        List<WebApiInfo> all = new ArrayList<>();
        for (ApiGroup group : groups) {
            all.addAll(group.getApis());
        }
        return all;
    }
}
