package com.example.myapplicationdemo.model;

public class Expense {

    private final int id;
    private final String description;
    private final String payer;
    private final double totalAmount;
    private final double myShare;
    private final String date;

    public Expense(int id, String description, String payer,
                   double totalAmount, double myShare, String date) {
        this.id = id;
        this.description = description;
        this.payer = payer;
        this.totalAmount = totalAmount;
        this.myShare = myShare;
        this.date = date;
    }

    public int getId() { return id; }
    public String getDescription() { return description; }
    public String getPayer() { return payer; }
    public double getTotalAmount() { return totalAmount; }
    public double getMyShare() { return myShare; }
    public String getDate() { return date; }
}
