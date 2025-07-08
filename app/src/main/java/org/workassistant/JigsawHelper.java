package org.workassistant;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class JigsawHelper extends JFrame {

    static {
        FlatLightLaf.setup();
    }

    public JigsawHelper() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        this.setLocationRelativeTo(null); // 窗体居中显示

        setLayout(new BorderLayout());

        JButton button = new JButton("选择");

        // 创建面板
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        JTextField jarLocation = new JTextField();
        panel.add(Box.createRigidArea(new Dimension(10, 0))); // 添加间隔
        panel.add(new JLabel("Jar包"));
        panel.add(Box.createRigidArea(new Dimension(10, 0))); // 添加间隔
        panel.add(jarLocation);
        panel.add(Box.createRigidArea(new Dimension(10, 0))); // 添加间隔
        panel.add(button);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("选择JAR包");
                int flag = chooser.showOpenDialog(JigsawHelper.this);
                //若选择了文件，则打印选择了什么文件
                if (flag == JFileChooser.APPROVE_OPTION) {
                    File selectedJar = chooser.getSelectedFile();
                    if (selectedJar != null) {
                        jarLocation.setText(selectedJar.getAbsolutePath());
                        readJar(selectedJar);
                    }
                }
            }
        });

        add(panel, BorderLayout.NORTH);
    }

    public void readJar(File file) {
        try (JarFile jarFile = new JarFile(file, false)) {
            Manifest manifest = jarFile.getManifest();
            Runtime.Version version = jarFile.getVersion();
            boolean multiRelease = jarFile.isMultiRelease();

            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();

                if (jarEntry.isDirectory()) {
                    continue;
                }

                if (jarEntry.getName().contains("moudle-info")) {
                    System.out.println(jarEntry);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        JigsawHelper jigsaw = new JigsawHelper();
        jigsaw.setVisible(true);
    }
}
