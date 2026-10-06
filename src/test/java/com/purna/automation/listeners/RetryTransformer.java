package com.purna.automation.listeners;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

public final class RetryTransformer implements IAnnotationTransformer {
    @Override public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method method) {
        Class owner = method == null ? testClass : method.getDeclaringClass();
        if (owner == null) return;
        String name = owner.getName();
        if (!name.startsWith("com.purna.automation.tests.") && !name.equals("com.purna.automation.api.ApiTest")) return;
        Class existing = annotation.getRetryAnalyzerClass();
        if (existing != null && !existing.getSimpleName().equals("DisabledRetryAnalyzer")) return;
        annotation.setRetryAnalyzer(RetryAnalyzer.class);
    }
}
