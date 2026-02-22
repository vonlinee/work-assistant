package org.assistant.tools.doc.export;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.assistant.tools.doc.*;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

/**
 * Exports API definitions to OpenAPI specification format.
 * <p>
 * Supports multiple OpenAPI versions:
 * <ul>
 * <li>{@link OpenApiVersion#V3_0} — OpenAPI 3.0.3</li>
 * <li>{@link OpenApiVersion#V3_1} — OpenAPI 3.1.0</li>
 * <li>{@link OpenApiVersion#SWAGGER_2} — Swagger 2.0</li>
 * </ul>
 * Output format is YAML for OpenAPI 3.x and JSON for Swagger 2.0.
 * </p>
 */
public class OpenApiExporter implements ApiExporter {

    public enum OpenApiVersion {
        V3_0("OpenAPI 3.0", "yaml", "3.0.3"),
        V3_1("OpenAPI 3.1", "yaml", "3.1.0"),
        SWAGGER_2("Swagger 2.0", "json", "2.0");

        final String displayName;
        final String extension;
        final String specVersion;

        OpenApiVersion(String displayName, String extension, String specVersion) {
            this.displayName = displayName;
            this.extension = extension;
            this.specVersion = specVersion;
        }
    }

    private final OpenApiVersion version;

    public OpenApiExporter(OpenApiVersion version) {
        this.version = version;
    }

    @Override
    public String getFormatName() {
        return version.displayName;
    }

    @Override
    public String getFileExtension() {
        return version.extension;
    }

    @Override
    public void export(ApiProject project, File output) throws IOException {
        if (version == OpenApiVersion.SWAGGER_2) {
            exportSwagger2(project, output);
        } else {
            exportOpenApi3(project, output);
        }
    }

    // --- OpenAPI 3.x ---

    private void exportOpenApi3(ApiProject project, File output) throws IOException {
        Map<String, Object> spec = new LinkedHashMap<>();
        spec.put("openapi", version.specVersion);

        // Info
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("title", project.getProjectName() != null ? project.getProjectName() : "API");
        info.put("version", project.getVersion() != null ? project.getVersion() : "1.0.0");
        if (project.getDescription() != null) {
            info.put("description", project.getDescription());
        }
        spec.put("info", info);

        // Servers
        if (project.getBasePath() != null && !project.getBasePath().isEmpty()) {
            List<Map<String, Object>> servers = new ArrayList<>();
            Map<String, Object> server = new LinkedHashMap<>();
            server.put("url", project.getBasePath());
            servers.add(server);
            spec.put("servers", servers);
        }

        // Tags
        List<Map<String, String>> tags = new ArrayList<>();
        for (ApiGroup group : project.getGroups()) {
            Map<String, String> tag = new LinkedHashMap<>();
            tag.put("name", group.getName());
            if (group.getDescription() != null && !group.getDescription().isEmpty()) {
                tag.put("description", group.getDescription());
            }
            tags.add(tag);
        }
        if (!tags.isEmpty()) {
            spec.put("tags", tags);
        }

        // Paths
        Map<String, Object> paths = new LinkedHashMap<>();
        for (ApiGroup group : project.getGroups()) {
            for (WebApiInfo api : group.getApis()) {
                String path = api.getPath() != null ? api.getPath() : "/";
                paths.computeIfAbsent(path, k -> new LinkedHashMap<>());
                @SuppressWarnings("unchecked")
                Map<String, Object> pathItem = (Map<String, Object>) paths.get(path);

                Map<String, Object> operation = buildOpenApi3Operation(api, group.getName());
                pathItem.put(api.getMethod().toLowerCase(), operation);
            }
        }
        spec.put("paths", paths);

        // Write as YAML
        DumperOptions opts = new DumperOptions();
        opts.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        opts.setPrettyFlow(true);
        opts.setAllowUnicode(true);
        Yaml yaml = new Yaml(opts);
        String yamlStr = yaml.dump(spec);
        Files.writeString(output.toPath(), yamlStr, StandardCharsets.UTF_8);
    }

