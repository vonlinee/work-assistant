package io.devpl.fxui.mapper;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

public class MyBatis {

    private static final SqlSessionFactory sqlSessionFactory;

    static {
        InputStream inputStream;
        try {
            inputStream = Resources.getResourceAsStream("mybatis-config.xml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
    }

    public static <T> T getMapper(Class<T> mapperClass) {
        // 自动提交
        SqlSession sqlSession = sqlSessionFactory.openSession(true);
        return sqlSessionFactory.getConfiguration().getMapper(mapperClass, sqlSession);
    }
}
