package com.example.myapplicationdemo.network;

import com.example.myapplicationdemo.MyApplication;
import com.example.myapplicationdemo.utils.SessionManager;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        String path = request.url().encodedPath();

        if (!path.contains("/auth/")) {
            String token = new SessionManager(MyApplication.getContext()).getToken();
            if (token != null) {
                request = request.newBuilder()
                        .header("Authorization", "Bearer " + token)
                        .build();
            }
        }

        return chain.proceed(request);
    }
}
