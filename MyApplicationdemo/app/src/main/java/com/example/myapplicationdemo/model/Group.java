package com.example.myapplicationdemo.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "groups")
public class Group {

    @PrimaryKey
    private final int id;
    private final String name;
    private final int memberCount;
    private final double myBalance;

    public Group(int id, String name, int memberCount, double myBalance) {
        this.id = id;
        this.name = name;
        this.memberCount = memberCount;
        this.myBalance = myBalance;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getMemberCount() { return memberCount; }
    public double getMyBalance() { return myBalance; }
}
