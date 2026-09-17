package com.example.myapplicationdemo.model;

import java.io.Serializable;

public class Expense implements Serializable {

    private final String id;
    private final String groupId;
    private final String description;
    private final String payer;
    private final double totalAmount;
    private final double myShare;
    private final String date;

    public Expense(String id, String groupId, String description, String payer,
                   double totalAmount, double myShare, String date) {
        this.id = id;
        this.groupId = groupId;
        this.description = description;
        this.payer = payer;
        this.totalAmount = totalAmount;
        this.myShare = myShare;
        this.date = date;
    }

    public String getId() { return id; }
    public String getGroupId() { return groupId; }
    public String getDescription() { return description; }
    public String getPayer() { return payer; }
    public double getTotalAmount() { return totalAmount; }
    public double getMyShare() { return myShare; }
    public String getDate() { return date; }
}
