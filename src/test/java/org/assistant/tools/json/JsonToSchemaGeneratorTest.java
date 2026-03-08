package org.assistant.tools.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonToSchemaGeneratorTest {

    @Test
    void testBasicSchemaGeneration() {
        String jsonPayload = """
                {
                  "name": "Jane",
                  "age": 30,
                  "isActive": true,
                  "roles": ["admin", "user"],
                  "address": {
                    "city": "New York",
                    "zip": "10001"
                  }
                }
                """;

        JsonElement element = JsonParser.parseString(jsonPayload);

        JsonToSchemaGenerator generator = new JsonToSchemaGenerator(
                "UserSchema",
                "http://example.com/user-schema.json",
                true,
                false);

        String schemaJson = generator.generate(element);

        // Basic assertions to ensure schema properties are present
        assertTrue(schemaJson.contains("\"$schema\": \"http://json-schema.org/draft-07/schema#\""));
        assertTrue(schemaJson.contains("\"$id\": \"http://example.com/user-schema.json\""));
        assertTrue(schemaJson.contains("\"title\": \"UserSchema\""));

        // Assert inferred types
        assertTrue(schemaJson.contains("\"type\": \"object\""));
        assertTrue(schemaJson.contains("\"type\": \"string\""));
        assertTrue(schemaJson.contains("\"type\": \"integer\""));
        assertTrue(schemaJson.contains("\"type\": \"boolean\""));
        assertTrue(schemaJson.contains("\"type\": \"array\""));

        // Assert required
        assertTrue(schemaJson.contains("\"name\""));
        assertTrue(schemaJson.contains("\"required\""));
        assertTrue(schemaJson.contains("\"additionalProperties\": false"));
    }
}
