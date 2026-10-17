package com.example.myapplicationdemo.model;

public class Group {

    private final String id;
    private final String name;
    private final int memberCount;
    private final double myBalance;
    private final String inviteCode;
    private final String creatorDeviceId;
    private final boolean isAdmin;

    public Group(String id, String name, int memberCount, double myBalance,
                 String inviteCode, String creatorDeviceId, boolean isAdmin) {
        this.id = id;
        this.name = name;
        this.memberCount = memberCount;
        this.myBalance = myBalance;
        this.inviteCode = inviteCode;
        this.creatorDeviceId = creatorDeviceId != null ? creatorDeviceId : "";
        this.isAdmin = isAdmin;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getMemberCount() { return memberCount; }
    public double getMyBalance() { return myBalance; }
    public String getInviteCode() { return inviteCode; }
    public String getCreatorDeviceId() { return creatorDeviceId; }
    public boolean isAdmin() { return isAdmin; }
    public boolean isCreator(String deviceId) { return creatorDeviceId.equals(deviceId); }
}
