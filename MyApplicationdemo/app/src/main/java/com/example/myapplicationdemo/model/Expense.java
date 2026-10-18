package com.example.myapplicationdemo.model;

import java.io.Serializable;

public class Expense implements Serializable {

    private final String id;
    private final String groupId;
    private final String description;
    private final String payer;
    private final double totalAmount;
    private final double myShare;
    private final String adderNickname;
    private final String date;
    private final String category;

    public Expense(String id, String groupId, String description, String payer,
                   double totalAmount, double myShare, String adderNickname,
                   String date, String category) {
        this.id = id;
        this.groupId = groupId;
        this.description = description;
        this.payer = payer;
        this.totalAmount = totalAmount;
        this.myShare = myShare;
        this.adderNickname = adderNickname != null ? adderNickname : "";
        this.date = date;
        this.category = category != null ? category : "其他";
    }

    public String getId() { return id; }
    public String getGroupId() { return groupId; }
    public String getDescription() { return description; }
    public String getPayer() { return payer; }
    public double getTotalAmount() { return totalAmount; }
    public double getMyShare() { return myShare; }
    public String getAdderNickname() { return adderNickname; }
    public String getDate() { return date; }
    public String getCategory() { return category; }

    /**
     * 計算指定成員在這筆支出中的分攤金額。
     * 若該成員是新增者，直接回傳 myShare；否則平均分配剩餘金額。
     */
    public double getShareFor(String nickname, int memberCount) {
        if (adderNickname.isEmpty() || adderNickname.equals(nickname)) {
            return myShare;
        }
        return (totalAmount - myShare) / Math.max(1, memberCount - 1);
    }
}
