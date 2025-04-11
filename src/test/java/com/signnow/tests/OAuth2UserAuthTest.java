package com.signnow.tests;

import com.signnow.utils.ReportListener;
import com.signnow.base.TestBase;
import com.signnow.config.EnvConfig;
import com.aventstack.extentreports.ExtentTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import jdk.jfr.Description;
import org.junit.jupiter.api.DisplayName;
import org.testng.annotations.Test;


import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;



public class OAuth2UserAuthTest  extends TestBase {


    @Test(description = "POST: Generar Access Token (Password Grant)")
    @Description("Test para generar el token de acesso usando  grant_type password")
    @DisplayName("POST: Generar Access Token")
    public void testPasswordGrantToken() {
        ExtentTest extentTest = ReportListener.test.get();
        extentTest.info("ℹ️ Iniciando Test: Generar Access Token con Password Grant");

        Response response = given()
                .log().ifValidationFails()
                .header("Authorization", "Basic " + EnvConfig.getBasicAuthToken())
                .contentType("application/x-www-form-urlencoded")
                .formParam("username", EnvConfig.getUsername())
                .formParam("password", EnvConfig.getPassword())
                .formParam("grant_type", "password")
                .formParam("scope", "*")
                .when()
                .post("/oauth2/token")
                .then()
                .log().ifValidationFails()
                .extract().response();

        assertThat(response.statusCode(), is(200));
        assertThat(response.jsonPath().getString("access_token"), is(notNullValue()));
        assertThat(response.jsonPath().getString("refresh_token"), is(notNullValue()));
        assertThat(response.jsonPath().getString("token_type"), is(equalToIgnoringCase("bearer")));
        assertThat(response.jsonPath().getString("scope"), is(equalTo("*")));


        String accessToken = response.jsonPath().getString("access_token");
        String refreshToken = response.jsonPath().getString("refresh_token");

        EnvConfig.updateAccessToken(accessToken);
        EnvConfig.updateRefreshToken(refreshToken);


        // Agregamos el valor de Env.getAccessToken al reporte del test
        extentTest.info("Access Token Generado: " + EnvConfig.getAccessToken());
        extentTest.info("Refresh Token Generado: " + EnvConfig.getRefreshToken());

    }

    @Test(description = "GET: Verificar Access Token obtenido con Password Grant",
            dependsOnMethods = {"testPasswordGrantToken"})
    @Description ("Test para verificar el token de acesso creado por el grant_type password anteriormente")
    @DisplayName("GET: Verificar Access Token")
    public void testAccessToken() {
        ExtentTest extentTest = ReportListener.test.get();
        String currentAccessToken = EnvConfig.getAccessToken();
        Assert.assertNotNull(currentAccessToken, "Access Token no debe ser null para este test");

        extentTest.info("ℹ️ Iniciando Test: Verificar Access Token");

        Response response = given()
                .log().ifValidationFails()
                .header("Authorization", "Bearer " + currentAccessToken)
                .when()
                .get("/oauth2/token")
                .then()
                .log().ifValidationFails()
                .extract().response();

        assertThat(response.statusCode(), is(200));
        assertThat(response.jsonPath().getString("access_token"), is(equalTo(currentAccessToken)));

        // Agregamos el valor de Env.getAccessToken al reporte del test
        extentTest.info("Access Token: " + EnvConfig.getAccessToken());
        extentTest.info("Verificación de Access Token completada con éxito.");

    }
}

