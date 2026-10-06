package com.purna.automation.framework;

import com.purna.automation.api.ApiClient;
import com.purna.automation.api.ApiEvidence;
import com.purna.automation.utilities.RetryBudget;
import com.purna.automation.utilities.SafeDiagnostics;
import com.purna.automation.utilities.ConfigReader;
import com.purna.automation.utilities.JsonUtils;
import com.purna.automation.utilities.TestDataUtils;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** Checks framework plumbing using a local HTTP server; does not validate the public API. */
public class FrameworkSelfTest {
    private HttpServer server;
    private ApiClient client;
    @BeforeClass(alwaysRun = true)
    public void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/echo", exchange -> {
            try {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                byte[] response = JsonUtils.toJson(Map.of("method", exchange.getRequestMethod(), "body", body))
                        .getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length);
                exchange.getResponseBody().write(response);
            } finally { exchange.close(); }
        });
        server.start();
        client = new ApiClient("http://127.0.0.1:" + server.getAddress().getPort());
    }
    @AfterClass(alwaysRun = true)
    public void stopServer() { if (server != null) server.stop(0); }
    @DataProvider(name = "verbs")
    public Object[][] verbs() { return new Object[][]{{"GET"}, {"POST"}, {"PUT"}, {"DELETE"}}; }
    @Test(dataProvider = "verbs", groups = "framework")
    public void apiClientSendsCorrectMethodAndJson(String method) {
        var payload = Map.of("title", "Local framework check");
        var response = switch (method) {
            case "GET" -> client.get("/echo");
            case "POST" -> client.post("/echo", payload);
            case "PUT" -> client.put("/echo", payload);
            case "DELETE" -> client.delete("/echo");
            default -> throw new IllegalArgumentException(method);
        };
        Assert.assertEquals(response.statusCode(), 200);
        Assert.assertEquals(response.jsonPath().getString("method"), method);
        String expected = method.equals("POST") || method.equals("PUT") ? JsonUtils.toJson(payload) : "";
        Assert.assertEquals(response.jsonPath().getString("body"), expected);
    }
    @Test(groups = "framework")
    public void dataLoadsWithoutLeakingLoginPasswords() {
        var valid = TestDataUtils.validLogin();
        Assert.assertTrue(valid.successful());
        Assert.assertEquals(valid.toString(), valid.id());
        Assert.assertFalse(valid.toString().contains(valid.password()));
        Assert.assertEquals(TestDataUtils.validLogins().length, 2);
        Assert.assertEquals(TestDataUtils.invalidLogins().length, 5);
        Assert.assertEquals(TestDataUtils.products().size(), 6);
        Assert.assertEquals(TestDataUtils.checkoutErrors().length, 3);
        Assert.assertEquals(TestDataUtils.sortModes().length, 4);
        Assert.assertEquals(TestDataUtils.customers().length, 2);
        Assert.assertEquals(TestDataUtils.posts().length, 2);
    }
    @Test(groups = "framework")
    public void systemPropertyOverridesDefault() {
        String original = System.getProperty("explicitWait");
        try {
            System.setProperty("explicitWait", "23");
            Assert.assertEquals(ConfigReader.positiveInt("explicitWait"), 23);
        } finally {
            if (original == null) System.clearProperty("explicitWait");
            else System.setProperty("explicitWait", original);
        }
    }
    @Test(groups = "framework")
    public void environmentProfilesFallbackAndOverride() {
        String oldEnv = System.getProperty("env");
        String oldWait = System.getProperty("explicitWait");
        try {
            System.clearProperty("explicitWait");
            System.setProperty("env", "qa");
            String environmentWait = System.getenv("AUTOMATION_EXPLICIT_WAIT");
            Assert.assertEquals(ConfigReader.get("explicitWait"), environmentWait == null ? "20" : environmentWait.trim());
            System.setProperty("env", "stage");
            Assert.assertEquals(ConfigReader.get("explicitWait"), environmentWait == null ? "25" : environmentWait.trim());
            System.setProperty("explicitWait", "7");
            Assert.assertEquals(ConfigReader.positiveInt("explicitWait"), 7);
            System.setProperty("env", "unknown");
            Assert.expectThrows(IllegalArgumentException.class, ConfigReader::environment);
        } finally {
            if (oldEnv == null) System.clearProperty("env"); else System.setProperty("env", oldEnv);
            if (oldWait == null) System.clearProperty("explicitWait"); else System.setProperty("explicitWait", oldWait);
        }
    }
    @Test(groups = "framework")
    public void retryBudgetIsBoundedAndIndependent() {
        var budget = new RetryBudget();
        Assert.assertFalse(budget.reserve("no retries", 0));
        Assert.assertTrue(budget.reserve("first row", 1));
        Assert.assertFalse(budget.reserve("first row", 1));
        Assert.assertTrue(budget.reserve("second row", 1));
        budget.finish("first row");
        Assert.assertEquals(budget.used("first row"), 0);
    }
    @Test(groups = "framework")
    public void safeApiEvidenceOmitsAllPayloadValues() {
        ApiEvidence.begin();
        var response = client.post("/echo", Map.of("password", "fixture-secret", "title", "also-omitted"));
        Assert.assertEquals(response.statusCode(), 200);
        var records = ApiEvidence.take();
        Assert.assertEquals(records.size(), 1);
        String trace = JsonUtils.toJson(records);
        Assert.assertFalse(trace.contains("fixture-secret"));
        Assert.assertFalse(trace.contains("also-omitted"));
        Assert.assertTrue(trace.contains("REDACTED"));
    }
    @Test(groups = "framework")
    public void malformedBodiesAndLocationsDoNotLeakContent() {
        Assert.assertFalse(JsonUtils.structure("<html>secret</html>").contains("secret"));
        Assert.assertEquals(SafeDiagnostics.location("https://user:password@example.test/posts/1?token=secret"),
                "https://example.test/posts/1");
        Assert.assertFalse(SafeDiagnostics.exception(new IllegalArgumentException("fixture-secret")).contains("fixture-secret"));
    }
}
