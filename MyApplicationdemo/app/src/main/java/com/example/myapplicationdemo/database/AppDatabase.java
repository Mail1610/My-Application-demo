package com.example.myapplicationdemo.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import com.example.myapplicationdemo.model.Expense;
import com.example.myapplicationdemo.model.Group;

@Database(entities = {Group.class, Expense.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract GroupDao groupDao();
    public abstract ExpenseDao expenseDao();
}
