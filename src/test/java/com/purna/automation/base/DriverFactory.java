package com.purna.automation.base;

import com.purna.automation.utilities.ConfigReader;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DriverFactory {
    private static final Logger LOG = LoggerFactory.getLogger(DriverFactory.class);
    private static final ThreadLocal<WebDriver> DRIVERS = new ThreadLocal<>();
    private DriverFactory() { }

    public static void create() {
        if (DRIVERS.get() != null) throw new IllegalStateException("Driver already exists on this thread");
        String browser = ConfigReader.get("browser").toLowerCase(Locale.ROOT);
        boolean headless = ConfigReader.bool("headless");
        int pageLoadTimeout = ConfigReader.positiveInt("pageLoadTimeout");
        WebDriver driver;
        switch (browser) {
            case "chrome" -> {
                ChromeOptions options = new ChromeOptions();
                if (headless) options.addArguments("--headless=new");
                options.addArguments("--window-size=1440,1000", "--disable-dev-shm-usage");
                options.setExperimentalOption("prefs", Map.of(
                        "credentials_enable_service", false,
                        "profile.password_manager_enabled", false,
                        "profile.password_manager_leak_detection", false));
                driver = new ChromeDriver(options);
            }
            case "firefox" -> {
                FirefoxOptions options = new FirefoxOptions();
                if (headless) options.addArguments("-headless");
                driver = new FirefoxDriver(options);
            }
            case "edge" -> {
                EdgeOptions options = new EdgeOptions();
                if (headless) options.addArguments("--headless=new");
                options.addArguments("--window-size=1440,1000", "--disable-dev-shm-usage");
                options.setExperimentalOption("prefs", Map.of(
                        "credentials_enable_service", false,
                        "profile.password_manager_enabled", false,
                        "profile.password_manager_leak_detection", false));
                driver = new EdgeDriver(options);
            }
            default -> throw new IllegalArgumentException("Unsupported browser: " + browser);
        }
        // Store immediately so teardown can clean up even if configuration fails.
        DRIVERS.set(driver);
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(pageLoadTimeout));
        driver.manage().window().setSize(new Dimension(1440, 1000));
        LOG.info("Initialized {} browser; headless={}", browser, headless);
    }

    public static WebDriver get() {
        return current().orElseThrow(() -> new IllegalStateException("No WebDriver on the current thread"));
    }

    public static Optional<WebDriver> current() { return Optional.ofNullable(DRIVERS.get()); }

    public static void quit() {
        WebDriver driver = DRIVERS.get();
        try {
            if (driver != null) driver.quit();
        } finally {
            DRIVERS.remove();
            LOG.info("Browser cleanup completed");
        }
    }
}
