package com.product.femverse.femverse;


import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;

public class ChangePasswordAndSignInTest {

    private static final String BASE_URL = "https://superwoman.trippleapps.com:8443";
    private static final String LOGIN_URL = BASE_URL + "/api/v1/auth/signin";
    private static final String CHANGE_PASSWORD_URL = BASE_URL + "/api/v1/settings/change-password";
    private static final String EMAIL = "naqeeby@example.com";

    private static final String PASSWORD_FILE = "current_password.txt";
    private static final String INITIAL_PASSWORD = "12345678";

    private String oldPassword;
    private String newPassword;
    private String token;

    @BeforeClass
    public void setup() {
        oldPassword = loadPassword();

        // Auto-increment password
        Matcher matcher = Pattern.compile("(\\d+)$").matcher(oldPassword);
        if (!matcher.find()) {
            throw new RuntimeException("Password must end with a number to auto-increment");
        }
        int number = Integer.parseInt(matcher.group(1));
        newPassword = oldPassword.substring(0, matcher.start(1)) + (number + 1);
    }

    private String loadPassword() {
        File file = new File(PASSWORD_FILE);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                return reader.readLine().trim();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return INITIAL_PASSWORD;
    }

    private void savePassword(String password) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PASSWORD_FILE))) {
            writer.write(password);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String login(String email, String password) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("device_id", "zoro");
        requestBody.put("email", email);
        requestBody.put("password", password);

        Response response = given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json; charset=UTF-8")
                .body(requestBody)
                .when()
                .post(LOGIN_URL)
                .then()
                .extract().response();

        int statusCode = response.getStatusCode();
        Assert.assertEquals(statusCode, 200, "SignIn failed!");
        System.out.println("✅ SignIn → PASSED | Status Code: " + statusCode);

        return response.jsonPath().getString("data.access_token");
    }

    @Test(priority = 1)
    public void changePassword() {
        // Login first
        token = login(EMAIL, oldPassword);

        // Change password
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("old_password", oldPassword);
        requestBody.put("new_password", newPassword);
        requestBody.put("confirm_password", newPassword);

        Response response = given()
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json; charset=UTF-8")
                .body(requestBody)
                .when()
                .patch(CHANGE_PASSWORD_URL)
                .then()
                .extract().response();

        int statusCode = response.getStatusCode();
        Assert.assertEquals(statusCode, 200, "Password change failed!");
        System.out.println("✅ changePassword → PASSED | Status Code: " + statusCode);

        savePassword(newPassword);
    }

    @Test(priority = 2, dependsOnMethods = {"changePassword"})
    public void signIn() {
        // Sign in with new password
        token = login(EMAIL, newPassword);
        Assert.assertNotNull(token, "Access token should not be null!");
    }
}
