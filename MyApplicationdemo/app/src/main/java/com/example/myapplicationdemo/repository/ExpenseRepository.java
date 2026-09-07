package com.example.myapplicationdemo.repository;

import com.example.myapplicationdemo.MyApplication;
import com.example.myapplicationdemo.database.ExpenseDao;
import com.example.myapplicationdemo.model.Expense;
import java.util.ArrayList;
import java.util.List;

public class ExpenseRepository {

    private final ExpenseDao dao;

    public ExpenseRepository() {
        dao = MyApplication.getDatabase().expenseDao();
    }

    public List<Expense> getByGroupId(int groupId) {
        List<Expense> expenses = dao.getByGroupId(groupId);
        if (expenses.isEmpty() && groupId == 1) {
            expenses = seedMockData();
            dao.insertAll(expenses);
        }
        return expenses;
    }

    public void deleteExpense(int expenseId) {
        dao.deleteById(expenseId);
    }

    public void restoreExpense(Expense expense) {
        dao.insert(expense);
    }

    public Expense addExpense(int groupId, String description, String payer,
                              double totalAmount, double myShare, String date) {
        int newId = dao.getMaxId() + 1;
        Expense expense = new Expense(newId, groupId, description, payer, totalAmount, myShare, date);
        dao.insert(expense);
        return expense;
    }

    private List<Expense> seedMockData() {
        List<Expense> list = new ArrayList<>();
        list.add(new Expense(1, 1, "超市購物", "小明", 900, 300, "08/29"));
        list.add(new Expense(2, 1, "水電費", "小花", 1800, 600, "08/25"));
        list.add(new Expense(3, 1, "外送晚餐", "阿強", 450, 150, "08/20"));
        list.add(new Expense(4, 1, "網路費", "小明", 600, 200, "08/15"));
        list.add(new Expense(5, 1, "衛生紙／清潔用品", "阿強", 300, 100, "08/10"));
        return list;
    }
}