    private Map<String, Object> buildOpenApi3Operation(WebApiInfo api, String tag) {
        Map<String, Object> op = new LinkedHashMap<>();

        if (api.getSummary() != null)
            op.put("summary", api.getSummary());
        if (api.getDescription() != null)
            op.put("description", api.getDescription());
        op.put("tags", List.of(tag));
        if (api.isDeprecated())
            op.put("deprecated", true);
        if (api.getMethodName() != null)
            op.put("operationId", api.getMethodName());

        // Parameters (non-body)
        List<Map<String, Object>> parameters = new ArrayList<>();
        Map<String, Object> requestBody = null;

        for (ApiParam param : api.getParams()) {
            if (param.getIn() == ParamLocation.BODY) {
                // OpenAPI 3.x uses requestBody
                requestBody = new LinkedHashMap<>();
                Map<String, Object> content = new LinkedHashMap<>();
                String mediaType = !api.getConsumes().isEmpty() ? api.getConsumes().get(0) : "application/json";
                Map<String, Object> schema = param.hasFields()
                        ? buildSchemaFromFields(param.getFields())
                        : Map.of("type", javaTypeToOpenApiType(param.getDataType()));
                Map<String, Object> mediaContent = new LinkedHashMap<>();
                mediaContent.put("schema", schema);
                content.put(mediaType, mediaContent);
                requestBody.put("content", content);
                if (param.isRequired())
                    requestBody.put("required", true);
            } else if (param.getIn() != null) {
                Map<String, Object> p = new LinkedHashMap<>();
                p.put("name", param.getName());
                p.put("in", param.getIn().name().toLowerCase());
                if (param.isRequired())
                    p.put("required", true);
                if (param.getDescription() != null && !param.getDescription().isEmpty()) {
                    p.put("description", param.getDescription());
                }
                Map<String, Object> schema = new LinkedHashMap<>();
                schema.put("type", javaTypeToOpenApiType(param.getDataType()));
                p.put("schema", schema);
                if (param.getDefaultValue() != null) {
                    schema.put("default", param.getDefaultValue());
                }
                parameters.add(p);
            }
        }

        if (!parameters.isEmpty())
            op.put("parameters", parameters);
        if (requestBody != null)
            op.put("requestBody", requestBody);

        // Responses
        Map<String, Object> responses = new LinkedHashMap<>();
        Map<String, Object> resp200 = new LinkedHashMap<>();
        resp200.put("description", "Successful operation");
        if (api.getReturnType() != null && !"void".equals(api.getReturnType())) {
            String mediaType = !api.getProduces().isEmpty() ? api.getProduces().get(0) : "application/json";
            Map<String, Object> content = new LinkedHashMap<>();
            Map<String, Object> mediaContent = new LinkedHashMap<>();
            Map<String, Object> schema;
            if (api.getReturnTypeFields() != null && !api.getReturnTypeFields().isEmpty()) {
                schema = buildSchemaFromFields(api.getReturnTypeFields());
            } else {
                schema = new LinkedHashMap<>();
                schema.put("type", javaTypeToOpenApiType(api.getReturnType()));
            }
            mediaContent.put("schema", schema);
            content.put(mediaType, mediaContent);
            resp200.put("content", content);
        }
        responses.put("200", resp200);
        op.put("responses", responses);

        return op;
    }

    // --- Swagger 2.0 ---

    private void exportSwagger2(ApiProject project, File output) throws IOException {
        Map<String, Object> spec = new LinkedHashMap<>();
        spec.put("swagger", "2.0");

        Map<String, Object> info = new LinkedHashMap<>();
        info.put("title", project.getProjectName() != null ? project.getProjectName() : "API");
        info.put("version", project.getVersion() != null ? project.getVersion() : "1.0.0");
        if (project.getDescription() != null)
            info.put("description", project.getDescription());
        spec.put("info", info);

        if (project.getBasePath() != null) {
            spec.put("basePath", project.getBasePath());
        }

        // Tags
        List<Map<String, String>> tags = new ArrayList<>();
        for (ApiGroup group : project.getGroups()) {
            Map<String, String> tag = new LinkedHashMap<>();
            tag.put("name", group.getName());
            if (group.getDescription() != null)
                tag.put("description", group.getDescription());
            tags.add(tag);
        }
        if (!tags.isEmpty())
            spec.put("tags", tags);

        // Paths
        Map<String, Object> paths = new LinkedHashMap<>();
        for (ApiGroup group : project.getGroups()) {
            for (WebApiInfo api : group.getApis()) {
                String path = api.getPath() != null ? api.getPath() : "/";
                paths.computeIfAbsent(path, k -> new LinkedHashMap<>());
                @SuppressWarnings("unchecked")
                Map<String, Object> pathItem = (Map<String, Object>) paths.get(path);

                Map<String, Object> operation = buildSwagger2Operation(api, group.getName());
                pathItem.put(api.getMethod().toLowerCase(), operation);
            }
        }
        spec.put("paths", paths);

        // Write as JSON
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        Files.writeString(output.toPath(), gson.toJson(spec), StandardCharsets.UTF_8);
    }

