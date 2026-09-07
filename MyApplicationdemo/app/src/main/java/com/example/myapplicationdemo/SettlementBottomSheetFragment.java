package com.example.myapplicationdemo;

import android.os.Bundle;
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
import com.example.myapplicationdemo.utils.SessionManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SettlementBottomSheetFragment extends BottomSheetDialogFragment {

    private static final String ARG_GROUP_ID = "group_id";

    public static SettlementBottomSheetFragment newInstance(int groupId) {
        SettlementBottomSheetFragment f = new SettlementBottomSheetFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_GROUP_ID, groupId);
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

        int groupId = requireArguments().getInt(ARG_GROUP_ID);
        TextView tvNetBalance = view.findViewById(R.id.tv_net_balance);
        LinearLayout llItems = view.findViewById(R.id.ll_settlement_items);
        TextView tvSettled = view.findViewById(R.id.tv_settled);

        String myName = new SessionManager(requireContext()).getUsername();
        ExpenseRepository repo = new ExpenseRepository();

        new Thread(() -> {
            List<Expense> expenses = repo.getByGroupId(groupId);
            if (!isAdded()) return;
            requireActivity().runOnUiThread(() ->
                    renderSettlement(tvNetBalance, llItems, tvSettled, expenses, myName));
        }).start();
    }

    private void renderSettlement(TextView tvNetBalance, LinearLayout llItems,
                                  TextView tvSettled, List<Expense> expenses, String myName) {
        // Compute balance from actual expenses
        double balance = 0;
        Map<String, Double> iOwe = new LinkedHashMap<>();
        double othersOweMe = 0;

        for (Expense e : expenses) {
            if (myName.equals(e.getPayer())) {
                double credit = e.getTotalAmount() - e.getMyShare();
                othersOweMe += credit;
                balance += credit;
            } else {
                iOwe.merge(e.getPayer(), e.getMyShare(), Double::sum);
                balance -= e.getMyShare();
            }
        }

        // Update net balance header
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

        boolean hasItems = false;

        for (Map.Entry<String, Double> entry : iOwe.entrySet()) {
            if (entry.getValue() > 0) {
                addRow(llItems, getString(R.string.label_settlement_i_owe,
                        entry.getKey(), entry.getValue()), false);
                hasItems = true;
            }
        }

        if (othersOweMe > 0) {
            addRow(llItems, getString(R.string.label_settlement_others_owe, othersOweMe), true);
            hasItems = true;
        }

        if (!hasItems) {
            tvSettled.setVisibility(View.VISIBLE);
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

    private int dpToPx(int dp) {
        return (int) (dp * requireContext().getResources().getDisplayMetrics().density);
    }
}
