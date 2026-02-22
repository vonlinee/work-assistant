package org.assistant.tools.doc.parser;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Detects the project type and extracts metadata from Maven or Gradle build
 * files.
 */
public class ProjectParser {

    private static final Logger log = LoggerFactory.getLogger(ProjectParser.class);

    /**
     * Parse a project at the given directory.
     *
     * @param projectDir root directory of the project
     * @return project info, never null
     */
    public ProjectInfo parse(Path projectDir) {
        ProjectInfo info = new ProjectInfo();
        info.setProjectRoot(projectDir);

        Path pomXml = projectDir.resolve("pom.xml");
        Path buildGradle = projectDir.resolve("build.gradle");
        Path buildGradleKts = projectDir.resolve("build.gradle.kts");

        if (Files.exists(pomXml)) {
            info.setBuildTool(ProjectInfo.BuildTool.MAVEN);
            parseMaven(pomXml, info);
        } else if (Files.exists(buildGradle)) {
            info.setBuildTool(ProjectInfo.BuildTool.GRADLE);
            parseGradle(buildGradle, info);
        } else if (Files.exists(buildGradleKts)) {
            info.setBuildTool(ProjectInfo.BuildTool.GRADLE);
            parseGradle(buildGradleKts, info);
        }

        // Detect source roots
        detectSourceRoots(projectDir, info);

        return info;
    }

    private void parseMaven(Path pomXml, ProjectInfo info) {
        try {
            SAXReader reader = new SAXReader();
            Document doc = reader.read(pomXml.toFile());
            Element root = doc.getRootElement();

            String name = getElementText(root, "name");
            String artifactId = getElementText(root, "artifactId");
            info.setProjectName(name != null ? name : artifactId);
            info.setVersion(getElementText(root, "version"));
            info.setDescription(getElementText(root, "description"));
        } catch (Exception e) {
            log.warn("Failed to parse pom.xml: {}", e.getMessage());
        }
    }

    private String getElementText(Element parent, String childName) {
        Element child = parent.element(childName);
        return child != null ? child.getTextTrim() : null;
    }

    private void parseGradle(Path buildFile, ProjectInfo info) {
        try {
            String content = Files.readString(buildFile);

            // Extract group/version from simple patterns
            Pattern versionPattern = Pattern.compile("version\\s*[=:]\\s*['\"]([^'\"]+)['\"]");
            Matcher versionMatcher = versionPattern.matcher(content);
            if (versionMatcher.find()) {
                info.setVersion(versionMatcher.group(1));
            }

            Pattern groupPattern = Pattern.compile("group\\s*[=:]\\s*['\"]([^'\"]+)['\"]");
            Matcher groupMatcher = groupPattern.matcher(content);
            if (groupMatcher.find()) {
                info.setProjectName(groupMatcher.group(1));
            }

            // If no name from group, use directory name
            if (info.getProjectName() == null) {
                info.setProjectName(buildFile.getParent().getFileName().toString());
            }
        } catch (Exception e) {
            log.warn("Failed to parse build.gradle: {}", e.getMessage());
        }
    }

    private void detectSourceRoots(Path projectDir, ProjectInfo info) {
        // Standard Maven/Gradle source roots
        String[] candidates = {
                "src/main/java",
                "src/main/kotlin",
                "src/main/groovy"
        };
        for (String candidate : candidates) {
            Path sourceRoot = projectDir.resolve(candidate);
            if (Files.isDirectory(sourceRoot)) {
                info.addSourceRoot(sourceRoot);
            }
        }

        // Multi-module: check immediate subdirectories for source roots
        File[] children = projectDir.toFile().listFiles(File::isDirectory);
        if (children != null) {
            for (File child : children) {
                if (child.getName().startsWith(".") || child.getName().equals("target")
                        || child.getName().equals("build")) {
                    continue;
                }
                for (String candidate : candidates) {
                    Path sourceRoot = child.toPath().resolve(candidate);
                    if (Files.isDirectory(sourceRoot)) {
                        info.addSourceRoot(sourceRoot);
                    }
                }
            }
        }
    }
}
