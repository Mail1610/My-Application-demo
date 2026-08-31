package com.example.myapplicationdemo.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "expenses")
public class Expense {

    @PrimaryKey
    private final int id;
    private final int groupId;
    private final String description;
    private final String payer;
    private final double totalAmount;
    private final double myShare;
    private final String date;

    public Expense(int id, int groupId, String description, String payer,
                   double totalAmount, double myShare, String date) {
        this.id = id;
        this.groupId = groupId;
        this.description = description;
        this.payer = payer;
        this.totalAmount = totalAmount;
        this.myShare = myShare;
        this.date = date;
    }

    public int getId() { return id; }
    public int getGroupId() { return groupId; }
    public String getDescription() { return description; }
    public String getPayer() { return payer; }
    public double getTotalAmount() { return totalAmount; }
    public double getMyShare() { return myShare; }
    public String getDate() { return date; }
}
