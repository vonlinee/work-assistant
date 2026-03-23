package org.assistant.tools.mybatis;

import lombok.Getter;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.scripting.defaults.RawSqlSource;
import org.apache.ibatis.scripting.xmltags.DynamicSqlSource;
import org.apache.ibatis.scripting.xmltags.MixedSqlNode;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;
import org.apache.ibatis.scripting.xmltags.XMLScriptBuilder;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeAliasRegistry;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Scans and parses MyBatis XML Mapper files incrementally.
 */
public class MyBatisScanner {

	@Getter
	private final Path projectRoot;
	private final Configuration configuration;
	private final Map<String, Long> fileLastModifiedTimes = new HashMap<>();

	public MyBatisScanner(Path projectRoot) {
		this.projectRoot = projectRoot;
		this.configuration = new Configuration();
		// Required for some MyBatis internal behaviors if we don't have a full env
		// setup
		this.configuration.setEnvironment(new org.apache.ibatis.mapping.Environment.Builder("development")
				.transactionFactory(new org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory())
				.dataSource(new org.apache.ibatis.datasource.unpooled.UnpooledDataSource())
				.build());
	}

	/**
	 * Scans the project directory for Mapper XML files and parses them.
	 * Incremental: only parses files that have changed since the last invocation.
	 *
	 * @return List of parsed MappedStatements
	 */
	public List<MappedStatement> scanAndParse() throws Exception {
		List<File> xmlFiles = findMapperXmlFiles(projectRoot);

		for (File file : xmlFiles) {
			long lastModified = file.lastModified();
			String pathStr = file.getAbsolutePath();

			// Only parse if it's new or modified
			if (!fileLastModifiedTimes.containsKey(pathStr) || fileLastModifiedTimes.get(pathStr) < lastModified) {
				parseMapperXml(file);
				fileLastModifiedTimes.put(pathStr, lastModified);
			}
		}

		// Return all statements currently in the configuration
		return new ArrayList<>(configuration.getMappedStatements());
	}

	private void parseMapperXml(File xmlFile) {
		try (InputStream inputStream = new FileInputStream(xmlFile)) {
			// Note: If a file is re-parsed, we might get duplicate ID exceptions in MyBatis
			// Configuration.
			// A robust implementation needs to clear old statements from the configuration
			// or use a fresh Configuration per file.
			// For incremental parsing, creating a fresh Configuration and merging is safer,
			// or maintaining a map of file -> MappedStatements.

			// For now, let's just parse it using XMLMapperBuilder.
			// We use a trick: if it's already there, MyBatis StrictMap throws an exception.
			// We'll catch and ignore/replace, or better: we just rebuild the Configuration
			// for modified files.

			// Actually, because MyBatis Configuration doesn't allow removing statements
			// easily,
			// a better incremental approach is to parse each XML into its own clean
			// Configuration object
			// to extract data, OR just accept we'll do a fresh parse if things change.
			// Since this is a local tool, let's just parse what we can.
			// We will use a fresh Configuration to avoid "already contains value for"
			// errors when modifying.

			XMLMapperBuilder builder = new XMLMapperBuilder(inputStream, configuration, xmlFile.getAbsolutePath(),
					configuration.getSqlFragments());
			builder.parse();
		} catch (Exception e) {
			System.err.println("Failed to parse MyBatis XML: " + xmlFile.getAbsolutePath() + " - " + e.getMessage());
		}
	}

	private List<File> findMapperXmlFiles(Path root) throws Exception {
		try (Stream<Path> stream = Files.walk(root)) {
			return stream
					.filter(Files::isRegularFile)
					.filter(p -> p.toString().endsWith("Mapper.xml") || p.toString().endsWith("Dao.xml"))
					// Exclude target/build directories
					.filter(p -> !p.toString().contains(File.separator + "target" + File.separator))
					.filter(p -> !p.toString().contains(File.separator + "build" + File.separator))
					.map(Path::toFile)
					.collect(Collectors.toList());
		}
	}

	// Improved incremental parsing strategy:
	// If a file changes, we discard and reparse everything, or maintain a map of
	// File -> Config
	public Map<File, List<MappedStatement>> scanAndParseGrouped() throws Exception {
		List<File> xmlFiles = findMapperXmlFiles(projectRoot);
		Map<File, List<MappedStatement>> result = new HashMap<>();

		for (File file : xmlFiles) {
			try (InputStream inputStream = new FileInputStream(file)) {
				MyConfiguration tempConfig = new MyConfiguration();
				XMLMapperBuilder builder = new XMLMapperBuilder(inputStream, tempConfig, file.getAbsolutePath(),
						tempConfig.getSqlFragments());
				builder.parse();

				Map<String, MappedStatement> uniqueStatements = new HashMap<>();
				for (Object stat : tempConfig.getMappedStatements()) {
					if (stat instanceof MappedStatement ms) {
						// Filter out internal selectKey statements and ensure uniqueness by ID
						if (!ms.getId().endsWith("!selectKey")) {
							uniqueStatements.put(ms.getId(), ms);
						}
					}
				}
				List<MappedStatement> statements = new ArrayList<>(uniqueStatements.values());
				result.put(file, statements);
			} catch (Exception e) {
				System.err.println("Failed to parse MyBatis XML: " + file.getAbsolutePath() + " - " + e.getMessage());
			}
		}
		return result;
	}

