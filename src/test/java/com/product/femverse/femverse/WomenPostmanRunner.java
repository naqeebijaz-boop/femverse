package com.product.femverse.femverse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static io.restassured.RestAssured.given;

public class WomenPostmanRunner {

    private static final String COLLECTION_PATH = "src/test/resources/women.json";
    private static final Map<String, String> variables = new HashMap<>();
    public static final List<Map<String, Object>> testResults = new ArrayList<>();

    // Credentials
    private static final String EMAIL = "naqeeb.ijaz@imaginationai.net";
    private static final String PASSWORD = "12345678";
    private static final String DEVICE_ID = "abcd";
    private static final String LOGIN_URL = "https://superwoman.trippleapps.com:8443/api/v1/auth/signin";

    @Test
    public void runWomenCollection() throws IOException {
        // Clear old variables
        variables.clear();

        // 1️⃣ Fetch fresh token
        fetchAccessToken();

        // 2️⃣ Load JSON collection
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(new File(COLLECTION_PATH));

        // 3️⃣ Load collection-level variables (ignore token value in JSON)
        if (root.has("variable")) {
            for (JsonNode varNode : root.get("variable")) {
                String key = varNode.get("key").asText();
                String value = varNode.get("value").asText();
                if (key.equalsIgnoreCase("token")) continue; // skip static token
                variables.put("{{" + key + "}}", value);
            }
        }

        // 4️⃣ Process all items in the collection
        if (root.has("item")) {
            for (JsonNode item : root.get("item")) {
                processItems(item);
            }
        }

        // 5️⃣ Sort results: PASSED first, FAILED later
        testResults.sort(Comparator.comparing(result -> result.get("status").equals("FAILED")));
    }

    private void fetchAccessToken() {
        RestAssured.useRelaxedHTTPSValidation();

        String loginBody = "{\n" +
                "  \"device_id\": \"" + DEVICE_ID + "\",\n" +
                "  \"email\": \"" + EMAIL + "\",\n" +
                "  \"password\": \"" + PASSWORD + "\"\n" +
                "}";

        Response response = given()
                .header("Content-Type", "application/json")
                .header("accept", "application/json")
                .body(loginBody)
                .post(LOGIN_URL);

        System.out.println("Login Status: " + response.getStatusCode());
        System.out.println("Login Body: " + response.asString());

        if (response.getStatusCode() == 200) {
            String accessToken = response.jsonPath().getString("data.access_token").trim();
            String refreshToken = response.jsonPath().getString("data.refresh_token").trim();

            // Save token in variables for JSON replacement
            variables.put("{{token}}", accessToken);
            variables.put("{{refresh_token}}", refreshToken);

            System.out.println("✅ Fetched access token successfully");
        } else {
            throw new RuntimeException("Failed to login: " + response.asString());
        }
    }

    private void processItems(JsonNode itemNode) {
        if (itemNode.has("item")) {
            for (JsonNode subItem : itemNode.get("item")) processItems(subItem);
            return;
        }

        String testName = itemNode.has("name") ? itemNode.get("name").asText() : "Unnamed Test";

        // Skip login items from JSON collection
        if (testName.equalsIgnoreCase("SignUp") || testName.equalsIgnoreCase("SignIn")) {
            System.out.println("➡️ Skipping: " + testName + " (login handled programmatically)");
            return;
        }

        if (!itemNode.has("request")) return;

        JsonNode request = itemNode.get("request");
        String method = request.has("method") ? request.get("method").asText() : "GET";
        String url = extractUrl(request.get("url"));
        Map<String, String> headers = extractHeaders(request.get("header"));
        String body = extractBody(request.get("body"));

        System.out.println("\n➡️ Running: " + testName);
        System.out.println("   Method: " + method);
        System.out.println("   URL: " + url);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("testName", testName);
        resultMap.put("method", method);
        resultMap.put("url", url);

        try {
            Response response = sendRequest(method, url, headers, body);
            int statusCode = response.getStatusCode();
            String responseBody = response.asString();

            resultMap.put("statusCode", statusCode);
            resultMap.put("response", responseBody);

            if (statusCode >= 200 && statusCode < 300) {
                System.out.println("✅ PASSED | Status Code: " + statusCode);
                resultMap.put("status", "PASSED");
            } else {
                System.out.println("❌ FAILED | Status Code: " + statusCode + " | Response: " + responseBody);
                resultMap.put("status", "FAILED");
            }

        } catch (Exception e) {
            System.out.println("❌ FAILED | Exception: " + e.getClass().getSimpleName() + " | " + e.getMessage());
            resultMap.put("status", "FAILED");
            resultMap.put("statusCode", 500);
            resultMap.put("response", e.getClass().getSimpleName() + ": " + e.getMessage());
        }

        testResults.add(resultMap);
    }

    private String extractUrl(JsonNode urlNode) {
        if (urlNode == null) return "";
        String rawUrl = urlNode.has("raw") ? urlNode.get("raw").asText() : "";
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            rawUrl = rawUrl.replace(entry.getKey(), entry.getValue());
        }
        return rawUrl;
    }

    private Map<String, String> extractHeaders(JsonNode headerNode) {
        Map<String, String> headers = new HashMap<>();
        if (headerNode != null && headerNode.isArray()) {
            for (JsonNode header : headerNode) {
                String key = header.get("key").asText();
                String value = header.get("value").asText();
                for (Map.Entry<String, String> entry : variables.entrySet()) {
                    value = value.replace(entry.getKey(), entry.getValue());
                }
                headers.put(key, value);
            }
        }

        // Always ensure Authorization uses fresh token
        headers.put("Authorization", "Bearer " + variables.get("{{token}}"));

        return headers;
    }

    private String extractBody(JsonNode bodyNode) {
        if (bodyNode != null && bodyNode.has("raw")) {
            String rawBody = bodyNode.get("raw").asText();
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                rawBody = rawBody.replace(entry.getKey(), entry.getValue());
            }
            return rawBody;
        }
        return null;
    }

    private Response sendRequest(String method, String url, Map<String, String> headers, String body) {
        RestAssured.useRelaxedHTTPSValidation();

        io.restassured.specification.RequestSpecification request = given()
                .headers(headers)
                .config(RestAssured.config().httpClient(HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", 10000)           // 10 sec connect timeout
                        .setParam("http.socket.timeout", 10000)              // 10 sec read timeout
                        .setParam("http.connection-manager.timeout", 10000)  // 10 sec manager timeout
                ));

        if (body != null && !body.isEmpty()) request.body(body);

        return switch (method.toUpperCase()) {
            case "POST" -> request.post(url);
            case "PUT" -> request.put(url);
            case "PATCH" -> request.patch(url);
          //  case "DELETE" -> request.delete(url);
            default -> request.get(url);
        };
    }
}
