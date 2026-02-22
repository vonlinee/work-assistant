package org.assistant.tools.doc.parser;

import org.assistant.tools.doc.FieldInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TypeResolverTest {

    @TempDir
    Path tempDir;

    private TypeResolver resolver;

    @BeforeEach
    void setUp() throws IOException {
        // Create a mock source tree with DTO classes
        Path srcRoot = tempDir.resolve("src/main/java/com/example/dto");
        Files.createDirectories(srcRoot);

        // UserDto.java
        Files.writeString(srcRoot.resolve("UserDto.java"), """
                package com.example.dto;

                import javax.validation.constraints.NotNull;
                import javax.validation.constraints.NotBlank;

                /**
                 * User data transfer object.
                 */
                public class UserDto {

                	/** User unique identifier */
                	@NotNull
                	private Long id;

                	/** User display name */
                	@NotBlank
                	private String name;

                	/** User email address */
                	private String email;

                	// Age of the user
                	private int age;

                	/** Home address */
                	private Address address;

                	private static final long serialVersionUID = 1L;
                }
                """);

        // Address.java
        Files.writeString(srcRoot.resolve("Address.java"), """
                package com.example.dto;

                public class Address {

                	/** Street name and number */
                	private String street;

                	/** City name */
                	private String city;

                	/** ZIP or postal code */
                	private String zipCode;
                }
                """);

        // OrderDto.java with @JsonProperty and @Schema
        Files.writeString(srcRoot.resolve("OrderDto.java"), """
                package com.example.dto;

                import com.fasterxml.jackson.annotation.JsonProperty;
                import io.swagger.v3.oas.annotations.media.Schema;

                public class OrderDto {

                	@JsonProperty("order_id")
                	private Long orderId;

                	@Schema(description = "Total price in cents", example = "1999")
                	private Integer totalPrice;

                	private String status = "PENDING";
                }
                """);

        // Status.java (enum — should not be resolved)
        Files.writeString(srcRoot.resolve("Status.java"), """
                package com.example.dto;

                public enum Status {
                	ACTIVE, INACTIVE;
                }
                """);

        resolver = new TypeResolver(List.of(tempDir.resolve("src/main/java")));
    }

    @Test
    void testResolveSimpleTypes() {
        assertTrue(resolver.resolve("String").isEmpty());
        assertTrue(resolver.resolve("int").isEmpty());
        assertTrue(resolver.resolve("Long").isEmpty());
        assertTrue(resolver.resolve("Boolean").isEmpty());
        assertTrue(resolver.resolve("Date").isEmpty());
    }

    @Test
    void testResolveUserDto() {
        List<FieldInfo> fields = resolver.resolve("UserDto");

        assertFalse(fields.isEmpty());
        assertEquals(5, fields.size()); // id, name, email, age, address (static excluded)

        // Check id field
        FieldInfo idField = fields.stream().filter(f -> "id".equals(f.getName())).findFirst().orElse(null);
        assertNotNull(idField);
        assertEquals("Long", idField.getType());
        assertEquals("User unique identifier", idField.getDescription());
        assertTrue(idField.isRequired()); // @NotNull

        // Check name field
        FieldInfo nameField = fields.stream().filter(f -> "name".equals(f.getName())).findFirst().orElse(null);
        assertNotNull(nameField);
        assertEquals("String", nameField.getType());
        assertTrue(nameField.isRequired()); // @NotBlank

        // Check email field
        FieldInfo emailField = fields.stream().filter(f -> "email".equals(f.getName())).findFirst().orElse(null);
        assertNotNull(emailField);
        assertFalse(emailField.isRequired());

        // Check age field (comment-based description)
        FieldInfo ageField = fields.stream().filter(f -> "age".equals(f.getName())).findFirst().orElse(null);
        assertNotNull(ageField);
        assertEquals("int", ageField.getType());
        assertEquals("Age of the user", ageField.getDescription());
    }

    @Test
    void testResolveNestedType() {
        List<FieldInfo> fields = resolver.resolve("UserDto");

        FieldInfo addressField = fields.stream().filter(f -> "address".equals(f.getName())).findFirst().orElse(null);
        assertNotNull(addressField);
        assertEquals("Address", addressField.getType());
        assertTrue(addressField.hasChildren());
        assertEquals(3, addressField.getChildren().size()); // street, city, zipCode

        FieldInfo streetField = addressField.getChildren().stream()
                .filter(f -> "street".equals(f.getName())).findFirst().orElse(null);
        assertNotNull(streetField);
        assertEquals("String", streetField.getType());
        assertEquals("Street name and number", streetField.getDescription());
    }

    @Test
    void testResolveWithAnnotations() {
        List<FieldInfo> fields = resolver.resolve("OrderDto");
        assertFalse(fields.isEmpty());

        // @JsonProperty renames field
        FieldInfo orderIdField = fields.stream()
                .filter(f -> "order_id".equals(f.getName())).findFirst().orElse(null);
        assertNotNull(orderIdField, "Should use @JsonProperty name 'order_id'");
        assertEquals("Long", orderIdField.getType());

        // @Schema provides description and example
        FieldInfo priceField = fields.stream()
                .filter(f -> "totalPrice".equals(f.getName())).findFirst().orElse(null);
        assertNotNull(priceField);
        assertEquals("Total price in cents", priceField.getDescription());
        assertEquals("1999", priceField.getExample());

        // Default value from initializer
        FieldInfo statusField = fields.stream()
                .filter(f -> "status".equals(f.getName())).findFirst().orElse(null);
        assertNotNull(statusField);
        assertEquals("PENDING", statusField.getDefaultValue());
    }

    @Test
    void testResolveGenericWrapper() {
        // List<UserDto> should unwrap to UserDto
        List<FieldInfo> fields = resolver.resolve("List<UserDto>");
        assertFalse(fields.isEmpty());
        assertEquals(5, fields.size());
    }

    @Test
    void testResolveEnum() {
        // Enums should not be resolved
        List<FieldInfo> fields = resolver.resolve("Status");
        assertTrue(fields.isEmpty());
    }

    @Test
    void testResolveCaching() {
        // Second call should return cached result
        List<FieldInfo> first = resolver.resolve("UserDto");
        List<FieldInfo> second = resolver.resolve("UserDto");
        assertSame(first, second);
    }

    @Test
    void testResolveUnknownType() {
        List<FieldInfo> fields = resolver.resolve("NonExistentClass");
        assertTrue(fields.isEmpty());
    }
}
