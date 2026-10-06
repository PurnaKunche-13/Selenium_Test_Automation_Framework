package com.purna.automation.pages;

import java.math.BigDecimal;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CheckoutPage extends BasePage {
    private static final Logger LOG = LoggerFactory.getLogger(CheckoutPage.class);
    private static final By INFO = By.id("checkout_info_container");
    private static final By FIRST = By.id("first-name");
    private static final By LAST = By.id("last-name");
    private static final By POSTAL = By.id("postal-code");
    private static final By CONTINUE = By.id("continue");
    private static final By ERROR = By.cssSelector("[data-test='error']");
    private static final By OVERVIEW = By.id("checkout_summary_container");
    private static final By NAMES = By.cssSelector(".cart_item .inventory_item_name");
    private static final By SUBTOTAL = By.cssSelector(".summary_subtotal_label");
    private static final By TAX = By.cssSelector(".summary_tax_label");
    private static final By TOTAL = By.cssSelector(".summary_total_label");
    private static final By FINISH = By.id("finish");
    private static final By CONFIRMATION = By.cssSelector(".complete-header");
    public CheckoutPage(WebDriver driver) { super(driver); wait.visible(INFO); }
    public CheckoutPage enterInformation(String first, String last, String postal) {
        LOG.info("event=checkout_information");
        type(FIRST, first); type(LAST, last); type(POSTAL, postal); return this;
    }
    public CheckoutPage submitInformation() { click(CONTINUE); return this; }
    public CheckoutPage waitForOverview() { wait.visible(OVERVIEW); return this; }
    public String errorMessage() { return text(ERROR); }
    public List<String> productNames() { return wait.visibleElements(NAMES).stream().map(e -> e.getText()).toList(); }
    public BigDecimal subtotal() { return money(text(SUBTOTAL)); }
    public BigDecimal tax() { return money(text(TAX)); }
    public BigDecimal total() { return money(text(TOTAL)); }
    public CheckoutPage finish() { LOG.info("event=checkout_finish"); click(FINISH); wait.visible(CONFIRMATION); return this; }
    public String confirmationMessage() { return text(CONFIRMATION); }
    public HeaderComponent header() { return new HeaderComponent(driver); }
}
