package org.assistant.tools.mybatis;

import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.builder.annotation.ProviderSqlSource;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.defaults.RawSqlSource;
import org.apache.ibatis.scripting.xmltags.*;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.session.Configuration;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MyBatis 3.5.19 参数解析工具
 * 1. 解析为扁平 ParamNode 列表（展开形式：user.name）
 * 2. 工具方法转为树形 ParamNode 结构
 */
public class SqlNodeParamParser {

    // -------------------------- 常量定义 --------------------------
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("#\\{([^}]+)}|\\$\\{([^}]+)}");
    private static final Pattern OGNL_PARAM_PATTERN = Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)*)\\b");
    private static final Pattern JDBC_TYPE_PATTERN = Pattern.compile("jdbcType\\s*=\\s*([a-zA-Z0-9_]+)");
    private static final Set<String> OGNL_KEYWORDS = new HashSet<>(Arrays.asList(
            "null", "true", "false", "and", "or", "not", "eq", "ne", "gt", "lt", "ge", "le",
            "isEmpty", "isNotEmpty", "size", "length", "contains"
    ));

		public List<ParamNode> parseToFlatParamNodeList(SqlSource sqlSource) {
			return parseToFlatParamNodeList(sqlSource, new Configuration());
		}
	private List<ParamNode> parseToFlatParamNodeList(SqlSource sqlSource, Configuration configuration) {
		if (sqlSource instanceof StaticSqlSource sss) {
			String sql = (String) getFieldValue(sss, "sql");
			Map<String, ParamMeta> paramMetaMap = new LinkedHashMap<>();
			parsePlaceholderParams(sql, paramMetaMap, configuration);
			return convertParamNodes(paramMetaMap);
		} else if (sqlSource instanceof DynamicSqlSource dss) {
			SqlNode sqlNode = (SqlNode) getFieldValue(dss, "rootSqlNode");
			return parseToFlatParamNodeList(sqlNode, configuration);
		} else if (sqlSource instanceof RawSqlSource rss) {
			/**
			 * RawSqlSource 的参数是在解析阶段就会转为预编译占位符
			 * @see XMLScriptBuilder#parseScriptNode()
			 */
			SqlSource delegateSqlSource = (SqlSource) getFieldValue(rss, "sqlSource");
			return parseToFlatParamNodeList(delegateSqlSource, configuration);
		} else if (sqlSource instanceof ProviderSqlSource) {
			throw new IllegalArgumentException("ProviderSqlSource is not supported");
		}
		return new ArrayList<>();
	}

    // -------------------------- 第一步：解析为扁平 ParamNode 列表（展开形式） --------------------------
    /**
     * 解析 SqlNode 为扁平 ParamNode 列表（参数名为展开形式：如 user.name）
     * @param rootSqlNode 根 SqlNode
     * @param configuration MyBatis 配置（可选）
     * @return 扁平 ParamNode 列表（无嵌套，参数名含 . 分隔符）
     */
    public List<ParamNode> parseToFlatParamNodeList(SqlNode rootSqlNode, @Nullable Configuration configuration) {
        Map<String, ParamMeta> paramMetaMap = new LinkedHashMap<>();
        traverseSqlNode(rootSqlNode, paramMetaMap, configuration);
        // 转换为扁平 ParamNode 列表（参数名为展开形式：如 user.name）
				return convertParamNodes(paramMetaMap);
    }

		private List<ParamNode> convertParamNodes(Map<String, ParamMeta> paramMetaMap) {
			List<ParamNode> flatList = new ArrayList<>();
			for (Map.Entry<String, ParamMeta> entry : paramMetaMap.entrySet()) {
				String fullKey = entry.getKey();
				ParamMeta meta = entry.getValue();
				ParamNode node = new ParamNode(fullKey, meta.value, meta.dataType);
				node.setJdbcType(meta.jdbcType);
				node.setParameterMapping(meta.parameterMapping);
				flatList.add(node);
			}
			return flatList;
		}

    // -------------------------- 第二步：工具方法 - 扁平列表转树形结构 --------------------------
    /**
     * 将扁平 ParamNode 列表（展开形式）转为树形结构
     * @param flatParamNodes 扁平列表（参数名如 user.name、user.age、userId）
     * @return 树形结构的根 ParamNode 列表
     */
    public List<ParamNode> convertFlatListToTree(List<ParamNode> flatParamNodes) {
        // 缓存节点，避免重复创建（key：完整参数名，如 user / user.name）
        Map<String, ParamNode> nodeCache = new HashMap<>();
        // 根节点列表（无父节点的顶级参数，如 user、userId）
        List<ParamNode> rootNodes = new ArrayList<>();
        for (ParamNode flatNode : flatParamNodes) {
            String fullKey = flatNode.getKey();
            if (fullKey == null || fullKey.isEmpty()) {
                continue;
            }
            // 拆分参数名（如 user.name → ["user", "name"]）
            String[] keySegments = fullKey.split("\\.");
            ParamNode parentNode = null;
            // 逐层构建嵌套节点
            for (int i = 0; i < keySegments.length; i++) {
                String segment = keySegments[i];
                String currentFullKey = buildFullKey(keySegments, i);

                // 从缓存获取或创建节点
                ParamNode currentNode = nodeCache.get(currentFullKey);
                if (currentNode == null) {
                    // 叶子节点（最后一层）：复用扁平节点的所有属性
                    if (i == keySegments.length - 1) {
                        // currentNode = new ParamNode(currentFullKey, flatNode.getValue(), flatNode.getDataType());
                        currentNode = new ParamNode(keySegments[keySegments.length - 1], flatNode.getValue(), flatNode.getDataType());
                        currentNode.setJdbcType(flatNode.getJdbcType());
                        currentNode.setParameterMapping(flatNode.getParameterMapping());
                    }
                    // 非叶子节点：仅保留 key，其他属性为默认
                    else {
                        currentNode = new ParamNode(currentFullKey, null, ParamDataType.UNKNOWN.name());
                    }
                    nodeCache.put(currentFullKey, currentNode);

                    // 根节点加入最终列表，非根节点加入父节点
                    if (parentNode == null) {
                        rootNodes.add(currentNode);
                    } else {
                        parentNode.addChild(currentNode);
                    }
                }
                parentNode = currentNode;
            }
        }
        return rootNodes;
    }

    // -------------------------- 核心遍历 & 辅助方法 --------------------------
    private void traverseSqlNode(SqlNode sqlNode, Map<String, ParamMeta> paramMetaMap, @Nullable Configuration configuration) {
        if (sqlNode == null) return;

        try {
            if (sqlNode instanceof MixedSqlNode) {
							@SuppressWarnings("unchecked")
                List<SqlNode> children = (List<SqlNode>) getFieldValue(sqlNode, "contents");
                for (SqlNode child : children) {
                    traverseSqlNode(child, paramMetaMap, configuration);
                }
            } else if (sqlNode instanceof TextSqlNode || sqlNode instanceof StaticTextSqlNode) {
                String text = (String) getFieldValue(sqlNode, "text");
                parsePlaceholderParams(text, paramMetaMap, configuration);
            } else if (sqlNode instanceof IfSqlNode) {
                String test = (String) getFieldValue(sqlNode, "test");
                parseOgnlParams(test, paramMetaMap);
                traverseSqlNode((SqlNode) getFieldValue(sqlNode, "contents"), paramMetaMap, configuration);
            } else if (sqlNode instanceof ForEachSqlNode) {
                String collection = (String) getFieldValue(sqlNode, "collectionExpression");
                ParamMeta meta = new ParamMeta();
                meta.value = collection;
                meta.dataType = ParamDataType.COLLECTION.name();
							paramMetaMap.put(collection, meta);
							String itemExpression = (String) getFieldValue(sqlNode, "item");
							HashMap<String, ParamMeta> tmp = new HashMap<>();
							traverseSqlNode((SqlNode) getFieldValue(sqlNode, "contents"), tmp, configuration);

							List<ParamMeta> paramMetaList = new ArrayList<>();
							for (Map.Entry<String, ParamMeta> entry : tmp.entrySet()) {
								String key = entry.getKey();
								if (key.startsWith(itemExpression)) {
									paramMetaList.add(entry.getValue());
								}
							}
							if (!paramMetaList.isEmpty()) {
								for (ParamMeta paramMeta : paramMetaList) {
									ParamMeta paramMeta1 = new ParamMeta();
									if (paramMeta.value.contains(".")) {
										paramMeta1.value = collection + "[0]" + paramMeta.value.replace("item", "");
										paramMetaMap.put(paramMeta1.value, paramMeta1);

										paramMetaMap.remove(collection);
									} else {
										// 集合元素类型为简单类型
										paramMeta1.value = paramMeta.value.replace("item", "");
										meta.componentType = ParamDataType.STRING.name();
									}
								}
							}
            } else if (sqlNode instanceof VarDeclSqlNode) {
                String name = (String) getFieldValue(sqlNode, "name");
                String expression = (String) getFieldValue(sqlNode, "expression");
                parseOgnlParams(expression, paramMetaMap);
                ParamMeta meta = new ParamMeta();
                meta.value = name;
                meta.dataType = ParamDataType.STRING.name();
                paramMetaMap.put(name, meta);
            } else if (sqlNode instanceof WhereSqlNode || sqlNode instanceof TrimSqlNode || sqlNode instanceof SetSqlNode) {
                traverseSqlNode((SqlNode) getFieldValue(sqlNode, "contents"), paramMetaMap, configuration);
            } else if (sqlNode instanceof ChooseSqlNode) {
							@SuppressWarnings("unchecked")
                List<SqlNode> ifNodes = (List<SqlNode>) getFieldValue(sqlNode, "ifSqlNodes");
                SqlNode otherwiseNode = (SqlNode) getFieldValue(sqlNode, "defaultSqlNode");
                for (SqlNode ifNode : ifNodes) traverseSqlNode(ifNode, paramMetaMap, configuration);
                traverseSqlNode(otherwiseNode, paramMetaMap, configuration);
            }
        } catch (Exception e) {
            throw new RuntimeException("解析 SqlNode 失败：" + sqlNode.getClass().getName(), e);
        }
    }

    private void parsePlaceholderParams(String text, Map<String, ParamMeta> paramMetaMap, @Nullable Configuration configuration) {
        if (text == null || text.isEmpty()) return;
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
        while (matcher.find()) {
            String content = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            if (content == null || content.isEmpty()) continue;

            String[] parts = content.split(",");
            String paramKey = parts[0].trim();
            if (paramKey.isEmpty()) continue;

            ParamMeta meta = new ParamMeta();
            meta.value = paramKey;
            meta.jdbcType = extractJdbcType(content);
            meta.dataType = inferDataType(paramKey, text);
            if (configuration != null) {
                try {
                    meta.parameterMapping = new ParameterMapping.Builder(configuration, paramKey, Object.class).build();
                } catch (Exception e) {
									throw new RuntimeException(e);
								}
            }
            paramMetaMap.putIfAbsent(paramKey, meta);
        }
    }

    private void parseOgnlParams(String expression, Map<String, ParamMeta> paramMetaMap) {
        if (expression == null || expression.isEmpty()) return;
        Matcher matcher = OGNL_PARAM_PATTERN.matcher(expression);
        while (matcher.find()) {
            String paramKey = matcher.group(1);
            if (paramKey == null || OGNL_KEYWORDS.contains(paramKey)) continue;

            ParamMeta meta = new ParamMeta();
            meta.value = paramKey;
            meta.dataType = inferDataType(paramKey, expression);
            paramMetaMap.putIfAbsent(paramKey, meta);
        }
    }

    private Object getFieldValue(Object obj, String fieldName) {
        if (obj == null || fieldName == null) {
					return null;
				}
				try {
					Class<?> clazz = obj.getClass();
					Field field = null;
					while (clazz != null && field == null) {
						try {
							field = clazz.getDeclaredField(fieldName);
						} catch (NoSuchFieldException e) {
							clazz = clazz.getSuperclass();
						}
					}
					if (field == null) throw new NoSuchFieldException("字段不存在：" + fieldName);
					field.setAccessible(true);
					return field.get(obj);
				} catch (Throwable throwable) {
					throw new RuntimeException("获取字段值失败：" + fieldName, throwable);
				}
    }

    private String extractJdbcType(String content) {
        Matcher matcher = JDBC_TYPE_PATTERN.matcher(content);
        return matcher.find() ? matcher.group(1).trim() : null;
    }

    private String inferDataType(String paramKey, String context) {
        if (context.contains("foreach") || context.contains("collection") || context.contains("isEmpty()")) {
            return ParamDataType.COLLECTION.name();
        } else if (context.contains("!=") || context.contains("==") || context.contains("and") || context.contains("or")) {
            return ParamDataType.BOOLEAN.name();
        } else if (paramKey.contains(".")) {
            return ParamDataType.STRING.name();
        } else {
            return ParamDataType.STRING.name();
        }
    }

    private String buildFullKey(String[] segments, int endIndex) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= endIndex; i++) {
            if (i > 0) sb.append(".");
            sb.append(segments[i]);
        }
        return sb.toString();
    }

    // -------------------------- 内部类 & 枚举 --------------------------
    private static class ParamMeta {
			String property;
        String value;
        String jdbcType;
        String dataType;
        ParameterMapping parameterMapping;
				String componentType;
    }

    // -------------------------- 测试示例 --------------------------
    public static void main(String[] args) {
        // 1. 构建测试 SqlNode
        VarDeclSqlNode bindNode = new VarDeclSqlNode("likeName", "'%' + user.name + '%'");
        TextSqlNode textNode = new TextSqlNode("SELECT * FROM user WHERE id = #{userId,jdbcType=INTEGER} AND name LIKE #{likeName} AND age > #{user.age}");
        IfSqlNode ifNode = new IfSqlNode(textNode, "user.tags.isEmpty()");
        MixedSqlNode rootNode = new MixedSqlNode(Arrays.asList(bindNode, ifNode));

        // 2. 第一步：解析为扁平 ParamNode 列表（展开形式）
        SqlNodeParamParser parser = new SqlNodeParamParser();
        List<ParamNode> flatList = parser.parseToFlatParamNodeList(rootNode, null);
        System.out.println("===== 第一步：扁平 ParamNode 列表（展开形式） =====");
        for (ParamNode node : flatList) {
            System.out.printf("Key: %-10s | Value: %-10s | JDBCType: %-8s | DataType: %s%n",
                    node.getKey(), node.getValue(), node.getJdbcType(), node.getDataType());
        }

        // 3. 第二步：转换为树形结构
        List<ParamNode> treeNodes = parser.convertFlatListToTree(flatList);
        System.out.println("\n===== 第二步：树形 ParamNode 结构 =====");
        printParamTree(treeNodes, 0);
    }

    /**
     * 递归打印树形结构
     */
    private static void printParamTree(List<ParamNode> nodes, int level) {
        String indent = "  ".repeat(level);
        for (ParamNode node : nodes) {
            System.out.printf("%s├─ Key: %s%n", indent, node.getKey());
            System.out.printf("%s│  ├─ Value: %s%n", indent, node.getValue());
            System.out.printf("%s│  ├─ JDBCType: %s%n", indent, node.getJdbcType());
            System.out.printf("%s│  └─ DataType: %s%n", indent, node.getDataType());
            if (node.hasChildren()) {
                List<ParamNode> children = new ArrayList<>();
                for (int i = 0; i < node.getChildCount(); i++) {
                    children.add((ParamNode) node.getChildAt(i));
                }
                printParamTree(children, level + 1);
            }
        }
    }
}