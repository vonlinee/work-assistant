package io.devpl.fxui.tools.mybatis;

import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.builder.xml.XMLStatementBuilder;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.session.Configuration;

public class MyXmlStatemtnBuilder extends XMLStatementBuilder {

    public MyXmlStatemtnBuilder(Configuration configuration, XNode context) {
        // mapper文件路径
        this(configuration, new MapperBuilderAssistant(configuration, null), context, null);
    }

    MyXmlStatemtnBuilder(Configuration configuration, MapperBuilderAssistant builderAssistant, XNode context, String databaseId) {
        super(configuration, builderAssistant, context, databaseId);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> Class<? extends T> resolveClass(String alias) {
        // MyBatis会判断Mapper标签resultType中指定的参数或者返回值类型是否存在，不存在则会抛出ClassNotFoundException
        // 不需要resultType，因此在这里将异常捕获，并随便返回一个类型
        Class<? extends T> clazz;
        try {
            clazz = super.resolveClass(alias);
        } catch (Exception exception) {
            clazz = (Class<? extends T>) Integer.class;
        }
        return clazz;
    }
}
