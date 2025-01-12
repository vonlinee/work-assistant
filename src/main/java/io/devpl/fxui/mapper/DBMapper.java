package io.devpl.fxui.mapper;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder;
import com.baomidou.mybatisplus.core.MybatisXMLConfigBuilder;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.pagination.dialects.MySqlDialect;
import io.devpl.fxui.utils.AppConfig;
import org.apache.ibatis.session.LocalCacheScope;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public abstract class DBMapper {

    static final Logger log = LoggerFactory.getLogger(DBMapper.class);
    static SqlSessionFactory sqlSessionFactory;

    static {
        URL mybaticConfigFile = Thread.currentThread().getContextClassLoader().getResource("mybatis-config.xml");
        try {
            assert mybaticConfigFile != null;
            try (FileInputStream fis = new FileInputStream(mybaticConfigFile.getFile())) {
                MybatisXMLConfigBuilder builder = new MybatisXMLConfigBuilder(fis);
                MybatisConfiguration configuration = (MybatisConfiguration) builder.parse();
                // 关闭缓存，MyBatis默认是开启缓存的
                configuration.setCacheEnabled(false);
                // 设置缓存作用域为单条语句
                configuration.setLocalCacheScope(LocalCacheScope.STATEMENT);
                MybatisPlusInterceptor mbpInterceptor = new MybatisPlusInterceptor();
                PaginationInnerInterceptor pageInterceptor = new PaginationInnerInterceptor();
                // 设置请求的页面大于最大页后操作，true调回到首页，false继续请求。默认false
                pageInterceptor.setOverflow(false);
                // 单页分页条数限制，默认无限制
                pageInterceptor.setMaxLimit(500L);
                pageInterceptor.setDialect(new MySqlDialect());
                // 设置数据库类型
                pageInterceptor.setDbType(DbType.MYSQL);
                mbpInterceptor.addInnerInterceptor(pageInterceptor);

                configuration.addInterceptor(mbpInterceptor);

                MybatisSqlSessionFactoryBuilder ssfb = new MybatisSqlSessionFactoryBuilder();
                sqlSessionFactory = ssfb.build(configuration);
            }
        } catch (IOException e) {
            log.error("MyBatis 初始化失败", e);
        }
    }

    public <T> T getMapper(Class<T> mapperClass) {
        return sqlSessionFactory.getConfiguration().getMapper(mapperClass, sqlSessionFactory.openSession());
    }

    /**
     * 新增一条数据
     * @param sql    新增SQL
     * @param params 参数列表
     * @return
     */
    public final int insert(String sql, Object... params) {
        return AppConfig.template.update(sql, params);
    }

    public final int update(String sql, Object... params) {
        return AppConfig.template.update(sql, params);
    }

    public final int delete(String sql, Object... params) {
        return AppConfig.template.update(sql, params);
    }

    public final int[] saveBatch(String sql, List<Object[]> params) {
        return AppConfig.template.batchUpdate(sql, params);
    }

    public final <T> T getOne(String sql, Class<T> clazz) {
        return (T) AppConfig.template.query(sql, new RowMapperResultSetExtractor<>(new BeanPropertyRowMapper<>(clazz)));
    }

    public final <T> List<T> getList(String sql, Class<T> clazz) {
        return (List<T>) AppConfig.template.queryForList("");
    }
}
