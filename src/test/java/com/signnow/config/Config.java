package com.signnow.config;

public class Config {
    public static String getClientId() {
        return System.getenv("CLIENT_ID");
    }

    public static String getClientSecret() {
        return System.getenv("CLIENT_SECRET");
    }
}