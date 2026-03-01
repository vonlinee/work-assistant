package org.assistant.tools.doc;

import java.util.ArrayList;
import java.util.List;

/**
 * A group of related API endpoints, typically corresponding to one controller
 * class.
 */

public class ApiGroup {

    /** Group name (usually the controller class simple name) */
    private String name;

    /** Group description (from Javadoc) */
    private String description;

    /**
     * Base path shared by all endpoints in this group (e.g.
     * class-level @RequestMapping)
     */
    private String basePath;

    /** Full qualified class name of the controller */
    private String controllerClass;

    /** Endpoints in this group */
    private List<WebApiInfo> apis = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public String getControllerClass() {
        return controllerClass;
    }

    public void setControllerClass(String controllerClass) {
        this.controllerClass = controllerClass;
    }

    public List<WebApiInfo> getApis() {
        return apis;
    }

    public void setApis(List<WebApiInfo> apis) {
        this.apis = apis;
    }

    public void addApi(WebApiInfo api) {
        this.apis.add(api);
    }
}
