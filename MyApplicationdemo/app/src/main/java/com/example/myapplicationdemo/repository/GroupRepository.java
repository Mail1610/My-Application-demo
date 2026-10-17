package com.example.myapplicationdemo.repository;

import com.example.myapplicationdemo.model.Group;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GroupRepository {

    private static final String COLLECTION = "groups";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface OnGroupsChangedListener {
        void onChanged(List<Group> groups);
    }

    public interface OnMemberCountChangedListener {
        void onChanged(int count);
    }

    public ListenerRegistration addGroupsListener(String deviceId,
                                                   OnGroupsChangedListener listener) {
        return db.collection(COLLECTION)
                .whereArrayContains("members", deviceId)
                .addSnapshotListener((snap, e) -> {
                    if (e != null || snap == null) return;
                    List<Group> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        list.add(docToGroup(doc, deviceId));
                    }
                    listener.onChanged(list);
                });
    }

    public ListenerRegistration addGroupListener(String groupId,
                                                  OnMemberCountChangedListener listener) {
        return db.collection(COLLECTION).document(groupId)
                .addSnapshotListener((doc, e) -> {
                    if (e != null || doc == null || !doc.exists()) return;
                    Long count = doc.getLong("memberCount");
                    listener.onChanged(count != null ? count.intValue() : 0);
                });
    }

    public void addGroup(String name, String nickname, String deviceId) throws Exception {
        String code = generateInviteCode();
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("inviteCode", code);
        data.put("members", Collections.singletonList(deviceId));
        data.put("memberCount", 1L);
        data.put("creatorDeviceId", deviceId);
        data.put("admins", Collections.singletonList(deviceId));
        Map<String, String> nicknames = new HashMap<>();
        nicknames.put(deviceId, nickname);
        data.put("nicknames", nicknames);
        Map<String, Double> balances = new HashMap<>();
        balances.put(deviceId, 0.0);
        data.put("balances", balances);
        Tasks.await(db.collection(COLLECTION).add(data));
    }

    /** @return true if group found and joined, false if invite code not found */
    public boolean joinGroup(String inviteCode, String nickname, String deviceId) throws Exception {
        QuerySnapshot snap = Tasks.await(
                db.collection(COLLECTION)
                        .whereEqualTo("inviteCode", inviteCode.trim().toUpperCase())
                        .limit(1)
                        .get()
        );
        if (snap.isEmpty()) return false;
        String groupId = snap.getDocuments().get(0).getId();
        Map<String, Object> updates = new HashMap<>();
        updates.put("members", FieldValue.arrayUnion(deviceId));
        updates.put("nicknames." + deviceId, nickname);
        updates.put("balances." + deviceId, 0.0);
        updates.put("memberCount", FieldValue.increment(1));
        Tasks.await(db.collection(COLLECTION).document(groupId).update(updates));
        return true;
    }

    /** Returns map of deviceId → nickname for all members. Call from background thread. */
    public Map<String, String> getMembersMap(String groupId) throws Exception {
        DocumentSnapshot doc = Tasks.await(db.collection(COLLECTION).document(groupId).get());
        @SuppressWarnings("unchecked")
        Map<String, Object> rawNicknames = (Map<String, Object>) doc.get("nicknames");
        @SuppressWarnings("unchecked")
        List<String> admins = (List<String>) doc.get("admins");
        String creatorId = doc.getString("creatorDeviceId");

        Map<String, String> result = new HashMap<>();
        if (rawNicknames != null) {
            for (Map.Entry<String, Object> entry : rawNicknames.entrySet()) {
                String deviceId = entry.getKey();
                String nickname = String.valueOf(entry.getValue());
                boolean isAdmin = (admins != null && admins.contains(deviceId))
                        || deviceId.equals(creatorId);
                result.put(deviceId, nickname + (isAdmin ? " ★" : ""));
            }
        }
        return result;
    }

    /** Returns sorted list of nicknames in the group. Call from background thread. */
    public List<String> getMembers(String groupId) throws Exception {
        Map<String, String> map = getMembersMap(groupId);
        List<String> names = new ArrayList<>(map.values());
        Collections.sort(names);
        return names;
    }

    /** Grant admin role to a member. Only current admins should call this. */
    public void grantAdmin(String groupId, String targetDeviceId) throws Exception {
        Tasks.await(db.collection(COLLECTION).document(groupId)
                .update("admins", FieldValue.arrayUnion(targetDeviceId)));
    }

    /** Revoke admin role. Creator cannot be revoked. */
    public void revokeAdmin(String groupId, String targetDeviceId,
                            String creatorDeviceId) throws Exception {
        if (targetDeviceId.equals(creatorDeviceId)) return;
        Tasks.await(db.collection(COLLECTION).document(groupId)
                .update("admins", FieldValue.arrayRemove(targetDeviceId)));
    }

    public void renameGroup(String groupId, String newName) throws Exception {
        Tasks.await(db.collection(COLLECTION).document(groupId).update("name", newName));
    }

    /** Returns true if updated, false if the code is already taken by another group. */
    public boolean updateInviteCode(String groupId, String newCode) throws Exception {
        QuerySnapshot snap = Tasks.await(
                db.collection(COLLECTION)
                        .whereEqualTo("inviteCode", newCode)
                        .limit(1)
                        .get()
        );
        if (!snap.isEmpty() && !snap.getDocuments().get(0).getId().equals(groupId)) {
            return false;
        }
        Tasks.await(db.collection(COLLECTION).document(groupId).update("inviteCode", newCode));
        return true;
    }

    /**
     * Returns remaining member count after leaving, or -1 if deviceId is not a member.
     */
    public int leaveGroup(String groupId, String deviceId) throws Exception {
        DocumentSnapshot doc = Tasks.await(db.collection(COLLECTION).document(groupId).get());
        @SuppressWarnings("unchecked")
        List<String> members = (List<String>) doc.get("members");
        if (members == null || !members.contains(deviceId)) return -1;
        int remaining = members.size() - 1;
        Map<String, Object> updates = new HashMap<>();
        updates.put("members", FieldValue.arrayRemove(deviceId));
        updates.put("nicknames." + deviceId, FieldValue.delete());
        updates.put("balances." + deviceId, FieldValue.delete());
        updates.put("admins", FieldValue.arrayRemove(deviceId));
        updates.put("memberCount", FieldValue.increment(-1));
        Tasks.await(db.collection(COLLECTION).document(groupId).update(updates));
        return remaining;
    }

    public void deleteGroup(String groupId) throws Exception {
        QuerySnapshot snap = Tasks.await(
                db.collection(COLLECTION).document(groupId).collection("expenses").get()
        );
        for (DocumentSnapshot doc : snap.getDocuments()) {
            Tasks.await(doc.getReference().delete());
        }
        Tasks.await(db.collection(COLLECTION).document(groupId).delete());
    }

    private Group docToGroup(DocumentSnapshot doc, String deviceId) {
        String name = doc.getString("name");
        Long memberCount = doc.getLong("memberCount");
        String inviteCode = doc.getString("inviteCode");
        String creatorDeviceId = doc.getString("creatorDeviceId");
        @SuppressWarnings("unchecked")
        List<String> admins = (List<String>) doc.get("admins");
        boolean isAdmin = (admins != null && admins.contains(deviceId))
                || deviceId.equals(creatorDeviceId);
        @SuppressWarnings("unchecked")
        Map<String, Object> balances = (Map<String, Object>) doc.get("balances");
        double balance = 0.0;
        if (balances != null && balances.containsKey(deviceId)) {
            Object val = balances.get(deviceId);
            if (val instanceof Number) balance = ((Number) val).doubleValue();
        }
        return new Group(
                doc.getId(),
                name != null ? name : "",
                memberCount != null ? memberCount.intValue() : 0,
                balance,
                inviteCode != null ? inviteCode : "",
                creatorDeviceId != null ? creatorDeviceId : "",
                isAdmin);
    }

    private String generateInviteCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder(6);
        Random rand = new Random();
        for (int i = 0; i < 6; i++) sb.append(chars.charAt(rand.nextInt(chars.length())));
        return sb.toString();
    }
}
