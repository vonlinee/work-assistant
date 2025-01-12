package io.devpl.fxui.tools.mybatis;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * 忽略DTD验证
 * <a href="https://blog.csdn.net/u011679955/article/details/93341605">...</a>
 */
public class IgnoreDTDEntityResolver implements EntityResolver {

    @Override
    public InputSource resolveEntity(String arg0, String arg1) throws SAXException, IOException {
        return new InputSource(new ByteArrayInputStream("<?xml version='1.0' encoding='UTF-8'?>".getBytes()));
    }
}
