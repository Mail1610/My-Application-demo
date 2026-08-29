package com.example.myapplicationdemo.utils;

import android.util.Log;

public class LogUtils {

    private static final String TAG = "顯App";

    public static void info(String message) {
        Log.i(TAG, message);
    }

    public static void error(String message) {
        Log.e(TAG, message);
    }

    public static void error(String message, Throwable t) {
        Log.e(TAG, message, t);
    }

    public static void debug(String message) {
        Log.d(TAG, message);
    }

    public static void warn(String message) {
        Log.w(TAG, message);
    }
}
