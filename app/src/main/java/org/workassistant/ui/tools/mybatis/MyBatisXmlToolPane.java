package org.workassistant.ui.tools.mybatis;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.workassistant.common.ParseResult;
import org.workassistant.ui.editor.CodeEditor;
import org.workassistant.ui.editor.LanguageMode;
import org.workassistant.ui.model.CommonJavaType;
import org.workassistant.util.DBUtils;
import org.workassistant.util.NumberUtils;
import org.workassistant.util.TypeUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class MyBatisXmlToolPane extends SplitPane {

    ParseResult result;

    MyBatis myBatis = new MyBatis();

    CodeEditor editor = CodeEditor.newInstance(LanguageMode.XML);

    ShowSqlInterceptor sqlInterceptor = new ShowSqlInterceptor();

    public MyBatisXmlToolPane() {
        VBox vBox = new VBox();
        this.getItems().add(vBox);
        TextField textField = new TextField();
        Button btn = new Button("解析参数");
        Button btn1 = new Button("预编译SQL");
        Button btn2 = new Button("可执行SQL");

        final VariableTableView table = new VariableTableView();

        btn.setOnAction(event -> {
            String text = editor.getText();
            if (text != null && !text.isEmpty()) {
                result = myBatis.parseSelectXml(text);
                table.clear();
                table.addItems(result.getRoot());
            }
        });

        HBox hBox = new HBox(textField, btn, btn1, btn2);
        vBox.getChildren().add(hBox);
        vBox.getChildren().add(editor.getView());

        TextArea sqlTextArea = new TextArea();

        VBox vBox1 = new VBox(table, sqlTextArea);

        sqlTextArea.prefHeightProperty().bind(vBox1.heightProperty().subtract(table.heightProperty()));

        this.getItems().add(vBox1);

        btn1.setOnAction(event -> {
            if (result == null) {
                Event.fireEvent(btn, new ActionEvent());
                return;
            }
            TreeItem<VarItem> root = table.getRoot();
            Map<String, Object> parameterObject = new HashMap<>();
            // 根节点不展示
            for (TreeItem<VarItem> child : root.getChildren()) {
                fill(child, parameterObject);
            }
            MappedStatement mappedStatement = result.getMappedStatement();
            // 预编译执行的SQL
            BoundSql boundSql = mappedStatement.getBoundSql(parameterObject);

        });

        btn2.setOnAction(event -> {
            if (result == null) {
                Event.fireEvent(btn, new ActionEvent());
            }
            Properties properties = new Properties();
            properties.setProperty("user", "root");
            properties.setProperty("password", "123456");
            String url = "jdbc:mysql://localhost:3306/devpl?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=GMT%2B8";
            try (Connection conn = DBUtils.getConnection(url, properties)) {
                JdbcTransactionFactory factory = new JdbcTransactionFactory();
                Transaction transaction = factory.newTransaction(conn);
                myBatis.configuration.addInterceptor(sqlInterceptor);
                Executor executor = myBatis.configuration.newExecutor(transaction);
                TreeItem<VarItem> root = table.getRoot();
                Map<String, Object> parameterObject = new HashMap<>();
                // 根节点不展示
                for (TreeItem<VarItem> child : root.getChildren()) {
                    fill(child, parameterObject);
                }
                // 方便起见，使用MyBatis执行sql查询的方式获取可执行的sql
                // 这一行肯定会抛异常，终止查询，获取执行的sql
                executor.query(result.getMappedStatement(), parameterObject, RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER);
            } catch (Exception e) {
                sqlTextArea.setText(e.getMessage());
            }
        });
    }

    /**
     * @param boundSql Sql封装
     * @param param    参数对象
     * @return 可执行的SQL
     * @see org.apache.ibatis.executor.parameter.ParameterHandler#setParameters(PreparedStatement)
     */
    public static String getExecutableSql(BoundSql boundSql, Map<String, Object> param) {
        TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry();
        // 参数都是按出现顺序排好序的
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        if (parameterMappings != null) {
            for (ParameterMapping parameterMapping : parameterMappings) {
                if (parameterMapping.getMode() != ParameterMode.OUT) {
                    Object value;
                    String propertyName = parameterMapping.getProperty();
                    // issue #448 ask first for additional params
                    if (boundSql.hasAdditionalParameter(propertyName)) {
                        value = boundSql.getAdditionalParameter(propertyName);
                    } else if (param == null) {
                        value = null;
                    } else if (typeHandlerRegistry.hasTypeHandler(param.getClass())) {
                        value = param;
                    } else {
                        ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
                        ObjectFactory objectFactory = new DefaultObjectFactory();
                        ObjectWrapperFactory objectWrapperFactory = new DefaultObjectWrapperFactory();
                        MetaObject metaObject = MetaObject.forObject(param, objectFactory, objectWrapperFactory, reflectorFactory);
                        value = metaObject.getValue(propertyName);
                    }
                    TypeHandler<?> typeHandler = parameterMapping.getTypeHandler();
                    JdbcType jdbcType = parameterMapping.getJdbcType();
                    System.out.println(value);
                }
            }
        }
        return "";
    }

    @SuppressWarnings("unchecked")
    private void fill(TreeItem<VarItem> parent, Map<String, Object> map) {
        ObservableList<TreeItem<VarItem>> children = parent.getChildren();
        VarItem value = parent.getValue();
        if (!children.isEmpty()) {
            String key = value.getName();
            Map<String, Object> valueMap;
            if (map.containsKey(key)) {
                valueMap = (Map<String, Object>) map.get(key);
            } else {
                valueMap = new HashMap<>();
                map.put(key, valueMap);
            }
            for (TreeItem<VarItem> child : children) {
                fill(child, valueMap);
            }
        } else {
            // 输入的都是字符串，需要推断值
            map.put(value.getName(), getValue(value));
        }
    }

    /**
     * 界面上输入的值都是字符串
     * 参数都是使用#{}进行指定，在给sql填充参数时字符串会使用引号包裹
     * 而数字不需要使用引号包裹，因此需要推断数据类型
     *
     * @param value 参数表中的一行数据
     * @return 参数值，将字符串推断为某个数据类型，比如字符串类型的数字，将会转化为数字类型
     */
    public static Object getValue(VarItem value) {
        Object val = value.getValue();
        if (!(val instanceof String)) {
            return val;
        }
        final String literalValue = String.valueOf(val);
        CommonJavaType type = value.getType();
        if (type == null) {
            // 根据字符串推断类型，结果只能是简单的类型，不会很复杂
            if (TypeUtils.isInteger(literalValue)) {
                type = CommonJavaType.INTEGER;
            } else if (TypeUtils.isDouble(literalValue)) {
                type = CommonJavaType.DOUBLE;
            } else {
                // 非数字类型的其他类型都可以当做字符串处理
                type = CommonJavaType.STRING;
            }
        }
        // 根据指定的类型进行类型推断
        return parseLiteralValue(literalValue, type);
    }

    private static Object parseLiteralValue(String literalValue, CommonJavaType javaType) {
        Object val;
        switch (javaType) {
            case BASE_INT, BASE_BYTE, BASE_LONG -> val = Integer.parseInt(literalValue);
            case BASE_BOOLEAN -> val = Boolean.parseBoolean(literalValue);
            case LOCAL_DATE_TIME -> val = LocalDateTime.parse(literalValue);
            case FLOAT, DOUBLE -> val = Double.parseDouble(literalValue);
            case INTEGER -> val = NumberUtils.parseInt(literalValue, 0);
            default -> val = literalValue;
        }
        return val;
    }
}

