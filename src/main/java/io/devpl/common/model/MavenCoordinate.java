package io.devpl.common.model;

import lombok.Data;

@Data
public class MavenCoordinate {

    private String groupId;

    private String artifactId;

    private String version;

    private String packaging;

    private String scope;
}
