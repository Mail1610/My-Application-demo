package com.example.myapplicationdemo.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.myapplicationdemo.model.Expense;
import com.example.myapplicationdemo.repository.ExpenseRepository;
import com.example.myapplicationdemo.repository.GroupRepository;
import com.example.myapplicationdemo.utils.LogUtils;
import com.example.myapplicationdemo.utils.SessionManager;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.List;

public class GroupDetailViewModel extends AndroidViewModel {

    private final MutableLiveData<List<Expense>> expenses = new MutableLiveData<>();
    private final MutableLiveData<Double> balance = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Integer> memberCount = new MutableLiveData<>();
    private final ExpenseRepository repository = new ExpenseRepository();
    private final GroupRepository groupRepository = new GroupRepository();
    private String currentGroupId;
    private String myNickname;
    private ListenerRegistration expenseListener;
    private ListenerRegistration groupListener;

    public GroupDetailViewModel(@NonNull Application application) {
        super(application);
        myNickname = new SessionManager(application).getNickname();
    }

    public LiveData<List<Expense>> getExpenses() { return expenses; }
    public LiveData<Double> getBalance() { return balance; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Integer> getMemberCount() { return memberCount; }

    public void loadExpenses(String groupId) {
        currentGroupId = groupId;
        loading.postValue(true);
        if (expenseListener != null) expenseListener.remove();
        expenseListener = repository.addExpensesListener(groupId, list -> {
            expenses.postValue(list);
            balance.postValue(computeBalance(list));
            loading.postValue(false);
        });
        if (groupListener != null) groupListener.remove();
        groupListener = groupRepository.addGroupListener(groupId, count ->
                memberCount.postValue(count));
    }

    public void addExpense(String description, String payer,
                           double totalAmount, double myShare, String date, String category) {
        new Thread(() -> {
            try {
                repository.addExpense(currentGroupId, description, payer, totalAmount, myShare,
                        myNickname, date, category);
            } catch (Exception e) {
                LogUtils.error("新增支出失敗", e);
            }
        }).start();
    }

    public void updateExpense(Expense old, String description, String payer,
                              double totalAmount, double myShare, String date, String category) {
        String adder = old.getAdderNickname() != null && !old.getAdderNickname().isEmpty()
                ? old.getAdderNickname() : myNickname;
        Expense updated = new Expense(old.getId(), old.getGroupId(),
                description, payer, totalAmount, myShare, adder, date, category);
        new Thread(() -> {
            try {
                repository.updateExpense(updated);
            } catch (Exception e) {
                LogUtils.error("更新支出失敗", e);
            }
        }).start();
    }

    public void deleteExpense(String expenseId) {
        new Thread(() -> {
            try {
                repository.deleteExpense(currentGroupId, expenseId);
            } catch (Exception e) {
                LogUtils.error("刪除支出失敗", e);
            }
        }).start();
    }

    public void restoreExpense(Expense expense) {
        new Thread(() -> {
            try {
                repository.restoreExpense(expense);
            } catch (Exception e) {
                LogUtils.error("復原支出失敗", e);
            }
        }).start();
    }

    public interface MembersCallback {
        void onResult(List<String> members);
    }

    public interface MembersMapCallback {
        void onResult(java.util.Map<String, String> membersMap);
    }

    public void loadMembers(String groupId, MembersCallback callback) {
        new Thread(() -> {
            try {
                List<String> members = groupRepository.getMembers(groupId);
                new android.os.Handler(android.os.Looper.getMainLooper())
                        .post(() -> callback.onResult(members));
            } catch (Exception e) {
                LogUtils.error("載入成員失敗", e);
            }
        }).start();
    }

    public void loadMembersWithIds(String groupId, MembersMapCallback callback) {
        new Thread(() -> {
            try {
                java.util.Map<String, String> map = groupRepository.getMembersMap(groupId);
                new android.os.Handler(android.os.Looper.getMainLooper())
                        .post(() -> callback.onResult(map));
            } catch (Exception e) {
                LogUtils.error("載入成員失敗", e);
            }
        }).start();
    }

    public interface LeaveGroupCallback {
        void onResult(int remainingMembers);
        void onError();
    }

    public void leaveGroup(String groupId, String deviceId, LeaveGroupCallback callback) {
        new Thread(() -> {
            try {
                int remaining = groupRepository.leaveGroup(groupId, deviceId);
                new android.os.Handler(android.os.Looper.getMainLooper())
                        .post(() -> callback.onResult(remaining));
            } catch (Exception e) {
                LogUtils.error("離開群組失敗", e);
                new android.os.Handler(android.os.Looper.getMainLooper())
                        .post(callback::onError);
            }
        }).start();
    }

    public void deleteGroup(String groupId) {
        new Thread(() -> {
            try { groupRepository.deleteGroup(groupId); }
            catch (Exception e) { LogUtils.error("刪除群組失敗", e); }
        }).start();
    }

    public void grantAdmin(String groupId, String targetDeviceId) {
        new Thread(() -> {
            try { groupRepository.grantAdmin(groupId, targetDeviceId); }
            catch (Exception e) { LogUtils.error("設為管理員失敗", e); }
        }).start();
    }

    public void revokeAdmin(String groupId, String targetDeviceId, String creatorDeviceId) {
        new Thread(() -> {
            try { groupRepository.revokeAdmin(groupId, targetDeviceId, creatorDeviceId); }
            catch (Exception e) { LogUtils.error("取消管理員失敗", e); }
        }).start();
    }

    public void renameGroup(String groupId, String newName) {
        new Thread(() -> {
            try {
                groupRepository.renameGroup(groupId, newName);
            } catch (Exception e) {
                LogUtils.error("重新命名群組失敗", e);
            }
        }).start();
    }

    public interface ChangeInviteCodeCallback {
        void onSuccess(String newCode);
        void onCodeTaken();
        void onError();
    }

    public void changeInviteCode(String groupId, String newCode, ChangeInviteCodeCallback callback) {
        new Thread(() -> {
            try {
                boolean updated = groupRepository.updateInviteCode(groupId, newCode);
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    if (updated) callback.onSuccess(newCode);
                    else callback.onCodeTaken();
                });
            } catch (Exception e) {
                LogUtils.error("修改邀請碼失敗", e);
                new android.os.Handler(android.os.Looper.getMainLooper())
                        .post(callback::onError);
            }
        }).start();
    }

    private double computeBalance(List<Expense> list) {
        int count = memberCount.getValue() != null ? memberCount.getValue() : 2;
        double bal = 0;
        for (Expense e : list) {
            double share = shareFor(e, myNickname, count);
            if (myNickname.equals(e.getPayer())) {
                bal += e.getTotalAmount() - share;
            } else {
                bal -= share;
            }
        }
        return bal;
    }

    private double shareFor(Expense e, String nickname, int memberCount) {
        String adder = e.getAdderNickname();
        if (adder == null || adder.isEmpty() || adder.equals(nickname)) {
            return e.getMyShare();
        }
        return (e.getTotalAmount() - e.getMyShare()) / Math.max(1, memberCount - 1);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (expenseListener != null) expenseListener.remove();
        if (groupListener != null) groupListener.remove();
    }
}
