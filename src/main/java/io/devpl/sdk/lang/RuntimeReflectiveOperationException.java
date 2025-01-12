package io.devpl.sdk.lang;

/**
 * 运行时反射操作异常，区别于ReflectiveOperationException，不用强制try catch
 *
 * @see ReflectiveOperationException
 */
public class RuntimeReflectiveOperationException extends RuntimeException {

    public RuntimeReflectiveOperationException(ReflectiveOperationException roe) {
        super(roe);
    }

    public RuntimeReflectiveOperationException(String message, ReflectiveOperationException roe) {
        super(message, roe);
    }
}
