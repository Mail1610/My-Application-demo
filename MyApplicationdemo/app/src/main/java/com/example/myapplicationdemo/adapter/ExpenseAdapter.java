package com.example.myapplicationdemo.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplicationdemo.databinding.ItemExpenseBinding;
import com.example.myapplicationdemo.model.Expense;
import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ViewHolder> {

    private static final int[] ACCENT_COLORS = {
        0xFF4361EE, 0xFF7B2FBE, 0xFFE63946, 0xFF00A878, 0xFFF4A261
    };

    private final List<Expense> expenses;

    public ExpenseAdapter(List<Expense> expenses) {
        this.expenses = expenses;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemExpenseBinding binding = ItemExpenseBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(expenses.get(position), position);
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemExpenseBinding binding;

        ViewHolder(ItemExpenseBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Expense expense, int position) {
            // Left accent strip color
            binding.viewAccent.setBackgroundColor(ACCENT_COLORS[position % ACCENT_COLORS.length]);

            // Split date "08/29" → month="08", day="29"
            String[] parts = expense.getDate().split("/");
            if (parts.length == 2) {
                binding.tvDateMonth.setText(parts[0] + "月");
                binding.tvDateDay.setText(parts[1]);
            } else {
                binding.tvDateMonth.setText("");
                binding.tvDateDay.setText(expense.getDate());
            }

            binding.tvDescription.setText(expense.getDescription());
            binding.tvPayer.setText(expense.getPayer() + " 付款");
            binding.tvAmount.setText(String.format("$%.0f", expense.getTotalAmount()));
            binding.tvMyShare.setText(String.format("我分攤 $%.0f", expense.getMyShare()));
        }
    }
}
