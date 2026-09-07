package com.example.myapplicationdemo.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.myapplicationdemo.model.Expense;
import com.example.myapplicationdemo.repository.ExpenseRepository;
import com.example.myapplicationdemo.utils.LogUtils;
import com.example.myapplicationdemo.utils.SessionManager;
import java.util.List;

public class GroupDetailViewModel extends AndroidViewModel {

    private final MutableLiveData<List<Expense>> expenses = new MutableLiveData<>();
    private final MutableLiveData<Double> balance = new MutableLiveData<>();
    private final ExpenseRepository repository = new ExpenseRepository();
    private int currentGroupId;

    public GroupDetailViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<Expense>> getExpenses() { return expenses; }
    public LiveData<Double> getBalance() { return balance; }

    public void loadExpenses(int groupId) {
        currentGroupId = groupId;
        refresh();
    }

    public void deleteExpense(int expenseId) {
        new Thread(() -> {
            try {
                repository.deleteExpense(expenseId);
                refreshSync();
            } catch (Exception e) {
                LogUtils.error("刪除支出失敗", e);
            }
        }).start();
    }

    public void restoreExpense(Expense expense) {
        new Thread(() -> {
            try {
                repository.restoreExpense(expense);
                refreshSync();
            } catch (Exception e) {
                LogUtils.error("復原支出失敗", e);
            }
        }).start();
    }

    public void addExpense(String description, String payer,
                           double totalAmount, double myShare, String date) {
        new Thread(() -> {
            try {
                repository.addExpense(currentGroupId, description, payer, totalAmount, myShare, date);
                refreshSync();
            } catch (Exception e) {
                LogUtils.error("新增支出失敗", e);
            }
        }).start();
    }

    private void refresh() {
        new Thread(() -> {
            try {
                refreshSync();
            } catch (Exception e) {
                LogUtils.error("載入支出失敗", e);
            }
        }).start();
    }

    private void refreshSync() {
        List<Expense> list = repository.getByGroupId(currentGroupId);
        expenses.postValue(list);
        balance.postValue(computeBalance(list));
    }

    private double computeBalance(List<Expense> list) {
        String myName = new SessionManager(getApplication()).getUsername();
        double bal = 0;
        for (Expense e : list) {
            if (myName.equals(e.getPayer())) {
                bal += e.getTotalAmount() - e.getMyShare();
            } else {
                bal -= e.getMyShare();
            }
        }
        return bal;
    }
}
