package com.purna.automation.utilities;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class JsonUtils {
    private static final Gson GSON = new Gson();
    private JsonUtils() { }
    public static <T> T read(String resource, Class<T> type) {
        InputStream input = JsonUtils.class.getClassLoader().getResourceAsStream(resource);
        if (input == null) throw new IllegalArgumentException("Missing JSON resource: " + resource);
        try (var reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
            T value = GSON.fromJson(reader, type);
            if (value == null) throw new IllegalArgumentException("Empty JSON resource: " + resource);
            return value;
        } catch (IOException | JsonSyntaxException | JsonIOException exception) {
            throw new IllegalArgumentException("Invalid JSON resource: " + resource, exception);
        }
    }
    public static String toJson(Object value) { return GSON.toJson(value); }
    public static String structure(String json) {
        if (json == null || json.isBlank()) return "[empty body]";
        if (json.length() > 16384) return "[body omitted; characters=" + json.length() + "]";
        try { return GSON.toJson(shape(JsonParser.parseString(json), 0)); }
        catch (RuntimeException exception) { return "[non-JSON body omitted]"; }
    }
    private static JsonElement shape(JsonElement value, int depth) {
        if (depth > 8) return GSON.toJsonTree("[depth limit]");
        if (value.isJsonObject()) {
            JsonObject result = new JsonObject();
            for (var entry : value.getAsJsonObject().entrySet()) {
                String key = entry.getKey();
                if (key.length() > 80) key = "[long field name omitted]";
                if (key.toLowerCase(java.util.Locale.ROOT).matches(".*(password|token|secret|authorization|api.?key|cookie|pin).*"))
                    result.addProperty(key, "[REDACTED]");
                else result.add(key, shape(entry.getValue(), depth + 1));
            }
            return result;
        }
        if (value.isJsonArray()) {
            JsonArray result = new JsonArray();
            int count = 0;
            for (JsonElement item : value.getAsJsonArray()) {
                if (count++ == 3) { result.add("[remaining items omitted]"); break; }
                result.add(shape(item, depth + 1));
            }
            return result;
        }
        if (value.isJsonNull()) return value;
        var primitive = value.getAsJsonPrimitive();
        return GSON.toJsonTree(primitive.isNumber() ? "[number]" : primitive.isBoolean() ? "[boolean]" : "[string]");
    }
}
