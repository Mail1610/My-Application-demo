package com.example.myapplicationdemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private GroupAdapter adapter;
    private MainViewModel viewModel;
    private SessionManager session;

    private final ActivityResultLauncher<String> notificationPermLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {});


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        session = new SessionManager(this);
        requestNotificationPermission();

        if (!session.hasNickname()) {
            showNicknameDialog(true);
            return;
        }

        initScreen();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            notificationPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private void initScreen() {
        setSupportActionBar(binding.toolbar);

        adapter = new GroupAdapter(new ArrayList<>());
        adapter.setOnGroupDeleteListener(this::showDeleteGroupDialog);
        binding.rvGroups.setLayoutManager(new LinearLayoutManager(this));
        binding.rvGroups.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        viewModel.getGroups().observe(this, this::onGroupsLoaded);
        viewModel.getLoading().observe(this, loading ->
                binding.swipeRefresh.setRefreshing(loading));
        viewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_LONG).show();
            }
        });

        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.loadGroups());
        binding.fabAddGroup.setOnClickListener(v -> showCreateGroupDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (viewModel != null) viewModel.loadGroups();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_join_group) {
            showJoinGroupDialog();
            return true;
        } else if (id == R.id.action_change_nickname) {
            showNicknameDialog(false);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showNicknameDialog(boolean firstTime) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_set_nickname, null);
        TextInputLayout tilNickname = dialogView.findViewById(R.id.til_nickname);
        TextInputEditText etNickname = dialogView.findViewById(R.id.et_nickname);

        if (!firstTime) {
            etNickname.setText(session.getNickname());
        }

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_title_set_nickname)
                .setView(dialogView)
                .setCancelable(!firstTime)
                .setPositiveButton(R.string.btn_confirm, null)
                .show();

        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String name = etNickname.getText() != null
                            ? etNickname.getText().toString().trim() : "";
                    if (TextUtils.isEmpty(name)) {
                        tilNickname.setError(getString(R.string.error_field_required));
                        return;
                    }
                    session.saveNickname(name);
                    dialog.dismiss();
                    if (firstTime) recreate();
                });
    }

    private void showCreateGroupDialog() {
        DialogCreateGroupBinding dialogBinding =
                DialogCreateGroupBinding.inflate(getLayoutInflater());

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_title_create_group)
                .setView(dialogBinding.getRoot())
                .setPositiveButton(R.string.btn_confirm, null)
                .setNegativeButton(R.string.btn_cancel, null)
                .show();

        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String name = dialogBinding.etGroupName.getText() != null
                            ? dialogBinding.etGroupName.getText().toString().trim() : "";
                    if (TextUtils.isEmpty(name)) {
                        dialogBinding.tilGroupName.setError(
                                getString(R.string.error_field_required));
                        return;
                    }
                    dialog.dismiss();
                    viewModel.addGroup(name);
                    Snackbar.make(binding.getRoot(),
                            R.string.msg_group_created, Snackbar.LENGTH_LONG).show();
                });
    }

    private void showJoinGroupDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_join_group, null);
        TextInputLayout tilCode = dialogView.findViewById(R.id.til_invite_code);
        TextInputEditText etCode = dialogView.findViewById(R.id.et_invite_code);

        etCode.addTextChangedListener(new TextWatcher() {
            private boolean editing = false;
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (editing) return;
                String upper = s.toString().toUpperCase(java.util.Locale.ROOT);
                if (!upper.equals(s.toString())) {
                    editing = true;
                    int cursor = etCode.getSelectionEnd();
                    s.replace(0, s.length(), upper);
                    etCode.setSelection(Math.min(cursor, upper.length()));
                    editing = false;
                }
            }
        });

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_title_join_group)
                .setView(dialogView)
                .setPositiveButton(R.string.btn_confirm, null)
                .setNegativeButton(R.string.btn_cancel, null)
                .show();

        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String code = etCode.getText() != null
                            ? etCode.getText().toString().trim() : "";
                    if (TextUtils.isEmpty(code)) {
                        tilCode.setError(getString(R.string.error_field_required));
                        return;
                    }
                    dialog.dismiss();
                    viewModel.joinGroup(
                            code,
                            () -> runOnUiThread(() ->
                                    Snackbar.make(binding.getRoot(),
                                            R.string.msg_group_joined, Snackbar.LENGTH_SHORT).show()),
                            () -> runOnUiThread(() ->
                                    Snackbar.make(binding.getRoot(),
                                            R.string.error_invite_code_not_found, Snackbar.LENGTH_LONG).show())
                    );
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
