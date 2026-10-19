package com.example.myapplicationdemo;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.example.myapplicationdemo.model.Expense;
import com.example.myapplicationdemo.repository.ExpenseRepository;
import com.example.myapplicationdemo.repository.GroupRepository;
import com.example.myapplicationdemo.utils.LogUtils;
import com.example.myapplicationdemo.utils.SessionManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SettlementBottomSheetFragment extends BottomSheetDialogFragment {

    private static final String ARG_GROUP_ID   = "group_id";
    private static final String ARG_MEMBER_COUNT = "member_count";

    private String suggestedPayee = null;
    private List<String> groupMembers = new ArrayList<>();
    private List<Expense> testExpenses = null;
    private List<String> testMembers = null;

    public static SettlementBottomSheetFragment newInstance(String groupId, int memberCount) {
        SettlementBottomSheetFragment f = new SettlementBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString(ARG_GROUP_ID, groupId);
        args.putInt(ARG_MEMBER_COUNT, memberCount);
        f.setArguments(args);
        return f;
    }

    public void setTestData(List<Expense> expenses, List<String> members) {
        this.testExpenses = expenses;
        this.testMembers = members;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settlement_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String groupId   = requireArguments().getString(ARG_GROUP_ID);
        int memberCount  = requireArguments().getInt(ARG_MEMBER_COUNT, 2);
        TextView tvNetBalance       = view.findViewById(R.id.tv_net_balance);
        LinearLayout llItems        = view.findViewById(R.id.ll_settlement_items);
        TextView tvSettled          = view.findViewById(R.id.tv_settled);
        TextView tvWaiting          = view.findViewById(R.id.tv_waiting_payment);
        MaterialButton btnRecordPayment = view.findViewById(R.id.btn_record_payment);

        String myName = new SessionManager(requireContext()).getUsername();
        ExpenseRepository repo = new ExpenseRepository();
        GroupRepository groupRepo = new GroupRepository();

        if (testExpenses != null) {
            groupMembers = testMembers != null ? testMembers : new ArrayList<>();
            renderSettlement(tvNetBalance, llItems, tvSettled, tvWaiting,
                    btnRecordPayment, testExpenses, myName, memberCount);
        } else {
            new Thread(() -> {
                try {
                    List<Expense> expenses = repo.getByGroupId(groupId);
                    List<String> members   = groupRepo.getMembers(groupId);
                    if (!isAdded()) return;
                    requireActivity().runOnUiThread(() -> {
                        groupMembers = members;
                        renderSettlement(tvNetBalance, llItems, tvSettled, tvWaiting,
                                btnRecordPayment, expenses, myName, memberCount);
                    });
                } catch (Exception e) {
                    LogUtils.error("載入結算失敗", e);
                }
            }).start();
        }

        btnRecordPayment.setOnClickListener(v ->
                showRecordPaymentDialog(groupId, repo, myName, memberCount,
                        tvNetBalance, llItems, tvSettled, tvWaiting, btnRecordPayment));
    }

    // ─── Record payment dialog ─────────────────────────────────────────────────

    private void showRecordPaymentDialog(String groupId, ExpenseRepository repo, String myName,
            int memberCount, TextView tvNetBalance, LinearLayout llItems, TextView tvSettled,
            TextView tvWaiting, MaterialButton btnRecordPayment) {

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_record_payment, null, false);
        TextInputLayout tilPayTo   = dialogView.findViewById(R.id.til_pay_to);
        TextInputEditText etPayTo  = dialogView.findViewById(R.id.et_pay_to);
        TextInputLayout tilAmount  = dialogView.findViewById(R.id.til_payment_amount);
        TextInputEditText etAmount = dialogView.findViewById(R.id.et_payment_amount);

        if (suggestedPayee != null && !suggestedPayee.isEmpty()) {
            etPayTo.setText(suggestedPayee);
            etPayTo.setSelection(suggestedPayee.length());
        }

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_title_record_payment)
                .setView(dialogView)
                .setPositiveButton(R.string.btn_confirm, null)
                .setNegativeButton(R.string.btn_cancel, null)
                .show();

        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String payTo     = etPayTo.getText() != null ? etPayTo.getText().toString().trim() : "";
            String amountStr = etAmount.getText() != null ? etAmount.getText().toString().trim() : "";
            tilPayTo.setError(null);
            tilAmount.setError(null);

            if (TextUtils.isEmpty(payTo)) {
                tilPayTo.setError(getString(R.string.error_field_required));
                return;
            }
            if (TextUtils.isEmpty(amountStr)) {
                tilAmount.setError(getString(R.string.error_field_required));
                return;
            }
            double amount;
            try {
                amount = Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                tilAmount.setError(getString(R.string.error_amount_invalid));
                return;
            }

            dialog.dismiss();
            String today = new SimpleDateFormat("MM/dd", Locale.getDefault()).format(new Date());
            double finalAmount = amount;

            new Thread(() -> {
                try {
                    repo.addExpense(groupId, getString(R.string.label_settlement_payment),
                            myName, finalAmount, 0.0, myName, today, "其他");
                    List<Expense> updated = repo.getByGroupId(groupId);
                    if (!isAdded()) return;
                    requireActivity().runOnUiThread(() -> {
                        Bundle result = new Bundle();
                        result.putBoolean("refreshed", true);
                        getParentFragmentManager().setFragmentResult("settlement_result", result);
                        llItems.removeAllViews();
                        tvSettled.setVisibility(View.GONE);
                        renderSettlement(tvNetBalance, llItems, tvSettled, tvWaiting,
                                btnRecordPayment, updated, myName, memberCount);
                    });
                } catch (Exception e) {
                    LogUtils.error("記錄付款失敗", e);
                }
            }).start();
        });
    }

    // ─── Settlement rendering ──────────────────────────────────────────────────

    private void renderSettlement(TextView tvNetBalance, LinearLayout llItems,
                                  TextView tvSettled, TextView tvWaiting,
                                  MaterialButton btnRecordPayment, List<Expense> expenses,
                                  String myName, int memberCount) {

        // ── 1. Personal balance (for header + button) ──
        double balance = 0;
        suggestedPayee = null;

        for (Expense e : expenses) {
            double share = e.getShareFor(myName, memberCount);
            if (myName.equals(e.getPayer())) {
                balance += e.getTotalAmount() - share;
            } else {
                balance -= share;
            }
        }

        // ── 2. Update header ──
        if (balance > 0) {
            tvNetBalance.setText(getString(R.string.label_balance_net_positive, balance));
            tvNetBalance.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_positive));
            btnRecordPayment.setVisibility(View.GONE);
            tvWaiting.setVisibility(View.VISIBLE);
        } else if (balance < 0) {
            tvNetBalance.setText(getString(R.string.label_balance_net_negative, Math.abs(balance)));
            tvNetBalance.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_negative));
            btnRecordPayment.setVisibility(View.VISIBLE);
            tvWaiting.setVisibility(View.GONE);
        } else {
            tvNetBalance.setText(R.string.label_net_balance_placeholder);
            tvNetBalance.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            btnRecordPayment.setVisibility(View.GONE);
            tvWaiting.setVisibility(View.GONE);
        }

        if (balance == 0 && isGroupSettled(expenses, memberCount)) {
            tvSettled.setVisibility(View.VISIBLE);
            return;
        }

        // ── 3. Global net balances for all members ──
        Map<String, Double> netBalances = new LinkedHashMap<>();
        for (String m : groupMembers) netBalances.put(m, 0.0);
        // Ensure current user is included even if not in groupMembers yet
        if (!netBalances.containsKey(myName)) netBalances.put(myName, 0.0);

        for (Expense e : expenses) {
            for (String m : netBalances.keySet()) {
                double share = e.getShareFor(m, memberCount);
                if (m.equals(e.getPayer())) {
                    netBalances.merge(m, e.getTotalAmount() - share, Double::sum);
                } else {
                    netBalances.merge(m, -share, Double::sum);
                }
            }
        }

        // Update suggested payee: the creditor with the most outstanding balance
        double maxCredit = 0;
        for (Map.Entry<String, Double> entry : netBalances.entrySet()) {
            if (!entry.getKey().equals(myName) && entry.getValue() > maxCredit) {
                maxCredit = entry.getValue();
                suggestedPayee = entry.getKey();
            }
        }

        // ── 4. Greedy minimum-transactions ──
        List<String[]> transactions = computeMinTransactions(netBalances);

        // ── 5. Render transaction list ──
        if (!transactions.isEmpty()) {
            addSectionHeader(llItems, getString(R.string.label_min_transactions_title));
            for (String[] tx : transactions) {
                String from = tx[0], to = tx[1], amount = tx[2];
                boolean iAmPaying   = from.equals(myName);
                boolean iAmReceiving = to.equals(myName);
                String label = getString(R.string.label_transaction_item, from, to, Double.parseDouble(amount));
                int color;
                if (iAmPaying)        color = R.color.color_negative;
                else if (iAmReceiving) color = R.color.color_positive;
                else                   color = R.color.color_text_secondary;
                addRow(llItems, label, color);
            }
        }
    }

    /**
     * Greedy min-transactions: repeatedly pair the largest debtor with the largest creditor.
     * Returns list of [from, to, amountString].
     */
    private List<String[]> computeMinTransactions(Map<String, Double> netBalances) {
        List<String[]> transactions = new ArrayList<>();

        List<Object[]> creditors = new ArrayList<>(); // {name, amount}
        List<Object[]> debtors   = new ArrayList<>(); // {name, amount} (amount > 0)

        for (Map.Entry<String, Double> entry : netBalances.entrySet()) {
            double b = entry.getValue();
            if (b > 0.005)  creditors.add(new Object[]{entry.getKey(), b});
            else if (b < -0.005) debtors.add(new Object[]{entry.getKey(), -b});
        }

        while (!creditors.isEmpty() && !debtors.isEmpty()) {
            creditors.sort((a, b) -> Double.compare((double) b[1], (double) a[1]));
            debtors.sort((a, b) -> Double.compare((double) b[1], (double) a[1]));

            Object[] c = creditors.get(0);
            Object[] d = debtors.get(0);

            double amount = Math.min((double) c[1], (double) d[1]);
            amount = Math.round(amount);

            if (amount >= 1) {
                transactions.add(new String[]{
                    (String) d[0],
                    (String) c[0],
                    String.valueOf((long) amount)
                });
            }

            c[1] = (double) c[1] - amount;
            d[1] = (double) d[1] - amount;

            if ((double) c[1] < 0.5) creditors.remove(0);
            if ((double) d[1] < 0.5) debtors.remove(0);
        }

        return transactions;
    }

    /** Returns true only when every member's net balance rounds to zero. */
    private boolean isGroupSettled(List<Expense> expenses, int memberCount) {
        if (groupMembers.isEmpty()) return false;
        Map<String, Double> net = new LinkedHashMap<>();
        for (String m : groupMembers) net.put(m, 0.0);
        for (Expense e : expenses) {
            for (String m : net.keySet()) {
                double share = e.getShareFor(m, memberCount);
                if (m.equals(e.getPayer())) net.merge(m, e.getTotalAmount() - share, Double::sum);
                else net.merge(m, -share, Double::sum);
            }
        }
        for (double b : net.values()) {
            if (Math.abs(b) > 0.5) return false;
        }
        return true;
    }

    // ─── UI helpers ───────────────────────────────────────────────────────────

    private void addSectionHeader(LinearLayout container, String text) {
        TextView tv = new TextView(requireContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dpToPx(12);
        tv.setLayoutParams(params);
        tv.setText(text);
        tv.setTextSize(12);
        tv.setLetterSpacing(0.08f);
        tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_text_hint));
        container.addView(tv);
    }

    private void addRow(LinearLayout container, String text, int colorRes) {
        TextView tv = new TextView(requireContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dpToPx(12);
        tv.setLayoutParams(params);
        tv.setText(text);
        tv.setTextSize(15);
        tv.setTextColor(ContextCompat.getColor(requireContext(), colorRes));
        container.addView(tv);
    }

    private int dpToPx(int dp) {
        return (int) (dp * requireContext().getResources().getDisplayMetrics().density);
    }
}
