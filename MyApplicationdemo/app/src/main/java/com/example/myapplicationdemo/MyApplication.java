package com.example.myapplicationdemo;

import android.app.Application;
import androidx.room.Room;
import com.example.myapplicationdemo.database.AppDatabase;

public class MyApplication extends Application {

    private static AppDatabase database;

    @Override
    public void onCreate() {
        super.onCreate();
        database = Room.databaseBuilder(this, AppDatabase.class, "xian-db").build();
    }

    public static AppDatabase getDatabase() {
        return database;
    }
}
