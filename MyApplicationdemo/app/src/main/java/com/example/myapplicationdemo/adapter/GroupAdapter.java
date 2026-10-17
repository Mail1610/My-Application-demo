package com.example.myapplicationdemo.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplicationdemo.GroupDetailActivity;
import com.example.myapplicationdemo.R;
import com.example.myapplicationdemo.databinding.ItemGroupBinding;
import com.example.myapplicationdemo.model.Group;
import java.util.ArrayList;
import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> {

    private static final int[] AVATAR_COLORS = {
        0xFF4361EE, 0xFF7B2FBE, 0xFFE63946, 0xFF00A878, 0xFFF4A261
    };

    public interface OnGroupDeleteListener {
        void onDelete(Group group);
    }

    private final List<Group> groups = new ArrayList<>();
    private OnGroupDeleteListener deleteListener;

    public GroupAdapter(List<Group> groups) {
        this.groups.addAll(groups);
    }

    public void setOnGroupDeleteListener(OnGroupDeleteListener listener) {
        this.deleteListener = listener;
    }

    public void updateData(List<Group> newGroups) {
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return groups.size(); }
            @Override public int getNewListSize() { return newGroups.size(); }
            @Override public boolean areItemsTheSame(int o, int n) {
                return groups.get(o).getId().equals(newGroups.get(n).getId());
            }
            @Override public boolean areContentsTheSame(int o, int n) {
                Group old = groups.get(o), neu = newGroups.get(n);
                return old.getName().equals(neu.getName())
                        && old.getMemberCount() == neu.getMemberCount()
                        && Double.compare(old.getMyBalance(), neu.getMyBalance()) == 0;
            }
        });
        groups.clear();
        groups.addAll(newGroups);
        result.dispatchUpdatesTo(this);
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
        holder.bind(groups.get(position), deleteListener);
    }

    @Override
    public int getItemCount() { return groups.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemGroupBinding binding;

        ViewHolder(ItemGroupBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Group group, OnGroupDeleteListener deleteListener) {
            Context ctx = itemView.getContext();

            String name = group.getName();
            String initial = (name != null && !name.isEmpty()) ? name.substring(0, 1) : "?";
            binding.tvGroupInitial.setText(initial);
            GradientDrawable circle = new GradientDrawable();
            circle.setShape(GradientDrawable.OVAL);
            circle.setColor(AVATAR_COLORS[Math.abs(group.getId().hashCode()) % AVATAR_COLORS.length]);
            binding.tvGroupInitial.setBackground(circle);

            binding.tvGroupName.setText(group.getName());
            binding.tvMemberCount.setText(ctx.getString(R.string.label_member_count, group.getMemberCount()));

            double balance = group.getMyBalance();
            if (balance > 0) {
                binding.tvBalance.setText(ctx.getString(R.string.label_balance_positive, balance));
                binding.tvBalance.setTextColor(ContextCompat.getColor(ctx, R.color.color_positive));
            } else if (balance < 0) {
                binding.tvBalance.setText(ctx.getString(R.string.label_balance_negative, Math.abs(balance)));
                binding.tvBalance.setTextColor(ContextCompat.getColor(ctx, R.color.color_negative));
            } else {
                binding.tvBalance.setText(R.string.label_my_settlement);
                binding.tvBalance.setTextColor(ContextCompat.getColor(ctx, R.color.color_settled));
            }

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(ctx, GroupDetailActivity.class);
                intent.putExtra("group_id", group.getId());
                intent.putExtra("group_name", group.getName());
                intent.putExtra("group_balance", group.getMyBalance());
                intent.putExtra("group_member_count", group.getMemberCount());
                intent.putExtra("group_invite_code", group.getInviteCode());
                intent.putExtra("group_is_admin", group.isAdmin());
                intent.putExtra("group_creator_device_id", group.getCreatorDeviceId());
                ctx.startActivity(intent);
            });

            itemView.setOnLongClickListener(v -> {
                if (!group.isAdmin()) {
                    com.google.android.material.snackbar.Snackbar.make(
                            itemView, R.string.error_admin_only, com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
                    return true;
                }
                if (deleteListener != null) deleteListener.onDelete(group);
                return true;
            });
        }
    }
}
