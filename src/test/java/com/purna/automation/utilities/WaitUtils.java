package com.purna.automation.utilities;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public final class WaitUtils {
    private final WebDriverWait wait;
    public WaitUtils(WebDriver driver) {
        wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigReader.positiveInt("explicitWait")));
        wait.pollingEvery(Duration.ofMillis(200));
        wait.ignoring(StaleElementReferenceException.class);
    }
    public WebElement visible(By locator) { return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)); }
    public WebElement present(By locator) { return wait.until(ExpectedConditions.presenceOfElementLocated(locator)); }
    public WebElement clickable(By locator) { return wait.until(ExpectedConditions.elementToBeClickable(locator)); }
    public boolean invisible(By locator) { return wait.until(ExpectedConditions.invisibilityOfElementLocated(locator)); }
    public boolean urlChangedFrom(String previous) { return wait.until(driver -> !driver.getCurrentUrl().equals(previous)); }
    public boolean urlContains(String fragment) { return wait.until(ExpectedConditions.urlContains(fragment)); }
    public boolean titleIs(String title) { return wait.until(ExpectedConditions.titleIs(title)); }
    public boolean textContains(By locator, String value) {
        return wait.until(ExpectedConditions.textToBePresentInElementLocated(locator, value));
    }
    public List<WebElement> visibleElements(By locator) {
        return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
    }
    public List<WebElement> countIs(By locator, int count) {
        if (count < 0) throw new IllegalArgumentException("Element count must be non-negative");
        return wait.until(ExpectedConditions.numberOfElementsToBe(locator, count));
    }
    public <T> T until(Function<? super WebDriver, T> condition) { return wait.until(condition); }
}
