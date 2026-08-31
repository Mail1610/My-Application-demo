package com.example.myapplicationdemo;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.myapplicationdemo.adapter.ExpenseAdapter;
import com.example.myapplicationdemo.databinding.ActivityGroupDetailBinding;
import com.example.myapplicationdemo.utils.LogUtils;
import com.example.myapplicationdemo.viewmodel.GroupDetailViewModel;
import java.util.ArrayList;

public class GroupDetailActivity extends AppCompatActivity {

    private ActivityGroupDetailBinding binding;
    private ExpenseAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        int groupId = getIntent().getIntExtra("group_id", 0);
        String groupName = getIntent().getStringExtra("group_name");
        double groupBalance = getIntent().getDoubleExtra("group_balance", 0);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(groupName);
        }

        setupBalance(groupBalance);

        adapter = new ExpenseAdapter(new ArrayList<>());
        binding.rvExpenses.setLayoutManager(new LinearLayoutManager(this));
        binding.rvExpenses.setAdapter(adapter);

        GroupDetailViewModel viewModel = new ViewModelProvider(this).get(GroupDetailViewModel.class);
        viewModel.getExpenses().observe(this, adapter::updateData);
        viewModel.loadExpenses(groupId);

        binding.btnSettlement.setOnClickListener(v -> {
            LogUtils.info("查看結算：" + groupName);
            // TODO: 開啟結算頁面
        });

        binding.fabAddExpense.setOnClickListener(v -> {
            LogUtils.info("新增支出：" + groupName);
            // TODO: 開啟新增支出頁面
        });
    }

    private void setupBalance(double balance) {
        if (balance > 0) {
            binding.tvBalanceAmount.setText(String.format("你將收到 $%.0f", balance));
            binding.tvBalanceAmount.setTextColor(ContextCompat.getColor(this, R.color.color_positive));
        } else if (balance < 0) {
            binding.tvBalanceAmount.setText(String.format("你需要支付 $%.0f", Math.abs(balance)));
            binding.tvBalanceAmount.setTextColor(ContextCompat.getColor(this, R.color.color_negative));
        } else {
            binding.tvBalanceAmount.setText("已結清");
            binding.tvBalanceAmount.setTextColor(ContextCompat.getColor(this, R.color.color_settled));
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
