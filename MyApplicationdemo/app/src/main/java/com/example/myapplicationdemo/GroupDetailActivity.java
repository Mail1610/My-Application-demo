package com.example.myapplicationdemo;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplicationdemo.adapter.ExpenseAdapter;
import com.example.myapplicationdemo.databinding.ActivityGroupDetailBinding;
import com.example.myapplicationdemo.databinding.DialogAddExpenseBinding;
import com.example.myapplicationdemo.databinding.DialogChangeInviteCodeBinding;
import com.example.myapplicationdemo.databinding.DialogRenameGroupBinding;
import com.example.myapplicationdemo.databinding.DialogSetNicknameBinding;
import com.example.myapplicationdemo.model.Expense;
import com.example.myapplicationdemo.utils.SessionManager;
import com.example.myapplicationdemo.viewmodel.GroupDetailViewModel;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GroupDetailActivity extends AppCompatActivity {

    private ActivityGroupDetailBinding binding;
    private ExpenseAdapter adapter;
    private GroupDetailViewModel viewModel;
    private String groupId;
    private int memberCount;
    private String groupName;
    private String inviteCode;
    private String deviceId;
    private boolean isAdmin;
    private String creatorDeviceId;
    private List<Expense> allExpenses = new ArrayList<>();
    private String selectedCategory = null;
    private String myNickname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        groupId = getIntent().getStringExtra("group_id");
        groupName = getIntent().getStringExtra("group_name");
        memberCount = getIntent().getIntExtra("group_member_count", 2);
        inviteCode = getIntent().getStringExtra("group_invite_code");
        deviceId = new SessionManager(this).getDeviceId();
        myNickname = new SessionManager(this).getNickname();
        isAdmin = getIntent().getBooleanExtra("group_is_admin", false);
        creatorDeviceId = getIntent().getStringExtra("group_creator_device_id");

        binding.tvMemberCount.setText(getString(R.string.label_member_count, memberCount));

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(groupName);
        }

        adapter = new ExpenseAdapter(new ArrayList<>());
        adapter.setOnExpenseEditListener(this::showEditExpenseDialog);
        binding.rvExpenses.setLayoutManager(new LinearLayoutManager(this));
        binding.rvExpenses.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(GroupDetailViewModel.class);
        viewModel.getExpenses().observe(this, this::onExpensesLoaded);
        viewModel.getBalance().observe(this, this::updateBalanceHeader);
        viewModel.getMemberCount().observe(this, count -> {
            memberCount = count;
            binding.tvMemberCount.setText(getString(R.string.label_member_count, count));
        });
        viewModel.getLoading().observe(this, loading ->
                binding.swipeRefresh.setRefreshing(loading));
        viewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_LONG).show();
            }
        });
        viewModel.loadExpenses(groupId);

        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.loadExpenses(groupId));
        binding.tvMemberCount.setOnClickListener(v -> showMembersDialog());

        setupSwipeToDelete();
        setupCategoryFilter();
        if (getIntent().getBooleanExtra("add_test_alice", false)) {
            binding.btnSettlement.setOnClickListener(v -> showSettlementTest());
        } else {
            binding.btnSettlement.setOnClickListener(v -> showSettlement());
        }
        binding.fabAddExpense.setOnClickListener(v -> showAddExpenseDialog());

        getSupportFragmentManager().setFragmentResultListener(
                "settlement_result", this,
                (requestKey, result) -> viewModel.loadExpenses(groupId));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_group_detail, menu);
        // Admin-only items
        menu.findItem(R.id.action_rename_group).setVisible(isAdmin);
        menu.findItem(R.id.action_change_invite_code).setVisible(isAdmin);
        menu.findItem(R.id.action_manage_members).setVisible(isAdmin);
        menu.findItem(R.id.action_delete_group).setVisible(isAdmin);
        boolean isCreator = deviceId.equals(creatorDeviceId);
        menu.findItem(R.id.action_leave_group).setVisible(!isCreator);
        menu.findItem(R.id.action_change_nickname).setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_rename_group) {
            showRenameDialog();
            return true;
        } else if (item.getItemId() == R.id.action_copy_invite_code) {
            copyInviteCode();
            return true;
        } else if (item.getItemId() == R.id.action_change_invite_code) {
            showChangeInviteCodeDialog();
            return true;
        } else if (item.getItemId() == R.id.action_change_nickname) {
            showChangeNicknameDialog();
            return true;
        } else if (item.getItemId() == R.id.action_leave_group) {
            showLeaveGroupDialog();
            return true;
        } else if (item.getItemId() == R.id.action_manage_members) {
            showManageMembersDialog();
            return true;
        } else if (item.getItemId() == R.id.action_delete_group) {
            showDeleteGroupDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void copyInviteCode() {
        String code = inviteCode != null ? inviteCode : "";
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText("invite_code", code));
        Snackbar.make(binding.getRoot(),
                getString(R.string.msg_invite_code_copied, code),
                Snackbar.LENGTH_LONG).show();
    }

    private void showChangeInviteCodeDialog() {
        DialogChangeInviteCodeBinding dialogBinding =
                DialogChangeInviteCodeBinding.inflate(getLayoutInflater());
        dialogBinding.etInviteCode.setText(inviteCode);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_title_change_invite_code)
                .setView(dialogBinding.getRoot())
                .setPositiveButton(R.string.btn_confirm, null)
                .setNegativeButton(R.string.btn_cancel, null)
                .show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String newCode = getText(dialogBinding.etInviteCode).toUpperCase(Locale.ROOT);
            if (newCode.length() != 6 || !newCode.matches("[A-Z0-9]{6}")) {
                dialogBinding.tilInviteCode.setError(getString(R.string.error_invite_code_invalid));
                return;
            }
            dialogBinding.tilInviteCode.setError(null);
            dialog.dismiss();
            viewModel.changeInviteCode(groupId, newCode, new GroupDetailViewModel.ChangeInviteCodeCallback() {
                @Override
                public void onSuccess(String updated) {
                    inviteCode = updated;
                    Snackbar.make(binding.getRoot(),
                            getString(R.string.msg_invite_code_updated, updated),
                            Snackbar.LENGTH_LONG).show();
                }
                @Override
                public void onCodeTaken() {
                    Snackbar.make(binding.getRoot(),
                            R.string.error_invite_code_taken,
                            Snackbar.LENGTH_LONG).show();
                }
                @Override
                public void onError() {
                    Snackbar.make(binding.getRoot(),
                            R.string.msg_load_failed,
                            Snackbar.LENGTH_LONG).show();
                }
            });
        });
    }

    private void showLeaveGroupDialog() {
        if (memberCount <= 1) {
            Snackbar.make(binding.getRoot(),
                    R.string.error_cannot_leave_last_member,
                    Snackbar.LENGTH_LONG).show();
            return;
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_title_leave_group)
                .setMessage(getString(R.string.dialog_msg_leave_group, groupName))
                .setPositiveButton(R.string.menu_leave_group, (d, w) -> {
                    viewModel.leaveGroup(groupId, deviceId, new GroupDetailViewModel.LeaveGroupCallback() {
                        @Override
                        public void onResult(int remainingMembers) {
                            Snackbar.make(binding.getRoot(),
                                    R.string.msg_group_left,
                                    Snackbar.LENGTH_SHORT).show();
                            finish();
                        }
                        @Override
                        public void onError() {
                            Snackbar.make(binding.getRoot(),
                                    R.string.msg_load_failed,
                                    Snackbar.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void showDeleteGroupDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_title_delete_group)
                .setMessage(getString(R.string.dialog_msg_delete_group, groupName))
                .setPositiveButton(R.string.btn_delete, (d, w) -> {
                    viewModel.deleteGroup(groupId);
                    finish();
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void showManageMembersDialog() {
        viewModel.loadMembersWithIds(groupId, membersMap -> {
            String[] deviceIds = membersMap.keySet().toArray(new String[0]);
            String[] displayNames = new String[deviceIds.length];
            for (int i = 0; i < deviceIds.length; i++) {
                displayNames[i] = membersMap.get(deviceIds[i]);
            }
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.dialog_title_manage_members)
                    .setItems(displayNames, (d, which) -> {
                        String targetId = deviceIds[which];
                        if (targetId.equals(deviceId)) return;
                        showMemberActionDialog(targetId, displayNames[which]);
                    })
                    .setPositiveButton(R.string.btn_confirm, null)
                    .show();
        });
    }

    private void showMemberActionDialog(String targetId, String displayName) {
        boolean targetIsAdmin = displayName.endsWith("★");
        boolean targetIsCreator = targetId.equals(creatorDeviceId);
        String cleanNickname = displayName.replace("★", "").trim();
        String adminAction = targetIsAdmin
                ? getString(R.string.btn_revoke_admin)
                : getString(R.string.btn_grant_admin);
        String setNoteAction = getString(R.string.menu_set_note);
        String[] options = targetIsCreator
                ? new String[]{adminAction, setNoteAction}
                : new String[]{adminAction, getString(R.string.menu_kick_member), setNoteAction};

        new MaterialAlertDialogBuilder(this)
                .setTitle(displayName)
                .setItems(options, (d, which) -> {
                    if (which == 0) {
                        if (targetIsAdmin) {
                            if (targetIsCreator) {
                                Snackbar.make(binding.getRoot(),
                                        R.string.error_cannot_revoke_creator_admin, Snackbar.LENGTH_SHORT).show();
                                return;
                            }
                            viewModel.revokeAdmin(groupId, targetId, creatorDeviceId);
                            Snackbar.make(binding.getRoot(), R.string.msg_admin_revoked, Snackbar.LENGTH_SHORT).show();
                        } else {
                            viewModel.grantAdmin(groupId, targetId);
                            Snackbar.make(binding.getRoot(), R.string.msg_admin_granted, Snackbar.LENGTH_SHORT).show();
                        }
                    } else if (!targetIsCreator && which == 1) {
                        showKickConfirmDialog(targetId, displayName);
                    } else {
                        showSetNoteDialog(targetId, cleanNickname);
                    }
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void showSetNoteDialog(String targetDeviceId, String nickname) {
        DialogSetNicknameBinding db = DialogSetNicknameBinding.inflate(getLayoutInflater());
        db.tilNickname.setHint(getString(R.string.hint_member_note));
        String currentNote = getMemberNote(targetDeviceId);
        db.etNickname.setText(currentNote);
        if (!currentNote.isEmpty()) db.etNickname.setSelection(currentNote.length());

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.dialog_title_set_note, nickname))
                .setView(db.getRoot())
                .setPositiveButton(R.string.btn_confirm, null)
                .setNegativeButton(R.string.btn_cancel, null)
                .show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            saveMemberNote(targetDeviceId, getText(db.etNickname));
            dialog.dismiss();
            Snackbar.make(binding.getRoot(), R.string.msg_note_saved, Snackbar.LENGTH_SHORT).show();
        });
    }

    private void showKickConfirmDialog(String targetId, String displayName) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_title_kick_member)
                .setMessage(getString(R.string.dialog_msg_kick_member, displayName))
                .setPositiveButton(R.string.btn_confirm, (d, w) ->
                        viewModel.kickMember(groupId, targetId, new GroupDetailViewModel.KickMemberCallback() {
                            @Override
                            public void onSuccess() {
                                Snackbar.make(binding.getRoot(),
                                        R.string.msg_member_kicked, Snackbar.LENGTH_SHORT).show();
                            }
                            @Override
                            public void onError() {
                                Snackbar.make(binding.getRoot(),
                                        R.string.msg_load_failed, Snackbar.LENGTH_LONG).show();
                            }
                        }))
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void showChangeNicknameDialog() {
        DialogSetNicknameBinding dialogBinding = DialogSetNicknameBinding.inflate(getLayoutInflater());
        String currentNickname = new SessionManager(this).getNickname();
        dialogBinding.etNickname.setText(currentNickname);
        if (!currentNickname.isEmpty()) {
            dialogBinding.etNickname.setSelection(currentNickname.length());
        }

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_title_change_nickname)
                .setView(dialogBinding.getRoot())
                .setPositiveButton(R.string.btn_confirm, null)
                .setNegativeButton(R.string.btn_cancel, null)
                .show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String newNickname = getText(dialogBinding.etNickname);
            if (TextUtils.isEmpty(newNickname)) {
                dialogBinding.tilNickname.setError(getString(R.string.error_field_required));
                return;
            }
            dialogBinding.tilNickname.setError(null);
            dialog.dismiss();
            viewModel.updateNickname(groupId, deviceId, newNickname,
                    new GroupDetailViewModel.UpdateNicknameCallback() {
                        @Override
                        public void onSuccess() {
                            new SessionManager(GroupDetailActivity.this).saveNickname(newNickname);
                            Snackbar.make(binding.getRoot(),
                                    R.string.msg_nickname_updated, Snackbar.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onError() {
                            Snackbar.make(binding.getRoot(),
                                    R.string.msg_load_failed, Snackbar.LENGTH_LONG).show();
                        }
                    });
        });
    }

    private void showMembersDialog() {
        viewModel.loadMembers(groupId, members -> {
            String[] items = members.toArray(new String[0]);
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.dialog_title_members)
                    .setItems(items, null)
                    .setPositiveButton(R.string.btn_confirm, null)
                    .show();
        });
    }

    private void showRenameDialog() {
        DialogRenameGroupBinding dialogBinding = DialogRenameGroupBinding.inflate(getLayoutInflater());
        dialogBinding.etGroupName.setText(groupName);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_title_rename_group)
                .setView(dialogBinding.getRoot())
                .setPositiveButton(R.string.btn_confirm, null)
                .setNegativeButton(R.string.btn_cancel, null)
                .show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String newName = getText(dialogBinding.etGroupName);
            if (TextUtils.isEmpty(newName)) {
                dialogBinding.tilGroupName.setError(getString(R.string.error_field_required));
                return;
            }
            dialog.dismiss();
            groupName = newName;
            if (getSupportActionBar() != null) getSupportActionBar().setTitle(newName);
            viewModel.renameGroup(groupId, newName);
            Snackbar.make(binding.getRoot(), R.string.msg_group_renamed, Snackbar.LENGTH_SHORT).show();
        });
    }

    private void onExpensesLoaded(List<Expense> expenses) {
        allExpenses = expenses != null ? expenses : new ArrayList<>();
        applyFilter();
    }

    private void applyFilter() {
        List<Expense> filtered;
        if (selectedCategory == null) {
            filtered = allExpenses;
        } else {
            filtered = new ArrayList<>();
            for (Expense e : allExpenses) {
                if (selectedCategory.equals(e.getCategory())) filtered.add(e);
            }
        }
        adapter.updateData(filtered);
        boolean isEmpty = filtered.isEmpty();
        binding.rvExpenses.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        binding.tvEmptyExpenses.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private void setupCategoryFilter() {
        binding.chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chip_filter_all)           selectedCategory = null;
            else if (id == R.id.chip_filter_food)          selectedCategory = "食";
            else if (id == R.id.chip_filter_housing)       selectedCategory = "住";
            else if (id == R.id.chip_filter_transport)     selectedCategory = "行";
            else if (id == R.id.chip_filter_entertainment) selectedCategory = "娛";
            else if (id == R.id.chip_filter_other)         selectedCategory = "其他";
            applyFilter();
        });
    }

    private void updateBalanceHeader(double balance) {
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

    private void setupSwipeToDelete() {
        int bgColor = ContextCompat.getColor(this, R.color.color_negative);
        Drawable deleteIcon = ContextCompat.getDrawable(this, R.drawable.ic_delete);
        int iconMargin = (int) (24 * getResources().getDisplayMetrics().density);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

                    @Override
                    public int getSwipeDirs(@NonNull RecyclerView recyclerView,
                                            @NonNull RecyclerView.ViewHolder viewHolder) {
                        int pos = viewHolder.getBindingAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION
                                && adapter.getItemViewType(pos) == ExpenseAdapter.TYPE_DATE_HEADER) {
                            return 0;
                        }
                        return super.getSwipeDirs(recyclerView, viewHolder);
                    }

                    @Override
                    public boolean onMove(@NonNull RecyclerView rv,
                                         @NonNull RecyclerView.ViewHolder vh,
                                         @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                        int position = viewHolder.getBindingAdapterPosition();
                        if (position == RecyclerView.NO_POSITION) return;
                        Expense deleted = adapter.getItemAt(position);
                        if (deleted == null) return;
                        viewModel.deleteExpense(deleted.getId());

                        Snackbar.make(binding.getRoot(),
                                        R.string.msg_expense_deleted,
                                        Snackbar.LENGTH_LONG)
                                .setAction(R.string.btn_undo, v -> viewModel.restoreExpense(deleted))
                                .show();
                    }

                    @Override
                    public void onChildDraw(@NonNull Canvas c,
                                           @NonNull RecyclerView recyclerView,
                                           @NonNull RecyclerView.ViewHolder viewHolder,
                                           float dX, float dY,
                                           int actionState, boolean isCurrentlyActive) {
                        if (actionState != ItemTouchHelper.ACTION_STATE_SWIPE) {
                            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                            return;
                        }

                        View itemView = viewHolder.itemView;
                        Paint paint = new Paint();
                        paint.setColor(bgColor);

                        if (dX < 0) {
                            c.drawRect(itemView.getRight() + dX, itemView.getTop(),
                                    itemView.getRight(), itemView.getBottom(), paint);
                            if (deleteIcon != null) {
                                int iconTop = itemView.getTop() + (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
                                int iconLeft = itemView.getRight() - iconMargin - deleteIcon.getIntrinsicWidth();
                                deleteIcon.setBounds(iconLeft, iconTop,
                                        iconLeft + deleteIcon.getIntrinsicWidth(),
                                        iconTop + deleteIcon.getIntrinsicHeight());
                                deleteIcon.draw(c);
                            }
                        }

                        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                    }
                });

        itemTouchHelper.attachToRecyclerView(binding.rvExpenses);
    }

    private void showAddExpenseDialog() {
        viewModel.loadMembersWithIds(groupId, membersMap -> {
            DialogAddExpenseBinding dialogBinding = DialogAddExpenseBinding.inflate(getLayoutInflater());
            String today = new SimpleDateFormat("MM/dd", Locale.getDefault()).format(new Date());
            dialogBinding.etDate.setText(today);
            setupPayerDropdown(dialogBinding, membersMap, myNickname);
            setupDatePicker(dialogBinding, null);
            setupEqualSplitButton(dialogBinding);

            AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.dialog_title_add_expense)
                    .setView(dialogBinding.getRoot())
                    .setPositiveButton(R.string.btn_confirm, null)
                    .setNegativeButton(R.string.btn_cancel, null)
                    .show();

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                if (!validateAndSubmitExpense(dialogBinding, dialog, null)) return;
            });
        });
    }

    private void showEditExpenseDialog(Expense expense) {
        viewModel.loadMembersWithIds(groupId, membersMap -> {
            DialogAddExpenseBinding dialogBinding = DialogAddExpenseBinding.inflate(getLayoutInflater());
            dialogBinding.etDescription.setText(expense.getDescription());
            dialogBinding.etTotalAmount.setText(formatAmount(expense.getTotalAmount()));
            dialogBinding.etMyShare.setText(formatAmount(expense.getMyShare()));
            dialogBinding.etDate.setText(expense.getDate());
            dialogBinding.chipGroupCategory.check(categoryToChipId(expense.getCategory()));
            setupPayerDropdown(dialogBinding, membersMap, expense.getPayer());
            setupDatePicker(dialogBinding, expense.getDate());
            setupEqualSplitButton(dialogBinding);

            AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.dialog_title_edit_expense)
                    .setView(dialogBinding.getRoot())
                    .setPositiveButton(R.string.btn_confirm, null)
                    .setNegativeButton(R.string.btn_cancel, null)
                    .show();

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                validateAndSubmitExpense(dialogBinding, dialog, expense);
            });
        });
    }

    private void setupPayerDropdown(DialogAddExpenseBinding db, Map<String, String> membersMap, String defaultNickname) {
        // Build display list: nickname → nickname(備註) if note exists
        Map<String, String> nicknameToDisplay = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : membersMap.entrySet()) {
            String rawNickname = entry.getValue().replace("★", "").trim();
            String note = getMemberNote(entry.getKey());
            String display = TextUtils.isEmpty(note) ? rawNickname : rawNickname + "(" + note + ")";
            nicknameToDisplay.put(rawNickname, display);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, new ArrayList<>(nicknameToDisplay.values()));
        db.etPayer.setAdapter(adapter);
        String displayDefault = nicknameToDisplay.containsKey(defaultNickname)
                ? nicknameToDisplay.get(defaultNickname) : defaultNickname;
        if (!TextUtils.isEmpty(displayDefault)) {
            db.etPayer.setText(displayDefault, false);
        }
    }

    private String getMemberNote(String targetDeviceId) {
        return getSharedPreferences("member_notes", MODE_PRIVATE)
                .getString(groupId + "_" + targetDeviceId, "");
    }

    private void saveMemberNote(String targetDeviceId, String note) {
        getSharedPreferences("member_notes", MODE_PRIVATE).edit()
                .putString(groupId + "_" + targetDeviceId, note).apply();
    }

    private void setupDatePicker(DialogAddExpenseBinding dialogBinding, String existingDate) {
        dialogBinding.etDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText(getString(R.string.hint_date))
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();
            picker.addOnPositiveButtonClickListener(selection -> {
                String formatted = new SimpleDateFormat("MM/dd", Locale.getDefault())
                        .format(new Date(selection));
                dialogBinding.etDate.setText(formatted);
            });
            picker.show(getSupportFragmentManager(), "date_picker");
        });
    }

    private void setupEqualSplitButton(DialogAddExpenseBinding dialogBinding) {
        dialogBinding.btnEqualSplit.setOnClickListener(v -> {
            String totalStr = getText(dialogBinding.etTotalAmount);
            if (TextUtils.isEmpty(totalStr)) {
                dialogBinding.tilTotalAmount.setError(getString(R.string.error_enter_total_first));
                return;
            }
            try {
                double total = Double.parseDouble(totalStr);
                int count = memberCount > 0 ? memberCount : 2;
                double share = Math.round((total / count) * 100.0) / 100.0;
                dialogBinding.etMyShare.setText(String.valueOf(share));
                dialogBinding.tilTotalAmount.setError(null);
            } catch (NumberFormatException e) {
                dialogBinding.tilTotalAmount.setError(getString(R.string.error_amount_invalid));
            }
        });
    }

    private boolean validateAndSubmitExpense(DialogAddExpenseBinding dialogBinding,
                                              AlertDialog dialog, Expense existing) {
        String desc = getText(dialogBinding.etDescription);
        String payerDisplay = getText(dialogBinding.etPayer);
        int parenIdx = payerDisplay.indexOf('(');
        String payer = parenIdx >= 0 ? payerDisplay.substring(0, parenIdx).trim() : payerDisplay;
        String totalStr = getText(dialogBinding.etTotalAmount);
        String shareStr = getText(dialogBinding.etMyShare);
        String date = getText(dialogBinding.etDate);

        dialogBinding.tilDescription.setError(null);
        dialogBinding.tilPayer.setError(null);
        dialogBinding.tilTotalAmount.setError(null);

        if (TextUtils.isEmpty(desc)) {
            dialogBinding.tilDescription.setError(getString(R.string.error_field_required));
            return false;
        }
        if (TextUtils.isEmpty(payer)) {
            dialogBinding.tilPayer.setError(getString(R.string.error_field_required));
            return false;
        }
        if (TextUtils.isEmpty(totalStr)) {
            dialogBinding.tilTotalAmount.setError(getString(R.string.error_field_required));
            return false;
        }

        double total, share;
        try {
            total = Double.parseDouble(totalStr);
            share = TextUtils.isEmpty(shareStr) ? total : Double.parseDouble(shareStr);
        } catch (NumberFormatException e) {
            dialogBinding.tilTotalAmount.setError(getString(R.string.error_amount_invalid));
            return false;
        }

        String finalDate = TextUtils.isEmpty(date)
                ? new SimpleDateFormat("MM/dd", Locale.getDefault()).format(new Date())
                : date;
        String category = chipIdToCategory(dialogBinding.chipGroupCategory.getCheckedChipId());

        dialog.dismiss();
        if (existing != null) {
            viewModel.updateExpense(existing, desc, payer, total, share, finalDate, category);
            Snackbar.make(binding.getRoot(), R.string.msg_expense_updated, Snackbar.LENGTH_SHORT).show();
        } else {
            viewModel.addExpense(desc, payer, total, share, finalDate, category);
        }
        return true;
    }

    private void showSettlement() {
        SettlementBottomSheetFragment sheet =
                SettlementBottomSheetFragment.newInstance(groupId, memberCount);
        sheet.show(getSupportFragmentManager(), "settlement");
    }

    private void showSettlementTest() {
        List<Expense> expenses = new ArrayList<>();
        expenses.add(new Expense("t1", groupId, "Dinner", "Ming", 600, 300, "Ming", "10/01", "食"));
        expenses.add(new Expense("t2", groupId, "Hotel",  "Alice", 900, 450, "Alice", "10/01", "住"));
        List<String> members = new ArrayList<>();
        members.add("Alice");
        members.add("Ming");
        SettlementBottomSheetFragment sheet =
                SettlementBottomSheetFragment.newInstance(groupId, 2);
        sheet.setTestData(expenses, members);
        sheet.show(getSupportFragmentManager(), "settlement");
    }

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private String getText(AutoCompleteTextView et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private int categoryToChipId(String category) {
        if (category == null) return R.id.chip_food;
        switch (category) {
            case "住": return R.id.chip_housing;
            case "行": return R.id.chip_transport;
            case "娛": return R.id.chip_entertainment;
            case "其他": return R.id.chip_other;
            default:   return R.id.chip_food;
        }
    }

    private String chipIdToCategory(int chipId) {
        if (chipId == R.id.chip_housing) return "住";
        if (chipId == R.id.chip_transport) return "行";
        if (chipId == R.id.chip_entertainment) return "娛";
        if (chipId == R.id.chip_other) return "其他";
        return "食";
    }

    private String formatAmount(double value) {
        if (value == Math.floor(value) && !Double.isInfinite(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
