package com.purna.automation.base;

import com.purna.automation.pages.LoginPage;
import com.purna.automation.pages.ProductsPage;
import com.purna.automation.utilities.ConfigReader;
import com.purna.automation.utilities.TestDataUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public abstract class BaseTest {
    private static final Logger LOG = LoggerFactory.getLogger(BaseTest.class);

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        DriverFactory.create();
        LOG.info("event=page_navigation environment={}", ConfigReader.environment());
        DriverFactory.get().get(startPageUrl());
    }

    protected String startPageUrl() { return ConfigReader.get("baseUrl"); }

    protected LoginPage loginPage() { return new LoginPage(DriverFactory.get()); }

    protected ProductsPage loginAsStandardUser() {
        var credentials = TestDataUtils.validLogin();
        return loginPage().login(credentials.username(), credentials.password());
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        // TestListener captures failure evidence after invocation, BEFORE this quits.
        try { DriverFactory.quit(); }
        finally { result.setAttribute("automation.driverSlotCleared", DriverFactory.current().isEmpty()); }
    }
}
