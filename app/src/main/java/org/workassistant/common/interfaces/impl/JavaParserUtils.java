package org.workassistant.common.interfaces.impl;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.printer.configuration.DefaultConfigurationOption;
import com.github.javaparser.printer.configuration.DefaultPrinterConfiguration;
import com.github.javaparser.printer.configuration.PrinterConfiguration;
import com.github.javaparser.utils.ParserCollectionStrategy;
import com.github.javaparser.utils.ProjectRoot;
import com.github.javaparser.utils.SourceRoot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

/**
 * <a href="https://houbb.github.io/2020/05/29/java-ast-06-comments">...</a>
 */
public class JavaParserUtils {

    private static final Logger log = LoggerFactory.getLogger(JavaParserUtils.class);

    private static final JavaParser JAVA_PARSER_INSTANCE;
    // 打印配置
    static PrinterConfiguration printerConfiguration;

    static {
        JAVA_PARSER_INSTANCE = new JavaParser(newSimpleParserConfiguration(8));
    }

    static {
        printerConfiguration = new DefaultPrinterConfiguration();
        printerConfiguration.addOption(new DefaultConfigurationOption(DefaultPrinterConfiguration.ConfigOption.PRINT_COMMENTS, true));
        printerConfiguration.addOption(new DefaultConfigurationOption(DefaultPrinterConfiguration.ConfigOption.PRINT_JAVADOC, true));
        printerConfiguration.addOption(new DefaultConfigurationOption(DefaultPrinterConfiguration.ConfigOption.COLUMN_ALIGN_FIRST_METHOD_CHAIN, true));
    }

    public static ParserConfiguration newSimpleParserConfiguration(int javaVersionNo) {
        ParserConfiguration.LanguageLevel level;
        try {
            level = ParserConfiguration.LanguageLevel.valueOf("JAVA_" + javaVersionNo);
        } catch (Exception exception) {
            level = ParserConfiguration.LanguageLevel.JAVA_8;
        }
        ParserConfiguration pf = new ParserConfiguration();
        pf.setLanguageLevel(level); // JDK8
        pf.setCharacterEncoding(StandardCharsets.UTF_8); // 源代码字符编码
        // 关闭注释分析 默认情况下启用注释分析，禁用将加快解析速度，但在处理单个源文件时速度提升不明显，如果要解析大量文件，建议禁用。
        pf.setAttributeComments(true);
        pf.setDoNotAssignCommentsPrecedingEmptyLines(true);
        // 设置为孤立注释
        pf.setDoNotAssignCommentsPrecedingEmptyLines(true);
        return pf;
    }

    /**
     * 解析工程下的所有Java文件
     *
     * @param path 工程根目录
     */
    public static void parseProject(String path) {
        Path root = Paths.get(path);
        ProjectRoot projectRoot = new ParserCollectionStrategy().collect(root);
        final List<SourceRoot> sourceRoots = projectRoot.getSourceRoots();
        sourceRoots.forEach(sourceRoot -> {
            try {
                sourceRoot.parseParallelized((localPath, absolutePath, result) -> {
                    if (result.isSuccessful()) {
                        Optional<CompilationUnit> resultOptional = result.getResult();
                        NodeList<TypeDeclaration<?>> typeDeclarations = resultOptional.map(CompilationUnit::getTypes)
                            .orElseGet(NodeList::new);
                        for (TypeDeclaration<?> typeDeclaration : typeDeclarations) {
                            TypeInfo typeInfo = TypeInfo.of(typeDeclaration);
                            typeInfo.setPath(absolutePath.toString());
                            TypeInfoRegistry.register(typeInfo);
                        }
                    }
                    return SourceRoot.Callback.Result.SAVE;
                });
            } catch (IOException e) {
                log.error("failed to parse file {}", path, e);
            }
        });
    }

    /**
     * 解析单个java源文件
     *
     * @param file        java源文件
     * @param javaVersion java版本号
     * @param visitor     CompilationUnitVisitor
     * @param <T>         返回结果
     * @return 返回结果
     */
    public static <T> Optional<T> parse(File file, int javaVersion, CompilationUnitVisitor<T> visitor) {
        ParseResult<CompilationUnit> pr = parseResult(file, javaVersion);
        if (pr.isSuccessful()) {
            return pr.getResult().map(visitor::visit);
        }
        return Optional.empty();
    }

    /**
     * 解析单个java源文件
     *
     * @param file    java源文件
     * @param visitor CompilationUnitVisitor
     * @param <T>     返回结果
     * @return 返回结果
     */
    public static <T> Optional<T> parse(File file, CompilationUnitVisitor<T> visitor) {
        return parse(file, 8, visitor);
    }

    public static <T> Optional<T> parse(Reader reader, CompilationUnitVisitor<T> visitor) {
        ParseResult<CompilationUnit> result = JAVA_PARSER_INSTANCE.parse(reader);
        if (result.isSuccessful()) {
            return result.getResult().map(visitor::visit);
        }
        return Optional.empty();
    }

