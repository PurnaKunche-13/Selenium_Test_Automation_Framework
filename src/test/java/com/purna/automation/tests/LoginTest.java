package com.purna.automation.tests;

import com.purna.automation.base.BaseTest;
import com.purna.automation.utilities.TestDataUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = {"ui", "regression", "login"})
public class LoginTest extends BaseTest {
    @Test(groups = "smoke", dataProvider = "validLogins", dataProviderClass = TestDataUtils.class,
          description = "TC001/TC033: Public demo users open Products")
    public void validLogin(TestDataUtils.LoginCase row) {
        Assert.assertEquals(loginPage().login(row.username(), row.password()).title(), "Products", row.id());
    }
    @Test(dataProvider = "invalidLogins", dataProviderClass = TestDataUtils.class,
          description = "TC002-TC006: Login validation")
    public void rejectedLogin(TestDataUtils.LoginCase row) {
        var login = loginPage().submit(row.username(), row.password());
        Assert.assertEquals(login.getErrorMessage(), row.error(), row.id());
        Assert.assertTrue(login.isDisplayed(), "Rejected login must keep the login form visible");
    }
    @Test(description = "TC007: Logout returns to the login form")
    public void logout() {
        Assert.assertTrue(loginAsStandardUser().header().logout().isDisplayed());
    }
}