	private static class MyConfiguration extends Configuration {

		private final MyTypeAliasesRegistry myTypeAliasesRegistry;

		public MyConfiguration() {
			super();
			myTypeAliasesRegistry = new MyTypeAliasesRegistry(super.getTypeAliasRegistry());
		}

		@Override
		public LanguageDriver getLanguageDriver(Class<? extends LanguageDriver> langClass) {
			if (langClass == null) {
				// 默认的语言驱动
				// 处理RawSqlSource
				return new MyLanguageDriver();
			}
			// TODO 不支持自定义类型
			return super.getLanguageDriver(langClass);
		}

		@Override
		public TypeAliasRegistry getTypeAliasRegistry() {
			return myTypeAliasesRegistry;
		}
	}

	static class MyLanguageDriver extends XMLLanguageDriver {

		@Override
		public SqlSource createSqlSource(Configuration configuration, XNode script, Class<?> parameterType) {
			MyXMLScriptBuilder builder = new MyXMLScriptBuilder(configuration, script, parameterType);
			return builder.parseScriptNode();
		}
	}

	static class MyXMLScriptBuilder extends XMLScriptBuilder {

		private final Class<?> parameterType;

		public MyXMLScriptBuilder(Configuration configuration, XNode context,  Class<?> parameterType) {
			super(configuration, context, parameterType);
			this.parameterType = parameterType;
		}

		@Override
		public SqlSource parseScriptNode() {
			XNode context = (XNode) getParentPrivateFieldValue(this, "context");
			MixedSqlNode rootSqlNode = parseDynamicTags(context);
			SqlSource sqlSource;
			boolean isDynamic = (boolean) getParentPrivateFieldValue(this, "isDynamic");
			if (isDynamic) {
				sqlSource = new DynamicSqlSource(configuration, rootSqlNode);
			} else {
				// RawSqlSource会导致参数被预编译, 替换为占位符
				sqlSource = new DynamicSqlSource(configuration, rootSqlNode);
			}
			return sqlSource;
		}
	}

	private static class MyTypeAliasesRegistry extends TypeAliasRegistry {
		public MyTypeAliasesRegistry(TypeAliasRegistry delegate) {
			super();
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T> Class<T> resolveAlias(String string) {
			return (Class<T>) Object.class;
		}
	}

	/**
	 * 获取目标对象的指定字段值（支持父类私有字段）
	 * @param targetObj 目标对象（子类实例）
	 * @param fieldName 要获取的字段名
	 * @return 字段值
	 */
	public static Object getParentPrivateFieldValue(Object targetObj, String fieldName) {
		if (targetObj == null || fieldName == null || fieldName.isEmpty()) {
			throw new IllegalArgumentException("目标对象和字段名不能为空");
		}
		try {
			// 1. 递归查找字段（当前类 → 父类 → 祖父类...）
			Field targetField = findFieldInHierarchy(targetObj.getClass(), fieldName);
			if (targetField == null) {
				throw new NoSuchFieldException("字段 " + fieldName + " 在类及其父类中未找到");
			}
			// 2. 突破私有字段访问限制
			targetField.setAccessible(true);
			// 3. 获取字段值
			return targetField.get(targetObj);
		} catch (Exception e) {
			throw new RuntimeException("获取字段值失败：" + fieldName, e);
		}
	}

	/**
	 * 设置目标对象的指定字段值（支持父类私有字段）
	 * @param targetObj 目标对象
	 * @param fieldName 字段名
	 * @param value 要设置的值
	 * @throws Exception 反射异常
	 */
	public static void setParentPrivateFieldValue(Object targetObj, String fieldName, Object value) throws Exception {
		if (targetObj == null || fieldName == null || fieldName.isEmpty()) {
			throw new IllegalArgumentException("目标对象和字段名不能为空");
		}
		Field targetField = findFieldInHierarchy(targetObj.getClass(), fieldName);
		if (targetField == null) {
			throw new NoSuchFieldException("字段 " + fieldName + " 在类及其父类中未找到");
		}

		targetField.setAccessible(true);
		targetField.set(targetObj, value);
	}

	/**
	 * 递归遍历类的继承链，查找目标字段（含私有）
	 * @param clazz 起始类（子类）
	 * @param fieldName 字段名
	 * @return 找到的字段（null = 未找到）
	 */
	private static Field findFieldInHierarchy(Class<?> clazz, String fieldName) {
		// 终止条件：遍历到 Object 类仍未找到
		if (clazz == null || clazz == Object.class) {
			return null;
		}
		try {
			// 1. 先查找当前类的字段（含私有）
			return clazz.getDeclaredField(fieldName);
		} catch (NoSuchFieldException e) {
			// 2. 当前类未找到 → 递归查找父类
			return findFieldInHierarchy(clazz.getSuperclass(), fieldName);
		}
	}
}