    public static ParseResult<CompilationUnit> parseResult(File file, int javaVersion) {
        JavaParser javaParser = new JavaParser(newSimpleParserConfiguration(javaVersion));
        ParseResult<CompilationUnit> result = null;
        try {
            result = javaParser.parse(file);
        } catch (FileNotFoundException e) {
            log.error("failed to parse file {}", file, e);
        }
        return result;
    }

    public static ParseResult<CompilationUnit> parseFile(File file) {
        JavaParser javaParser = new JavaParser();
        ParseResult<CompilationUnit> result;
        try {
            result = javaParser.parse(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        Optional<CompilationUnit> parseResult = result.getResult();
        if (parseResult.isPresent()) {
            // 每一个java文件就是一个编译单元
            CompilationUnit compilationUnit = parseResult.get();
            String packageName = null;
            final Optional<PackageDeclaration> packageDeclarationOptional = compilationUnit.getPackageDeclaration();
            if (packageDeclarationOptional.isPresent()) {
                final PackageDeclaration packageDeclaration = packageDeclarationOptional.get();
                packageName = packageDeclaration.getNameAsString();
            }
            NodeList<ImportDeclaration> imports = compilationUnit.getImports();
            ImportInfo importInfo = ImportInfo.extract(imports);
            importInfo.setPackageName(packageName);
            // 返回编译单元中的所有顶级类型声明
            NodeList<TypeDeclaration<?>> types = compilationUnit.getTypes();
            for (TypeDeclaration<?> type : types) {
                if (type.isTopLevelType()) {
                    NodeList<BodyDeclaration<?>> members = type.getMembers();
                    for (BodyDeclaration<?> member : members) {
                        if (member.isFieldDeclaration()) {
                            FieldDeclaration fd = member.asFieldDeclaration();
                            if (fd.isStatic()) {

                            } else {
                                NodeList<VariableDeclarator> variables = fd.getVariables();
                                VariableDeclarator variableDeclarator = variables.get(0);
                                final Type variableDeclaratorType = variableDeclarator.getType();
                                if (variableDeclaratorType.isClassOrInterfaceType()) {
                                    ClassOrInterfaceType ciType = variableDeclaratorType.asClassOrInterfaceType();
                                    final SimpleName name = ciType.getName();
                                    ciType.getTypeArguments().ifPresent(typeArguments -> {
                                        // 判断泛型个数
                                        if (isCollectionType(name.getIdentifier())) {
                                            // 泛型都是 ClassOrInterfaceType，包括String，不会是基础类型
                                            // 泛型可能还存在嵌套泛型，比如List<Map<String, Object>
                                            Type typeArg = typeArguments.get(0);
                                            if (typeArg.isClassOrInterfaceType()) {
                                                ClassOrInterfaceType classOrInterfaceType = typeArg.asClassOrInterfaceType();
                                                SimpleName typeArgName = classOrInterfaceType.getName();
                                                String typeName = importInfo.get(typeArgName);
                                                System.out.println(typeName);
                                            }
                                        } else if (isMapType(name.getIdentifier())) {
                                        }
                                    });
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public static boolean isCollectionType(String identifier) {
        return "List".equals(identifier) || "Set".equals(identifier) || "Collection".equals(identifier);
    }

    public static boolean isMapType(String identifier) {
        return identifier != null && identifier.contains("Map");
    }

    /**
     * 处理类型,方法,成员
     *
     * @param node
     */
    public static void processNode(Node node) {
        if (node instanceof TypeDeclaration) {
            // 类型声明
            // do something with this type declaration
        } else if (node instanceof MethodDeclaration) {
            // 方法声明
            // do something with this method declaration
            String methodName = ((MethodDeclaration) node).getName().getIdentifier();
            System.out.println("方法: " + methodName);
        } else if (node instanceof FieldDeclaration) {
            // 成员变量声明
            // do something with this field declaration
            // 注释
            Comment comment = node.getComment().orElse(null);
            // 变量
            NodeList<VariableDeclarator> variables = ((FieldDeclaration) node).getVariables();
            SimpleName fieldName = variables.get(0).getName();
            if (comment != null) {
                System.out.print(handleComment(comment.getContent()));
            }
            System.out.print("\t");
            System.out.print(fieldName);
            System.out.println();
        }
        for (Node child : node.getChildNodes()) {
            processNode(child);
        }
    }

    private static boolean handleComment(String content) {
        return false;
    }

    public static String toString(CompilationUnit compilationUnit) {
        return compilationUnit.toString(printerConfiguration);
    }

    public static Name newTypeName(String typeName) {
        final int i = typeName.lastIndexOf(".");
        if (i < 0) {
            return new Name(typeName);
        }
        return new Name(new Name(typeName.substring(0, i)), typeName.substring(i + 1));
    }

    /**
     * 解析字符串
     *
     * @param sourceCode 源代码
     * @return 解析结果
     */
    public static ParseResult<CompilationUnit> parseString(String sourceCode) {
        return JAVA_PARSER_INSTANCE.parse(sourceCode);
    }
}
