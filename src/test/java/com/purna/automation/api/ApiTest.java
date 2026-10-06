package com.purna.automation.api;

import com.purna.automation.utilities.ConfigReader;
import com.purna.automation.utilities.TestDataUtils;
import io.restassured.response.Response;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = {"api", "regression"})
public class ApiTest {
    private final ApiClient client = new ApiClient();
    private void assertJson(Response response, int status) {
        Assert.assertEquals(response.statusCode(), status, "HTTP status");
        Assert.assertNotNull(response.contentType(), "Missing Content-Type");
        Assert.assertTrue(response.contentType().toLowerCase(Locale.ROOT).contains("application/json"), "JSON Content-Type");
        int budget = ConfigReader.nonNegativeInt("apiMaxResponseTimeMs");
        if (budget > 0) Assert.assertTrue(response.time() <= budget, "Response exceeded optional time budget: " + response.time());
    }
    @Test(groups = "smoke", description = "TC027: GET known post")
    public void getPost() {
        var response = client.get(ApiEndpoints.post(1));
        assertJson(response, 200);
        assertPostStructure(response.jsonPath().getMap("$"));
        Assert.assertEquals(response.jsonPath().getInt("id"), 1);
        Assert.assertEquals(response.jsonPath().getInt("userId"), 1);
        Assert.assertFalse(response.jsonPath().getString("title").isBlank());
        Assert.assertFalse(response.jsonPath().getString("body").isBlank());
    }
    @Test(description = "TC028: GET post collection")
    public void listPosts() {
        var response = client.get(ApiEndpoints.POSTS);
        assertJson(response, 200);
        List<Map<String, Object>> posts = response.jsonPath().getList("$");
        posts.forEach(this::assertPostStructure);
        List<Integer> ids = response.jsonPath().getList("id", Integer.class);
        Assert.assertFalse(ids.isEmpty());
        Assert.assertTrue(ids.contains(1));
        Assert.assertEquals(ids.stream().distinct().count(), (long) ids.size(), "Unique post IDs");
    }
    @Test(description = "TC029: GET missing post returns 404")
    public void missingPost() {
        var response = client.get(ApiEndpoints.post(999999));
        assertJson(response, 404);
        Assert.assertTrue(response.jsonPath().getMap("$").isEmpty());
    }
    @Test(dataProvider = "posts", dataProviderClass = TestDataUtils.class,
          description = "TC030/TC042: POST echoes JSON payloads and a generated ID")
    public void createPost(TestDataUtils.PostCase row) {
        var data = row.payload();
        var response = client.post(ApiEndpoints.POSTS, data);
        assertJson(response, 201);
        assertPayload(response, data);
        Assert.assertTrue(response.jsonPath().getInt("id") > 0);
    }
    @Test(description = "TC031: PUT echoes updated fields for an existing post")
    public void updatePost() {
        var data = TestDataUtils.post();
        var response = client.put(ApiEndpoints.post(1), data);
        assertJson(response, 200);
        assertPayload(response, data);
        Assert.assertEquals(response.jsonPath().getInt("id"), 1);
    }
    @Test(description = "TC032: DELETE returns the demo API's empty JSON response")
    public void deletePost() {
        var response = client.delete(ApiEndpoints.post(1));
        assertJson(response, 200);
        Assert.assertTrue(response.jsonPath().getMap("$").isEmpty());
    }
    private void assertPayload(Response response, TestDataUtils.PostData data) {
        assertPostStructure(response.jsonPath().getMap("$"));
        Assert.assertEquals(response.jsonPath().getInt("userId"), data.userId());
        Assert.assertEquals(response.jsonPath().getString("title"), data.title());
        Assert.assertEquals(response.jsonPath().getString("body"), data.body());
    }
    private void assertPostStructure(Map<String, Object> post) {
        Assert.assertNotNull(post, "Post must be an object");
        Assert.assertTrue(post.keySet().containsAll(List.of("id", "userId", "title", "body")), "Required post fields");
        Assert.assertTrue(post.get("id") instanceof Number, "id must be numeric");
        Assert.assertTrue(post.get("userId") instanceof Number, "userId must be numeric");
        Assert.assertTrue(post.get("title") instanceof String, "title must be text");
        Assert.assertTrue(post.get("body") instanceof String, "body must be text");
    }
}
