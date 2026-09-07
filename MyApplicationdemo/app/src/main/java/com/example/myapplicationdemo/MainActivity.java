package com.example.myapplicationdemo;

import android.os.Bundle;
import android.text.TextUtils;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.myapplicationdemo.adapter.GroupAdapter;
import com.example.myapplicationdemo.databinding.ActivityMainBinding;
import com.example.myapplicationdemo.databinding.DialogCreateGroupBinding;
import com.example.myapplicationdemo.model.Group;
import com.example.myapplicationdemo.viewmodel.MainViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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

        binding.fabAddGroup.setOnClickListener(v -> showCreateGroupDialog());
    }

    private void showCreateGroupDialog() {
        DialogCreateGroupBinding dialogBinding = DialogCreateGroupBinding.inflate(getLayoutInflater());

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_title_create_group)
                .setView(dialogBinding.getRoot())
                .setPositiveButton(R.string.btn_confirm, (dialog, which) -> {
                    String name = "";
                    if (dialogBinding.etGroupName.getText() != null) {
                        name = dialogBinding.etGroupName.getText().toString().trim();
                    }
                    if (TextUtils.isEmpty(name)) {
                        dialogBinding.tilGroupName.setError(getString(R.string.error_field_required));
                        return;
                    }
                    viewModel.addGroup(name);
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void onGroupsLoaded(List<Group> groups) {
        adapter.updateData(groups);
        setupSummaryCard(groups);
    }

    private void setupSummaryCard(List<Group> groups) {
        double total = 0;
        for (Group g : groups) total += g.getMyBalance();

        binding.tvGroupsSummary.setText(getString(R.string.label_groups_summary, groups.size()));

        if (total > 0) {
            binding.tvNetBalance.setText(getString(R.string.label_balance_net_positive, total));
            binding.tvNetBalance.setTextColor(ContextCompat.getColor(this, R.color.color_positive));
        } else if (total < 0) {
            binding.tvNetBalance.setText(getString(R.string.label_balance_net_negative, Math.abs(total)));
            binding.tvNetBalance.setTextColor(ContextCompat.getColor(this, R.color.color_negative));
        } else {
            binding.tvNetBalance.setText(R.string.label_net_balance_placeholder);
            binding.tvNetBalance.setTextColor(ContextCompat.getColor(this, R.color.color_settled));
        }
    }
}
