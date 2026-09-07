package com.example.myapplicationdemo;

import android.os.Bundle;
import android.text.TextUtils;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.myapplicationdemo.adapter.ExpenseAdapter;
import com.example.myapplicationdemo.databinding.ActivityGroupDetailBinding;
import com.example.myapplicationdemo.databinding.DialogAddExpenseBinding;
import com.example.myapplicationdemo.viewmodel.GroupDetailViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class GroupDetailActivity extends AppCompatActivity {

    private ActivityGroupDetailBinding binding;
    private ExpenseAdapter adapter;
    private GroupDetailViewModel viewModel;
    private int groupId;
    private double groupBalance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        groupId = getIntent().getIntExtra("group_id", 0);
        String groupName = getIntent().getStringExtra("group_name");
        groupBalance = getIntent().getDoubleExtra("group_balance", 0);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(groupName);
        }

        setupBalance(groupBalance);

        adapter = new ExpenseAdapter(new ArrayList<>());
        binding.rvExpenses.setLayoutManager(new LinearLayoutManager(this));
        binding.rvExpenses.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(GroupDetailViewModel.class);
        viewModel.getExpenses().observe(this, adapter::updateData);
        viewModel.loadExpenses(groupId);

        binding.btnSettlement.setOnClickListener(v -> showSettlement());
        binding.fabAddExpense.setOnClickListener(v -> showAddExpenseDialog());
    }

    private void showAddExpenseDialog() {
        DialogAddExpenseBinding dialogBinding = DialogAddExpenseBinding.inflate(getLayoutInflater());

        String today = new SimpleDateFormat("MM/dd", Locale.getDefault()).format(new Date());

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_title_add_expense)
                .setView(dialogBinding.getRoot())
                .setPositiveButton(R.string.btn_confirm, (dialog, which) -> {
                    String desc = getText(dialogBinding.etDescription);
                    String payer = getText(dialogBinding.etPayer);
                    String totalStr = getText(dialogBinding.etTotalAmount);
                    String shareStr = getText(dialogBinding.etMyShare);

                    if (TextUtils.isEmpty(desc)) {
                        dialogBinding.tilDescription.setError(getString(R.string.error_field_required));
                        return;
                    }
                    if (TextUtils.isEmpty(payer)) {
                        dialogBinding.tilPayer.setError(getString(R.string.error_field_required));
                        return;
                    }

                    double total, share;
                    try {
                        total = Double.parseDouble(totalStr);
                        share = TextUtils.isEmpty(shareStr) ? total : Double.parseDouble(shareStr);
                    } catch (NumberFormatException e) {
                        dialogBinding.tilTotalAmount.setError(getString(R.string.error_amount_invalid));
                        return;
                    }

                    viewModel.addExpense(desc, payer, total, share, today);
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void showSettlement() {
        SettlementBottomSheetFragment sheet =
                SettlementBottomSheetFragment.newInstance(groupId, groupBalance);
        sheet.show(getSupportFragmentManager(), "settlement");
    }

    private String getText(com.google.android.material.textfield.TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private void setupBalance(double balance) {
        if (balance > 0) {
            binding.tvBalanceAmount.setText(getString(R.string.label_will_receive, balance));
            binding.tvBalanceAmount.setTextColor(ContextCompat.getColor(this, R.color.color_positive));
        } else if (balance < 0) {
            binding.tvBalanceAmount.setText(getString(R.string.label_need_to_pay, Math.abs(balance)));
            binding.tvBalanceAmount.setTextColor(ContextCompat.getColor(this, R.color.color_negative));
        } else {
            binding.tvBalanceAmount.setText(R.string.label_net_balance_placeholder);
            binding.tvBalanceAmount.setTextColor(ContextCompat.getColor(this, R.color.color_settled));
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
