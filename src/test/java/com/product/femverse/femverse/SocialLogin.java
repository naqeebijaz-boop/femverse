package com.product.femverse.femverse;


import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.testng.Assert.assertEquals;

public class SocialLogin {

    @Test
    public void signupNewUser() {
        RestAssured.baseURI = "http://172.16.10.185";

        // Timestamp for unique email/device
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String email = "user" + timestamp + "@example.com";
        String deviceId = "device-" + timestamp;

        // Prepare JSON body as Map
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("confirm_password", "SecurePass123!");
        requestBody.put("device_id", deviceId);
        requestBody.put("email", email);
        requestBody.put("first_name", "John");
        requestBody.put("last_name", "Doe");
        requestBody.put("password", "SecurePass123!");
        requestBody.put("timezone", "Asia/Karachi");

        // Send POST request
        Response response = given()
                .header("accept", "application/json")
                .header("Content-Type", "application/json")
                .body(requestBody)
                .when()
                .post("/api/v1/auth/signup")
                .then()
                .extract().response();

        System.out.println("Response: " + response.asString());

        assertEquals(response.getStatusCode(), 200, "Signup failed!");

        String userId = response.jsonPath().getString("data.user_id");
        String accessToken = response.jsonPath().getString("data.access_token");

        System.out.println("User ID: " + userId);
        System.out.println("Access Token: " + accessToken);
    }
}

