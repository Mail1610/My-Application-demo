package com.example.myapplicationdemo.repository;

import com.example.myapplicationdemo.MyApplication;
import com.example.myapplicationdemo.database.ExpenseDao;
import com.example.myapplicationdemo.database.GroupDao;
import com.example.myapplicationdemo.model.Expense;
import com.example.myapplicationdemo.model.Group;
import com.example.myapplicationdemo.network.ApiClient;
import com.example.myapplicationdemo.network.ApiService;
import com.example.myapplicationdemo.network.dto.GroupRequest;
import com.example.myapplicationdemo.network.dto.GroupResponse;
import com.example.myapplicationdemo.utils.LogUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Response;

public class GroupRepository {

    private final GroupDao dao;
    private final ExpenseDao expenseDao;
    private final ApiService api;

    public GroupRepository() {
        dao = MyApplication.getDatabase().groupDao();
        expenseDao = MyApplication.getDatabase().expenseDao();
        api = ApiClient.getInstance();
    }

    /** API-first：從伺服器拉取，失敗則回傳 Room 快取。餘額以 Room 支出即時計算。 */
    public List<Group> getAll(String username) {
        List<Group> groups;
        try {
            Response<List<GroupResponse>> resp = api.getGroups().execute();
            if (resp.isSuccessful() && resp.body() != null) {
                groups = toModels(resp.body());
                dao.deleteAll();
                dao.insertAll(groups);
            } else {
                groups = dao.getAll();
            }
        } catch (IOException e) {
            LogUtils.warn("群組 API 不可用，使用本地快取");
            groups = dao.getAll();
        }

        // 用 Room 的支出資料即時覆蓋餘額。
        // 若 Room 尚無資料（使用者未開啟過該群組），保留 API 回傳的餘額。
        List<Group> result = new ArrayList<>();
        for (Group g : groups) {
            List<Expense> expenses = expenseDao.getByGroupId(g.getId());
            double balance = expenses.isEmpty()
                    ? g.getMyBalance()
                    : computeBalance(expenses, username);
            result.add(new Group(g.getId(), g.getName(), g.getMemberCount(), balance));
        }
        return result;
    }

    private double computeBalance(List<Expense> expenses, String username) {
        double bal = 0;
        for (Expense e : expenses) {
            if (username != null && username.equals(e.getPayer())) {
                bal += e.getTotalAmount() - e.getMyShare();
            } else {
                bal -= e.getMyShare();
            }
        }
        return bal;
    }

    public void addGroup(String name) {
        try {
            Response<GroupResponse> resp = api.createGroup(new GroupRequest(name)).execute();
            if (resp.isSuccessful() && resp.body() != null) {
                dao.insert(toModel(resp.body()));
                return;
            }
        } catch (IOException e) {
            LogUtils.warn("新增群組 API 失敗");
        }
        int newId = dao.getMaxId() + 1;
        dao.insert(new Group(newId, name, 1, 0.0));
    }

    public void deleteGroup(int groupId) {
        try {
            api.deleteGroup(groupId).execute();
        } catch (IOException e) {
            LogUtils.warn("刪除群組 API 失敗");
        }
        dao.deleteById(groupId);
        MyApplication.getDatabase().expenseDao().deleteByGroupId(groupId);
    }

    // ── Mapping ────────────────────────────────────────────────────

    private List<Group> toModels(List<GroupResponse> responses) {
        List<Group> list = new ArrayList<>();
        for (GroupResponse r : responses) list.add(toModel(r));
        return list;
    }

    private Group toModel(GroupResponse r) {
        return new Group(r.id, r.name, r.memberCount, r.myBalance);
    }
}
