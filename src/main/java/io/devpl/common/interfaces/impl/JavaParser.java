package io.devpl.common.interfaces.impl;

import java.io.InputStream;

public interface JavaParser {

    ClassParseResult parseClass(InputStream source);
}
