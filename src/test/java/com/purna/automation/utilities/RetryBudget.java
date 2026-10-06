package com.purna.automation.utilities;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Tracks extra attempts separately for each logical invocation. Keys are never logged. */
public final class RetryBudget {
    private final ConcurrentMap<Object, Integer> used = new ConcurrentHashMap<>();
    public synchronized boolean reserve(Object key, int maximum) {
        if (maximum < 0) throw new IllegalArgumentException("Retry count must be non-negative");
        int count = used.getOrDefault(key, 0);
        if (count >= maximum) return false;
        used.put(key, count + 1);
        return true;
    }
    public int used(Object key) { return used.getOrDefault(key, 0); }
    public void finish(Object key) { used.remove(key); }
    public void clear() { used.clear(); }
}
