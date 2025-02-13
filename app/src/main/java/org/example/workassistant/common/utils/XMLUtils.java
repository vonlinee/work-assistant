package org.example.workassistant.common.utils;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.xml.sax.InputSource;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class XMLUtils {

    private static final String START_START = "<";
    private static final String END = ">";
    private static final String END_START = "</";

    private static final SAXReader SAX_READER = new SAXReader();
    private static final OutputFormat defaultFormat;

    static {
        defaultFormat = new OutputFormat();
        defaultFormat.setIndent(true);
        defaultFormat.setNewlines(true);
    }

    public static void fromString(String xmlFragment) {
        InputSource in = new InputSource(new StringReader(xmlFragment));
        in.setEncoding(StandardCharsets.UTF_8.name());
        Document document = readDocument(in);// 如果是xml文件这里直接写xml文件的绝对路径
        Element root = document.getRootElement();// 获取根节点
        List<Element> elements = root.elements();// 查找子节点
        for (Element element : elements) {// 读取到第一个子节点
            System.out.println(element.getStringValue());
        }
    }

    private static Document readDocument(InputSource inputSource) {
        try {
            return SAX_READER.read(inputSource);
        } catch (Exception exception) {
            throw new RuntimeException("failed to read document!");
        }
    }

    /**
     * 将文本用html标签包起来
     *
     * @param content 文本
     * @param tagName 标签名
     * @return
     */
    public static String wrapWithTagName(String content, String tagName) {
        return START_START + tagName + END + content + END_START + tagName + END;
    }

    public static Map<String, Object> parseXml(String xml) {
        return parseXml(xml, StandardCharsets.UTF_8.name());
    }

    /**
     * 将xml转为key value
     *
     * @param xml xml格式字符串
     * @return map
     */
    public static Map<String, Object> parseXml(String xml, String charsetName) {
        if (xml == null || xml.isBlank()) {
            return null;
        }
        try {
            return parseXml(xml.getBytes(charsetName), charsetName);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 解析xml数据
     */
    private static Map<String, Object> parseXml(byte[] xmlBytes, String charset) {
        SAXReader reader = new SAXReader(false);
        InputSource source = new InputSource(new ByteArrayInputStream(xmlBytes));
        source.setEncoding(charset);
        Map<String, Object> map = new HashMap<>();
        try {
            Document doc = reader.read(source);
            Iterator<Element> iter = doc.getRootElement()
                .elementIterator();
            while (iter.hasNext()) {
                Element e = iter.next();
                if (!e.elementIterator()
                    .hasNext()) {
                    map.put(e.getName(), e.getTextTrim());
                    continue;
                }
                Iterator<Element> iterator = e.elementIterator();
                Map<String, String> param = new HashMap<>();
                while (iterator.hasNext()) {
                    Element el = iterator.next();
                    param.put(el.getName(), el.getTextTrim());
                }
                map.put(e.getName(), param);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    public static String format(Document document) {
        try {
            final StringWriter sw = new StringWriter();
            XMLWriter xmlWriter = new XMLWriter(sw, defaultFormat);
            xmlWriter.write(document);
            xmlWriter.close();
            return sw.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void main(String[] args) throws DocumentException {
        String xml = """
            <localJarDependency>
                <groupId>org.dom4j</groupId>
                <artifactId>dom4j</artifactId>
                <version>2.1.3</version>
            </localJarDependency>""".indent(8);

        final Map<String, Object> map = XMLUtils.parseXml(xml);

        System.out.println(map);
    }

    /**
     * 将内容以<![CDATA[ ]>进行包裹
     * 被<![CDATA[]]>这个标记所包含的内容将表示为纯文本，比如<![CDATA[<]]>表示文本内容“<”。
     * <a href="https://www.runoob.com/xml/xml-cdata.html">CDATA</a>
     *
     * @param content 待包裹的内容
     * @return 包裹结果
     */
    public static String wrapWithCDATA(String content) {
        return "<![CDATA[" + content + "]]>";
    }
}
