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

    public void addGroup(String name, String nickname, String deviceId) throws Exception {
        String code = generateInviteCode();
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("inviteCode", code);
        data.put("members", Collections.singletonList(deviceId));
        data.put("memberCount", 1L);
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
                        .whereEqualTo("inviteCode", inviteCode.toUpperCase())
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

    public void renameGroup(String groupId, String newName) throws Exception {
        Tasks.await(db.collection(COLLECTION).document(groupId).update("name", newName));
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
                inviteCode != null ? inviteCode : "");
    }

    private String generateInviteCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder(6);
        Random rand = new Random();
        for (int i = 0; i < 6; i++) sb.append(chars.charAt(rand.nextInt(chars.length())));
        return sb.toString();
    }
}
