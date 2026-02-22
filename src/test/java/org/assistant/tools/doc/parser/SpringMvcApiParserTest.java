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

class SpringMvcApiParserTest {

    private SpringMvcApiParser parser;
    private JavaParser javaParser;

    @BeforeEach
    void setUp() {
        parser = new SpringMvcApiParser();
        javaParser = new JavaParser();
    }

    @Test
    void testSupportsSpringController() {
        String source = """
                package com.example;
                import org.springframework.web.bind.annotation.RestController;

                @RestController
                public class UserController {
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
    void testParseGetMapping() {
        String source = """
                package com.example;
                import org.springframework.web.bind.annotation.*;

                @RestController
                @RequestMapping("/api/users")
                public class UserController {

                	/**
                	 * Get all users
                	 * @param page page number
                	 */
                	@GetMapping
                	public List<User> getUsers(@RequestParam int page) {
                		return null;
                	}
                }
                """;
        CompilationUnit cu = parse(source);
        List<ApiGroup> groups = parser.parse(cu);

        assertEquals(1, groups.size());
        ApiGroup group = groups.get(0);
        assertEquals("UserController", group.getName());
        assertEquals("/api/users", group.getBasePath());

        assertEquals(1, group.getApis().size());
        WebApiInfo api = group.getApis().get(0);
        assertEquals("GET", api.getMethod());
        assertEquals("/api/users", api.getPath());
        assertEquals("Get all users", api.getSummary());
        assertEquals("getUsers", api.getMethodName());

        assertEquals(1, api.getParams().size());
        ApiParam param = api.getParams().get(0);
        assertEquals("page", param.getName());
        assertEquals("int", param.getDataType());
        assertEquals(ParamLocation.QUERY, param.getIn());
        assertEquals("page number", param.getDescription());
    }

    @Test
    void testParsePostMappingWithRequestBody() {
        String source = """
                package com.example;
                import org.springframework.web.bind.annotation.*;

                @RestController
                @RequestMapping("/api")
                public class OrderController {

                	@PostMapping(value = "/orders", consumes = "application/json", produces = "application/json")
                	public Order createOrder(@RequestBody OrderDto dto) {
                		return null;
                	}
                }
                """;
        CompilationUnit cu = parse(source);
        List<ApiGroup> groups = parser.parse(cu);

        assertEquals(1, groups.size());
        WebApiInfo api = groups.get(0).getApis().get(0);
        assertEquals("POST", api.getMethod());
        assertEquals("/api/orders", api.getPath());
        assertEquals(List.of("application/json"), api.getConsumes());
        assertEquals(List.of("application/json"), api.getProduces());

        ApiParam bodyParam = api.getParams().get(0);
        assertEquals(ParamLocation.BODY, bodyParam.getIn());
        assertEquals("OrderDto", bodyParam.getDataType());
    }

    @Test
    void testParsePathVariable() {
        String source = """
                package com.example;
                import org.springframework.web.bind.annotation.*;

                @RestController
                @RequestMapping("/api/users")
                public class UserController {

                	@GetMapping("/{id}")
                	public User getUser(@PathVariable Long id) {
                		return null;
                	}

                	@DeleteMapping("/{id}")
                	public void deleteUser(@PathVariable("id") Long userId) {
                	}
                }
                """;
        CompilationUnit cu = parse(source);
        List<ApiGroup> groups = parser.parse(cu);

        assertEquals(1, groups.size());
        assertEquals(2, groups.get(0).getApis().size());

        WebApiInfo getApi = groups.get(0).getApis().get(0);
        assertEquals("GET", getApi.getMethod());
        assertEquals("/api/users/{id}", getApi.getPath());
        ApiParam pathParam = getApi.getParams().get(0);
        assertEquals(ParamLocation.PATH, pathParam.getIn());
        assertTrue(pathParam.isRequired());

        WebApiInfo deleteApi = groups.get(0).getApis().get(1);
        assertEquals("DELETE", deleteApi.getMethod());
        assertEquals("id", deleteApi.getParams().get(0).getName());
    }

    @Test
    void testParseDeprecatedEndpoint() {
        String source = """
                package com.example;
                import org.springframework.web.bind.annotation.*;

                @RestController
                public class LegacyController {

                	@Deprecated
                	@GetMapping("/old")
                	public String oldEndpoint() {
                		return "";
                	}
                }
                """;
        CompilationUnit cu = parse(source);
        List<ApiGroup> groups = parser.parse(cu);
        assertTrue(groups.get(0).getApis().get(0).isDeprecated());
    }

    @Test
    void testParseRequestMappingWithMethod() {
        String source = """
                package com.example;
                import org.springframework.web.bind.annotation.*;

                @RestController
                public class TestController {

                	@RequestMapping(value = "/test", method = RequestMethod.PUT)
                	public void update(@RequestBody String body) {
                	}
                }
                """;
        CompilationUnit cu = parse(source);
        List<ApiGroup> groups = parser.parse(cu);
        assertEquals("PUT", groups.get(0).getApis().get(0).getMethod());
    }

    private CompilationUnit parse(String source) {
        ParseResult<CompilationUnit> result = javaParser.parse(source);
        assertTrue(result.isSuccessful(), "Parse failed: " + result.getProblems());
        return result.getResult().orElseThrow();
    }
}
