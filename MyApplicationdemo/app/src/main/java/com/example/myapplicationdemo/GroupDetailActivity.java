package com.example.myapplicationdemo;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.myapplicationdemo.adapter.ExpenseAdapter;
import com.example.myapplicationdemo.databinding.ActivityGroupDetailBinding;
import com.example.myapplicationdemo.model.Expense;
import com.example.myapplicationdemo.utils.LogUtils;
import java.util.ArrayList;
import java.util.List;

public class GroupDetailActivity extends AppCompatActivity {

    private ActivityGroupDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String groupName = getIntent().getStringExtra("group_name");
        double groupBalance = getIntent().getDoubleExtra("group_balance", 0);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(groupName);
        }

        setupBalance(groupBalance);

        binding.rvExpenses.setLayoutManager(new LinearLayoutManager(this));
        binding.rvExpenses.setAdapter(new ExpenseAdapter(getMockExpenses()));

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

    private List<Expense> getMockExpenses() {
        List<Expense> list = new ArrayList<>();
        list.add(new Expense(1, "超市購物", "小明", 900, 300, "08/29"));
        list.add(new Expense(2, "水電費", "小花", 1800, 600, "08/25"));
        list.add(new Expense(3, "外送晚餐", "阿強", 450, 150, "08/20"));
        list.add(new Expense(4, "網路費", "小明", 600, 200, "08/15"));
        list.add(new Expense(5, "衛生紙／清潔用品", "阿強", 300, 100, "08/10"));
        return list;
    }
}
