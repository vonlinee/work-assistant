package io.devpl.fxui.controller;

import io.devpl.sdk.util.StringUtils;
import io.devpl.fxui.tools.text.TextHandleMap;
import io.devpl.fxui.fxtras.mvvm.FxmlView;
import io.devpl.common.utils.XMLUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * 文本处理工具
 */
public class TextHandleView extends FxmlView {

    @FXML
    public BorderPane bopRoot;
    @FXML
    public SplitPane sppTxtArea;
    @FXML
    public Group grpLeft;
    @FXML
    public Group grpRight;
    @FXML
    public TextArea ttaLeft;
    @FXML
    public TextArea ttaRight;
    @FXML
    public FlowPane flpBottomBtnGroup;
    @FXML
    public Button btnMavenToGradle;
    @FXML
    public Button btnGradleToMaven;

    private final TextHandleMap handleRuleMap = new TextHandleMap();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sppTxtArea.prefWidthProperty()
                .bind(bopRoot.widthProperty());
        sppTxtArea.setDividerPositions(0.5);
        ttaLeft.prefWidthProperty()
                .bind(bopRoot.widthProperty()
                        .divide(2));
        ttaRight.prefWidthProperty()
                .bind(bopRoot.widthProperty()
                        .divide(2));
        batchAddBtnTriggerEventHandler(flpBottomBtnGroup, event -> handleRuleMap.handle(event.getSource(), ttaLeft, ttaRight));
        registerTextHandler();
    }

    /**
     * 递归给Pane的所有按钮添加一个相同的ActionEvent事件处理
     * @param pane    容器
     * @param handler 事件处理
     */
    private void batchAddBtnTriggerEventHandler(Pane pane, EventHandler<ActionEvent> handler) {
        for (Node child : pane.getChildren()) {
            if (child instanceof Pane) {
                final Pane childPane = (Pane) child;
                batchAddBtnTriggerEventHandler(childPane, handler);
            }
            if (child instanceof Button) {
                final Button btn = (Button) child;
                btn.setOnAction(handler);
            }
        }
    }

    private void registerTextHandler() {
        // Maven-Gradle坐标转换
        handleRuleMap.register(btnMavenToGradle, source -> {
            // 简单校验
            if (StringUtils.containsAny(source, ":")) {
                return null;
            }
            final Map<String, Object> map = XMLUtils.parseXml(source);
            return map.getOrDefault("groupId", "") + ":" + map.getOrDefault("artifactId", "") + ":" + map.getOrDefault("version", "");
        });
        // Gradle-Maven坐标转换
        handleRuleMap.register(btnGradleToMaven, source -> {
            if (StringUtils.containsAny(source, "<", ">", "/>")) {
                return null;
            }
            String[] coordinates = StringUtils.split(source, ":");
            String[] newCoordinates = new String[]{"", "", ""};
            StringBuilder sb = new StringBuilder();
            if (coordinates.length < 3) {
                System.arraycopy(coordinates, 0, newCoordinates, 0, coordinates.length);
            } else if (coordinates.length > 3) {
                System.arraycopy(coordinates, 0, newCoordinates, 0, 2);
                for (int i = 2; i < coordinates.length; i++) {
                    sb.append(coordinates[i]);
                }
                newCoordinates[2] = sb.toString();
            } else {
                newCoordinates = coordinates;
            }
            sb.delete(0, sb.length());
            sb.append("\n\t")
                    .append(XMLUtils.wrapWithTagName(newCoordinates[0], "groupId"));
            sb.append("\n\t")
                    .append(XMLUtils.wrapWithTagName(newCoordinates[1], "artifactId"));
            sb.append("\n\t")
                    .append(XMLUtils.wrapWithTagName(newCoordinates[2], "version"));
            sb.append("\n");
            return XMLUtils.wrapWithTagName(sb.toString(), "dependency");
            // ch.qos.logback:logback-classic:1.4.0
        });
    }
}
