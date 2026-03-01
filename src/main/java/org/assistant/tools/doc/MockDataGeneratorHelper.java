package org.assistant.tools.doc;

import java.util.List;

public class MockDataGeneratorHelper {

    public String generateMockUrl(WebApiInfo api) {
        return MockDataGenerator.generateMockUrl(api);
    }

    public String generateMockHeaders(List<ApiParam> params) {
        return MockDataGenerator.generateMockHeaders(params);
    }

    public String generateMockRequest(List<ApiParam> params) {
        return MockDataGenerator.generateMockRequest(params);
    }

    public String generateMockResponse(List<FieldInfo> returnTypeFields, String returnType) {
        return MockDataGenerator.generateMockResponse(returnTypeFields, returnType);
    }
}
