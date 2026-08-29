package com.example.myapplicationdemo;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.myapplicationdemo.adapter.GroupAdapter;
import com.example.myapplicationdemo.databinding.ActivityMainBinding;
import com.example.myapplicationdemo.model.Group;
import com.example.myapplicationdemo.utils.LogUtils;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        binding.rvGroups.setLayoutManager(new LinearLayoutManager(this));
        binding.rvGroups.setAdapter(new GroupAdapter(getMockGroups()));

        binding.fabAddGroup.setOnClickListener(v -> {
            LogUtils.info("新增群組");
            // TODO: 開啟建立群組頁面
        });
    }

    private List<Group> getMockGroups() {
        List<Group> list = new ArrayList<>();
        list.add(new Group(1, "室友帳", 3, -350));
        list.add(new Group(2, "日本旅遊", 4, 1200));
        list.add(new Group(3, "情侶帳", 2, 0));
        return list;
    }
}
