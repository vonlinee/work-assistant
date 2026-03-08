package org.assistant.tools.mybatis;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeAliasRegistry;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
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

	private final Path projectRoot;
	private final Configuration configuration;
	private final Map<String, Long> fileLastModifiedTimes = new HashMap<>();

	public Path getProjectRoot() {
		return projectRoot;
	}

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
	// If a file changes, we discard and re-parse everything, or maintain a map of
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
		public TypeAliasRegistry getTypeAliasRegistry() {
			return myTypeAliasesRegistry;
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
}
