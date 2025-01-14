package org.example.workassistant.fxui.view;

import org.example.workassistant.common.utils.FileComparator;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.girod.javafx.svgimage.SVGLoader;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Consumer;

/**
 * 文件树结构
 */
public class FileTreeView extends TreeView<FileNode> {

    private File rootDirectory;
    private Comparator<File> comparator;

    public File getRootDirectory() {
        return rootDirectory;
    }

    private Consumer<FileNode> onNodeClickHandler;

    public void setRootDirectory(File rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

    public FileTreeView(File root) {
        setCellFactory(param -> new FileNodeTreeCell());
        setShowRoot(true);
        this.comparator = new FileComparator();
        updateRoot(this.rootDirectory = root);
    }

    public Consumer<FileNode> getOnNodeClickHandler() {
        return onNodeClickHandler;
    }

    public void setOnNodeClickHandler(Consumer<FileNode> onNodeClickHandler) {
        this.onNodeClickHandler = onNodeClickHandler;
    }

    public void updateRoot(File rootDirectory) {
        if (rootDirectory == null) {
            return;
        }
        TreeItem<FileNode> root = getRoot();
        if (root != null) {
            root.setValue(newTreeNode(rootDirectory));
        } else {
            root = newTreeItem(rootDirectory);
            this.setRoot(root);
        }
    }

    private TreeItem<FileNode> newTreeItem(File file) {
        FileNode node = newTreeNode(file);
        TreeItem<FileNode> item = new TreeItem<>(node);
        String iconName = getFileTypeIcon(node);
        item.setGraphic(SVGLoader.load(getClass().getClassLoader().getResource(iconName)));
        return item;
    }

    private String getFileTypeIcon(FileNode node) {
        if (node.isLeaf()) {
            final String extension = node.getExtension();
            return switch (extension) {
                case "java" -> "icon/fileTypes/java.svg";
                case "jar" -> "icon/nodes/ppJar.svg";
                case "html" -> "icon/fileTypes/html.svg";
                case "json" -> "icon/fileTypes/json.svg";
                case "txt" -> "icon/fileTypes/text.svg";
                case "xml" -> "icon/fileTypes/xml.svg";
                case "gz" -> "icon/fileTypes/archive.svg";
                case "yaml" -> "icon/fileTypes/yaml.svg";
                case "sql" -> "icon/svg/sql.svg";
                case "bat" -> "icon/svg/bat.svg";
                case "zip" -> "icon/svg/zip.svg";
                case "vue" -> "icon/svg/vue.svg";
                case "ts" -> "icon/svg/ts.svg";
                case "css" -> "icon/svg/css.svg";
                case "js" -> "icon/svg/js.svg";
                default -> "icon/fileTypes/any_type.svg";
            };
        } else {
            return "icon/svg/folder.svg";
        }
    }

    private FileNode newTreeNode(File file) {
        FileNode fileNode = new FileNode();
        fileNode.setLeaf(!file.isDirectory());
        fileNode.setSelectable(true);
        fileNode.setPath(file.getAbsolutePath());
        fileNode.setName(file.getName());
        fileNode.setAbsolutePath(file.getAbsolutePath());
        return fileNode;
    }

    static class FileNodeTreeCell extends TreeCell<FileNode> {

        public FileNodeTreeCell() {
            setOnMouseClicked(event -> {
                TreeItem<FileNode> treeItem = getTreeItem();
                FileTreeView treeView = (FileTreeView) getTreeView();
                if (treeItem != null) {
                    FileNode fileNode = treeItem.getValue();
                    if (fileNode != null && !fileNode.isLeaf() && treeItem.isLeaf() && treeItem.getChildren().size() == 0 && fileNode.getAbsolutePath() != null && !fileNode.getAbsolutePath().isEmpty()) {
                        // 加载子节点
                        File file = new File(fileNode.getAbsolutePath());
                        File[] files = file.listFiles();
                        if (files != null && files.length > 0) {
                            Arrays.sort(files, treeView.getComparator());
                            ObservableList<TreeItem<FileNode>> children = treeItem.getChildren();
                            for (File value : files) {
                                children.add(treeView.newTreeItem(value));
                            }
                        }
                        treeItem.setExpanded(true);
                    }
                    if (treeView.onNodeClickHandler != null) {
                        treeView.onNodeClickHandler.accept(treeItem.getValue());
                    }
                }
            });
        }

        @Override
        protected void updateItem(FileNode item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                setText(item.getName());
                setGraphic(getTreeItem().getGraphic());
            }
        }
    }

    public final void setComparator(Comparator<File> comparator) {
        this.comparator = comparator;
    }

    public final Comparator<File> getComparator() {
        return comparator;
    }
}
