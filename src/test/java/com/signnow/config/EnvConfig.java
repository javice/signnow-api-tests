package com.signnow.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class EnvConfig {
    private static final Logger logger = LoggerFactory.getLogger(EnvConfig.class);
    private static final Dotenv dotenv ;

    private static final String clientId;
    private static final String clientSecret;
    private static final String basicAuthToken;
    private static final String username;
    private static final String password;

    private static String currentAccessToken;
    private static String currentRefreshToken;
    private static String currentAuthorizationCode;

    // Bloque estático para cargar la configuración inicial del .env
    static {
        dotenv = Dotenv.configure()
                .ignoreIfMissing() // No fallar si .env no existe (opcional)
                .load();

        clientId = dotenv.get("CLIENT_ID", "default_client_id_if_missing"); // Proporcionar default opcional
        clientSecret = dotenv.get("CLIENT_SECRET", "default_client_secret_if_missing");
        basicAuthToken = dotenv.get("BASIC_AUTH_TOKEN");
        username = dotenv.get("USERNAME");
        password = dotenv.get("PASSWORD");

        // Inicializar los tokens dinámicos (opcionalmente leerlos del .env como valor inicial si existen)
        currentAccessToken = dotenv.get("ACCESS_TOKEN"); // Puede ser null si no está en .env
        currentRefreshToken = dotenv.get("REFRESH_TOKEN");
        currentAuthorizationCode = dotenv.get("AUTHORIZATION_CODE");

        logger.info("EnvConfig inicializado. Client ID: {}", clientId != null ? "cargado" : "NO cargado");
        // Puedes añadir más logs para verificar la carga inicial
    }


    // --- Métodos Getters para configuración estática ---
    public static String getClientId() {
        return clientId;
    }

    public static String getClientSecret() {
        return clientSecret;
    }

    public static String getBasicAuthToken() {
        return basicAuthToken;
    }

    public static String getUsername() {
        return username;
    }

    public static String getPassword() {
        return password;
    }

    public static String getAccessToken() {
        // Ya no necesita leer de dotenv cada vez
        return currentAccessToken;
    }

    public static void updateAccessToken(String accessToken) {
        logger.debug("Actualizando Access Token en memoria: {}", accessToken != null ? accessToken.substring(0, Math.min(accessToken.length(), 10)) + "..." : "null");
        // Simplemente actualiza la variable estática
        currentAccessToken = accessToken;
        // NO escribe en el archivo .env
    }

    public static String getRefreshToken() {
        return currentRefreshToken;
    }

    public static void updateRefreshToken(String refreshToken) {
        logger.debug("Actualizando Refresh Token en memoria.");
        currentRefreshToken = refreshToken;
        // NO escribe en el archivo .env
    }

    public static String getAuthorizationCode() {
        return currentAuthorizationCode;
    }

    public static void updateAuthorizationCode(String authorizationCode) {
        logger.debug("Actualizando Authorization Code en memoria.");
        currentAuthorizationCode = authorizationCode;
        // NO escribe en el archivo .env
    }
}


