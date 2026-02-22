package org.assistant.tools.doc.export;

import org.assistant.tools.doc.ApiGroup;
import org.assistant.tools.doc.ApiParam;
import org.assistant.tools.doc.ApiProject;
import org.assistant.tools.doc.WebApiInfo;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

/**
 * Exports API definitions to a plain YAML file (not OpenAPI spec).
 */
public class YamlExporter implements ApiExporter {

    @Override
    public String getFormatName() {
        return "YAML";
    }

    @Override
    public String getFileExtension() {
        return "yaml";
    }

    @Override
    public void export(ApiProject project, File output) throws IOException {
        DumperOptions opts = new DumperOptions();
        opts.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        opts.setPrettyFlow(true);
        opts.setAllowUnicode(true);
        Yaml yaml = new Yaml(opts);

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("projectName", project.getProjectName());
        root.put("version", project.getVersion());
        root.put("description", project.getDescription());
        root.put("basePath", project.getBasePath());

        List<Map<String, Object>> groupList = new ArrayList<>();
        for (ApiGroup group : project.getGroups()) {
            Map<String, Object> gMap = new LinkedHashMap<>();
            gMap.put("name", group.getName());
            gMap.put("basePath", group.getBasePath());
            gMap.put("description", group.getDescription());
            gMap.put("controllerClass", group.getControllerClass());

            List<Map<String, Object>> apiList = new ArrayList<>();
            for (WebApiInfo api : group.getApis()) {
                apiList.add(apiToMap(api));
            }
            gMap.put("apis", apiList);
            groupList.add(gMap);
        }
        root.put("groups", groupList);

        String yamlStr = yaml.dump(root);
        Files.writeString(output.toPath(), yamlStr, StandardCharsets.UTF_8);
    }

    private Map<String, Object> apiToMap(WebApiInfo api) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("method", api.getMethod());
        map.put("path", api.getPath());
        map.put("summary", api.getSummary());
        map.put("description", api.getDescription());
        map.put("returnType", api.getReturnType());
        map.put("deprecated", api.isDeprecated());
        if (!api.getConsumes().isEmpty())
            map.put("consumes", api.getConsumes());
        if (!api.getProduces().isEmpty())
            map.put("produces", api.getProduces());

        if (!api.getParams().isEmpty()) {
            List<Map<String, Object>> params = new ArrayList<>();
            for (ApiParam p : api.getParams()) {
                Map<String, Object> pm = new LinkedHashMap<>();
                pm.put("name", p.getName());
                pm.put("dataType", p.getDataType());
                pm.put("in", p.getIn() != null ? p.getIn().name().toLowerCase() : "query");
                pm.put("required", p.isRequired());
                if (p.getDefaultValue() != null)
                    pm.put("defaultValue", p.getDefaultValue());
                if (p.getDescription() != null && !p.getDescription().isEmpty())
                    pm.put("description", p.getDescription());
                params.add(pm);
            }
            map.put("params", params);
        }
        return map;
    }
}
