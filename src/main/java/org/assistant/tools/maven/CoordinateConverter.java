package org.assistant.tools.maven;

/**
 * Converts dependency coordinates between Maven and Gradle formats.
 *
 * Supported input/output formats:
 * - Maven XML dependency snippet
 * - Maven short coordinate (groupId:artifactId:version)
 * - Gradle Groovy DSL (implementation 'g:a:v')
 * - Gradle Kotlin DSL (implementation("g:a:v"))
 */
public class CoordinateConverter {

    /**
     * Auto-detects the input format and parses it into a MavenArtifact.
     * Returns null if the input is not recognisable.
     */
    public MavenArtifact parse(String input) {
        if (input == null || input.isBlank())
            return null;
        String trimmed = input.strip();

        // Maven XML snippet
        if (trimmed.contains("<dependency>") || trimmed.contains("<groupId>")) {
            return parseMavenXml(trimmed);
        }

        // Gradle DSL – implementation("g:a:v") or implementation 'g:a:v'
        if (trimmed.contains("(") || trimmed.contains("'")) {
            String coord = extractGradleCoord(trimmed);
            if (coord != null)
                return MavenArtifact.fromMavenCoordinate(coord);
        }

        // Plain coordinate g:a:v
        if (trimmed.contains(":")) {
            return MavenArtifact.fromMavenCoordinate(trimmed);
        }

        return null;
    }

    // ---- Output formatters ----

    public String toMavenXml(MavenArtifact a) {
        return a.toMavenXml();
    }

    public String toMavenCoordinate(MavenArtifact a) {
        return a.toMavenCoordinate();
    }

    public String toGradleGroovy(MavenArtifact a) {
        return a.toGradleGroovy();
    }

    public String toGradleKotlin(MavenArtifact a) {
        return a.toGradleKotlin();
    }

    /** Returns a full conversion report for the given artifact. */
    public String fullReport(MavenArtifact a) {
        return "── Maven XML ──────────────────────────────\n"
                + a.toMavenXml() + "\n\n"
                + "── Maven short ────────────────────────────\n"
                + a.toMavenCoordinate() + "\n\n"
                + "── Gradle Groovy DSL ──────────────────────\n"
                + a.toGradleGroovy() + "\n\n"
                + "── Gradle Kotlin DSL ──────────────────────\n"
                + a.toGradleKotlin() + "\n";
    }

    // ---- Private helpers ----

    private MavenArtifact parseMavenXml(String xml) {
        MavenArtifact a = new MavenArtifact();
        a.setGroupId(extractXmlTag(xml, "groupId"));
        a.setArtifactId(extractXmlTag(xml, "artifactId"));
        a.setVersion(extractXmlTag(xml, "version"));
        String packaging = extractXmlTag(xml, "type");
        a.setPackaging(packaging != null ? packaging : "jar");
        if (a.getGroupId() == null && a.getArtifactId() == null)
            return null;
        return a;
    }

    /** Extracts the text content of a simple XML element like <tag>value</tag>. */
    private String extractXmlTag(String xml, String tag) {
        String open = "<" + tag + ">";
        String close = "</" + tag + ">";
        int start = xml.indexOf(open);
        int end = xml.indexOf(close);
        if (start < 0 || end < 0 || end <= start)
            return null;
        return xml.substring(start + open.length(), end).trim();
    }

    /**
     * Extracts the g:a:v coordinate from a Gradle dependency line.
     * Handles both:
     * implementation "g:a:v"
     * implementation("g:a:v")
     * testImplementation 'g:a:v'
     */
    private String extractGradleCoord(String line) {
        // Find the quoted segment
        int q1 = -1;
        char quoteChar = 0;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"' || c == '\'') {
                q1 = i;
                quoteChar = c;
                break;
            }
        }
        if (q1 < 0)
            return null;
        int q2 = line.indexOf(quoteChar, q1 + 1);
        if (q2 < 0)
            return null;
        String quoted = line.substring(q1 + 1, q2).trim();
        return quoted.isEmpty() ? null : quoted;
    }
}
