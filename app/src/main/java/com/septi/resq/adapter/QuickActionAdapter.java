package com.septi.resq.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.septi.resq.R;
import com.septi.resq.model.QuickAction;

import java.util.List;

public class QuickActionAdapter extends RecyclerView.Adapter<QuickActionAdapter.QuickActionViewHolder> {
    private final List<QuickAction> quickActions;
    private OnItemClickListener listener;

    public QuickActionAdapter(List<QuickAction> quickActions) {

        this.quickActions = quickActions;
    }

    @NonNull
    @Override
    public QuickActionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_quick_action, parent, false);
        return new QuickActionViewHolder(view, listener, quickActions);
    }


    @Override
    public void onBindViewHolder(@NonNull QuickActionViewHolder holder, int position) {
        QuickAction quickAction = quickActions.get(position);
        holder.bind(quickAction);
    }

    @Override
    public int getItemCount() {
        return quickActions.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(QuickAction quickAction, int position);
    }

    static class QuickActionViewHolder extends RecyclerView.ViewHolder {
        ImageView iconView;
        TextView titleView;
        List<QuickAction> quickActions;

        QuickActionViewHolder(View itemView, final OnItemClickListener listener, List<QuickAction> quickActions) {
            super(itemView);
            this.quickActions = quickActions;
            iconView = itemView.findViewById(R.id.iv_icon);
            titleView = itemView.findViewById(R.id.tv_title);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(quickActions.get(position), position);
                }
            });
        }

        void bind(QuickAction quickAction) {
            iconView.setImageResource(quickAction.getIconResource());
            titleView.setText(quickAction.getTitle());
        }
    }

}