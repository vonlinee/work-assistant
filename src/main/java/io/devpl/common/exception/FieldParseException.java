package io.devpl.common.exception;

/**
 * 字段解析异常
 */
public class FieldParseException extends RuntimeException {
    public FieldParseException(String msg) {
        super(msg);
    }

    public FieldParseException(Throwable throwable) {
        super(throwable);
    }
}
