package org.example.workassistant.fxui.tools.maven;

import lombok.Data;

import java.nio.file.Path;
import java.nio.file.Paths;

@Data
public class LocalJarDependency {
    private String groupId;
    private String artifactId;
    private String version;

    private String jarLocation;

    /**
     * 是否模块化，包含module-info.java文件
     */
    private Boolean modular;

    @Override
    public String toString() {
        return groupId + ':' + artifactId + ':' + version;
    }

    public LocalJarDependency() {
    }

    public LocalJarDependency(String identifier) {
        String[] split = identifier.split(":");
        if (split.length == 2) {
            groupId = split[0];
            artifactId = split[1];
        } else if (split.length == 3) {
            groupId = split[0];
            artifactId = split[1];
            version = split[2];
        }
    }

    public String getJarLocation(String repository) {

        Path path = Paths.get(repository, groupId, artifactId, version);

        return path.toAbsolutePath().toString();
    }
}