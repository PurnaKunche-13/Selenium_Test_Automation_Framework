package com.purna.automation.listeners;

import com.aventstack.extentreports.ExtentReports;
import com.purna.automation.utilities.ConfigReader;
import com.purna.automation.utilities.JsonUtils;
import java.util.Map;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public final class ReportManager {
    private static final String RUN_ID = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")
            .withZone(ZoneOffset.UTC).format(Instant.now()) + "-" + UUID.randomUUID().toString().substring(0, 8);
    private static ExtentReports report;
    private ReportManager() { }
    public static String runId() { return RUN_ID; }

    public static synchronized void start() {
        if (report != null) return;
        Path directory = Path.of("reports", RUN_ID);
        try { Files.createDirectories(directory); }
        catch (IOException exception) { throw new IllegalStateException("Cannot create report directory", exception); }
        var spark = new ExtentSparkReporter(directory.resolve("index.html").toString());
        spark.config().setDocumentTitle("Selenium Test Automation Framework");
        spark.config().setReportName("UI and API execution report");
        report = new ExtentReports();
        report.attachReporter(spark);
        report.setSystemInfo("Java", System.getProperty("java.version"));
        report.setSystemInfo("OS", System.getProperty("os.name"));
        report.setSystemInfo("Run ID", RUN_ID);
        report.setSystemInfo("Environment", ConfigReader.environment());
        report.setSystemInfo("Configured UI browser", ConfigReader.get("browser"));
        report.setSystemInfo("Maximum extra attempts", ConfigReader.get("maxRetries"));
    }
    public static synchronized ExtentTest create(String name, String description) {
        start();
        return report.createTest(name, description);
    }
    public static Path directory() { return Path.of("reports", RUN_ID); }
    public static synchronized void summary(Map<String, Long> counts) {
        try { Files.writeString(directory().resolve("attempt-summary.json"), JsonUtils.toJson(counts)); }
        catch (IOException exception) { throw new IllegalStateException("Cannot write attempt summary", exception); }
    }
    public static synchronized void flush() { if (report != null) report.flush(); }
}
