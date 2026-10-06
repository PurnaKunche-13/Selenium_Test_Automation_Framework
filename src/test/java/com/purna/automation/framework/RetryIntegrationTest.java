package com.purna.automation.framework;

import com.purna.automation.listeners.RetryAnalyzer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.testng.Assert;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class RetryIntegrationTest {
    public static class RecoveringProbe {
        private final ConcurrentHashMap<String, AtomicInteger> calls = new ConcurrentHashMap<>();
        @DataProvider(name = "rows") public Object[][] rows() { return new Object[][]{{"first"}, {"second"}}; }
        @Test(dataProvider = "rows", retryAnalyzer = RetryAnalyzer.class, groups = "retry-probe")
        public void recoversOnSecondAttempt(String row) {
            if (calls.computeIfAbsent(row, ignored -> new AtomicInteger()).incrementAndGet() == 1)
                Assert.fail("Controlled first-attempt failure");
        }
        @Test(retryAnalyzer = RetryAnalyzer.class, groups = "retry-probe")
        public void realSkipIsNotRetried() { throw new SkipException("Controlled skip"); }
    }
    public static class ExhaustedProbe {
        @Test(retryAnalyzer = RetryAnalyzer.class, groups = "retry-probe")
        public void alwaysFails() { Assert.fail("Controlled persistent failure"); }
    }
    private static final class Observer implements ITestListener {
        int passed;
        int failed;
        int scheduled;
        int skipped;
        @Override public void onTestStart(ITestResult result) { RetryAnalyzer.attempt(result); }
        @Override public void onTestSuccess(ITestResult result) { passed++; RetryAnalyzer.finish(result); }
        @Override public void onTestFailure(ITestResult result) { failed++; RetryAnalyzer.finish(result); }
        @Override public void onTestSkipped(ITestResult result) {
            if (RetryAnalyzer.scheduled(result)) scheduled++;
            else { skipped++; RetryAnalyzer.finish(result); }
        }
    }
    private TestNG run(Class<?> probe, Observer observer) {
        var runner = new TestNG();
        runner.setUseDefaultListeners(false);
        runner.setTestClasses(new Class<?>[]{probe});
        runner.addListener(observer);
        runner.run();
        return runner;
    }
    @Test(groups = "framework", description = "Controlled nested fixtures verify actual TestNG retry behavior")
    public void retriesArePerRowAndFinalFailuresRemainFailures() {
        String previous = System.getProperty("maxRetries");
        try {
            System.setProperty("maxRetries", "1");
            Observer recovering = new Observer();
            Assert.assertFalse(run(RecoveringProbe.class, recovering).hasFailure());
            Assert.assertEquals(recovering.passed, 2);
            Assert.assertEquals(recovering.scheduled, 2);
            Assert.assertEquals(recovering.skipped, 1);
            Observer exhausted = new Observer();
            Assert.assertTrue(run(ExhaustedProbe.class, exhausted).hasFailure());
            Assert.assertEquals(exhausted.scheduled, 1);
            Assert.assertEquals(exhausted.failed, 1);
            Assert.assertEquals(exhausted.passed, 0);
        } finally {
            if (previous == null) System.clearProperty("maxRetries"); else System.setProperty("maxRetries", previous);
        }
    }
}
