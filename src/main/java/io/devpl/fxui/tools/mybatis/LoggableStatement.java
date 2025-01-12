package io.devpl.fxui.tools.mybatis;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * <a href="https://blog.csdn.net/kingwin28/article/details/129014291?spm=1001.2101.3001.6650.2&utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7EYuanLiJiHua%7EPosition-2-129014291-blog-108041346.235%5Ev32%5Epc_relevant_increate_t0_download_v2&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7EYuanLiJiHua%7EPosition-2-129014291-blog-108041346.235%5Ev32%5Epc_relevant_increate_t0_download_v2&utm_relevant_index=3">...</a>
 */
public class LoggableStatement implements PreparedStatement {

    /**
     * used for storing parameter values needed for producing log
     */
    private final ArrayList<Object> parameterValues;

    /**
     * the query string with question marks as parameter placeholders
     */
    private final String sqlTemplate;

    /**
     * a statement created from a real database connection
     */
    private final PreparedStatement wrappedStatement;

    public LoggableStatement(Connection connection, String sql) throws SQLException {
        // use connection to make a prepared statement
        wrappedStatement = connection.prepareStatement(sql);
        sqlTemplate = sql;
        parameterValues = new ArrayList<>();
    }

    private void saveQueryParamValue(int position, Object obj) {
        String strValue;
        if (obj instanceof String || obj instanceof Date) {
            // if we have a String, include '' in the saved value
            strValue = "'" + obj + "'";
        } else {
            if (obj == null) {
                // convert null to the string null
                strValue = "null";
            } else {
                // unknown object (includes all Numbers), just call toString
                strValue = obj.toString();
            }
        }
        // if we are setting a position larger than current size of
        // parameterValues, first make it larger
        while (position >= parameterValues.size()) {
            parameterValues.add(null);
        }
        // save the parameter
        parameterValues.set(position, strValue);
    }

    // 这一步是对ArrayList与sql进行处理，输出完整的sql语句
    public String getQueryString() {
        int len = sqlTemplate.length();
        StringBuilder t = new StringBuilder(len * 2);
        if (parameterValues != null) {
            int i = 1, limit = 0, base = 0;
            while ((limit = sqlTemplate.indexOf('?', limit)) != -1) {
                t.append(sqlTemplate, base, limit);
                t.append(parameterValues.get(i));
                i++;
                limit++;
                base = limit;
            }
            if (base < len) {
                t.append(sqlTemplate.substring(base));
            }
        }
        return t.toString();
    }

    @Override
    public void addBatch() throws SQLException {
        wrappedStatement.addBatch();
    }

    @Override
    public void clearParameters() throws SQLException {
        wrappedStatement.clearParameters();
    }

    @Override
    public boolean execute() throws SQLException {
        return wrappedStatement.execute();
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        return wrappedStatement.executeQuery();
    }

    @Override
    public int executeUpdate() throws SQLException {
        return wrappedStatement.executeUpdate();
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return wrappedStatement.getMetaData();
    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        return wrappedStatement.getParameterMetaData();
    }

