package com.purna.automation.utilities;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.testng.annotations.DataProvider;

public final class TestDataUtils {
    private TestDataUtils() { }
    public record LoginCase(String id, String username, String password, boolean successful, String error) {
        public LoginCase {
            Objects.requireNonNull(id); Objects.requireNonNull(username); Objects.requireNonNull(password);
            Objects.requireNonNull(error);
        }
        // TestNG may print data-provider parameters: never print credentials.
        @Override public String toString() { return id; }
    }
    public record Product(String id, String name, BigDecimal price) {
        @Override public String toString() { return id; }
    }
    public record Customer(String firstName, String lastName, String postalCode) { }
    public record CheckoutCase(String id, String firstName, String lastName, String postalCode, String error) {
        @Override public String toString() { return id; }
    }
    public record CustomerCase(String id, String firstName, String lastName, String postalCode) {
        @Override public String toString() { return id; }
    }
    public record PostCase(String id, PostData payload) {
        @Override public String toString() { return id; }
    }
    public record PostData(int userId, String title, String body) { }

    public static LoginCase[] loginCases() { return JsonUtils.read("testdata/loginData.json", LoginCase[].class); }
    public static LoginCase validLogin() {
        return Arrays.stream(loginCases()).filter(LoginCase::successful).findFirst().orElseThrow();
    }
    public static List<Product> products() { return List.of(JsonUtils.read("testdata/products.json", Product[].class)); }
    public static Customer customer() { return JsonUtils.read("testdata/customer.json", Customer.class); }
    public static PostData post() { return JsonUtils.read("testdata/post.json", PostData.class); }

    @DataProvider(name = "validLogins")
    public static Object[][] validLogins() {
        return Arrays.stream(loginCases()).filter(LoginCase::successful).map(row -> new Object[]{row}).toArray(Object[][]::new);
    }
    @DataProvider(name = "products")
    public static Object[][] productRows() {
        return products().stream().map(row -> new Object[]{row}).toArray(Object[][]::new);
    }
    @DataProvider(name = "customers")
    public static Object[][] customers() {
        return Arrays.stream(JsonUtils.read("testdata/customers.json", CustomerCase[].class))
                .map(row -> new Object[]{row}).toArray(Object[][]::new);
    }
    @DataProvider(name = "posts")
    public static Object[][] posts() {
        return Arrays.stream(JsonUtils.read("testdata/posts.json", PostCase[].class))
                .map(row -> new Object[]{row}).toArray(Object[][]::new);
    }
    @DataProvider(name = "invalidLogins")
    public static Object[][] invalidLogins() {
        return Arrays.stream(loginCases()).filter(row -> !row.successful()).map(row -> new Object[]{row}).toArray(Object[][]::new);
    }
    @DataProvider(name = "checkoutErrors")
    public static Object[][] checkoutErrors() {
        return Arrays.stream(JsonUtils.read("testdata/checkoutErrors.json", CheckoutCase[].class))
                .map(row -> new Object[]{row}).toArray(Object[][]::new);
    }
    @DataProvider(name = "sortModes")
    public static Object[][] sortModes() {
        return JsonUtils.read("testdata/sortModes.json", String[][].class);
    }
}
