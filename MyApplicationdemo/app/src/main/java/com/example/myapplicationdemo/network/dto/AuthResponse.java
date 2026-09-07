package com.example.myapplicationdemo.network.dto;

public class AuthResponse {
    private String token;
    private int userId;
    private String email;
    private String username;

    public String getToken() { return token; }
    public int getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getUsername() { return username; }
}
