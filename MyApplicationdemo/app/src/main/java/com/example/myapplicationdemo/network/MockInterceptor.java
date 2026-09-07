package com.example.myapplicationdemo.network;

import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MockInterceptor implements Interceptor {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    @Override
    public Response intercept(Chain chain) throws IOException {
        String path = chain.request().url().encodedPath();
        String body = resolveBody(path);

        return new Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create(body, JSON))
                .build();
    }

    private String resolveBody(String path) {
        if (path.contains("/auth/login") || path.contains("/auth/register")) {
            return "{\"token\":\"mock-jwt-token-abc123\","
                    + "\"userId\":1,"
                    + "\"email\":\"user@example.com\","
                    + "\"username\":\"測試使用者\"}";
        }
        return "{}";
    }
}
