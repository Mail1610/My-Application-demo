package com.example.myapplicationdemo.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.myapplicationdemo.model.Expense;
import java.util.List;

@Dao
public interface ExpenseDao {

    @Query("SELECT * FROM expenses WHERE groupId = :groupId")
    List<Expense> getByGroupId(int groupId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Expense> expenses);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Expense expense);

    @Query("SELECT COALESCE(MAX(id), 0) FROM expenses")
    int getMaxId();

    @Query("DELETE FROM expenses WHERE id = :expenseId")
    void deleteById(int expenseId);

    @Query("DELETE FROM expenses WHERE groupId = :groupId")
    void deleteByGroupId(int groupId);

    @Query("DELETE FROM expenses")
    void deleteAll();
}
