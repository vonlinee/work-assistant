package org.example.workassistant.ui.tools.maven;

import lombok.Data;

import java.io.File;

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

    /**
     * 本地jar包地址
     *
     * @param repository 本地仓库根目录
     * @return jar包地址
     */
    public String getJarLocation(String repository) {
        // 将 groupId 转换为路径
        String groupPath = groupId.replace('.', File.separatorChar);
        return repository + File.separator + groupPath + File.separator + artifactId + File.separator + version
                + File.separator + artifactId + "-" + version + ".jar";
    }
}