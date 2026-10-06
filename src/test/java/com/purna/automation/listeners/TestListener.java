package com.purna.automation.listeners;

import com.aventstack.extentreports.ExtentTest;
import com.purna.automation.api.ApiEvidence;
import com.purna.automation.base.BaseTest;
import com.purna.automation.utilities.ConfigReader;
import com.purna.automation.utilities.JsonUtils;
import com.purna.automation.utilities.SafeDiagnostics;
import com.purna.automation.utilities.ScreenshotUtils;
import com.purna.automation.utilities.TestDataUtils;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.testng.IExecutionListener;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

public final class TestListener implements ITestListener, IInvokedMethodListener, IExecutionListener {
    private static final Logger LOG = LoggerFactory.getLogger(TestListener.class);
    private static final String NODE = "automation.reportNode";
    public static final String EVIDENCE = "automation.screenshot";
    private static final String CAPTURE_ATTEMPTED = "automation.captureAttempted";
    private static final String API = "automation.apiEvidence";
    private static final String LOCATION = "automation.failureLocation";
    private static final String TIMESTAMP = "automation.failureTimestamp";
    private final LongAdder failedAttempts = new LongAdder();
    private final LongAdder recoveredTests = new LongAdder();
    private final LongAdder finalFailures = new LongAdder();
    private final LongAdder passedTests = new LongAdder();
    private final LongAdder retriedAttempts = new LongAdder();
    private final LongAdder skippedTests = new LongAdder();
    private final LongAdder configurationFailures = new LongAdder();

