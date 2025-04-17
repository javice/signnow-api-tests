package com.signnow.tests;

import com.signnow.services.AuthService;
import com.signnow.utils.ReportListener;
import com.signnow.base.TestBase;
import com.signnow.config.EnvConfig;
import com.aventstack.extentreports.ExtentTest;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;



public class OAuth2UserAuthTest  extends TestBase {


    @Test(description = "POST: Generar Access Token (Password Grant)")
    public void testPasswordGrantToken() {
        ExtentTest extentTest = ReportListener.test.get();
        extentTest.info("ℹ️ Iniciando Test: Generar Access Token con Password Grant");
        extentTest.info("<b>Descripción:</b> Este test verifica la obtención de un token de acceso ");



        Response response = AuthService.getPasswordGrantToken(
                EnvConfig.getUsername(),
                EnvConfig.getPassword(),
                EnvConfig.getBasicAuthToken()
        );

        assertThat(response.statusCode(), is(200));
        assertThat(response.jsonPath().getString("access_token"), is(notNullValue()));
        assertThat(response.jsonPath().getString("refresh_token"), is(notNullValue()));
        assertThat(response.jsonPath().getString("token_type"), is(equalToIgnoringCase("bearer")));
        assertThat(response.jsonPath().getString("scope"), is(equalTo("*")));

        // Actualizar tokens
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
    public void testAccessToken() {
        ExtentTest extentTest = ReportListener.test.get();
        String currentAccessToken = EnvConfig.getAccessToken();
        Assert.assertNotNull(currentAccessToken, "Access Token no debe ser null para este test");

        extentTest.info("ℹ️ Iniciando Test: Verificar Access Token");
        Response response = AuthService.verifyAccessToken(currentAccessToken);

        assertThat(response.statusCode(), is(200));
        assertThat(response.jsonPath().getString("access_token"), is(equalTo(currentAccessToken)));

        // Agregamos el valor de Env.getAccessToken al reporte del test
        extentTest.info("Access Token: " + EnvConfig.getAccessToken());
        extentTest.info("Verificación de Access Token completada con éxito.");

    }
}

