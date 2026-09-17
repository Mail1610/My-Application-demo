package com.example.myapplicationdemo.utils;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.UUID;

public class SessionManager {

    private static final String PREF_NAME = "xian_session";
    private static final String KEY_DEVICE_ID = "device_id";
    private static final String KEY_NICKNAME = "nickname";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public String getDeviceId() {
        String id = prefs.getString(KEY_DEVICE_ID, null);
        if (id == null) {
            id = UUID.randomUUID().toString();
            prefs.edit().putString(KEY_DEVICE_ID, id).apply();
        }
        return id;
    }

    public void saveNickname(String nickname) {
        prefs.edit().putString(KEY_NICKNAME, nickname).apply();
    }

    public String getNickname() {
        return prefs.getString(KEY_NICKNAME, "");
    }

    /** Alias kept for backward compatibility with SettlementBottomSheetFragment. */
    public String getUsername() {
        return getNickname();
    }

    public boolean hasNickname() {
        String n = getNickname();
        return n != null && !n.isEmpty();
    }

    public void clearNickname() {
        prefs.edit().remove(KEY_NICKNAME).apply();
    }
}
