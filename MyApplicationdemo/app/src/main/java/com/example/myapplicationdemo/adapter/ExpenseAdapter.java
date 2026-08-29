package com.example.myapplicationdemo.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplicationdemo.databinding.ItemExpenseBinding;
import com.example.myapplicationdemo.model.Expense;
import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ViewHolder> {

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
        holder.bind(expenses.get(position));
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

        void bind(Expense expense) {
            binding.tvDescription.setText(expense.getDescription());
            binding.tvPayer.setText(expense.getPayer() + " 付款");
            binding.tvDate.setText(expense.getDate());
            binding.tvAmount.setText(String.format("$%.0f", expense.getTotalAmount()));
            binding.tvMyShare.setText(String.format("我的分攤：$%.0f", expense.getMyShare()));
        }
    }
}
