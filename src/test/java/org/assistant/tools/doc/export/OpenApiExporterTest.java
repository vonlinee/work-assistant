package org.assistant.tools.doc.export;

import org.assistant.tools.doc.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class OpenApiExporterTest {

    @TempDir
    Path tempDir;

    @Test
    void testExportOpenApi30() throws IOException {
        ApiProject project = buildSampleProject();
        File output = tempDir.resolve("openapi30.yaml").toFile();

        OpenApiExporter exporter = new OpenApiExporter(OpenApiExporter.OpenApiVersion.V3_0);
        assertEquals("OpenAPI 3.0", exporter.getFormatName());
        assertEquals("yaml", exporter.getFileExtension());

        exporter.export(project, output);

        String content = Files.readString(output.toPath());
        assertFalse(content.isEmpty());

        // Parse and verify structure
        Yaml yaml = new Yaml();
        @SuppressWarnings("unchecked")
        Map<String, Object> spec = yaml.load(content);

        assertEquals("3.0.3", spec.get("openapi"));

        @SuppressWarnings("unchecked")
        Map<String, Object> info = (Map<String, Object>) spec.get("info");
        assertEquals("Test Project", info.get("title"));
        assertEquals("1.0.0", info.get("version"));

        @SuppressWarnings("unchecked")
        Map<String, Object> paths = (Map<String, Object>) spec.get("paths");
        assertTrue(paths.containsKey("/api/users"));
        assertTrue(paths.containsKey("/api/users/{id}"));

        // Check GET /api/users
        @SuppressWarnings("unchecked")
        Map<String, Object> usersPath = (Map<String, Object>) paths.get("/api/users");
        assertTrue(usersPath.containsKey("get"));

        @SuppressWarnings("unchecked")
        Map<String, Object> getOp = (Map<String, Object>) usersPath.get("get");
        assertEquals("List all users", getOp.get("summary"));
        assertNotNull(getOp.get("parameters"));
    }

    @Test
    void testExportOpenApi31() throws IOException {
        ApiProject project = buildSampleProject();
        File output = tempDir.resolve("openapi31.yaml").toFile();

        OpenApiExporter exporter = new OpenApiExporter(OpenApiExporter.OpenApiVersion.V3_1);
        exporter.export(project, output);

        Yaml yaml = new Yaml();
        @SuppressWarnings("unchecked")
        Map<String, Object> spec = yaml.load(Files.readString(output.toPath()));
        assertEquals("3.1.0", spec.get("openapi"));
    }

    @Test
    void testExportSwagger20() throws IOException {
        ApiProject project = buildSampleProject();
        File output = tempDir.resolve("swagger2.json").toFile();

        OpenApiExporter exporter = new OpenApiExporter(OpenApiExporter.OpenApiVersion.SWAGGER_2);
        assertEquals("json", exporter.getFileExtension());
        exporter.export(project, output);

        String content = Files.readString(output.toPath());
        assertTrue(content.contains("\"swagger\": \"2.0\""));
        assertTrue(content.contains("/api/users"));
    }

    private ApiProject buildSampleProject() {
        ApiProject project = new ApiProject();
        project.setProjectName("Test Project");
        project.setVersion("1.0.0");
        project.setDescription("A test project");

        ApiGroup group = new ApiGroup();
        group.setName("UserController");
        group.setBasePath("/api/users");
        group.setControllerClass("com.example.UserController");

        // GET /api/users
        WebApiInfo getAll = new WebApiInfo();
        getAll.setMethod("GET");
        getAll.setPath("/api/users");
        getAll.setSummary("List all users");
        getAll.setReturnType("List<User>");
        getAll.setMethodName("listUsers");
        getAll.setControllerClass("com.example.UserController");
        getAll.setProduces(List.of("application/json"));

        ApiParam pageParam = new ApiParam();
        pageParam.setName("page");
        pageParam.setDataType("Integer");
        pageParam.setIn(ParamLocation.QUERY);
        pageParam.setDefaultValue("0");
        getAll.addParam(pageParam);

        group.addApi(getAll);

        // GET /api/users/{id}
        WebApiInfo getOne = new WebApiInfo();
        getOne.setMethod("GET");
        getOne.setPath("/api/users/{id}");
        getOne.setSummary("Get user by ID");
        getOne.setReturnType("User");
        getOne.setMethodName("getUser");

        ApiParam idParam = new ApiParam();
        idParam.setName("id");
        idParam.setDataType("Long");
        idParam.setIn(ParamLocation.PATH);
        idParam.setRequired(true);
        getOne.addParam(idParam);

        group.addApi(getOne);

        project.addGroup(group);
        return project;
    }
}
