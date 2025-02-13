package org.example.workassistant.ui.tools.maven;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class AnalyzePomDependencies {

    public static List<LocalJarDependency> parseDependencies(File projectDir) {
        File pomFile = null;
        for (File file : projectDir.listFiles()) {
            if (file.getName().endsWith("pom.xml")) {
                pomFile = file;
                break;
            }
        }

        List<LocalJarDependency> dependencies = new ArrayList<>();
        try {
            // 创建 XML 解析器
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(pomFile);
            // 获取所有依赖节点
            NodeList dependencyNodes = document.getElementsByTagName("dependency");
            // 遍历依赖节点并创建 Dependency 对象
            for (int i = 0; i < dependencyNodes.getLength(); i++) {
                Element dependencyElement = (Element) dependencyNodes.item(i);

                String groupId = dependencyElement.getElementsByTagName("groupId").item(0).getTextContent();
                String artifactId = dependencyElement.getElementsByTagName("artifactId").item(0).getTextContent();
                String version = dependencyElement.getElementsByTagName("version").item(0).getTextContent();
                LocalJarDependency localJarDependency = new LocalJarDependency();
                localJarDependency.setGroupId(groupId);
                localJarDependency.setArtifactId(artifactId);
                localJarDependency.setVersion(version);
                dependencies.add(localJarDependency);
            }

            NodeList properties = document.getElementsByTagName("properties");
            Properties propertiesValues = new Properties();
            for (int i = 0; i < properties.getLength(); i++) {
                Element propertyElement = (Element) properties.item(i);

                NodeList childNodes = propertyElement.getChildNodes();

                for (int j = 0; j < childNodes.getLength(); j++) {
                    Node item = childNodes.item(j);
                    String tagName = item.getNodeName();
                    String textContent = item.getTextContent();
                    propertiesValues.setProperty(tagName, textContent);
                }
            }

            for (LocalJarDependency localJarDependency : dependencies) {
                if (localJarDependency.getVersion().contains("$")) {
                    String version = localJarDependency.getVersion();

                    version = version.replace("${", "").replace("}", "");

                    version = propertiesValues.getProperty(version);

                    localJarDependency.setVersion(version);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dependencies;
    }
}