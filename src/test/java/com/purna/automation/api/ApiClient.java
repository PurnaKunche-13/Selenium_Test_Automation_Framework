package com.purna.automation.api;

import com.purna.automation.utilities.ConfigReader;
import com.purna.automation.utilities.JsonUtils;
import com.purna.automation.utilities.SafeDiagnostics;
import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ApiClient {
    private static final Logger LOG = LoggerFactory.getLogger(ApiClient.class);
    private final String baseUrl;
    public ApiClient() { this(ConfigReader.get("apiBaseUrl")); }
    public ApiClient(String baseUrl) { this.baseUrl = baseUrl; }
    private RequestSpecification request() {
        var http = HttpClientConfig.httpClientConfig()
                .setParam("http.connection.timeout", ConfigReader.positiveInt("apiConnectTimeoutMs"))
                .setParam("http.socket.timeout", ConfigReader.positiveInt("apiReadTimeoutMs"));
        // Each call owns its specification; no shared RestAssured.baseURI mutation.
        return RestAssured.given().baseUri(baseUrl).accept(ContentType.JSON)
                .config(RestAssuredConfig.config().httpClient(http)).filter(new SafeApiFilter());
    }
    private Response send(Method method, String path, Object body) {
        var request = request();
        if (body != null) request.contentType(ContentType.JSON).body(JsonUtils.toJson(body));
        LOG.info("event=api_request method={} location={}", method, SafeDiagnostics.location(baseUrl + path));
        Response response = request.request(method, path);
        LOG.info("event=api_response method={} status={} durationMs={}", method, response.statusCode(), response.time());
        return response;
    }
    public Response get(String path) { return send(Method.GET, path, null); }
    public Response post(String path, Object body) { return send(Method.POST, path, body); }
    public Response put(String path, Object body) { return send(Method.PUT, path, body); }
    public Response delete(String path) { return send(Method.DELETE, path, null); }
}
