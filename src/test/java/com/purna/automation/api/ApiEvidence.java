package com.purna.automation.api;

import java.util.ArrayList;
import java.util.List;

public final class ApiEvidence {
    public record Exchange(String method, String location, int status, long durationMs,
                           String contentType, String requestStructure, String responseStructure, String errorType) { }
    private static final ThreadLocal<List<Exchange>> EXCHANGES = ThreadLocal.withInitial(ArrayList::new);
    private ApiEvidence() { }
    public static void begin() { EXCHANGES.remove(); }
    public static void record(Exchange exchange) {
        var records = EXCHANGES.get();
        if (records.size() == 10) records.remove(0);
        records.add(exchange);
    }
    public static List<Exchange> take() {
        try { return List.copyOf(EXCHANGES.get()); }
        finally { EXCHANGES.remove(); }
    }
}
