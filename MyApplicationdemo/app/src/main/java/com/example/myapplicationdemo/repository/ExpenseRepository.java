package com.example.myapplicationdemo.repository;

import com.example.myapplicationdemo.model.Expense;
import com.example.myapplicationdemo.utils.LogUtils;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpenseRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface OnExpensesChangedListener {
        void onChanged(List<Expense> expenses);
    }

    public ListenerRegistration addExpensesListener(String groupId,
                                                     OnExpensesChangedListener listener) {
        return db.collection("groups").document(groupId)
                .collection("expenses")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snap, e) -> {
                    if (e != null || snap == null) return;
                    List<Expense> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        list.add(docToExpense(doc, groupId));
                    }
                    listener.onChanged(list);
                });
    }

    /** Synchronous fetch — call only from a background thread. */
    public List<Expense> getByGroupId(String groupId) throws Exception {
        QuerySnapshot snap = Tasks.await(
                db.collection("groups").document(groupId)
                        .collection("expenses")
                        .orderBy("createdAt", Query.Direction.DESCENDING)
                        .get()
        );
        List<Expense> list = new ArrayList<>();
        for (DocumentSnapshot doc : snap.getDocuments()) {
            list.add(docToExpense(doc, groupId));
        }
        return list;
    }

    public Expense addExpense(String groupId, String description, String payer,
                              double totalAmount, double myShare, String adderNickname,
                              String date, String category) throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("description", description);
        data.put("payer", payer);
        data.put("totalAmount", totalAmount);
        data.put("myShare", myShare);
        data.put("adderNickname", adderNickname != null ? adderNickname : "");
        data.put("date", date);
        data.put("category", category);
        data.put("createdAt", FieldValue.serverTimestamp());
        DocumentReference ref = db.collection("groups").document(groupId)
                .collection("expenses").document();
        Tasks.await(ref.set(data));
        asyncRecompute(groupId);
        return new Expense(ref.getId(), groupId, description, payer, totalAmount, myShare,
                adderNickname, date, category);
    }

    public void deleteExpense(String groupId, String expenseId) throws Exception {
        Tasks.await(db.collection("groups").document(groupId)
                .collection("expenses").document(expenseId).delete());
        asyncRecompute(groupId);
    }

    public void updateExpense(Expense expense) throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("description", expense.getDescription());
        data.put("payer", expense.getPayer());
        data.put("totalAmount", expense.getTotalAmount());
        data.put("myShare", expense.getMyShare());
        data.put("adderNickname", expense.getAdderNickname());
        data.put("date", expense.getDate());
        data.put("category", expense.getCategory());
        Tasks.await(db.collection("groups").document(expense.getGroupId())
                .collection("expenses").document(expense.getId()).update(data));
        asyncRecompute(expense.getGroupId());
    }

    public void restoreExpense(Expense expense) throws Exception {
        addExpense(expense.getGroupId(), expense.getDescription(), expense.getPayer(),
                expense.getTotalAmount(), expense.getMyShare(), expense.getAdderNickname(),
                expense.getDate(), expense.getCategory());
    }

    private void asyncRecompute(String groupId) {
        new Thread(() -> {
            try { recomputeAndUpdateBalances(groupId); }
            catch (Exception e) { LogUtils.error("balance sync failed", e); }
        }).start();
    }

    private void recomputeAndUpdateBalances(String groupId) throws Exception {
        DocumentSnapshot groupDoc = Tasks.await(
                db.collection("groups").document(groupId).get()
        );
        @SuppressWarnings("unchecked")
        Map<String, Object> nicknameMap = (Map<String, Object>) groupDoc.get("nicknames");
        if (nicknameMap == null) return;

        QuerySnapshot snap = Tasks.await(
                db.collection("groups").document(groupId).collection("expenses").get()
        );
        List<Expense> expenses = new ArrayList<>();
        for (DocumentSnapshot doc : snap.getDocuments()) {
            expenses.add(docToExpense(doc, groupId));
        }

        int memberCount = nicknameMap.size();
        Map<String, Object> updates = new HashMap<>();
        for (Map.Entry<String, Object> entry : nicknameMap.entrySet()) {
            String deviceId = entry.getKey();
            String nickname = String.valueOf(entry.getValue());
            updates.put("balances." + deviceId, computeBalanceFor(expenses, nickname, memberCount));
        }
        Tasks.await(db.collection("groups").document(groupId).update(updates));
    }

    private double computeBalanceFor(List<Expense> expenses, String nickname, int memberCount) {
        double bal = 0;
        for (Expense e : expenses) {
            double share = e.getShareFor(nickname, memberCount);
            if (nickname.equals(e.getPayer())) {
                bal += e.getTotalAmount() - share;
            } else {
                bal -= share;
            }
        }
        return bal;
    }

    private Expense docToExpense(DocumentSnapshot doc, String groupId) {
        Double total = doc.getDouble("totalAmount");
        Double share = doc.getDouble("myShare");
        String category = doc.getString("category");
        String adderNickname = doc.getString("adderNickname");
        return new Expense(
                doc.getId(),
                groupId,
                doc.getString("description") != null ? doc.getString("description") : "",
                doc.getString("payer") != null ? doc.getString("payer") : "",
                total != null ? total : 0.0,
                share != null ? share : 0.0,
                adderNickname != null ? adderNickname : "",
                doc.getString("date") != null ? doc.getString("date") : "",
                category != null ? category : "其他");
    }
}
