package com.example.myapplicationdemo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplicationdemo.R;
import com.example.myapplicationdemo.databinding.ItemExpenseBinding;
import com.example.myapplicationdemo.model.Expense;
import java.util.ArrayList;
import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ViewHolder> {

    private static final int[] ACCENT_COLORS = {
        0xFF4361EE, 0xFF7B2FBE, 0xFFE63946, 0xFF00A878, 0xFFF4A261
    };

    private final List<Expense> expenses = new ArrayList<>();

    public ExpenseAdapter(List<Expense> expenses) {
        this.expenses.addAll(expenses);
    }

    public void updateData(List<Expense> newExpenses) {
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return expenses.size(); }
            @Override public int getNewListSize() { return newExpenses.size(); }
            @Override public boolean areItemsTheSame(int o, int n) {
                return expenses.get(o).getId() == newExpenses.get(n).getId();
            }
            @Override public boolean areContentsTheSame(int o, int n) {
                Expense old = expenses.get(o), neu = newExpenses.get(n);
                return old.getDescription().equals(neu.getDescription())
                        && Double.compare(old.getTotalAmount(), neu.getTotalAmount()) == 0
                        && Double.compare(old.getMyShare(), neu.getMyShare()) == 0;
            }
        });
        expenses.clear();
        expenses.addAll(newExpenses);
        result.dispatchUpdatesTo(this);
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
    public int getItemCount() { return expenses.size(); }

    public Expense getItemAt(int position) { return expenses.get(position); }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemExpenseBinding binding;

        ViewHolder(ItemExpenseBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Expense expense, int position) {
            Context ctx = itemView.getContext();
            binding.viewAccent.setBackgroundColor(ACCENT_COLORS[position % ACCENT_COLORS.length]);

            String[] parts = expense.getDate().split("/");
            if (parts.length == 2) {
                binding.tvDateMonth.setText(ctx.getString(R.string.label_month_format, parts[0]));
                binding.tvDateDay.setText(parts[1]);
            } else {
                binding.tvDateMonth.setText("");
                binding.tvDateDay.setText(expense.getDate());
            }

            binding.tvDescription.setText(expense.getDescription());
            binding.tvPayer.setText(ctx.getString(R.string.label_paid_by_user, expense.getPayer()));
            binding.tvAmount.setText(ctx.getString(R.string.label_amount_format, expense.getTotalAmount()));
            binding.tvMyShare.setText(ctx.getString(R.string.label_my_share_amount, expense.getMyShare()));
        }
    }
}
