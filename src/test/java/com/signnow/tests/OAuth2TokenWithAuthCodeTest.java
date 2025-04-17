package com.signnow.tests;

import com.aventstack.extentreports.ExtentTest;
import com.signnow.config.EnvConfig;
import com.signnow.utils.ReportListener;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jdk.jfr.Description;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.testng.Assert;
import org.testng.annotations.Test;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class OAuth2TokenWithAuthCodeTest {

    private static final String TOKEN_URL = "/oauth2/token";
    private static final String APPLICATIONS_URL = "/v2/applications";
    private static final String REDIRECT_URI_PREFIX = "[https://api.signnow.com](https://api.signnow.com)";


    // Helper method robusto para parsear query parameters
    private Map<String, String> parseQueryParams(String query) throws UnsupportedEncodingException {
        if (query == null || query.isEmpty()) {
            return Map.of(); // Mapa vacío si no hay query
        }
        return Arrays.stream(query.split("&"))
                .map(param -> param.split("=", 2)) // Divide en clave/valor, max 2 partes
                .filter(pair -> pair.length == 2) // Asegura que hay clave y valor
                .collect(Collectors.toMap(
                        pair -> URLDecoder.decode(pair[0], StandardCharsets.UTF_8),
                        pair -> URLDecoder.decode(pair[1], StandardCharsets.UTF_8),
                        (v1, v2) -> v1 // Maneja claves duplicadas (se queda con la primera)
                ));
    }



    @Test(description = "POST: Generar Access Token y Refresh Token iniciales (usando password grant)")
    @Description("Test para generar un código de autorización pasando por el header el BasicAuthToken")
    @DisplayName("POST: Generar Authorization Code")
    public void testGenerateAuthorizationCode() {

        ExtentTest extentTest = ReportListener.test.get();
        extentTest.info("Iniciando Test: Generar Access Token y Refresh Token iniciales");

        Response response = given()
                .log().ifValidationFails()
                .header("Authorization", "Basic " + EnvConfig.getBasicAuthToken())
                .contentType("application/x-www-form-urlencoded")
                .formParam("username", EnvConfig.getUsername())
                .formParam("password", EnvConfig.getPassword())
                .formParam("grant_type", "password")
                .formParam("scope", "*")
                .when()
                .post(TOKEN_URL)
                .then()
                .log().ifValidationFails()
                .extract().response();

        assertThat(response.statusCode(), is(200));
        assertThat(response.jsonPath().getString("access_token"), is(notNullValue()));
        assertThat(response.jsonPath().getString("refresh_token"), is(notNullValue()));
        assertThat(response.jsonPath().getString("token_type"), is(equalToIgnoringCase("bearer")));
        assertThat(response.jsonPath().getString("scope"), is(equalTo("*")));


        String refreshToken = response.jsonPath().getString("refresh_token");
        EnvConfig.updateRefreshToken(refreshToken);

        // Agregamos Los valores de ACCESS_TOKEN y REFRESH_TOKEN al reporte del test
        extentTest.info("Access Token: " + EnvConfig.getAccessToken());
        extentTest.info("Refresh Token: " + EnvConfig.getRefreshToken());
        extentTest.info("✅ Tokens 'Access' y 'Refresh' generados y almacenados con éxito.");
    }


    @Test(description = "GET: Obtener Client ID usando el Access Token",
            dependsOnMethods = {"testGenerateAuthorizationCode"})
    @Description("Test para obtener el client_id mediante una solicitud GET")
    @DisplayName("GET: Obtener Client ID")
    public void testClientId() {
        ExtentTest extentTest = ReportListener.test.get();
        extentTest.info("Iniciando Test: Obtener Client ID");

        String currentAccessToken = EnvConfig.getAccessToken();
        assertThat("Access Token no debe ser null para este test", currentAccessToken, is(notNullValue()));

        Response response = given()
                .log().ifValidationFails()
                .header("Authorization", "Bearer " + currentAccessToken)
                .when()
                .get(APPLICATIONS_URL)
                .then()
                .log().ifValidationFails()
                .extract().response();

        assertThat(response.statusCode(), is(200));
        assertThat(response.jsonPath().getList("data"), is(not(empty())));
        assertThat(response.jsonPath().getString("data[0].client_id"), is(equalTo(EnvConfig.getClientId())));
        assertThat(response.jsonPath().getString("data[0].client_secret"), is(equalTo(EnvConfig.getClientSecret())));


        // Agregamos los valores de CLIENT_ID  y CLIENT_SECRET al reporte del test
        extentTest.info("Client ID Verificado : " + EnvConfig.getClientId());
        extentTest.info("Client Secret Verificado: " + EnvConfig.getClientSecret());
        extentTest.info("✅ Client ID y Secret verificados con éxito.");

    }


    @Test(description = "GET: Obtener Authorization Code parseando HTML de redirección",
            dependsOnMethods = {"testClientId"}) // Depende de tener el Client ID y Access Token
    @Description("Test para obtener un código de autorización incluido en una URL de redirección")
    @DisplayName("GET: Obtener Authorization Code mediante una URL de redirección")
    public void testAuthCodeViaRedirect() {
        ExtentTest extentTest = ReportListener.test.get();
        extentTest.info("Iniciando Test: Obtener Authorization Code desde HTML");

        String expectedRedirectUriPrefix = "https://api.signnow.com";
        String clientId = EnvConfig.getClientId();
        String accessToken = EnvConfig.getAccessToken();

        // Verificar precondiciones
        assertThat("Client ID no debe ser null para este test", clientId, is(notNullValue()));
        assertThat("Access Token no debe ser null para este test", accessToken, is(notNullValue()));

        // Realizar la solicitud para obtener el código de autorización
        Response response = requestAuthorizationCode(clientId, accessToken, expectedRedirectUriPrefix);

        // Validar respuesta básica
        assertThat(response.statusCode(), is(302));
        assertThat(response.contentType(), containsString(ContentType.HTML.toString()));

        // Extraer el código de autorización
        String responseBody = response.getBody().asString();
        String authorizationCode = extractAuthCodeFromHtml(responseBody, expectedRedirectUriPrefix, extentTest);

        // Validar y almacenar el código
        assertThat("El código de autorización no pudo ser extraído.", authorizationCode, is(notNullValue()));
        EnvConfig.updateAuthorizationCode(authorizationCode);

        // Información para el reporte
        extentTest.info("Authorization Code extraído: " + EnvConfig.getAuthorizationCode());
        extentTest.info("✅ Código de autorización obtenido y almacenado con éxito desde HTML.");
    }

    /**
     * Realiza la solicitud HTTP para obtener el código de autorización
     */
    private Response requestAuthorizationCode(String clientId, String accessToken, String redirectUri) {
        return given()
                .log().ifValidationFails()
                .redirects().follow(false)
                .when()
                .get("/oauth2/userauth?response_type=code&redirect_uri=" +
                        redirectUri + "&client_id=" + clientId + "&access_token=" + accessToken)
                .then()
                .log().ifValidationFails()
                .extract().response();
    }

    /**
     * Extrae el código de autorización del HTML de la respuesta
     */
    private String extractAuthCodeFromHtml(String responseBody, String expectedRedirectUriPrefix, ExtentTest extentTest) {
        Document document = Jsoup.parse(responseBody);
        extentTest.info("Respuesta HTML recibida. Buscando meta refresh tag...");

        // Buscar etiqueta meta refresh
        Element metaRefresh = document.selectFirst("meta[http-equiv=refresh]");
        assertThat("No se encontró la etiqueta <meta http-equiv=\"refresh\"> en el HTML.",
                metaRefresh, is(notNullValue()));

        // Obtener el atributo content
        String content = metaRefresh.attr("content");
        assertThat("El atributo 'content' está vacío o no existe en la etiqueta meta refresh.",
                content, is(notNullValue()));
        extentTest.info("Atributo 'content' encontrado: " + content);

        // Extraer URL del contenido
        String urlString = extractUrlFromContent(content);
        assertThat("No se pudo extraer la URL del atributo 'content': " + content,
                urlString, is(notNullValue()));
        extentTest.info("URL extraída del meta tag: " + urlString);

        // Validar que la URL comienza con el prefijo esperado
        assertThat("La URL extraída (" + urlString + ") no comienza con el prefijo esperado (" +
                expectedRedirectUriPrefix + ")", urlString, startsWith(expectedRedirectUriPrefix));

        // Extraer el código de autorización de la URL
        try {
            return extractAuthCodeFromUrl(urlString);
        } catch (URISyntaxException | UnsupportedEncodingException e) {
            Assert.fail("Error al procesar la URL: " + urlString + ". Error: " + e.getMessage());
            return null; // Nunca llegaría aquí debido al Assert.fail
        }
    }

    /**
     * Extrae la URL del atributo content de la etiqueta meta refresh
     */
    private String extractUrlFromContent(String content) {
        int urlIndex = content.toLowerCase().indexOf("url=");
        if (urlIndex == -1) {
            return null;
        }

        String urlString = content.substring(urlIndex + 4).trim();

        // Eliminar comillas si existen
        if (urlString.startsWith("'") && urlString.endsWith("'")) {
            urlString = urlString.substring(1, urlString.length() - 1);
        } else if (urlString.startsWith("\"") && urlString.endsWith("\"")) {
            urlString = urlString.substring(1, urlString.length() - 1);
        }

        return urlString;
    }

    /**
     * Extrae el código de autorización de la URL
     */
    private String extractAuthCodeFromUrl(String urlString) throws URISyntaxException, UnsupportedEncodingException {
        URI redirectUri = new URI(urlString);
        String query = redirectUri.getQuery();

        assertThat("La URL extraída no contiene una query string: " + urlString,
                query, is(notNullValue()));

        Map<String, String> queryParams = parseQueryParams(query);
        String authCode = queryParams.get("code");

        assertThat("Parámetro 'code' no encontrado en la query de la URL: " + query,
                authCode, is(notNullValue()));
        assertThat("El valor del parámetro 'code' está vacío.",
                authCode, is(not(emptyString())));

        return authCode;
    }

    @Test(description = "POST: Obtener Access Token utilizando el Authorization Code",
            dependsOnMethods = {"testAuthCodeViaRedirect"})
    @Description("Test para obtener un token de acceso utilizando el código de autorización")
    @DisplayName("POST: Obtener Access Token utilizando Authorization Code")
    public void testAccessTokenWithAuthCode() {
        String authCode = EnvConfig.getAuthorizationCode();
        ExtentTest extentTest = ReportListener.test.get();
        extentTest.info("Iniciando Test: Obtener Access Token con Authorization Code");

        assertThat("El código de autorización es null. El test 'testAuthCodeViaRedirect' pudo haber fallado.", authCode, is(notNullValue()));
        assertThat("El código de autorización parece inválido o no se actualizó: " + authCode, authCode, is(not(emptyString())));

        extentTest.info("Usando Authorization Code: " + authCode);

        // Añadimos el Authorization Code obtenido anteriormente al reporte
        extentTest.info("Authorization Code Previo: " + authCode);

        Response response = given()
                .log().ifValidationFails()
                .header("Authorization", "Basic " + EnvConfig.getBasicAuthToken())
                .contentType("application/x-www-form-urlencoded")
                .formParam("code", authCode)
                .formParam("grant_type", "authorization_code")
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

        // Agregamos los valores de ACCESS_TOKEN y REFRESH_TOKEN al reporte del test

        extentTest.info("Access Token: " + EnvConfig.getAccessToken());
        extentTest.info("Refresh Token: " + EnvConfig.getRefreshToken());
        extentTest.info("✅ Nuevos tokens obtenidos con éxito usando Authorization Code.");
    }

}
