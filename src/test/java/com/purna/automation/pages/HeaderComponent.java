package com.purna.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HeaderComponent extends BasePage {
    private static final Logger LOG = LoggerFactory.getLogger(HeaderComponent.class);
    private static final By CART = By.cssSelector(".shopping_cart_link");
    private static final By BADGE = By.cssSelector(".shopping_cart_badge");
    private static final By MENU = By.id("react-burger-menu-btn");
    private static final By LOGOUT = By.id("logout_sidebar_link");
    public HeaderComponent(WebDriver driver) { super(driver); }
    public int cartCount() {
        var badges = driver.findElements(BADGE);
        return badges.isEmpty() ? 0 : Integer.parseInt(badges.get(0).getText());
    }
    public int waitForCartCount(int expected) {
        wait.until(ignored -> cartCount() == expected);
        return cartCount();
    }
    public CartPage openCart() { click(CART); return new CartPage(driver); }
    public LoginPage logout() { LOG.info("event=logout"); click(MENU); click(LOGOUT); return new LoginPage(driver); }
}
