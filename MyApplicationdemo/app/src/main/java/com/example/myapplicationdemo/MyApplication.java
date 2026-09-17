package com.example.myapplicationdemo;

import android.app.Application;
import android.content.Context;
import com.google.firebase.firestore.FirebaseFirestore;

public class MyApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        FirebaseFirestore.getInstance().useEmulator("localhost", 8080);
    }

    public static Context getContext() { return context; }
}
