package com.signnow.base;

import com.aventstack.extentreports.ExtentTest;
import com.signnow.config.EnvConfig;
import com.signnow.utils.ReportListener;
import io.restassured.RestAssured;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class TestBase {

    protected ExtentTest extentTest;

    @BeforeClass
    public void setupClass() {
        // Configuración global para todas las pruebas
        RestAssured.baseURI = "https://api.signnow.com";

        // Configuración de logging (opcional)
        try {
            PrintStream logStream = new PrintStream(new FileOutputStream("test-output/restassured.log"));
            RestAssured.filters(
                    new RequestLoggingFilter(LogDetail.ALL, logStream),
                    new ResponseLoggingFilter(LogDetail.ALL, logStream)
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @BeforeMethod
    public void setupMethod() {
        // Inicializar el reporte para cada método de prueba
        extentTest = ReportListener.test.get();
    }
}