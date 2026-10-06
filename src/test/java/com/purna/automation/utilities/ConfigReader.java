package com.purna.automation.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

public final class ConfigReader {
    private static final Properties DEFAULTS = load("config.properties");
    private static final Set<String> ENVIRONMENTS = Set.of("local", "qa", "stage");
    private ConfigReader() { }

    private static Properties load(String resource) {
        Properties properties = new Properties();
        try (InputStream stream = ConfigReader.class.getClassLoader().getResourceAsStream(resource)) {
            if (stream == null) throw new IllegalStateException("Missing configuration resource: " + resource);
            properties.load(stream);
            return properties;
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot load configuration: " + resource, exception);
        }
    }

    private static String override(String key) {
        String value = System.getProperty(key);
        if (value != null) return value;
        String environmentKey = "AUTOMATION_" + key.replaceAll("([a-z0-9])([A-Z])", "$1_$2")
                .replace('.', '_').toUpperCase(Locale.ROOT);
        return System.getenv(environmentKey);
    }

    public static String environment() {
        String selected = override("env");
        if (selected == null) selected = DEFAULTS.getProperty("env", "local");
        selected = selected.trim().toLowerCase(Locale.ROOT);
        if (!ENVIRONMENTS.contains(selected))
            throw new IllegalArgumentException("env must be local, qa or stage");
        return selected;
    }

    private static final class Profiles {
        private static final Properties QA = load("config-qa.properties");
        private static final Properties STAGE = load("config-stage.properties");
    }

    public static String get(String key) {
        if (key.equals("env")) return environment();
        String selected = environment();
        String value = override(key);
        if (value == null && !selected.equals("local"))
            value = (selected.equals("qa") ? Profiles.QA : Profiles.STAGE).getProperty(key);
        if (value == null) value = DEFAULTS.getProperty(key);
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Missing configuration: " + key);
        return value.trim();
    }

    public static int nonNegativeInt(String key) {
        try {
            int value = Integer.parseInt(get(key));
            if (value < 0) throw new NumberFormatException("negative");
            return value;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(key + " must be a non-negative integer", exception);
        }
    }
    public static int positiveInt(String key) {
        int value = nonNegativeInt(key);
        if (value == 0) throw new IllegalArgumentException(key + " must be greater than zero");
        return value;
    }
    public static boolean bool(String key) {
        String value = get(key);
        if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false"))
            throw new IllegalArgumentException(key + " must be true or false");
        return Boolean.parseBoolean(value);
    }
}
