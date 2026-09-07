package com.example.myapplicationdemo.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.myapplicationdemo.model.Group;
import java.util.List;

@Dao
public interface GroupDao {

    @Query("SELECT * FROM groups")
    List<Group> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Group> groups);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Group group);

    @Query("SELECT COALESCE(MAX(id), 0) FROM groups")
    int getMaxId();

    @Query("DELETE FROM groups")
    void deleteAll();
}
