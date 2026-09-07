package com.example.myapplicationdemo;

import android.app.Application;
import android.content.Context;
import androidx.room.Room;
import com.example.myapplicationdemo.database.AppDatabase;

public class MyApplication extends Application {

    private static AppDatabase database;
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        database = Room.databaseBuilder(this, AppDatabase.class, "xian-db").build();
    }

    public static AppDatabase getDatabase() { return database; }
    public static Context getContext() { return context; }
}
