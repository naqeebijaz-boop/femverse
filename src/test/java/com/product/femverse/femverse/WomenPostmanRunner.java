package com.product.femverse.femverse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static io.restassured.RestAssured.given;

public class WomenPostmanRunner {

    private static final String COLLECTION_PATH = "src/test/resources/women.json"; 
    private static final Map<String, String> variables = new HashMap<>();

    // Store results for listener
    public static final List<Map<String, Object>> testResults = new ArrayList<>();

    // Credentials for login
    private static final String EMAIL = "naqeeb.ijaz@imaginationai.net";
    private static final String PASSWORD = "12345678";
    private static final String DEVICE_ID = "mobile-device-id";
    private static final String LOGIN_URL = "https://superwoman.trippleapps.com:8443/api/v1/auth/signin";

    @Test
    public void runWomenCollection() throws IOException {
        // 1️⃣ First, login and get token
        fetchAccessToken();

        // 2️⃣ Load JSON collection
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(new File(COLLECTION_PATH));

        if (root.has("variable")) {
            for (JsonNode varNode : root.get("variable")) {
                String key = varNode.get("key").asText();
                String value = varNode.get("value").asText();
                variables.put("{{" + key + "}}", value);
            }
        }

        if (root.has("item")) {
            for (JsonNode item : root.get("item")) {
                processItems(item);
            }
        }
    }

    private void fetchAccessToken() {
        RestAssured.useRelaxedHTTPSValidation();
        Map<String, String> loginBody = new HashMap<>();
        loginBody.put("email", EMAIL);
        loginBody.put("password", PASSWORD);
        loginBody.put("device_id", DEVICE_ID);

        Response response = given()
                .header("Content-Type", "application/json")
                .header("accept", "application/json")
                .body(loginBody)
                .post(LOGIN_URL);

        if (response.getStatusCode() == 200) {
            String accessToken = response.jsonPath().getString("data.access_token");
            String refreshToken = response.jsonPath().getString("data.refresh_token");

            variables.put("{{token}}", accessToken);
            variables.put("{{refresh_token}}", refreshToken);

            System.out.println("✅ Fetched access token successfully");
        } else {
            throw new RuntimeException("Failed to login: " + response.asString());
        }
    }

    private void processItems(JsonNode itemNode) {
        if (itemNode.has("item")) {
            for (JsonNode subItem : itemNode.get("item")) {
                processItems(subItem);
            }
        } else if (itemNode.has("request")) {
            JsonNode request = itemNode.get("request");

            String method = request.has("method") ? request.get("method").asText() : "GET";
            String url = extractUrl(request.get("url"));
            Map<String, String> headers = extractHeaders(request.get("header"));
            String body = extractBody(request.get("body"));
            String testName = itemNode.get("name").asText();

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

                if (statusCode == 200) {
                    System.out.println("✅ PASSED | Status Code: " + statusCode);
                    resultMap.put("status", "PASSED");
                } else {
                    System.out.println("❌ FAILED | Status Code: " + statusCode);
                    resultMap.put("status", "FAILED");
                }

                resultMap.put("statusCode", statusCode);
                resultMap.put("response", responseBody);

            } catch (Exception e) {
                System.out.println("❌ FAILED | " + e.getMessage());
                resultMap.put("status", "FAILED");
                resultMap.put("statusCode", 500);
                resultMap.put("response", e.getMessage());
            }

            testResults.add(resultMap);

            // Sort testResults: PASSED first, FAILED next
            testResults.sort((a, b) -> {
                String statusA = (String) a.get("status");
                String statusB = (String) b.get("status");
                return statusB.equals("FAILED") ? -1 : statusA.equals("FAILED") ? 1 : 0;
            });
        }
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
                headers.put(header.get("key").asText(), header.get("value").asText());
            }
        }
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
        io.restassured.specification.RequestSpecification request = given().headers(headers);

        // Add Authorization header dynamically
        if (variables.containsKey("{{token}}")) {
            request.header("Authorization", "Bearer " + variables.get("{{token}}"));
        }

        if (body != null && !body.isEmpty()) request.body(body);

        return switch (method.toUpperCase()) {
            case "POST" -> request.post(url);
            case "PUT" -> request.put(url);
          //  case "DELETE" -> request.delete(url);
            case "PATCH" -> request.patch(url);
            default -> request.get(url);
        };
    }
}
