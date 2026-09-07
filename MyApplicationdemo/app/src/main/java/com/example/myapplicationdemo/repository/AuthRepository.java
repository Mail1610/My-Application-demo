package com.example.myapplicationdemo.repository;

import com.example.myapplicationdemo.network.ApiClient;
import com.example.myapplicationdemo.network.ApiService;
import com.example.myapplicationdemo.network.dto.AuthResponse;
import com.example.myapplicationdemo.network.dto.LoginRequest;
import com.example.myapplicationdemo.network.dto.RegisterRequest;
import java.io.IOException;

public class AuthRepository {

    private final ApiService api = ApiClient.getInstance();

    public AuthResponse login(String email, String password) throws IOException {
        return api.login(new LoginRequest(email, password)).execute().body();
    }

    public AuthResponse register(String username, String email, String password) throws IOException {
        return api.register(new RegisterRequest(username, email, password)).execute().body();
    }
}
