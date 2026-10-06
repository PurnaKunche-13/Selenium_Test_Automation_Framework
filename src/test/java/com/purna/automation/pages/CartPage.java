package com.purna.automation.pages;

import java.math.BigDecimal;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openqa.selenium.WebElement;

public final class CartPage extends BasePage {
    private static final Logger LOG = LoggerFactory.getLogger(CartPage.class);
    private static final By CONTENTS = By.id("cart_contents_container");
    private static final By ITEMS = By.cssSelector(".cart_item");
    private static final By NAME = By.cssSelector(".inventory_item_name");
    private static final By PRICE = By.cssSelector(".inventory_item_price");
    private static final By QUANTITY = By.cssSelector(".cart_quantity");
    private static final By REMOVE = By.tagName("button");
    private static final By CONTINUE = By.id("continue-shopping");
    private static final By CHECKOUT = By.id("checkout");
    public CartPage(WebDriver driver) { super(driver); wait.visible(CONTENTS); }
    public HeaderComponent header() { return new HeaderComponent(driver); }
    public List<String> productNames() {
        return driver.findElements(ITEMS).stream().map(item -> item.findElement(NAME).getText()).toList();
    }
    private WebElement product(String name) {
        return wait.until(ignored -> driver.findElements(ITEMS).stream()
                .filter(item -> item.findElement(NAME).getText().equals(name)).findFirst().orElse(null));
    }
    public BigDecimal priceOf(String name) { return money(product(name).findElement(PRICE).getText()); }
    public int quantityOf(String name) { return Integer.parseInt(product(name).findElement(QUANTITY).getText()); }
    public CartPage remove(String name) {
        LOG.info("event=cart_remove product={}", name);
        product(name).findElement(REMOVE).click();
        wait.until(ignored -> !productNames().contains(name));
        return this;
    }
    public ProductsPage continueShopping() { click(CONTINUE); return new ProductsPage(driver); }
    public CheckoutPage checkout() { click(CHECKOUT); return new CheckoutPage(driver); }
}
