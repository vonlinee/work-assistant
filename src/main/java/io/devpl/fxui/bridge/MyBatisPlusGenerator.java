package io.devpl.fxui.bridge;
/*
import io.devpl.codegen.generator.FastAutoGenerator;
import io.devpl.codegen.generator.config.DateType;
import io.devpl.codegen.generator.config.OutputFile;
import io.devpl.codegen.generator.config.ProjectConfiguration;
import io.devpl.codegen.template.model.ControllerTemplateArguments;
import io.devpl.codegen.template.model.EntityTemplateArguments;
import io.devpl.codegen.template.model.MapperTemplateArguments;
import io.devpl.codegen.template.model.ServiceTemplateArguments;
import io.devpl.codegen.template.velocity.VelocityTemplateEngine;
import io.devpl.fxui.model.CodeGenContext;
import io.devpl.fxui.model.ConnectionConfig;
import io.devpl.fxui.model.ConnectionRegistry;
import io.devpl.fxui.model.TableGeneration;
import io.devpl.sdk.util.CollectionUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
 */

/**
 * 整合mybatis-plus生成器
 */
public class MyBatisPlusGenerator {

//    /**
//     * 生成
//     *
//     * @param context 待生成的参数
//     * @throws Exception 任意异常都会被捕获
//     */
//    public List<File> generate(CodeGenContext context) throws Exception {
//
//        ProjectConfiguration projectConfiguration = context.getProjectConfiguration();
//
//        Map<String, List<TableGeneration>> targetTables = CollectionUtils.groupingBy(context.getTargetedTables()
//            .values(), TableGeneration::getConnectionName);
//
//        VelocityTemplateEngine engine = new VelocityTemplateEngine();
//
//        List<File> list = new ArrayList<>();
//
//        for (Map.Entry<String, List<TableGeneration>> entry : targetTables.entrySet()) {
//            // 连接名称
//            String connectionName = entry.getKey();
//            // 按数据库名称分组
//            Map<String, List<TableGeneration>> groupingByDbName =
//                CollectionUtils.groupingBy(entry.getValue(), TableGeneration::getDatabaseName);
//            for (Map.Entry<String, List<TableGeneration>> dbEntry : groupingByDbName.entrySet()) {
//                ConnectionConfig connConfig = ConnectionRegistry.get(connectionName);
//
//                String dbName = dbEntry.getKey();
//                String outputRootDir = projectConfiguration.getProjectRootFolder();
//
//                // 一个数据库一个代码生成器
//                FastAutoGenerator autoGenerator = FastAutoGenerator.create(connConfig.getConnectionUrl(dbName), connConfig.getUsername(), connConfig.getPassword());
//
//                autoGenerator.globalConfig(builder -> {
//                    builder.author(""); // 设置作者名 默认值:作者
//                    builder.disableOpenDir(); // 不打开生成目录
//                    // .enableSwagger() // 开启 swagger 模式
//                    // .enableSpringdoc()  // 开启 springdoc 模式  @Schema注解
//                    builder.dateType(DateType.TIME_PACK);  // 时间策略
//                    builder.commentDatePattern("yyyy-MM-dd HH:mm:ss");// 注释日期 默认值: yyyy-MM-dd
//                    builder.outputDir(outputRootDir); // 指定输出根目录 默认值: windows:D:// linux or mac : /tmp
//                });
//
//                // 包名配置
//                autoGenerator.packageConfig(builder -> {
//                    // 包配置(PackageConfig)
//                    String parentPackage = projectConfiguration.getParentPackage();
//                    builder.parentPackageName(parentPackage); // 设置父包名
//                    builder.moduleName(""); // 设置父包模块名
//                    builder.entity("entity");  // Entity 包名
//                    builder.service("service");  // Service 包名
//                    builder.serviceImpl("service.impl");  // Service 包名
//                    builder.controller("controller"); // Controller 包名
//
//                    String parentFolder = "/" + parentPackage.replace(".", "/");
//
//                    // 路径信息
//                    Map<OutputFile, String> pathInfoMap = new HashMap<>();
//                    pathInfoMap.put(OutputFile.CONTROLLER, outputRootDir + parentFolder + "/controller");
//                    pathInfoMap.put(OutputFile.MAPPER_XML, outputRootDir + parentFolder + "/mapping");
//                    pathInfoMap.put(OutputFile.ENTITY, outputRootDir + parentFolder + "/entity");
//                    pathInfoMap.put(OutputFile.SERVICE, outputRootDir + parentFolder + "/service");
//                    pathInfoMap.put(OutputFile.SERVICE_IMPL, outputRootDir + parentFolder + "/service/impl");
//                    builder.pathInfo(pathInfoMap); // 设置mapperXml生成路径
//                });
//                // 所有生成的表名
//                List<String> tableNames = dbEntry.getValue().stream().map(TableGeneration::getTableName).toList();
//                autoGenerator.strategyConfig(builder -> {
//                    // builder.enableSkipView(); // 开启大写命名
//                    // builder.enableSchema(); // 启用 schema
//                    builder.addTablePrefix("");
//                    builder.addTableSuffix("");
//                    builder.addInclude(tableNames); // 设置需要生成的表名
//                    // Entity策略配置
//                    EntityTemplateArguments.Builder entityBuilder = builder.entityBuilder();
//                    entityBuilder.enableLombok();  // 使用Lombok
//                    entityBuilder.enableTableFieldAnnotation(); // 字段添加TableField注解
//                    entityBuilder.mapperBuilder();
//                    entityBuilder.enableFileOverride();
//                    // Mapper配置
//                    MapperTemplateArguments.Builder mapperBuilder = builder.mapperBuilder();
//                    mapperBuilder.enableFileOverride();
//                    mapperBuilder.enableBaseResultMap(); // 生成默认的ResultMap标签
//                    // Controller 配置
//                    ControllerTemplateArguments.Builder controllerBuilder = builder.controllerBuilder();
//                    controllerBuilder.enableFileOverride();
//                    // Service配置
//                    ServiceTemplateArguments.Builder serviceBuilder = builder.serviceBuilder();
//                    serviceBuilder.enableFileOverride();
//                });
//                autoGenerator.templateEngine(engine); // 使用Freemarker引擎模板，默认的是Velocity引擎模板
//                autoGenerator.execute();
//            }
//        }
//
//        return list;
//    }
}
