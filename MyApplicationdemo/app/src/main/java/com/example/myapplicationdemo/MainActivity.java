package com.example.myapplicationdemo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.myapplicationdemo.adapter.GroupAdapter;
import com.example.myapplicationdemo.databinding.ActivityMainBinding;
import com.example.myapplicationdemo.databinding.DialogCreateGroupBinding;
import com.example.myapplicationdemo.model.Group;
import com.example.myapplicationdemo.utils.SessionManager;
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
        adapter.setOnGroupDeleteListener(this::showDeleteGroupDialog);
        binding.rvGroups.setLayoutManager(new LinearLayoutManager(this));
        binding.rvGroups.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        viewModel.getGroups().observe(this, this::onGroupsLoaded);

        binding.fabAddGroup.setOnClickListener(v -> showCreateGroupDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.loadGroups();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            showLogoutDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showLogoutDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_title_logout)
                .setMessage(R.string.dialog_msg_logout)
                .setPositiveButton(R.string.btn_confirm, (dialog, which) -> logout())
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void logout() {
        new SessionManager(this).clearSession();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void showCreateGroupDialog() {
        DialogCreateGroupBinding dialogBinding = DialogCreateGroupBinding.inflate(getLayoutInflater());

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_title_create_group)
                .setView(dialogBinding.getRoot())
                .setPositiveButton(R.string.btn_confirm, null)
                .setNegativeButton(R.string.btn_cancel, null)
                .show();

        // Override to prevent auto-dismiss on validation failure
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = dialogBinding.etGroupName.getText() != null
                    ? dialogBinding.etGroupName.getText().toString().trim() : "";
            if (TextUtils.isEmpty(name)) {
                dialogBinding.tilGroupName.setError(getString(R.string.error_field_required));
                return;
            }
            dialog.dismiss();
            viewModel.addGroup(name);
        });
    }

    private void showDeleteGroupDialog(Group group) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_title_delete_group)
                .setMessage(getString(R.string.dialog_msg_delete_group, group.getName()))
                .setPositiveButton(R.string.btn_delete, (dialog, which) ->
                        viewModel.deleteGroup(group.getId()))
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void onGroupsLoaded(List<Group> groups) {
        adapter.updateData(groups);
        setupSummaryCard(groups);

        boolean isEmpty = groups == null || groups.isEmpty();
        binding.rvGroups.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        binding.tvEmptyGroups.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
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
