package org.assistant.tools.maven;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Scans the local Maven repository (~/.m2/repository by default) and builds
 * an in-memory index of available artifacts.
 *
 * The repo directory structure is:
 * {groupId-as-path}/{artifactId}/{version}/{artifactId}-{version}.{packaging}
 */
public class LocalRepoScanner {

    private static final Logger log = LoggerFactory.getLogger(LocalRepoScanner.class);

    /**
     * Returns the default local repository path by resolving ~/.m2/repository,
     * or reading the MAVEN_LOCAL_REPO environment variable if set.
     */
    public static String defaultRepoPath() {
        String env = System.getenv("MAVEN_LOCAL_REPO");
        if (env != null && !env.isBlank())
            return env;
        return System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository";
    }

    /**
     * Scans the repository at the given path and returns all found artifacts.
     * The scan walks the directory tree: group/artifact/version/*.jar
     *
     * @param repoPath   absolute path to the local repo root
     * @param onProgress callback invoked periodically with a progress message (may
     *                   be null)
     */
    public List<MavenArtifact> scan(String repoPath, Consumer<String> onProgress) {
        File repoRoot = new File(repoPath);
        if (!repoRoot.exists() || !repoRoot.isDirectory()) {
            log.warn("Local repository not found at: {}", repoPath);
            return Collections.emptyList();
        }

        List<MavenArtifact> results = new ArrayList<>();
        scanGroup(repoRoot, repoRoot, results, onProgress);
        log.info("Scanned {} artifacts from {}", results.size(), repoPath);
        return results;
    }

    /**
     * Searches the provided list with a case-insensitive keyword filter across
     * groupId, artifactId and version.
     */
    public List<MavenArtifact> search(List<MavenArtifact> all, String keyword) {
        if (keyword == null || keyword.isBlank())
            return all;
        String kw = keyword.toLowerCase().trim();
        List<MavenArtifact> results = new ArrayList<>();
        for (MavenArtifact a : all) {
            if (matches(a, kw))
                results.add(a);
        }
        return results;
    }

    private boolean matches(MavenArtifact a, String kw) {
        return contains(a.getGroupId(), kw)
                || contains(a.getArtifactId(), kw)
                || contains(a.getVersion(), kw)
                || contains(a.toMavenCoordinate(), kw);
    }

    private boolean contains(String s, String kw) {
        return s != null && s.toLowerCase().contains(kw);
    }

    // ---- Private scanning logic ----

    /**
     * Recursively traverses the file system.
     * Depth-first: group → artifactId → version → files.
     */
    private void scanGroup(File repoRoot, File dir,
            List<MavenArtifact> results, Consumer<String> onProgress) {
        File[] children = dir.listFiles();
        if (children == null)
            return;

        for (File child : children) {
            if (!child.isDirectory())
                continue;
            // Check if this directory looks like an artifact version directory
            // (contains a .pom file matching the directory name convention)
            if (isVersionDir(child)) {
                MavenArtifact artifact = buildArtifact(repoRoot, child);
                if (artifact != null) {
                    results.add(artifact);
                    if (onProgress != null && results.size() % 500 == 0) {
                        onProgress.accept("Indexed " + results.size() + " artifacts...");
                    }
                }
            } else {
                scanGroup(repoRoot, child, results, onProgress);
            }
        }
    }

    /**
     * Returns true if the given directory is a version directory —
     * i.e. it contains a file "{artifactId}-{version}.pom" or ".jar".
     */
    private boolean isVersionDir(File dir) {
        File[] files = dir.listFiles();
        if (files == null)
            return false;
        String dirName = dir.getName();
        for (File f : files) {
            String name = f.getName();
            if (f.isFile() && (name.endsWith(".pom") || name.endsWith(".jar"))) {
                if (name.contains(dirName))
                    return true;
            }
        }
        return false;
    }

    /**
     * Builds a MavenArtifact from the version directory node.
     * Layout: repoRoot/{group-path}/{artifactId}/{version}/
     */
    private MavenArtifact buildArtifact(File repoRoot, File versionDir) {
        try {
            String version = versionDir.getName();
            File artifactDir = versionDir.getParentFile();
            String artifactId = artifactDir.getName();

            // groupId is the relative path from repoRoot to artifactDir's parent,
            // dots-separated
            File groupDir = artifactDir.getParentFile();
            String relPath = repoRoot.toURI().relativize(groupDir.toURI()).getPath();
            if (relPath.endsWith("/"))
                relPath = relPath.substring(0, relPath.length() - 1);
            String groupId = relPath.replace('/', '.');

            MavenArtifact artifact = new MavenArtifact(groupId, artifactId, version, "jar");

            // Find the main JAR (not -sources, -javadoc, -tests)
            File[] files = versionDir.listFiles();
            if (files != null) {
                for (File f : files) {
                    String name = f.getName();
                    if (name.endsWith(".jar")
                            && !name.endsWith("-sources.jar")
                            && !name.endsWith("-javadoc.jar")
                            && !name.endsWith("-tests.jar")) {
                        artifact.setJarPath(f.getAbsolutePath());
                        artifact.setSizeBytes(f.length());
                        break;
                    }
                }
                // Check if pom-only (no jar)
                if (artifact.getJarPath() == null) {
                    for (File f : files) {
                        if (f.getName().endsWith(".pom")) {
                            artifact.setPackaging("pom");
                            break;
                        }
                    }
                }
            }
            return artifact;
        } catch (Exception e) {
            log.debug("Failed to parse artifact from {}: {}", versionDir, e.getMessage());
            return null;
        }
    }
}
