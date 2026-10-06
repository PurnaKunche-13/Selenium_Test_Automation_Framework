package com.purna.automation.listeners;

import com.purna.automation.utilities.ConfigReader;
import com.purna.automation.utilities.RetryBudget;
import java.util.Arrays;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RetryAnalyzer implements IRetryAnalyzer {
    public static final String ATTEMPT = "automation.attempt";
    public static final String SCHEDULED = "automation.retryScheduled";
    private static final RetryBudget BUDGET = new RetryBudget();
    private static final Logger LOG = LoggerFactory.getLogger(RetryAnalyzer.class);

    private static final class InvocationKey {
        private final Object instance;
        private final Object context;
        private final String method;
        private final Object[] parameters;
        InvocationKey(ITestResult result) {
            instance = result.getInstance();
            context = result.getTestContext();
            method = result.getMethod().getQualifiedName();
            parameters = result.getParameters().clone();
        }
        @Override public boolean equals(Object other) {
            return other instanceof InvocationKey key && instance == key.instance && context == key.context
                    && method.equals(key.method) && Arrays.deepEquals(parameters, key.parameters);
        }
        @Override public int hashCode() {
            return 31 * (31 * System.identityHashCode(instance) + System.identityHashCode(context))
                    + 31 * method.hashCode() + Arrays.deepHashCode(parameters);
        }
    }
    public static int attempt(ITestResult result) {
        Object saved = result.getAttribute(ATTEMPT);
        if (saved instanceof Integer value) return value;
        int value = BUDGET.used(new InvocationKey(result)) + 1;
        result.setAttribute(ATTEMPT, value);
        return value;
    }
    @Override public boolean retry(ITestResult result) {
        if (result.getStatus() != ITestResult.FAILURE) return false;
        if (Arrays.stream(result.getMethod().getGroups()).anyMatch(group -> group.equals("diagnostic") || group.equals("framework")))
            return false;
        int failedAttempt = attempt(result);
        if (!BUDGET.reserve(new InvocationKey(result), ConfigReader.nonNegativeInt("maxRetries"))) return false;
        result.setAttribute(SCHEDULED, true);
        LOG.warn("event=retry_scheduled method={} failedAttempt={} nextAttempt={}",
                result.getMethod().getMethodName(), failedAttempt, failedAttempt + 1);
        return true;
    }
    public static boolean scheduled(ITestResult result) { return Boolean.TRUE.equals(result.getAttribute(SCHEDULED)); }
    public static void finish(ITestResult result) { BUDGET.finish(new InvocationKey(result)); }
    public static void clear() { BUDGET.clear(); }
}
