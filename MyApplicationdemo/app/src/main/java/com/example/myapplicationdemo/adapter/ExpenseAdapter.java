package com.example.myapplicationdemo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplicationdemo.R;
import com.example.myapplicationdemo.databinding.ItemDateHeaderBinding;
import com.example.myapplicationdemo.databinding.ItemExpenseBinding;
import com.example.myapplicationdemo.model.Expense;
import java.util.ArrayList;
import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_DATE_HEADER = 0;
    public static final int TYPE_EXPENSE     = 1;

    private static int categoryColor(String category) {
        if (category == null) return 0xFF9E9E9E;
        switch (category) {
            case "食": return 0xFFF4A261;
            case "住": return 0xFF4361EE;
            case "行": return 0xFF00A878;
            case "娛": return 0xFF7B2FBE;
            default:   return 0xFF9E9E9E;
        }
    }

    public interface OnExpenseEditListener {
        void onEdit(Expense expense);
    }

    private final List<Object> items = new ArrayList<>();
    private OnExpenseEditListener editListener;

    public ExpenseAdapter(List<Expense> expenses) {
        items.addAll(buildItems(expenses));
    }

    public void setOnExpenseEditListener(OnExpenseEditListener listener) {
        this.editListener = listener;
    }

    private static List<Object> buildItems(List<Expense> expenses) {
        List<Object> result = new ArrayList<>();
        String lastDate = null;
        for (Expense e : expenses) {
            String date = e.getDate();
            if (!date.equals(lastDate)) {
                result.add(formatDateHeader(date));
                lastDate = date;
            }
            result.add(e);
        }
        return result;
    }

    private static String formatDateHeader(String date) {
        String[] parts = date.split("/");
        if (parts.length == 2) {
            return parts[0] + "月" + parts[1] + "日";
        }
        return date;
    }

    public void updateData(List<Expense> newExpenses) {
        List<Object> newItems = buildItems(newExpenses);
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return items.size(); }
            @Override public int getNewListSize() { return newItems.size(); }

            @Override
            public boolean areItemsTheSame(int o, int n) {
                Object old = items.get(o), neu = newItems.get(n);
                if (old instanceof String && neu instanceof String) return old.equals(neu);
                if (old instanceof Expense && neu instanceof Expense)
                    return ((Expense) old).getId().equals(((Expense) neu).getId());
                return false;
            }

            @Override
            public boolean areContentsTheSame(int o, int n) {
                Object old = items.get(o), neu = newItems.get(n);
                if (old instanceof String) return old.equals(neu);
                if (old instanceof Expense && neu instanceof Expense) {
                    Expense oe = (Expense) old, ne = (Expense) neu;
                    return oe.getDescription().equals(ne.getDescription())
                            && Double.compare(oe.getTotalAmount(), ne.getTotalAmount()) == 0
                            && Double.compare(oe.getMyShare(), ne.getMyShare()) == 0;
                }
                return false;
            }
        });
        items.clear();
        items.addAll(newItems);
        result.dispatchUpdatesTo(this);
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof String ? TYPE_DATE_HEADER : TYPE_EXPENSE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_DATE_HEADER) {
            return new DateHeaderViewHolder(
                    ItemDateHeaderBinding.inflate(inflater, parent, false));
        }
        return new ExpenseViewHolder(
                ItemExpenseBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof DateHeaderViewHolder) {
            ((DateHeaderViewHolder) holder).bind((String) items.get(position));
        } else {
            ((ExpenseViewHolder) holder).bind((Expense) items.get(position), editListener);
        }
    }

    @Override
    public int getItemCount() { return items.size(); }

    /** Returns the Expense at position, or null if the position is a date header. */
    public Expense getItemAt(int position) {
        if (position < 0 || position >= items.size()) return null;
        Object item = items.get(position);
        return item instanceof Expense ? (Expense) item : null;
    }

    // ── ViewHolders ──────────────────────────────────────────────────────────

    static class DateHeaderViewHolder extends RecyclerView.ViewHolder {
        private final ItemDateHeaderBinding binding;
        DateHeaderViewHolder(ItemDateHeaderBinding b) {
            super(b.getRoot());
            this.binding = b;
        }
        void bind(String dateLabel) {
            binding.tvDateHeader.setText(dateLabel);
        }
    }

    static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        private final ItemExpenseBinding binding;
        ExpenseViewHolder(ItemExpenseBinding b) {
            super(b.getRoot());
            this.binding = b;
        }
        void bind(Expense expense, OnExpenseEditListener editListener) {
            Context ctx = itemView.getContext();
            binding.viewAccent.setBackgroundColor(categoryColor(expense.getCategory()));
            binding.tvDescription.setText(expense.getDescription());
            binding.tvPayer.setText(ctx.getString(R.string.label_paid_by_user, expense.getPayer()));
            binding.tvAmount.setText(ctx.getString(R.string.label_amount_format, expense.getTotalAmount()));
            binding.tvMyShare.setText(ctx.getString(R.string.label_my_share_amount, expense.getMyShare()));
            itemView.setOnLongClickListener(v -> {
                if (editListener != null) editListener.onEdit(expense);
                return true;
            });
        }
    }
}
