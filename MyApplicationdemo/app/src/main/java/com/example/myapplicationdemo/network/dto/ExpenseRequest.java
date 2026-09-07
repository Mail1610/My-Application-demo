package com.example.myapplicationdemo.network.dto;

public class ExpenseRequest {
    private final String description;
    private final String payer;
    private final double totalAmount;
    private final double myShare;
    private final String date;

    public ExpenseRequest(String description, String payer,
                          double totalAmount, double myShare, String date) {
        this.description = description;
        this.payer = payer;
        this.totalAmount = totalAmount;
        this.myShare = myShare;
        this.date = date;
    }
}
