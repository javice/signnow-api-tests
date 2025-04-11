package com.signnow.base;

import com.aventstack.extentreports.ExtentTest;
import com.signnow.utils.ReportListener;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.testng.annotations.BeforeClass;

public class TestBase {

    private ExtentTest extentTest;

    @BeforeClass
    public void setup() {
        RestAssured.baseURI = "https://api.signnow.com";
    }

}