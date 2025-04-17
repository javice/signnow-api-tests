package com.signnow.services;

import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class AuthService {

    public static Response getPasswordGrantToken(String username, String password, String basicAuthToken) {
        return given()
                .log().ifValidationFails()
                .header("Authorization", "Basic " + basicAuthToken)
                .contentType("application/x-www-form-urlencoded")
                .formParam("username", username)
                .formParam("password", password)
                .formParam("grant_type", "password")
                .formParam("scope", "*")
                .when()
                .post("/oauth2/token");
    }

    public static Response verifyAccessToken(String accessToken) {
        return given()
                .log().ifValidationFails()
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/oauth2/token");
    }

    // Más métodos de servicio aquí...
}
