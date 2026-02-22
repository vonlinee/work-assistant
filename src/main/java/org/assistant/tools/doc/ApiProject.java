package org.assistant.tools.doc;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Top-level container representing all APIs discovered in a project.
 */
@Data
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
