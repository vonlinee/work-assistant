package org.example.workassistant.common.interfaces.impl;

import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.javadoc.Javadoc;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * AST 字段解析
 */
public abstract class JavaASTUtils {

    public static List<MetaField> parseFields(String content) throws IOException {
        ParseResult<CompilationUnit> result = JavaParserUtils.parseString(content);
        if (result.isSuccessful()) {
            return result.getResult().map(JavaASTUtils::parseFields).orElse(Collections.emptyList());
        }
        return Collections.emptyList();
    }

    public static List<MetaField> parseFields(File file) {
        ParseResult<CompilationUnit> result = JavaParserUtils.parseResult(file, 8);
        if (result.isSuccessful()) {
            return result.getResult().map(JavaASTUtils::parseFields).orElse(Collections.emptyList());
        }
        return Collections.emptyList();
    }

    /**
     * 解析模型信息
     *
     * @param content 源代码
     * @return MetaModel
     */
    public static MetaModel parseModel(String content) {
        ParseResult<CompilationUnit> result = JavaParserUtils.parseString(content);
        if (result.isSuccessful()) {
            return result.getResult().map(JavaASTUtils::parseModel).orElse(null);
        }
        return null;
    }

    /**
     * @param cu 单个实体类的源码解析得到的结果
     * @return 模型信息
     */
    public static MetaModel parseModel(CompilationUnit cu) {
        MetaModel model = new MetaModel();
        NodeList<TypeDeclaration<?>> types = cu.getTypes();
        if (!types.isEmpty()) {
            TypeDeclaration<?> typeDeclaration = types.get(0);
            model.setName(typeDeclaration.getName().getIdentifier());
            model.setFields(parseFields(typeDeclaration));
        }
        return model;
    }

    public static List<MetaField> parseFields(TypeDeclaration<?> type) {
        List<FieldDeclaration> fields = type.getFields();
        List<MetaField> fieldMetaDataList = new ArrayList<>();
        for (FieldDeclaration field : fields) {
            if (field.isStatic()) {
                continue; // 忽略静态变量
            }
            MetaField fieldMetaData = new MetaField();
            NodeList<VariableDeclarator> variables = field.getVariables();

            for (VariableDeclarator variable : variables) {
                String fieldName = variable.getName().asString();
                // 字段名
                fieldMetaData.setName(fieldName);
                fieldMetaData.setIdentifier(fieldName);
                // 类型名称
                fieldMetaData.setDataType(variable.getTypeAsString());
            }
            // 注释信息
            fieldMetaData.setDescription(findFieldDescription(field));
            fieldMetaDataList.add(fieldMetaData);
        }
        return fieldMetaDataList;
    }

    /**
     * 解析字段信息
     *
     * @param cu CompilationUnit
     * @return 字段信息列表
     */
    public static List<MetaField> parseFields(CompilationUnit cu) {
        NodeList<TypeDeclaration<?>> types = cu.getTypes();
        List<MetaField> fields = new ArrayList<>();
        for (TypeDeclaration<?> type : types) {
            fields.addAll(parseFields(type));
        }
        return fields;
    }

    /**
     * 字段注释信息
     *
     * @param field 字段声明
     * @return 注释信息
     */
    private static String findFieldDescription(FieldDeclaration field) {
        return field.getJavadocComment()
            .map(JavadocComment::parse)
            .map(Javadoc::toText)
            .orElse("")
            .replace("\t", "")
            .replace("\r", "")
            .replace("\n", "");
    }
}
