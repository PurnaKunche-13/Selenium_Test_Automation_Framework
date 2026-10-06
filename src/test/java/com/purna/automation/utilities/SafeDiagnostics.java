package com.purna.automation.utilities;

import java.net.URI;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/** Conservative diagnostics: omit unknown URL paths, query values and exception messages. */
public final class SafeDiagnostics {
    private SafeDiagnostics() { }
    public static String location(String value) {
        try {
            URI uri = URI.create(value);
            if ("data".equalsIgnoreCase(uri.getScheme())) return "data:[content omitted]";
            if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme()))
                return "[non-HTTP location omitted]";
            if (uri.getHost() == null) return "[location unavailable]";
            String path = uri.getRawPath();
            if (path == null) path = "";
            if (!path.matches("/(?:[A-Za-z][A-Za-z0-9_-]*\\.html)?")
                    && !path.matches("/posts(?:/[0-9]+)?")) path = "/[path omitted]";
            return uri.getScheme() + "://" + uri.getHost() + (uri.getPort() < 0 ? "" : ":" + uri.getPort()) + path;
        } catch (IllegalArgumentException exception) { return "[location unavailable]"; }
    }
    public static String html(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
    public static String contentType(String value) {
        if (value == null) return "[missing]";
        String type = value.split(";", 2)[0].trim();
        return type.matches("[A-Za-z0-9.+-]+/[A-Za-z0-9.+-]+") ? type : "[unexpected content type]";
    }
    public static String exception(Throwable failure) {
        if (failure == null) return "Failure without an exception";
        StringBuilder text = new StringBuilder();
        Set<Throwable> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        int causes = 0;
        while (failure != null && seen.add(failure) && causes++ < 8) {
            text.append(failure.getClass().getName()).append(": [message omitted for safe reporting]\n");
            for (int index = 0; index < Math.min(8, failure.getStackTrace().length); index++)
                text.append("  at ").append(failure.getStackTrace()[index]).append('\n');
            failure = failure.getCause();
            if (failure != null) text.append("Caused by: ");
        }
        return text.toString();
    }
}
