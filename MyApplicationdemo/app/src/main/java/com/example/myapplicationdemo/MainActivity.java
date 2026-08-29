package com.example.myapplicationdemo;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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

        List<Group> groups = getMockGroups();
        setupSummaryCard(groups);

        binding.rvGroups.setLayoutManager(new LinearLayoutManager(this));
        binding.rvGroups.setAdapter(new GroupAdapter(groups));

        binding.fabAddGroup.setOnClickListener(v -> {
            LogUtils.info("新增群組");
            // TODO: 開啟建立群組頁面
        });
    }

    private void setupSummaryCard(List<Group> groups) {
        double total = 0;
        for (Group g : groups) total += g.getMyBalance();

        binding.tvGroupsSummary.setText("參與 " + groups.size() + " 個群組");

        if (total > 0) {
            binding.tvNetBalance.setText(String.format("+$%.0f", total));
            binding.tvNetBalance.setTextColor(ContextCompat.getColor(this, R.color.color_positive));
        } else if (total < 0) {
            binding.tvNetBalance.setText(String.format("-$%.0f", Math.abs(total)));
            binding.tvNetBalance.setTextColor(ContextCompat.getColor(this, R.color.color_negative));
        } else {
            binding.tvNetBalance.setText("已結清");
            binding.tvNetBalance.setTextColor(ContextCompat.getColor(this, R.color.color_settled));
        }
    }

    private List<Group> getMockGroups() {
        List<Group> list = new ArrayList<>();
        list.add(new Group(1, "室友帳", 3, -350));
        list.add(new Group(2, "日本旅遊", 4, 1200));
        list.add(new Group(3, "情侶帳", 2, 0));
        return list;
    }
}
