package org.assistant.tools.maven;

/**
 * Represents a Maven artifact found in the local repository.
 */
public class MavenArtifact {

    private String groupId;
    private String artifactId;
    private String version;
    private String packaging; // jar, pom, war, etc.
    private String jarPath; // absolute path to the jar file (may be null for pom-only)
    private long sizeBytes;

    public MavenArtifact() {
    }

    public MavenArtifact(String groupId, String artifactId, String version, String packaging) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.packaging = packaging;
    }

    // ---- Coordinate helpers ----

    /** Returns canonical Maven coordinate: groupId:artifactId:version */
    public String toMavenCoordinate() {
        return groupId + ":" + artifactId + ":" + version;
    }

    /** Returns Maven XML dependency snippet */
    public String toMavenXml() {
        return "<dependency>\n" +
                "    <groupId>" + groupId + "</groupId>\n" +
                "    <artifactId>" + artifactId + "</artifactId>\n" +
                "    <version>" + version + "</version>\n" +
                "</dependency>";
    }

    /** Returns Gradle (Groovy DSL) dependency string */
    public String toGradleGroovy() {
        return "implementation '" + groupId + ":" + artifactId + ":" + version + "'";
    }

    /** Returns Gradle (Kotlin DSL) dependency string */
    public String toGradleKotlin() {
        return "implementation(\"" + groupId + ":" + artifactId + ":" + version + "\")";
    }

    /** Returns Gradle short notation */
    public String toGradleShort() {
        return groupId + ":" + artifactId + ":" + version;
    }

    // ---- Parsing ----

    /**
     * Parses a Maven coordinate string (groupId:artifactId:version or
     * groupId:artifactId).
     */
    public static MavenArtifact fromMavenCoordinate(String coord) {
        if (coord == null)
            return null;
        String[] parts = coord.trim().split(":");
        MavenArtifact a = new MavenArtifact();
        if (parts.length >= 2) {
            a.setGroupId(parts[0].trim());
            a.setArtifactId(parts[1].trim());
        }
        if (parts.length >= 3) {
            a.setVersion(parts[2].trim());
        }
        return a;
    }

    // ---- Getters & setters ----

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String a) {
        this.artifactId = a;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPackaging() {
        return packaging;
    }

    public void setPackaging(String packaging) {
        this.packaging = packaging;
    }

    public String getJarPath() {
        return jarPath;
    }

    public void setJarPath(String jarPath) {
        this.jarPath = jarPath;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    @Override
    public String toString() {
        return toMavenCoordinate();
    }
}
