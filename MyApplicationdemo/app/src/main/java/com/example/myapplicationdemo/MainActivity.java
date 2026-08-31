package com.example.myapplicationdemo;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.myapplicationdemo.adapter.GroupAdapter;
import com.example.myapplicationdemo.databinding.ActivityMainBinding;
import com.example.myapplicationdemo.model.Group;
import com.example.myapplicationdemo.utils.LogUtils;
import com.example.myapplicationdemo.viewmodel.MainViewModel;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private GroupAdapter adapter;
    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        adapter = new GroupAdapter(new ArrayList<>());
        binding.rvGroups.setLayoutManager(new LinearLayoutManager(this));
        binding.rvGroups.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        viewModel.getGroups().observe(this, this::onGroupsLoaded);

        binding.fabAddGroup.setOnClickListener(v -> {
            LogUtils.info("新增群組");
            // TODO: 開啟建立群組頁面
        });
    }

    private void onGroupsLoaded(List<Group> groups) {
        adapter.updateData(groups);
        setupSummaryCard(groups);
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
}
