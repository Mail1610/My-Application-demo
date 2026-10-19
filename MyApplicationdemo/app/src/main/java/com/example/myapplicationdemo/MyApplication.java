package com.example.myapplicationdemo;

import android.app.Application;
import android.content.Context;
import com.example.myapplicationdemo.utils.LogUtils;
import com.example.myapplicationdemo.utils.SessionManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class MyApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        registerFcmToken();
    }

    public static Context getContext() { return context; }

    private void registerFcmToken() {
        String deviceId = new SessionManager(this).getDeviceId();
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(token -> {
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("token", token);
            data.put("updatedAt", com.google.firebase.firestore.FieldValue.serverTimestamp());
            FirebaseFirestore.getInstance()
                    .collection("fcmTokens")
                    .document(deviceId)
                    .set(data)
                    .addOnFailureListener(e -> LogUtils.error("FCM token 儲存失敗", e));
        }).addOnFailureListener(e -> LogUtils.error("取得 FCM token 失敗", e));
    }
}
