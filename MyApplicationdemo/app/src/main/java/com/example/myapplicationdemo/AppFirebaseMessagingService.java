package com.example.myapplicationdemo;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import com.example.myapplicationdemo.utils.LogUtils;
import com.example.myapplicationdemo.utils.SessionManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.util.HashMap;
import java.util.Map;

public class AppFirebaseMessagingService extends FirebaseMessagingService {

    public static final String CHANNEL_ID = "expense_notifications";

    @Override
    public void onNewToken(String token) {
        String deviceId = new SessionManager(this).getDeviceId();
        saveTokenToFirestore(deviceId, token);
    }

    @Override
    public void onMessageReceived(RemoteMessage message) {
        String title = message.getNotification() != null ? message.getNotification().getTitle() : "";
        String body  = message.getNotification() != null ? message.getNotification().getBody()  : "";
        showNotification(title, body);
    }

    private void saveTokenToFirestore(String deviceId, String token) {
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("updatedAt", com.google.firebase.firestore.FieldValue.serverTimestamp());
        FirebaseFirestore.getInstance()
                .collection("fcmTokens")
                .document(deviceId)
                .set(data)
                .addOnFailureListener(e -> LogUtils.error("FCM token 儲存失敗", e));
    }

    private void showNotification(String title, String body) {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (nm.getNotificationChannel(CHANNEL_ID) == null) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID, "支出通知", NotificationManager.IMPORTANCE_DEFAULT);
            nm.createNotificationChannel(ch);
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setContentIntent(pi);

        nm.notify((int) System.currentTimeMillis(), builder.build());
    }
}
