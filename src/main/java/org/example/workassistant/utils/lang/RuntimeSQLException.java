package org.example.workassistant.utils.lang;

import java.sql.SQLException;

/**
 * 包装SQLException为RuntimeException 数据库访问异常
 *
 * @see SQLException
 */
public class RuntimeSQLException extends RuntimeException {

    public RuntimeSQLException(SQLException sqlException) {
        super(sqlException);
    }

    public RuntimeSQLException(String message) {
        super(message);
    }

    public RuntimeSQLException(String message, Object... args) {
        super(message.formatted(args));
    }

    public RuntimeSQLException(String message, SQLException sqlException) {
        super(message, sqlException);
    }

    public static RuntimeSQLException wrap(SQLException sqlException) {
        return new RuntimeSQLException(sqlException.getMessage(), sqlException);
    }

    public static RuntimeSQLException wrap(Throwable throwable) {
        return new RuntimeSQLException(throwable.getMessage());
    }

    public static RuntimeSQLException wrap(String message, SQLException sqlException, Object... args) {
        return new RuntimeSQLException(message.formatted(args), sqlException);
    }
}
