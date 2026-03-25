package ro.company.visionslot.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

final class JsonTestUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JsonTestUtils() {
    }

    static String extractJsonPath(String json, String jsonPath) throws Exception {
        JsonNode root = OBJECT_MAPPER.readTree(json);
        String normalizedPath = jsonPath.replace("$.", "");
        String[] nodes = normalizedPath.split("\\.");
        JsonNode current = root;
        for (String node : nodes) {
            current = current.get(node);
        }
        return current.asText();
    }
}
