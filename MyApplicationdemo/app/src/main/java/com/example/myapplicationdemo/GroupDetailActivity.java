package com.example.myapplicationdemo;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplicationdemo.adapter.ExpenseAdapter;
import com.example.myapplicationdemo.databinding.ActivityGroupDetailBinding;
import com.example.myapplicationdemo.databinding.DialogAddExpenseBinding;
import com.example.myapplicationdemo.model.Expense;
import com.example.myapplicationdemo.viewmodel.GroupDetailViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
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

        setupSwipeToDelete();

        binding.btnSettlement.setOnClickListener(v -> showSettlement());
        binding.fabAddExpense.setOnClickListener(v -> showAddExpenseDialog());
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
                        if (position == RecyclerView.NO_ID) return;
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

                        android.view.View itemView = viewHolder.itemView;
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
