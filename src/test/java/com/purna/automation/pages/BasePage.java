package com.purna.automation.pages;

import com.purna.automation.utilities.WaitUtils;
import java.math.BigDecimal;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public abstract class BasePage {
    protected final WebDriver driver;
    protected final WaitUtils wait;
    protected BasePage(WebDriver driver) { this.driver = driver; this.wait = new WaitUtils(driver); }
    protected void click(By locator) { wait.clickable(locator).click(); }
    protected void type(By locator, String value) {
        var element = wait.visible(locator);
        element.clear();
        element.sendKeys(value);
    }
    protected String text(By locator) { return wait.visible(locator).getText().trim(); }
    protected static BigDecimal money(String text) {
        return new BigDecimal(text.substring(text.indexOf('$') + 1).trim());
    }
}
