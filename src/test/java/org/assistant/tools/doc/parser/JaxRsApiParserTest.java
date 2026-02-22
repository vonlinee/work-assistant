package org.assistant.tools.doc.parser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import org.assistant.tools.doc.ApiGroup;
import org.assistant.tools.doc.ApiParam;
import org.assistant.tools.doc.ParamLocation;
import org.assistant.tools.doc.WebApiInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JaxRsApiParserTest {

    private JaxRsApiParser parser;
    private JavaParser javaParser;

    @BeforeEach
    void setUp() {
        parser = new JaxRsApiParser();
        javaParser = new JavaParser();
    }

    @Test
    void testSupportsJaxRsResource() {
        String source = """
                package com.example;
                import javax.ws.rs.Path;

                @Path("/users")
                public class UserResource {
                }
                """;
        CompilationUnit cu = parse(source);
        assertTrue(parser.supports(cu));
    }

    @Test
    void testDoesNotSupportPlainClass() {
        String source = """
                package com.example;
                public class UserService {
                }
                """;
        CompilationUnit cu = parse(source);
        assertFalse(parser.supports(cu));
    }

    @Test
    void testParseGetEndpoint() {
        String source = """
                package com.example;
                import javax.ws.rs.*;
                import javax.ws.rs.core.MediaType;

                @Path("/api/users")
                @Produces("application/json")
                public class UserResource {

                	/**
                	 * Get all users
                	 * @param page the page number
                	 */
                	@GET
                	public List<User> getUsers(@QueryParam("page") int page) {
                		return null;
                	}
                }
                """;
        CompilationUnit cu = parse(source);
        List<ApiGroup> groups = parser.parse(cu);

        assertEquals(1, groups.size());
        ApiGroup group = groups.get(0);
        assertEquals("UserResource", group.getName());
        assertEquals("/api/users", group.getBasePath());

        assertEquals(1, group.getApis().size());
        WebApiInfo api = group.getApis().get(0);
        assertEquals("GET", api.getMethod());
        assertEquals("/api/users", api.getPath());
        assertEquals("Get all users", api.getSummary());

        ApiParam param = api.getParams().get(0);
        assertEquals("page", param.getName());
        assertEquals(ParamLocation.QUERY, param.getIn());
    }

    @Test
    void testParsePostWithPathParam() {
        String source = """
                package com.example;
                import javax.ws.rs.*;

                @Path("/resources")
                public class MyResource {

                	@POST
                	@Path("/{id}/items")
                	@Consumes("application/json")
                	public void addItem(@PathParam("id") Long id, String body) {
                	}
                }
                """;
        CompilationUnit cu = parse(source);
        List<ApiGroup> groups = parser.parse(cu);

        WebApiInfo api = groups.get(0).getApis().get(0);
        assertEquals("POST", api.getMethod());
        assertEquals("/resources/{id}/items", api.getPath());
        assertEquals(List.of("application/json"), api.getConsumes());

        assertEquals(2, api.getParams().size());
        ApiParam pathParam = api.getParams().get(0);
        assertEquals("id", pathParam.getName());
        assertEquals(ParamLocation.PATH, pathParam.getIn());
        assertTrue(pathParam.isRequired());

        ApiParam bodyParam = api.getParams().get(1);
        assertEquals(ParamLocation.BODY, bodyParam.getIn());
    }

    @Test
    void testParseDefaultValue() {
        String source = """
                package com.example;
                import javax.ws.rs.*;

                @Path("/items")
                public class ItemResource {

                	@GET
                	public List<Item> list(@DefaultValue("0") @QueryParam("offset") int offset,
                	                       @DefaultValue("20") @QueryParam("limit") int limit) {
                		return null;
                	}
                }
                """;
        CompilationUnit cu = parse(source);
        List<ApiGroup> groups = parser.parse(cu);

        WebApiInfo api = groups.get(0).getApis().get(0);
        assertEquals(2, api.getParams().size());

        ApiParam offset = api.getParams().get(0);
        assertEquals("offset", offset.getName());
        assertEquals("0", offset.getDefaultValue());

        ApiParam limit = api.getParams().get(1);
        assertEquals("limit", limit.getName());
        assertEquals("20", limit.getDefaultValue());
    }

    @Test
    void testParseMultipleHttpMethods() {
        String source = """
                package com.example;
                import javax.ws.rs.*;

                @Path("/data")
                public class DataResource {

                	@GET
                	public String getData() { return ""; }

                	@PUT
                	public void updateData(String body) {}

                	@DELETE
                	@Path("/{id}")
                	public void deleteData(@PathParam("id") String id) {}
                }
                """;
        CompilationUnit cu = parse(source);
        List<ApiGroup> groups = parser.parse(cu);

        assertEquals(3, groups.get(0).getApis().size());
        assertEquals("GET", groups.get(0).getApis().get(0).getMethod());
        assertEquals("PUT", groups.get(0).getApis().get(1).getMethod());
        assertEquals("DELETE", groups.get(0).getApis().get(2).getMethod());
        assertEquals("/data/{id}", groups.get(0).getApis().get(2).getPath());
    }

    private CompilationUnit parse(String source) {
        ParseResult<CompilationUnit> result = javaParser.parse(source);
        assertTrue(result.isSuccessful(), "Parse failed: " + result.getProblems());
        return result.getResult().orElseThrow();
    }
}
