package org.assistant.tools.doc;

import org.assistant.ui.controls.Button;
import org.assistant.ui.controls.Label;
import org.assistant.ui.pane.BorderPane;
import org.assistant.ui.pane.HBox;

import javax.swing.*;
import java.awt.*;

/**
 * Dialog to show mock request and response JSON for an API.
 */
public class MockDataDialog extends JDialog {

    public MockDataDialog(Window owner, WebApiInfo api) {
        super(owner, "Mock Data: " + api.getMethod() + " " + api.getPath(), ModalityType.MODELESS);
        setSize(900, 600);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        String method = api.getMethod().toUpperCase();
        String mockUrl = MockDataGenerator.generateMockUrl(api);
        String mockHeaders = MockDataGenerator.generateMockHeaders(api.getParams());

        JPanel reqPanel;
        if ("GET".equals(method) || "DELETE".equals(method)) {
            reqPanel = createRequestTablePanel("Request Data", mockUrl, mockHeaders, api.getParams());
        } else {
            String mockBody = MockDataGenerator.generateMockRequest(api.getParams());
            reqPanel = createRequestBodyPanel("Request Data", mockUrl, mockHeaders, mockBody);
        }

        String mockResponse = MockDataGenerator.generateMockResponse(api.getReturnTypeFields(), api.getReturnType());
        JPanel respPanel = createMockPanel("Mock Response JSON", mockResponse);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.5);
        splitPane.setLeftComponent(reqPanel);
        splitPane.setRightComponent(respPanel);

        setContentPane(splitPane);
    }

    private JPanel createRequestTablePanel(String title, String url, String headers, java.util.List<ApiParam> params) {
        BorderPane pane = new BorderPane();
        pane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        Box topBox = Box.createVerticalBox();
        Label titleLabel = new Label(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
        topBox.add(titleLabel);
        topBox.add(Box.createVerticalStrut(10));

        JTextArea urlArea = new JTextArea("URL: " + url);
        urlArea.setEditable(false);
        urlArea.setOpaque(false);
        urlArea.setLineWrap(true);
        topBox.add(urlArea);

        if (headers != null && !headers.isEmpty()) {
            topBox.add(Box.createVerticalStrut(5));
            JTextArea hArea = new JTextArea("Headers:\n" + headers);
            hArea.setEditable(false);
            hArea.setOpaque(false);
            topBox.add(hArea);
        }
        topBox.add(Box.createVerticalStrut(10));
        pane.setTop(topBox);

        if (params != null && !params.isEmpty()) {
            // Only keep parameters that are query, path, form, header
            java.util.List<ApiParam> list = params.stream()
                    .filter(p -> p.getIn() != ParamLocation.BODY).toList();

            Object[][] data = new Object[list.size()][3];
            for (int i = 0; i < list.size(); i++) {
                ApiParam p = list.get(i);
                data[i][0] = p.getName();
                data[i][1] = p.getIn() != null ? p.getIn().name() : "";
                data[i][2] = MockDataGenerator.getDummyValue(p.getDataType(), p.getExample(), p.getDefaultValue());
            }
            JTable table = new JTable(data, new String[] { "Name", "Location", "Mock Value" });
            pane.setCenter(new JScrollPane(table));
        }

        return pane;
    }

    private JPanel createRequestBodyPanel(String title, String url, String headers, String body) {
        BorderPane pane = new BorderPane();
        pane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        Box topBox = Box.createVerticalBox();
        Label titleLabel = new Label(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
        topBox.add(titleLabel);
        topBox.add(Box.createVerticalStrut(10));

        JTextArea urlArea = new JTextArea("URL: " + url);
        urlArea.setEditable(false);
        urlArea.setOpaque(false);
        urlArea.setLineWrap(true);
        topBox.add(urlArea);

        if (headers != null && !headers.isEmpty()) {
            topBox.add(Box.createVerticalStrut(5));
            JTextArea hArea = new JTextArea("Headers:\n" + headers);
            hArea.setEditable(false);
            hArea.setOpaque(false);
            topBox.add(hArea);
        }
        topBox.add(Box.createVerticalStrut(10));
        topBox.add(new JLabel("Body JSON:"));
        pane.setTop(topBox);

        JTextArea textArea = new JTextArea(body);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        textArea.setTabSize(4);
        textArea.setEditable(true);

        JScrollPane scrollPane = new JScrollPane(textArea);
        pane.setCenter(scrollPane);

        Button copyBtn = new Button("Copy JSON");
        copyBtn.addActionListener(e -> {
            textArea.selectAll();
            textArea.copy();
            textArea.setCaretPosition(0);
        });

        HBox bottomBox = new HBox();
        bottomBox.add(copyBtn);
        pane.setBottom(bottomBox);

        return pane;
    }

    private JPanel createMockPanel(String title, String content) {
        BorderPane pane = new BorderPane();
        pane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        Label titleLabel = new Label(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        pane.setTop(titleLabel);

        JTextArea textArea = new JTextArea(content);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        textArea.setTabSize(4);
        textArea.setEditable(true);

        JScrollPane scrollPane = new JScrollPane(textArea);
        pane.setCenter(scrollPane);

        Button copyBtn = new Button("Copy to Clipboard");
        copyBtn.addActionListener(e -> {
            textArea.selectAll();
            textArea.copy();
            textArea.setCaretPosition(0);
        });

        HBox bottomBox = new HBox();
        bottomBox.add(copyBtn);
        pane.setBottom(bottomBox);

        return pane;
    }
}
