package com.purna.automation.utilities;

import com.purna.automation.base.DriverFactory;
import com.purna.automation.listeners.ReportManager;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ScreenshotUtils {
    private static final Logger LOG = LoggerFactory.getLogger(ScreenshotUtils.class);
    private ScreenshotUtils() { }
    public record Evidence(Path path, String base64, String url, String browser, String timestamp) { }
    public static String currentUrl() {
        try { return DriverFactory.current().map(driver -> SafeDiagnostics.location(driver.getCurrentUrl())).orElse("[no browser]"); }
        catch (RuntimeException failure) { return "[URL unavailable]"; }
    }
    public static Optional<Evidence> capture(String name) {
        var driver = DriverFactory.current();
        if (driver.isEmpty() || !(driver.get() instanceof TakesScreenshot screenshotDriver)) return Optional.empty();
        try {
            byte[] png = screenshotDriver.getScreenshotAs(OutputType.BYTES);
            Path directory = Path.of("screenshots", ReportManager.runId());
            Files.createDirectories(directory);
            String safeName = name.replaceAll("[^a-zA-Z0-9._-]", "_");
            if (safeName.length() > 100) safeName = safeName.substring(0, 100);
            Instant captured = Instant.now();
            String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS").withZone(ZoneOffset.UTC).format(captured);
            Path path = directory.resolve(safeName + "-" + timestamp + "-" + UUID.randomUUID() + ".png");
            Files.write(path, png);
            String url = currentUrl();
            String browser = ConfigReader.get("browser");
            try {
                Files.writeString(Path.of(path + ".json"), JsonUtils.toJson(Map.of(
                        "test", name, "timestamp", captured.toString(), "url", url, "browser", browser)));
            } catch (java.io.IOException metadataFailure) {
                LOG.warn("event=screenshot_metadata_unavailable exception={}", metadataFailure.getClass().getSimpleName());
            }
            LOG.info("event=screenshot_saved file={}", path);
            return Optional.of(new Evidence(path, Base64.getEncoder().encodeToString(png), url, browser, captured.toString()));
        } catch (Exception exception) {
            // Evidence failure must never replace the original test failure.
            LOG.warn("Screenshot unavailable: {}", exception.getClass().getSimpleName());
            return Optional.empty();
        }
    }
}
