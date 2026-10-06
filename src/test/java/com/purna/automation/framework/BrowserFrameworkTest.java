package com.purna.automation.framework;

import com.purna.automation.base.BaseTest;
import com.purna.automation.base.DriverFactory;
import com.purna.automation.utilities.WaitUtils;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.Assert;
import org.testng.annotations.Test;

public class BrowserFrameworkTest extends BaseTest {
    private final ConcurrentHashMap<String, Long> sessions = new ConcurrentHashMap<>();
    private final CyclicBarrier simultaneous = new CyclicBarrier(3);
    @Override protected String startPageUrl() {
        return "data:text/html,%3Chtml%3E%3Chead%3E%3Ctitle%3EFramework%20check%3C/title%3E%3C/head%3E"
                + "%3Cbody%3E%3Cbutton%20id='ready'%3EReady%3C/button%3E%3Cul%3E%3Cli%3EOne%3C/li%3E%3Cli%3ETwo%3C/li%3E%3C/ul%3E%3C/body%3E%3C/html%3E";
    }
    @Test(groups = "framework", invocationCount = 3, threadPoolSize = 3,
          description = "Three real sessions exercise isolation and explicit-wait contracts")
    public void eachThreadOwnsAnIndependentBrowser() throws Exception {
        var driver = DriverFactory.get();
        String id = ((RemoteWebDriver) driver).getSessionId().toString();
        Assert.assertNull(sessions.putIfAbsent(id, Thread.currentThread().getId()), "Session IDs must be unique");
        simultaneous.await(45, TimeUnit.SECONDS);
        Assert.assertEquals(sessions.size(), 3);
        Assert.assertSame(DriverFactory.get(), driver);
        var wait = new WaitUtils(driver);
        Assert.assertTrue(wait.present(By.id("ready")).isDisplayed());
        Assert.assertTrue(wait.visible(By.id("ready")).isDisplayed());
        Assert.assertTrue(wait.clickable(By.id("ready")).isEnabled());
        Assert.assertTrue(wait.textContains(By.id("ready"), "Ready"));
        Assert.assertTrue(wait.invisible(By.id("absent")));
        Assert.assertTrue(wait.titleIs("Framework check"));
        Assert.assertEquals(wait.countIs(By.tagName("li"), 2).size(), 2);
        Assert.assertEquals(wait.countIs(By.id("absent"), 0).size(), 0);
        String previous = driver.getCurrentUrl();
        driver.get(startPageUrl() + "#changed");
        Assert.assertTrue(wait.urlChangedFrom(previous));
        Assert.assertTrue(wait.urlContains("#changed"));
        Assert.assertTrue(wait.until(current -> current.getTitle().equals("Framework check")));
    }
}
