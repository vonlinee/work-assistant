package org.example.workassistant.common.utils;

import org.example.workassistant.utils.lang.RuntimeIOException;
import org.example.workassistant.utils.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class MavenProjectAnalyser implements ProjectAnalyser {

    public static Set<String> extractPackageNames(Path sourceRootPath) throws IOException {
        Set<String> packageNames = new HashSet<>();
        try (Stream<Path> stream = Files.walk(sourceRootPath)) {
            stream.filter(path -> Files.isDirectory(path) && !path.equals(sourceRootPath))
                .forEach(path -> {
                    String packageName = extractPackageName(path, sourceRootPath);
                    if (packageName != null && !packageName.isEmpty()) {
                        packageNames.add(packageName);
                    }
                });
        }
        return packageNames;
    }

    private static String extractPackageName(Path dir, Path sourceRootPath) {
        String fileName = dir.getFileName().toString();
        int lastIndex = fileName.lastIndexOf('.');
        if (lastIndex == -1) {
            return null; // Invalid Java file name (no extension)
        }
        String className = fileName.substring(0, lastIndex);
        String relativePath = dir.getParent().relativize(sourceRootPath).toString();
        if (relativePath.isEmpty()) {
            return ""; // Top-level package (no subdirectories)
        }
        relativePath = relativePath.replace(File.separatorChar, '.');
        return relativePath + "." + className.substring(0, className.lastIndexOf('$')); // Exclude inner classes
    }

    @Override
    public boolean isProjectRootDirectory(File entryFile) {
        try (Stream<Path> stream = Files.list(entryFile.toPath())) {
            return stream.anyMatch(path -> path.toString().endsWith("pom.xml"));
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public Path getSourceRoot(Path projectRoot) {
        return projectRoot.resolve(Path.of("src/main/java"));
    }

    @Override
    public Path getResourceRoot(Path projectRoot) {
        return projectRoot.resolve(Path.of("src/main/resources"));
    }

    @Override
    public Set<String> getPackageNames(File projectRoot) {
        Path sourceRootPath = getSourceRoot(projectRoot.toPath());
        if (!Files.exists(sourceRootPath)) {
            throw new RuntimeIOException("路径不存在", null);
        }
        try {
            return extractPackageNames(sourceRootPath);
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }
    }

    /**
     * 解析本地项目模块信息
     *
     * @param entryFile 入口文件 例如pom.xml，build.grade等
     * @return 模块信息
     */
    @Override
    public ProjectModule analyse(File entryFile) {
        try {
            //通过DocumentBuilderFactory工厂
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            //通过DocumentBuilderFactory工厂创建DocumentBuilder对象
            DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
            //使用DocumentBuilder的parse方法，从文件中解析出Document（文档）对象
            Document document = documentBuilder.parse(entryFile);
            //通过Document的getElementsByTagName方法，获取相应的NodeList节点集
            // 获取文档元素，及根节点
            Element rootEl = document.getDocumentElement();

            ProjectModule rootModule = new ProjectModule(entryFile.getName());
            rootModule.setRootPath(entryFile.getParentFile().getAbsolutePath());
            NodeList childNodes = rootEl.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node item = childNodes.item(i);
                switch (item.getNodeName()) {
                    case "groupId" -> {
                        item.normalize();
                        rootModule.setGroupId(item.getTextContent());
                    }
                    case "artifactId" -> {
                        item.normalize();
                        rootModule.setArtifactId(item.getTextContent());
                        rootModule.setName(item.getTextContent());
                    }
                    case "version" -> {
                        item.normalize();
                        rootModule.setVersion(item.getTextContent());
                    }
                }
            }

            NodeList packagingElement = rootEl.getElementsByTagName("packaging");
            if (packagingElement.getLength() == 0) {
                return rootModule;
            }
            Node packagingNode = packagingElement.item(0);
            if (packagingNode != null) {
                rootModule.setPackageWay(StringUtils.trim(packagingNode.getTextContent()));
            }

            // 子模块
            NodeList modules = rootEl.getElementsByTagName("modules");
            if (modules.getLength() == 0) {
                return rootModule;
            }
            Node modulesNode = modules.item(0);
            modules = modulesNode.getChildNodes();
            int l = modules.getLength();
            for (int i = 0; i < l; i++) {
                Node item = modules.item(i);
                if ("module".equals(item.getNodeName())) {
                    rootModule.addModule(StringUtils.trim(item.getTextContent()));
                }
            }

            if (rootModule.hasModules()) {
                // 解析子模块
                for (ProjectModule module : rootModule.getModules()) {
                    String moduleName = module.getName();

                    ProjectModule childModule = analyse(new File(entryFile.getParentFile(), moduleName + File.separator + "pom.xml"));
                    module.merge(childModule);
                }
            }

            return rootModule;
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }
    }
}