    @Override
    public void setArray(int i, Array x) throws SQLException {
        wrappedStatement.setArray(i, x);
        saveQueryParamValue(i, x);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        wrappedStatement.setAsciiStream(parameterIndex, x, length);
        saveQueryParamValue(parameterIndex, x);
    }

    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        wrappedStatement.setBigDecimal(parameterIndex, x);
        saveQueryParamValue(parameterIndex, x);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
        wrappedStatement.setBinaryStream(parameterIndex, x, length);
        saveQueryParamValue(parameterIndex, x);
    }

    @Override
    public void setBlob(int i, Blob x) throws SQLException {
        wrappedStatement.setBlob(i, x);
        saveQueryParamValue(i, x);
    }

    @Override
    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        wrappedStatement.setBoolean(parameterIndex, x);
        saveQueryParamValue(parameterIndex, x);
    }

    @Override
    public void setByte(int parameterIndex, byte x) throws SQLException {
        wrappedStatement.setByte(parameterIndex, x);
        saveQueryParamValue(parameterIndex, x);
    }

    @Override
    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        wrappedStatement.setBytes(parameterIndex, x);
        saveQueryParamValue(parameterIndex, x);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        wrappedStatement.setCharacterStream(parameterIndex, reader, length);
        saveQueryParamValue(parameterIndex, reader);
    }

    @Override
    public void setClob(int i, Clob x) throws SQLException {
        wrappedStatement.setClob(i, x);
        saveQueryParamValue(i, x);
    }

    @Override
    public void setDate(int parameterIndex, Date x) throws SQLException {
        wrappedStatement.setDate(parameterIndex, x);
        saveQueryParamValue(parameterIndex, x);
    }

    @Override
    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        wrappedStatement.setDate(parameterIndex, x, cal);
        saveQueryParamValue(parameterIndex, x);
    }

    @Override
    public void setDouble(int parameterIndex, double x) throws SQLException {
        wrappedStatement.setDouble(parameterIndex, x);
        saveQueryParamValue(parameterIndex, x);
    }

    @Override
    public void setFloat(int parameterIndex, float x) throws SQLException {
        wrappedStatement.setFloat(parameterIndex, x);
        saveQueryParamValue(parameterIndex, x);
    }

    @Override
    public void setInt(int parameterIndex, int x) throws SQLException {
        wrappedStatement.setInt(parameterIndex, x);
        saveQueryParamValue(parameterIndex, x);
    }

    @Override
    public void setLong(int parameterIndex, long x) throws SQLException {
        wrappedStatement.setLong(parameterIndex, x);
        saveQueryParamValue(parameterIndex, x);
    }

    @Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        wrappedStatement.setNull(parameterIndex, sqlType);
        saveQueryParamValue(parameterIndex, sqlType);
    }

    @Override
    public void setNull(int paramIndex, int sqlType, String typeName) throws SQLException {
        wrappedStatement.setNull(paramIndex, sqlType, typeName);
        saveQueryParamValue(paramIndex, sqlType);
    }

    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException {
        wrappedStatement.setObject(parameterIndex, x);
        saveQueryParamValue(parameterIndex, x);
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        wrappedStatement.setObject(parameterIndex, x, targetSqlType);
        saveQueryParamValue(parameterIndex, x);
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scale) throws SQLException {
        wrappedStatement.setObject(parameterIndex, x, targetSqlType, scale);
        saveQueryParamValue(parameterIndex, x);
    }

    @Override
    public void setRef(int i, Ref x) throws SQLException {
        wrappedStatement.setRef(i, x);
        saveQueryParamValue(i, x);
    }

    @Override
    public void setShort(int parameterIndex, short x) throws SQLException {
        wrappedStatement.setShort(parameterIndex, x);
        saveQueryParamValue(parameterIndex, x);
    }

    @Override
    public void setString(int parameterIndex, String x) throws SQLException {
        wrappedStatement.setString(parameterIndex, x);
        saveQueryParamValue(parameterIndex, x);
    }

    @Override
    public void setTime(int parameterIndex, Time x) throws SQLException {
        wrappedStatement.setTime(parameterIndex, x);
        saveQueryParamValue(parameterIndex, x);
    }

    @Override
    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        wrappedStatement.setTime(parameterIndex, x, cal);
        saveQueryParamValue(parameterIndex, x);
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        wrappedStatement.setTimestamp(parameterIndex, x);
        saveQueryParamValue(parameterIndex, x);
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        wrappedStatement.setTimestamp(parameterIndex, x, cal);
        saveQueryParamValue(parameterIndex, x);
    }

    @Override
    public void setURL(int parameterIndex, URL x) throws SQLException {
        wrappedStatement.setURL(parameterIndex, x);
        saveQueryParamValue(parameterIndex, x);
    }

    @Override
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        wrappedStatement.setCharacterStream(parameterIndex, new InputStreamReader(x), length);
        saveQueryParamValue(parameterIndex, x);
    }

    @Override
    public void addBatch(String sql) throws SQLException {
        wrappedStatement.addBatch(sql);
    }

    @Override
    public void cancel() throws SQLException {
        wrappedStatement.cancel();
    }

    @Override
    public void clearBatch() throws SQLException {
        wrappedStatement.clearBatch();
    }

    @Override
    public void clearWarnings() throws SQLException {
        wrappedStatement.clearWarnings();
    }

    @Override
    public void close() throws SQLException {
        wrappedStatement.close();
    }

    @Override
    public boolean execute(String sql) throws SQLException {
        return wrappedStatement.execute(sql);
    }

    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        return wrappedStatement.execute(sql, autoGeneratedKeys);
    }

    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        return wrappedStatement.execute(sql, columnIndexes);
    }

    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        return wrappedStatement.execute(sql, columnNames);
    }

    @Override
    public int[] executeBatch() throws SQLException {
        return wrappedStatement.executeBatch();
    }

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        return wrappedStatement.executeQuery(sql);
    }

    @Override

    public int executeUpdate(String sql) throws SQLException {
        return wrappedStatement.executeUpdate(sql);
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        return wrappedStatement.executeUpdate(sql, autoGeneratedKeys);
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        return wrappedStatement.executeUpdate(sql, columnIndexes);
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        return wrappedStatement.executeUpdate(sql, columnNames);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return wrappedStatement.getConnection();
    }

    @Override
    public int getFetchDirection() throws SQLException {
        return wrappedStatement.getFetchDirection();
    }

    @Override
    public int getFetchSize() throws SQLException {
        return wrappedStatement.getFetchSize();
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        return wrappedStatement.getGeneratedKeys();
    }

    @Override
    public int getMaxFieldSize() throws SQLException {
        return wrappedStatement.getMaxFieldSize();
    }

    @Override
    public int getMaxRows() throws SQLException {
        return wrappedStatement.getMaxRows();
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        return wrappedStatement.getMoreResults();
    }

    @Override
    public boolean getMoreResults(int current) throws SQLException {
        return wrappedStatement.getMoreResults(current);
    }

    @Override
    public int getQueryTimeout() throws SQLException {
        return wrappedStatement.getQueryTimeout();
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
        return wrappedStatement.getResultSet();
    }

    @Override
    public int getResultSetConcurrency() throws SQLException {
        return wrappedStatement.getResultSetConcurrency();
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        return wrappedStatement.getResultSetHoldability();
    }

    @Override
    public int getResultSetType() throws SQLException {
        return wrappedStatement.getResultSetType();
    }

    @Override
    public int getUpdateCount() throws SQLException {
        return wrappedStatement.getUpdateCount();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return wrappedStatement.getWarnings();
    }

    @Override
    public void setCursorName(String name) throws SQLException {
        wrappedStatement.setCursorName(name);
    }

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {
        wrappedStatement.setEscapeProcessing(enable);
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        wrappedStatement.setFetchDirection(direction);
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
        wrappedStatement.setFetchSize(rows);
    }

    @Override
    public void setMaxFieldSize(int max) throws SQLException {
        wrappedStatement.setMaxFieldSize(max);
    }

    @Override
    public void setMaxRows(int max) throws SQLException {
        wrappedStatement.setMaxFieldSize(max);
    }

    @Override
    public void setQueryTimeout(int seconds) throws SQLException {
        wrappedStatement.setQueryTimeout(seconds);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {


    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {


    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {


    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {


    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {


    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {


    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {


    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {


    }

    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException {


    }

    @Override
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {


    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {


    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {


    }

    @Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException {


    }

    @Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException {


    }

    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {


    }

    @Override
    public void setNString(int parameterIndex, String value) throws SQLException {


    }

    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException {


    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {


    }

    @Override
    public boolean isClosed() throws SQLException {

        return false;
    }

    @Override
    public boolean isPoolable() throws SQLException {

        return false;
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {


    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {

        return false;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public void closeOnCompletion() throws SQLException {

    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {

        return false;
    }
}