    @Override public void onExecutionStart() { RetryAnalyzer.clear(); ReportManager.start(); }
    @Override public void onExecutionFinish() {
        try {
            ReportManager.summary(Map.of("failedAttempts", failedAttempts.sum(), "recoveredTests", recoveredTests.sum(),
                    "finalFailures", finalFailures.sum(), "passedTests", passedTests.sum(),
                    "retriedAttempts", retriedAttempts.sum(), "skippedTests", skippedTests.sum(),
                    "configurationFailures", configurationFailures.sum()));
            ReportManager.flush();
        } finally { RetryAnalyzer.clear(); MDC.clear(); }
    }
    private String name(ITestResult result) {
        String name = result.getTestClass().getRealClass().getSimpleName() + "." + result.getMethod().getMethodName();
        for (Object parameter : result.getParameters()) {
            String id = null;
            if (parameter instanceof TestDataUtils.LoginCase row) id = row.id();
            else if (parameter instanceof TestDataUtils.CheckoutCase row) id = row.id();
            else if (parameter instanceof TestDataUtils.Product row) id = row.id();
            else if (parameter instanceof TestDataUtils.CustomerCase row) id = row.id();
            else if (parameter instanceof TestDataUtils.PostCase row) id = row.id();
            else if (parameter instanceof String text && text.matches("TC[0-9]{3}")) id = text;
            if (id != null) name += " [" + id + "]";
        }
        return name;
    }
    private String browser(ITestResult result) {
        return result.getInstance() instanceof BaseTest ? ConfigReader.get("browser") : "none (API/framework)";
    }
    private ExtentTest node(ITestResult result) {
        synchronized (result) {
            ExtentTest test = (ExtentTest) result.getAttribute(NODE);
            if (test == null) {
                String description = result.getMethod().getDescription();
                test = ReportManager.create(name(result) + " | attempt " + RetryAnalyzer.attempt(result), description == null ? "" : description);
                test.assignCategory(result.getTestClass().getRealClass().getName());
                String[] groups = result.getMethod().getGroups();
                if (groups.length > 0) test.assignCategory(groups);
                test.info("Class: " + result.getTestClass().getRealClass().getName() + "; method: " + result.getMethod().getMethodName());
                test.info("Browser: " + browser(result) + "; environment: " + ConfigReader.environment());
                result.setAttribute(NODE, test);
            }
            return test;
        }
    }
    private void capture(ITestResult result) {
        if (Boolean.TRUE.equals(result.getAttribute(CAPTURE_ATTEMPTED))) return;
        result.setAttribute(CAPTURE_ATTEMPTED, true);
        result.setAttribute(LOCATION, ScreenshotUtils.currentUrl());
        result.setAttribute(TIMESTAMP, Instant.now().toString());
        ScreenshotUtils.capture(name(result) + "-attempt-" + RetryAnalyzer.attempt(result))
                .ifPresent(evidence -> result.setAttribute(EVIDENCE, evidence));
    }
    private void fail(ITestResult result) {
        if (Boolean.TRUE.equals(result.getAttribute("automation.failureReported"))) return;
        result.setAttribute("automation.failureReported", true);
        var test = node(result);
        test.fail(SafeDiagnostics.html(SafeDiagnostics.exception(result.getThrowable())));
        test.info("Failure time (UTC): " + result.getAttribute(TIMESTAMP) + "; location: " + result.getAttribute(LOCATION));
        if (result.getAttribute(EVIDENCE) instanceof ScreenshotUtils.Evidence screenshot)
            test.addScreenCaptureFromBase64String(screenshot.base64(), screenshot.path().getFileName().toString());
        Object api = result.getAttribute(API);
        if (api != null) {
            String safeTrace = JsonUtils.toJson(api);
            test.info("Sanitized API evidence: " + SafeDiagnostics.html(safeTrace));
            LOG.error("event=api_failure_evidence trace={}", safeTrace);
        }
        LOG.error("event=test_failed test={} attempt={} exception={}", name(result), RetryAnalyzer.attempt(result),
                result.getThrowable() == null ? "unknown" : result.getThrowable().getClass().getName());
    }
    private void duration(ITestResult result) {
        node(result).info("Execution time: " + Math.max(0, result.getEndMillis() - result.getStartMillis()) + " ms");
    }
    @Override public void onTestStart(ITestResult result) {
        RetryAnalyzer.attempt(result);
        node(result);
        LOG.info("event=test_started test={} attempt={}", name(result), RetryAnalyzer.attempt(result));
    }
    @Override public void beforeInvocation(IInvokedMethod method, ITestResult result) {
        if (method.isTestMethod()) ApiEvidence.begin();
        MDC.put("test", name(result));
        MDC.put("environment", ConfigReader.environment());
        MDC.put("browser", browser(result));
    }
    @Override public void afterInvocation(IInvokedMethod method, ITestResult result) {
        try {
            if (method.isTestMethod()) result.setAttribute(API, ApiEvidence.take());
            if (result.getStatus() != ITestResult.FAILURE) return;
            capture(result);
            fail(result);
            if (method.isConfigurationMethod()) {
                configurationFailures.increment();
                node(result).assignCategory("configuration");
                fail(result);
                duration(result);
            }
        } finally { MDC.clear(); }
    }
    @Override public void onTestSuccess(ITestResult result) {
        int attempt = RetryAnalyzer.attempt(result);
        if (attempt > 1) {
            recoveredTests.increment();
            node(result).assignCategory("flaky");
            node(result).warning("Recovered after a failed attempt; earlier failure remains in this report");
        }
        passedTests.increment();
        node(result).pass("Assertions passed on attempt " + attempt);
        duration(result);
        RetryAnalyzer.finish(result);
        LOG.info("event=test_passed test={} attempt={}", name(result), attempt);
    }
    @Override public void onTestFailure(ITestResult result) {
        failedAttempts.increment();
        capture(result);
        fail(result);
        duration(result);
        if (RetryAnalyzer.scheduled(result)) {
            retriedAttempts.increment();
            node(result).warning("Retry scheduled; this failed attempt is retained");
        } else {
            finalFailures.increment();
            RetryAnalyzer.finish(result);
        }
    }
    @Override public void onTestSkipped(ITestResult result) {
        // TestNG represents attempts selected for retry as skipped. Preserve their real failed outcome.
        if (RetryAnalyzer.scheduled(result)) {
            failedAttempts.increment(); retriedAttempts.increment();
            capture(result); fail(result); duration(result);
            node(result).warning("Retry scheduled; this failed attempt is retained");
        } else {
            skippedTests.increment();
            node(result).skip("Skipped; no retry scheduled");
            if (result.getThrowable() != null) node(result).info(SafeDiagnostics.exception(result.getThrowable()));
            duration(result); RetryAnalyzer.finish(result);
        }
    }
}
