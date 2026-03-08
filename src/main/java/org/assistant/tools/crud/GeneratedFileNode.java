package org.assistant.tools.crud;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Tree node that holds a generated file's relative path and content.
 * Leaf nodes hold actual file content; directory nodes hold only a path.
 */
public class GeneratedFileNode extends DefaultMutableTreeNode {

    private final String filePath; // relative path, e.g. "com/example/entity/User.java"
    private final String content; // null for directory/folder nodes
    private final boolean isDirectory;

    /** Creates a leaf node (file). */
    public GeneratedFileNode(String filePath, String content) {
        super(fileName(filePath));
        this.filePath = filePath;
        this.content = content;
        this.isDirectory = false;
    }

    /** Creates an intermediate directory node. */
    public GeneratedFileNode(String dirName) {
        super(dirName);
        this.filePath = dirName;
        this.content = null;
        this.isDirectory = true;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String fileName(String path) {
        int idx = path.lastIndexOf('/');
        return idx >= 0 ? path.substring(idx + 1) : path;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String getFilePath() {
        return filePath;
    }

    public String getContent() {
        return content;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public boolean isFile() {
        return !isDirectory;
    }

    /** Detects Java vs XML vs SQL for syntax-highlighting in the viewer. */
    public String getSyntaxStyle() {
        if (filePath == null)
            return "text/plain";
        if (filePath.endsWith(".java"))
            return "text/java";
        if (filePath.endsWith(".xml"))
            return "text/xml";
        if (filePath.endsWith(".sql"))
            return "text/sql";
        return "text/plain";
    }
}
