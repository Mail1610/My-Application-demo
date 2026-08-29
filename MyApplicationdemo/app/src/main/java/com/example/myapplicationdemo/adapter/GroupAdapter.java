package com.example.myapplicationdemo.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplicationdemo.GroupDetailActivity;
import com.example.myapplicationdemo.R;
import com.example.myapplicationdemo.databinding.ItemGroupBinding;
import com.example.myapplicationdemo.model.Group;
import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> {

    private static final int[] AVATAR_COLORS = {
        0xFF4361EE, 0xFF7B2FBE, 0xFFE63946, 0xFF00A878, 0xFFF4A261
    };

    private final List<Group> groups;

    public GroupAdapter(List<Group> groups) {
        this.groups = groups;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGroupBinding binding = ItemGroupBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(groups.get(position));
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemGroupBinding binding;

        ViewHolder(ItemGroupBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Group group) {
            Context ctx = itemView.getContext();

            // Avatar
            binding.tvGroupInitial.setText(group.getName().substring(0, 1));
            GradientDrawable circle = new GradientDrawable();
            circle.setShape(GradientDrawable.OVAL);
            circle.setColor(AVATAR_COLORS[group.getId() % AVATAR_COLORS.length]);
            binding.tvGroupInitial.setBackground(circle);

            binding.tvGroupName.setText(group.getName());
            binding.tvMemberCount.setText(group.getMemberCount() + " " + ctx.getString(R.string.label_members));

            double balance = group.getMyBalance();
            if (balance > 0) {
                binding.tvBalance.setText(String.format("收 $%.0f", balance));
                binding.tvBalance.setTextColor(ContextCompat.getColor(ctx, R.color.color_positive));
            } else if (balance < 0) {
                binding.tvBalance.setText(String.format("付 $%.0f", Math.abs(balance)));
                binding.tvBalance.setTextColor(ContextCompat.getColor(ctx, R.color.color_negative));
            } else {
                binding.tvBalance.setText("已結清");
                binding.tvBalance.setTextColor(ContextCompat.getColor(ctx, R.color.color_settled));
            }

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(ctx, GroupDetailActivity.class);
                intent.putExtra("group_id", group.getId());
                intent.putExtra("group_name", group.getName());
                intent.putExtra("group_balance", group.getMyBalance());
                ctx.startActivity(intent);
            });
        }
    }
}
