package com.example.myapplicationdemo.viewmodel;

public class AuthState {

    public enum Status { LOADING, SUCCESS, ERROR }

    public final Status status;
    public final String message;

    private AuthState(Status status, String message) {
        this.status = status;
        this.message = message;
    }

    public static AuthState loading() { return new AuthState(Status.LOADING, null); }
    public static AuthState success() { return new AuthState(Status.SUCCESS, null); }
    public static AuthState error(String message) { return new AuthState(Status.ERROR, message); }
}
