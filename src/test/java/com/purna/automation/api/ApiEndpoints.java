package com.purna.automation.api;

public final class ApiEndpoints {
    public static final String POSTS = "/posts";
    private ApiEndpoints() { }
    public static String post(int id) {
        if (id <= 0) throw new IllegalArgumentException("Post id must be positive");
        return POSTS + "/" + id;
    }
}
