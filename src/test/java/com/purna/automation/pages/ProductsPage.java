package com.purna.automation.pages;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ProductsPage extends BasePage {
    private static final Logger LOG = LoggerFactory.getLogger(ProductsPage.class);
    private static final By TITLE = By.cssSelector(".title");
    private static final By INVENTORY = By.id("inventory_container");
    private static final By ITEMS = By.cssSelector(".inventory_item");
    private static final By NAME = By.cssSelector(".inventory_item_name");
    private static final By PRICE = By.cssSelector(".inventory_item_price");
    private static final By BUTTON = By.tagName("button");
    private static final By SORT = By.cssSelector("[data-test='product-sort-container']");
    public ProductsPage(WebDriver driver) { super(driver); wait.visible(INVENTORY); }
    public String title() { return text(TITLE); }
    public HeaderComponent header() { return new HeaderComponent(driver); }
    public List<String> productNames() {
        return wait.visibleElements(ITEMS).stream().map(item -> item.findElement(NAME).getText()).toList();
    }
    public List<BigDecimal> productPrices() {
        return wait.visibleElements(ITEMS).stream().map(item -> money(item.findElement(PRICE).getText())).toList();
    }
    public Map<String, BigDecimal> catalog() {
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        for (WebElement item : wait.visibleElements(ITEMS))
            result.put(item.findElement(NAME).getText(), money(item.findElement(PRICE).getText()));
        return result;
    }
    private WebElement product(String name) {
        return wait.until(ignored -> findProduct(name));
    }
    private WebElement findProduct(String name) {
        return driver.findElements(ITEMS).stream()
                .filter(item -> item.findElement(NAME).getText().equals(name)).findFirst().orElse(null);
    }
    public ProductsPage addToCart(String name) {
        WebElement button = product(name).findElement(BUTTON);
        if (!button.getText().equals("Add to cart")) throw new IllegalStateException("Already in cart: " + name);
        button.click();
        wait.until(ignored -> {
            WebElement current = findProduct(name);
            return current != null && current.findElement(BUTTON).getText().equals("Remove");
        });
        LOG.info("event=cart_add product={}", name);
        return this;
    }
    public ProductsPage sort(String value) {
        LOG.info("event=products_sort mode={}", value);
        new Select(wait.visible(SORT)).selectByValue(value);
        wait.until(ignored -> new Select(driver.findElement(SORT)).getFirstSelectedOption()
                .getAttribute("value").equals(value));
        return this;
    }
    public List<String> waitForNames(List<String> expected) {
        wait.until(ignored -> driver.findElements(ITEMS).stream()
                .map(item -> item.findElement(NAME).getText()).toList().equals(expected));
        return productNames();
    }
    public List<BigDecimal> waitForPrices(List<BigDecimal> expected) {
        wait.until(ignored -> driver.findElements(ITEMS).stream()
                .map(item -> money(item.findElement(PRICE).getText())).toList().equals(expected));
        return productPrices();
    }
}