    private Map<String, Object> buildSwagger2Operation(WebApiInfo api, String tag) {
        Map<String, Object> op = new LinkedHashMap<>();

        if (api.getSummary() != null)
            op.put("summary", api.getSummary());
        if (api.getDescription() != null)
            op.put("description", api.getDescription());
        op.put("tags", List.of(tag));
        if (api.isDeprecated())
            op.put("deprecated", true);
        if (api.getMethodName() != null)
            op.put("operationId", api.getMethodName());
        if (!api.getConsumes().isEmpty())
            op.put("consumes", api.getConsumes());
        if (!api.getProduces().isEmpty())
            op.put("produces", api.getProduces());

        // Parameters (Swagger 2.0 uses parameters for body too)
        List<Map<String, Object>> parameters = new ArrayList<>();
        for (ApiParam param : api.getParams()) {
            Map<String, Object> p = new LinkedHashMap<>();
            p.put("name", param.getName());
            String inVal = param.getIn() != null ? param.getIn().name().toLowerCase() : "query";
            p.put("in", inVal);
            if (param.isRequired())
                p.put("required", true);
            if (param.getDescription() != null && !param.getDescription().isEmpty()) {
                p.put("description", param.getDescription());
            }

            if ("body".equals(inVal)) {
                Map<String, Object> schema = param.hasFields()
                        ? buildSchemaFromFields(param.getFields())
                        : Map.of("type", javaTypeToOpenApiType(param.getDataType()));
                p.put("schema", schema);
            } else {
                p.put("type", javaTypeToOpenApiType(param.getDataType()));
                if (param.getDefaultValue() != null)
                    p.put("default", param.getDefaultValue());
            }

            parameters.add(p);
        }
        if (!parameters.isEmpty())
            op.put("parameters", parameters);

        // Responses
        Map<String, Object> responses = new LinkedHashMap<>();
        Map<String, Object> resp200 = new LinkedHashMap<>();
        resp200.put("description", "Successful operation");
        responses.put("200", resp200);
        op.put("responses", responses);

        return op;
    }

    // --- Type mapping ---

    private String javaTypeToOpenApiType(String javaType) {
        if (javaType == null)
            return "object";
        // Strip generics
        String base = javaType.contains("<") ? javaType.substring(0, javaType.indexOf('<')) : javaType;
        return switch (base.toLowerCase()) {
            case "string", "char", "character" -> "string";
            case "int", "integer", "long", "short", "byte" -> "integer";
            case "float", "double", "bigdecimal" -> "number";
            case "boolean" -> "boolean";
            case "list", "set", "collection", "arraylist", "linkedlist" -> "array";
            case "void" -> "object";
            default -> "object";
        };
    }

    /**
     * Build an OpenAPI schema object from resolved FieldInfo.
     * Generates properties, required arrays, and nested schemas recursively.
     */
    private Map<String, Object> buildSchemaFromFields(List<FieldInfo> fields) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new LinkedHashMap<>();
        List<String> required = new ArrayList<>();

        for (FieldInfo field : fields) {
            Map<String, Object> prop = new LinkedHashMap<>();
            if (field.hasChildren()) {
                prop = buildSchemaFromFields(field.getChildren());
            } else {
                prop.put("type", javaTypeToOpenApiType(field.getType()));
            }
            if (field.getDescription() != null && !field.getDescription().isEmpty()) {
                prop.put("description", field.getDescription());
            }
            if (field.getExample() != null && !field.getExample().isEmpty()) {
                prop.put("example", field.getExample());
            }
            if (field.getDefaultValue() != null && !field.getDefaultValue().isEmpty()) {
                prop.put("default", field.getDefaultValue());
            }
            if (field.isRequired()) {
                required.add(field.getName());
            }
            properties.put(field.getName(), prop);
        }

        schema.put("properties", properties);
        if (!required.isEmpty()) {
            schema.put("required", required);
        }
        return schema;
    }
}
