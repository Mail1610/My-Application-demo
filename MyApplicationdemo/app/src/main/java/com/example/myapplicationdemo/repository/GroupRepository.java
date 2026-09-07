package com.example.myapplicationdemo.repository;

import com.example.myapplicationdemo.MyApplication;
import com.example.myapplicationdemo.database.GroupDao;
import com.example.myapplicationdemo.model.Group;
import java.util.ArrayList;
import java.util.List;

public class GroupRepository {

    private final GroupDao dao;

    public GroupRepository() {
        dao = MyApplication.getDatabase().groupDao();
    }

    public List<Group> getAll() {
        List<Group> groups = dao.getAll();
        if (groups.isEmpty()) {
            groups = seedMockData();
            dao.insertAll(groups);
        }
        return groups;
    }

    public Group addGroup(String name) {
        int newId = dao.getMaxId() + 1;
        Group group = new Group(newId, name, 1, 0.0);
        dao.insert(group);
        return group;
    }

    private List<Group> seedMockData() {
        List<Group> list = new ArrayList<>();
        list.add(new Group(1, "室友帳", 3, -350));
        list.add(new Group(2, "日本旅遊", 4, 1200));
        list.add(new Group(3, "情侶帳", 2, 0));
        return list;
    }
}
