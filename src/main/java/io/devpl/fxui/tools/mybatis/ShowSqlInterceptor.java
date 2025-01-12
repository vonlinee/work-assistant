package io.devpl.fxui.tools.mybatis;

import com.alibaba.druid.sql.SQLUtils;
import com.mysql.cj.jdbc.ClientPreparedStatement;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.logging.jdbc.PreparedStatementLogger;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.PreparedStatement;
import java.sql.Statement;

/**
 * 在MyBatis执行sql之前获取到真实执行的sql
 * 可以直接在数据库执行
 */
@Intercepts({@Signature(type = StatementHandler.class, method = "parameterize", args = {Statement.class})})
public class ShowSqlInterceptor implements Interceptor {

    private String sql;

    /**
     * @param invocation 包含方法执行的相关信息
     * @return 返回值
     * @throws Throwable 异常
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 获取被拦截的对象
        Object target = invocation.getTarget();
        // 获取被拦截的方法
        Method method = invocation.getMethod();
        // 获取被拦截的方法的参数 Statement对象，实际是PrepareStatement对象
        Object[] args = invocation.getArgs();

        // 执行被拦截的方法前，做一些事情

        // 执行被拦截的方法
        Object result = invocation.proceed();

        Object arg = args[0];
        // org.apache.ibatis.logging.jdbc.PreparedStatementLogger@23a5cb2a
        if (arg instanceof Proxy) {
            Object value = ReflectionUtils.getValue(arg, "h");
            PreparedStatementLogger logger = (PreparedStatementLogger) value;
            assert logger != null;
            PreparedStatement stmt = logger.getPreparedStatement();
            // 仅适用于mysql
            ClientPreparedStatement pstmt = (ClientPreparedStatement) stmt;

//            this.sql = pstmt.asSql(false);
        }

        // 执行被拦截的方法后，做一些事情
        if (this.sql != null) {
            // 中断执行
            throw new RuntimeException(SQLUtils.formatMySql(sql));
        }
        // 返回执行结果
        return result;
    }

    public String getSql() {
        return sql;
    }
}
