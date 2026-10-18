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
import com.example.myapplicationdemo.utils.LogUtils;
import com.example.myapplicationdemo.utils.SessionManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SettlementBottomSheetFragment extends BottomSheetDialogFragment {

    private static final String ARG_GROUP_ID = "group_id";
    private static final String ARG_MEMBER_COUNT = "member_count";

    private String suggestedPayee = null;

    public static SettlementBottomSheetFragment newInstance(String groupId, int memberCount) {
        SettlementBottomSheetFragment f = new SettlementBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString(ARG_GROUP_ID, groupId);
        args.putInt(ARG_MEMBER_COUNT, memberCount);
        f.setArguments(args);
        return f;
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

        String groupId = requireArguments().getString(ARG_GROUP_ID);
        int memberCount = requireArguments().getInt(ARG_MEMBER_COUNT, 2);
        TextView tvNetBalance = view.findViewById(R.id.tv_net_balance);
        LinearLayout llItems = view.findViewById(R.id.ll_settlement_items);
        TextView tvSettled = view.findViewById(R.id.tv_settled);
        MaterialButton btnRecordPayment = view.findViewById(R.id.btn_record_payment);

        String myName = new SessionManager(requireContext()).getUsername();
        ExpenseRepository repo = new ExpenseRepository();

        new Thread(() -> {
            try {
                List<Expense> expenses = repo.getByGroupId(groupId);
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() ->
                        renderSettlement(tvNetBalance, llItems, tvSettled, expenses, myName, memberCount));
            } catch (Exception e) {
                LogUtils.error("載入結算失敗", e);
            }
        }).start();

        btnRecordPayment.setOnClickListener(v ->
                showRecordPaymentDialog(groupId, repo, myName, memberCount, tvNetBalance, llItems, tvSettled));
    }

    private void showRecordPaymentDialog(String groupId, ExpenseRepository repo, String myName,
            int memberCount, TextView tvNetBalance, LinearLayout llItems, TextView tvSettled) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_record_payment, null, false);

        TextInputLayout tilPayTo = dialogView.findViewById(R.id.til_pay_to);
        TextInputEditText etPayTo = dialogView.findViewById(R.id.et_pay_to);
        TextInputLayout tilAmount = dialogView.findViewById(R.id.til_payment_amount);
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
            String payTo = etPayTo.getText() != null ? etPayTo.getText().toString().trim() : "";
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
            String paymentLabel = getString(R.string.label_settlement_payment);

            new Thread(() -> {
                try {
                    repo.addExpense(groupId, paymentLabel, myName, finalAmount, 0.0,
                            myName, today, "其他");
                    List<Expense> updated = repo.getByGroupId(groupId);
                    if (!isAdded()) return;
                    requireActivity().runOnUiThread(() -> {
                        Bundle result = new Bundle();
                        result.putBoolean("refreshed", true);
                        getParentFragmentManager().setFragmentResult("settlement_result", result);
                        llItems.removeAllViews();
                        tvSettled.setVisibility(View.GONE);
                        renderSettlement(tvNetBalance, llItems, tvSettled, updated, myName, memberCount);
                    });
                } catch (Exception e) {
                    LogUtils.error("記錄付款失敗", e);
                }
            }).start();
        });
    }

    private void renderSettlement(TextView tvNetBalance, LinearLayout llItems,
                                  TextView tvSettled, List<Expense> expenses,
                                  String myName, int memberCount) {
        double balance = 0;
        Map<String, Double> iOwe = new LinkedHashMap<>();
        double othersOweMe = 0;

        for (Expense e : expenses) {
            double share = shareFor(e, myName, memberCount);
            if (myName.equals(e.getPayer())) {
                double credit = e.getTotalAmount() - share;
                othersOweMe += credit;
                balance += credit;
            } else {
                iOwe.merge(e.getPayer(), share, Double::sum);
                balance -= share;
            }
        }

        // Update suggested payee: person with the largest outstanding debt
        suggestedPayee = null;
        double maxDebt = 0;
        for (Map.Entry<String, Double> entry : iOwe.entrySet()) {
            if (entry.getValue() > maxDebt) {
                maxDebt = entry.getValue();
                suggestedPayee = entry.getKey();
            }
        }

        if (balance > 0) {
            tvNetBalance.setText(getString(R.string.label_balance_net_positive, balance));
            tvNetBalance.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_positive));
        } else if (balance < 0) {
            tvNetBalance.setText(getString(R.string.label_balance_net_negative, Math.abs(balance)));
            tvNetBalance.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_negative));
        } else {
            tvNetBalance.setText(R.string.label_net_balance_placeholder);
            tvNetBalance.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        }

        // When balance is zero, skip individual items — show settled state directly
        if (balance == 0) {
            tvSettled.setVisibility(View.VISIBLE);
            return;
        }

        for (Map.Entry<String, Double> entry : iOwe.entrySet()) {
            if (entry.getValue() > 0) {
                addRow(llItems, getString(R.string.label_settlement_i_owe,
                        entry.getKey(), entry.getValue()), false);
            }
        }

        if (othersOweMe > 0) {
            addRow(llItems, getString(R.string.label_settlement_others_owe, othersOweMe), true);
        }
    }

    private void addRow(LinearLayout container, String text, boolean isPositive) {
        TextView tv = new TextView(requireContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dpToPx(12);
        tv.setLayoutParams(params);
        tv.setText(text);
        tv.setTextSize(15);
        tv.setTextColor(ContextCompat.getColor(requireContext(),
                isPositive ? R.color.color_positive : R.color.color_negative));
        container.addView(tv);
    }

    private double shareFor(Expense e, String nickname, int memberCount) {
        String adder = e.getAdderNickname();
        if (adder == null || adder.isEmpty() || adder.equals(nickname)) {
            return e.getMyShare();
        }
        return (e.getTotalAmount() - e.getMyShare()) / Math.max(1, memberCount - 1);
    }

    private int dpToPx(int dp) {
        return (int) (dp * requireContext().getResources().getDisplayMetrics().density);
    }
}
