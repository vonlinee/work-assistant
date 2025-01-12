package io.devpl.fxui.app;

import io.devpl.common.model.MavenCoordinate;
import io.devpl.fxui.utils.FileUtils;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Maven工具
 */
public class MavenTool extends BorderPane {

    TextArea textArea = new TextArea();

    private static final String MAVEN_HOME = System.getenv("MAVEN_HOME");

    /**
     * Maven配置文件settings.xml中配置的本地仓库地址
     */
    private static String localRepository;

    static {
        Path settingXml = Path.of(MAVEN_HOME, "conf", "settings.xml");
        try (InputStream is = Files.newInputStream(settingXml)) {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(is);
            doc.getDocumentElement().normalize();
            Node localRepositoryNode = doc.getElementsByTagName("localRepository").item(0);
            localRepository = localRepositoryNode.getTextContent();
        } catch (IOException | SAXException | ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public MavenTool() {
        setCenter(textArea);

        textArea.setText("""
                        <dependency>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                            <version>1.18.28</version>
                            <scope>compile</scope>
                        </dependency>
            """);

        HBox bottom = new HBox();

        Button button1 = new Button("Maven转Gradle坐标");
        button1.setOnAction(event -> {

        });

        Button button2 = new Button("Gradle转Maven坐标");
        button2.setOnAction(event -> {

        });

        Button button3 = new Button("去重");
        button3.setOnAction(event -> {
        });

        Button button4 = new Button("打开本地仓库");
        button4.setOnAction(event -> {
            String text = textArea.getText();
            if (text != null && !text.isBlank()) {
                List<MavenCoordinate> coordinates = parse(text);
                if (coordinates.size() == 1) {
                    MavenCoordinate coordinate = coordinates.get(0);
                    if (coordinate.getVersion() == null) {
                        coordinate.setVersion("");
                    }
                    Path path = Paths.get(localRepository,
                        coordinate.getGroupId().replace(".", "/"),
                        coordinate.getArtifactId(),
                        coordinate.getVersion());
                    if (Files.exists(path)) {
                        FileUtils.open(path.toFile());
                    } else {
                        System.out.println(path + " 不存在");
                    }
                }
            }
        });

        bottom.getChildren().addAll(button1, button2, button3, button4);

        setBottom(bottom);
    }

    public static List<MavenCoordinate> parse(String xml) {
        List<MavenCoordinate> coordinates = new ArrayList<>();
        try (ByteArrayInputStream is = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))) {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(is);
            doc.getDocumentElement().normalize();

            Element documentElement = doc.getDocumentElement();
            String nodeName = documentElement.getNodeName();
            if ("dependencies".equals(nodeName)) {
                NodeList dependencyNodes = documentElement.getElementsByTagName("dependency");
                for (int i = 0; i < dependencyNodes.getLength(); i++) {
                    Node item = dependencyNodes.item(i);
                    MavenCoordinate mavenCoordinate = parse(item);
                    coordinates.add(mavenCoordinate);
                }
            } else if ("dependency".equals(nodeName)) {
                coordinates.add(parse(documentElement));
            }
        } catch (Exception exception) {

        }
        return coordinates;
    }

    /**
     * @param node dependency节点
     * @return MavenCoordinate 坐标信息
     */
    static MavenCoordinate parse(Node node) {
        NodeList childNodes = node.getChildNodes();
        MavenCoordinate coordinate = new MavenCoordinate();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if (item.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            if ("dependency".equals(node.getNodeName())) {
                NodeList childNodes1 = node.getChildNodes();
                for (int i1 = 0; i1 < childNodes1.getLength(); i1++) {
                    Node item1 = childNodes1.item(i1);
                    if (item1.getNodeType() != Node.ELEMENT_NODE) {
                        continue;
                    }
                    switch (item1.getNodeName()) {
                        case "groupId" -> coordinate.setGroupId(item1.getTextContent());
                        case "artifactId" -> coordinate.setArtifactId(item1.getTextContent());
                        case "version" -> coordinate.setVersion(item1.getTextContent());
                        case "scope" -> coordinate.setScope(item1.getTextContent());
                    }
                }
            }

        }
        return coordinate;
    }
}
