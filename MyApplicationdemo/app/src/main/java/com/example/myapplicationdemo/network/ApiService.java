package com.example.myapplicationdemo.network;

import com.example.myapplicationdemo.network.dto.AuthResponse;
import com.example.myapplicationdemo.network.dto.LoginRequest;
import com.example.myapplicationdemo.network.dto.RegisterRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    @POST("api/auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("api/auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);
}
