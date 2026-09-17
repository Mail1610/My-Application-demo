package com.example.myapplicationdemo.model;

public class Group {

    private final String id;
    private final String name;
    private final int memberCount;
    private final double myBalance;
    private final String inviteCode;

    public Group(String id, String name, int memberCount, double myBalance, String inviteCode) {
        this.id = id;
        this.name = name;
        this.memberCount = memberCount;
        this.myBalance = myBalance;
        this.inviteCode = inviteCode;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getMemberCount() { return memberCount; }
    public double getMyBalance() { return myBalance; }
    public String getInviteCode() { return inviteCode; }
}
