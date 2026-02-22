package org.assistant.tools.doc;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * A group of related API endpoints, typically corresponding to one controller
 * class.
 */
@Data
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

    public void addApi(WebApiInfo api) {
        this.apis.add(api);
    }
}
