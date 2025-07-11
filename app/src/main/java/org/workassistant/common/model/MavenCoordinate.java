package org.workassistant.common.model;

import lombok.Data;

/**
 * Maven 坐标
 */
@Data
public class MavenCoordinate {

    private String groupId;

    private String artifactId;

    private String version;

    private String packaging;

    private String scope;
}
