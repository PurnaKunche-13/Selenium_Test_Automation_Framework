package com.purna.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LoginPage extends BasePage {
    private static final Logger LOG = LoggerFactory.getLogger(LoginPage.class);
    private static final By USERNAME = By.id("user-name");
    private static final By PASSWORD = By.id("password");
    private static final By LOGIN = By.id("login-button");
    private static final By ERROR = By.cssSelector("[data-test='error']");
    public LoginPage(WebDriver driver) { super(driver); wait.visible(LOGIN); }
    public LoginPage enterUsername(String value) { type(USERNAME, value); return this; }
    public LoginPage enterPassword(String value) { type(PASSWORD, value); return this; }
    public LoginPage submit(String username, String password) {
        LOG.info("Submitting login form");
        enterUsername(username); enterPassword(password); click(LOGIN);
        return this;
    }
    public ProductsPage login(String username, String password) {
        submit(username, password);
        return new ProductsPage(driver);
    }
    public String getErrorMessage() { return text(ERROR); }
    public boolean isDisplayed() { return wait.visible(LOGIN).isDisplayed(); }
}
