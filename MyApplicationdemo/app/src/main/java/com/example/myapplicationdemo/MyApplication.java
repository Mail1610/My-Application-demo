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
        // 模擬器要用 10.0.2.2 才能連到 host machine，實體裝置用 localhost
        String firestoreHost = isEmulator() ? "10.0.2.2" : "localhost";
        FirebaseFirestore.getInstance().useEmulator(firestoreHost, 8080);
        registerFcmToken();
    }

    public static Context getContext() { return context; }

    private static boolean isEmulator() {
        return android.os.Build.FINGERPRINT.startsWith("generic")
                || android.os.Build.FINGERPRINT.startsWith("unknown")
                || android.os.Build.MODEL.contains("google_sdk")
                || android.os.Build.MODEL.contains("Emulator")
                || android.os.Build.MODEL.contains("Android SDK built for x86")
                || android.os.Build.MANUFACTURER.contains("Genymotion")
                || android.os.Build.PRODUCT.contains("sdk_google")
                || android.os.Build.PRODUCT.contains("google_sdk")
                || android.os.Build.PRODUCT.contains("sdk")
                || android.os.Build.PRODUCT.contains("sdk_x86")
                || android.os.Build.HARDWARE.contains("goldfish")
                || android.os.Build.HARDWARE.contains("ranchu");
    }

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
