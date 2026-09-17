package com.example.myapplicationdemo;

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
import com.example.myapplicationdemo.databinding.DialogRenameGroupBinding;
import com.example.myapplicationdemo.model.Expense;
import com.example.myapplicationdemo.viewmodel.GroupDetailViewModel;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GroupDetailActivity extends AppCompatActivity {

    private ActivityGroupDetailBinding binding;
    private ExpenseAdapter adapter;
    private GroupDetailViewModel viewModel;
    private String groupId;
    private int memberCount;
    private String groupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        groupId = getIntent().getStringExtra("group_id");
        groupName = getIntent().getStringExtra("group_name");
        memberCount = getIntent().getIntExtra("group_member_count", 2);

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
        viewModel.getLoading().observe(this, loading ->
                binding.swipeRefresh.setRefreshing(loading));
        viewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_LONG).show();
            }
        });
        viewModel.loadExpenses(groupId);

        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.loadExpenses(groupId));

        setupSwipeToDelete();
        binding.btnSettlement.setOnClickListener(v -> showSettlement());
        binding.fabAddExpense.setOnClickListener(v -> showAddExpenseDialog());

        getSupportFragmentManager().setFragmentResultListener(
                "settlement_result", this,
                (requestKey, result) -> viewModel.loadExpenses(groupId));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_group_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_rename_group) {
            showRenameDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
        adapter.updateData(expenses);
        boolean isEmpty = expenses == null || expenses.isEmpty();
        binding.rvExpenses.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        binding.tvEmptyExpenses.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
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
        DialogAddExpenseBinding dialogBinding = DialogAddExpenseBinding.inflate(getLayoutInflater());
        String today = new SimpleDateFormat("MM/dd", Locale.getDefault()).format(new Date());
        dialogBinding.etDate.setText(today);

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
    }

    private void showEditExpenseDialog(Expense expense) {
        DialogAddExpenseBinding dialogBinding = DialogAddExpenseBinding.inflate(getLayoutInflater());
        dialogBinding.etDescription.setText(expense.getDescription());
        dialogBinding.etPayer.setText(expense.getPayer());
        dialogBinding.etTotalAmount.setText(String.valueOf(expense.getTotalAmount()));
        dialogBinding.etMyShare.setText(String.valueOf(expense.getMyShare()));
        dialogBinding.etDate.setText(expense.getDate());

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
        String payer = getText(dialogBinding.etPayer);
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

        dialog.dismiss();
        if (existing != null) {
            viewModel.updateExpense(existing, desc, payer, total, share, finalDate);
            Snackbar.make(binding.getRoot(), R.string.msg_expense_updated, Snackbar.LENGTH_SHORT).show();
        } else {
            viewModel.addExpense(desc, payer, total, share, finalDate);
        }
        return true;
    }

    private void showSettlement() {
        SettlementBottomSheetFragment sheet =
                SettlementBottomSheetFragment.newInstance(groupId);
        sheet.show(getSupportFragmentManager(), "settlement");
    }

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
