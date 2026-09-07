package com.example.myapplicationdemo.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.myapplicationdemo.model.Expense;

import com.example.myapplicationdemo.repository.ExpenseRepository;
import com.example.myapplicationdemo.utils.LogUtils;
import java.util.List;

public class GroupDetailViewModel extends AndroidViewModel {

    private final MutableLiveData<List<Expense>> expenses = new MutableLiveData<>();
    private final ExpenseRepository repository = new ExpenseRepository();
    private int currentGroupId;

    public GroupDetailViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<Expense>> getExpenses() {
        return expenses;
    }

    public void loadExpenses(int groupId) {
        currentGroupId = groupId;
        new Thread(() -> {
            try {
                expenses.postValue(repository.getByGroupId(groupId));
            } catch (Exception e) {
                LogUtils.error("載入支出失敗", e);
            }
        }).start();
    }

    public void deleteExpense(int expenseId) {
        new Thread(() -> {
            try {
                repository.deleteExpense(expenseId);
                expenses.postValue(repository.getByGroupId(currentGroupId));
            } catch (Exception e) {
                LogUtils.error("刪除支出失敗", e);
            }
        }).start();
    }

    public void restoreExpense(Expense expense) {
        new Thread(() -> {
            try {
                repository.restoreExpense(expense);
                expenses.postValue(repository.getByGroupId(currentGroupId));
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
                expenses.postValue(repository.getByGroupId(currentGroupId));
            } catch (Exception e) {
                LogUtils.error("新增支出失敗", e);
            }
        }).start();
    }
}
