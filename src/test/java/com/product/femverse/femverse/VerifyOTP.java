package com.product.femverse.femverse;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.testng.Assert.assertEquals;

public class VerifyOTP {

    @Test
    public void verifyOtp() {
        // Base URI
        RestAssured.baseURI = "https://prod.femverse.ai";

        // Bearer token (replace with actual token or fetch dynamically)
        String accessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6ImIzM2Q4Mjg0LTgzMzktNGE4My05NjM5LWFiNjUyMjdjMzZkMyIsImVtYWlsIjoidGVzdGluZ0BnbWFpbC5jb20iLCJyb2xlcyI6WyJ1c2VyIl0sImlzX3ZlcmlmaWVkIjpmYWxzZSwiZXhwIjoxNzU4MTc5MzQyfQ.14zv57YVhpWglex2uAY3M1Db4XkXm1dYyGrbrRUpXVw";

        // Prepare JSON body as Map
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("email", "testing@gmail.com");
        requestBody.put("otp", "123456");
        requestBody.put("type", "EMAIL_VERIFICATION");

        // Send POST request
        Response response = given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json; charset=UTF-8")
                .header("Authorization", "Bearer " + accessToken)
                .body(requestBody)
                .when()
                .post("/api/v1/auth/verify-otp")
                .then()
                .extract().response();

        System.out.println("Response: " + response.asString());

        // Validate response
        assertEquals(response.getStatusCode(), 200, "OTP verification failed!");

        // Extract fields safely (if any)
        String message = response.jsonPath().getString("message");
        System.out.println("Server Message: " + message);
    }
}
