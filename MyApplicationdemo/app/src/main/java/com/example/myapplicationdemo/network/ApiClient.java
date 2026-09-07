package com.example.myapplicationdemo.network;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    // TODO: 後端上線後替換為真實 URL，並移除 MockInterceptor
    private static final String BASE_URL = "https://api.xian-app.example.com/";
    private static ApiService instance;

    public static synchronized ApiService getInstance() {
        if (instance == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor())
                    .addInterceptor(new MockInterceptor()) // TODO: 後端完成後移除此行
                    .build();

            instance = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(ApiService.class);
        }
        return instance;
    }
}
