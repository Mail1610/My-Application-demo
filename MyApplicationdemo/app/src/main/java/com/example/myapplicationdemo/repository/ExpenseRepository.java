package com.example.myapplicationdemo.repository;

import com.example.myapplicationdemo.MyApplication;
import com.example.myapplicationdemo.database.ExpenseDao;
import com.example.myapplicationdemo.model.Expense;
import com.example.myapplicationdemo.network.ApiClient;
import com.example.myapplicationdemo.network.ApiService;
import com.example.myapplicationdemo.network.dto.ExpenseRequest;
import com.example.myapplicationdemo.network.dto.ExpenseResponse;
import com.example.myapplicationdemo.utils.LogUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Response;

public class ExpenseRepository {

    private final ExpenseDao dao;
    private final ApiService api;

    public ExpenseRepository() {
        dao = MyApplication.getDatabase().expenseDao();
        api = ApiClient.getInstance();
    }

    /** API-first：從伺服器拉取，失敗則回傳 Room 快取。 */
    public List<Expense> getByGroupId(int groupId) {
        try {
            Response<List<ExpenseResponse>> resp = api.getExpenses(groupId).execute();
            if (resp.isSuccessful() && resp.body() != null) {
                List<Expense> expenses = toModels(resp.body());
                dao.deleteByGroupId(groupId);
                dao.insertAll(expenses);
                return expenses;
            }
        } catch (IOException e) {
            LogUtils.warn("支出 API 不可用，使用本地快取");
        }
        return dao.getByGroupId(groupId);
    }

    public Expense addExpense(int groupId, String description, String payer,
                              double totalAmount, double myShare, String date) {
        ExpenseRequest req = new ExpenseRequest(description, payer, totalAmount, myShare, date);
        try {
            Response<ExpenseResponse> resp = api.createExpense(groupId, req).execute();
            if (resp.isSuccessful() && resp.body() != null) {
                Expense expense = toModel(resp.body());
                dao.insert(expense);
                return expense;
            }
        } catch (IOException e) {
            LogUtils.warn("新增支出 API 失敗");
        }
        // 離線 fallback
        int newId = dao.getMaxId() + 1;
        Expense expense = new Expense(newId, groupId, description, payer, totalAmount, myShare, date);
        dao.insert(expense);
        return expense;
    }

    public void deleteExpense(int expenseId) {
        try {
            api.deleteExpense(expenseId).execute();
        } catch (IOException e) {
            LogUtils.warn("刪除支出 API 失敗");
        }
        dao.deleteById(expenseId);
    }

    public void restoreExpense(Expense expense) {
        ExpenseRequest req = new ExpenseRequest(expense.getDescription(), expense.getPayer(),
                expense.getTotalAmount(), expense.getMyShare(), expense.getDate());
        try {
            Response<ExpenseResponse> resp = api.createExpense(expense.getGroupId(), req).execute();
            if (resp.isSuccessful() && resp.body() != null) {
                dao.insert(toModel(resp.body()));
                return;
            }
        } catch (IOException e) {
            LogUtils.warn("復原支出 API 失敗");
        }
        dao.insert(expense);
    }

    // ── Mapping ────────────────────────────────────────────────────

    private List<Expense> toModels(List<ExpenseResponse> responses) {
        List<Expense> list = new ArrayList<>();
        for (ExpenseResponse r : responses) list.add(toModel(r));
        return list;
    }

    private Expense toModel(ExpenseResponse r) {
        return new Expense(r.id, r.groupId, r.description, r.payer,
                r.totalAmount, r.myShare, r.date);
    }
}
