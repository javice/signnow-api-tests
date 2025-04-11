

package com.signnow.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import java.lang.reflect.Method;

import io.restassured.RestAssured;
import io.restassured.config.RestAssuredConfig;
import io.restassured.config.SSLConfig;
import org.junit.platform.commons.util.AnnotationUtils;
import org.junit.jupiter.api.DisplayName;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;



public class ReportListener implements ITestListener {

    private static ExtentReports extent;
    public static ThreadLocal<ExtentTest> test = new ThreadLocal<>();

    @Override
    public void onStart(ITestContext context) {
        ExtentHtmlReporter htmlReporter = new ExtentHtmlReporter("report.html");
        htmlReporter.config().setTheme(Theme.STANDARD);
        htmlReporter.config().setDocumentTitle("SignNow API Test Report");
        htmlReporter.config().setReportName("OAuth2 Resultados del Flow Test. Colección importada desde Postman");

        extent = new ExtentReports();
        extent.attachReporter(htmlReporter);
    }

    @Override
    public void onTestStart(ITestResult result) {
        // Deshabilitamos la validación de certificados SSL. NO RECOMENDADO PARA PRODUCCIÓN.
        RestAssured.config = RestAssuredConfig.newConfig().sslConfig(new SSLConfig().allowAllHostnames().relaxedHTTPSValidation());

        Method method = result.getMethod().getConstructorOrMethod().getMethod();
        String testName = AnnotationUtils.findAnnotation(method, DisplayName.class).map(DisplayName::value).orElse(method.getName());

        ExtentTest extentTest = extent.createTest(testName);
        jdk.jfr.Description descriptionAnnotation = method.getAnnotation(jdk.jfr.Description.class);

        if (descriptionAnnotation != null) {
            extentTest.info(descriptionAnnotation.value());
        } else {
            extentTest.info("No hay descripción disponible");
        }
        test.set(extentTest);
    }

    @Override
    public void onTestSuccess(ITestResult result) {

        test.get().pass("✅ Test superado con éxito");
        test.get().info("Tiempo de ejecución: " + (result.getEndMillis() - result.getStartMillis()) + " ms");
        test.get().info("Código de estado: " + result.getStatus());


    }

    @Override
    public void onTestFailure(ITestResult result) {
        test.get().fail("❌ Test fallido: " + result.getThrowable());
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        test.get().skip("⚠️ Test saltado");
    }

    @Override
    public void onFinish(ITestContext context) {
        extent.flush();
    }
}


