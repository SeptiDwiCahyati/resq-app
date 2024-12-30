package com.septi.rescuu.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.septi.rescuu.R;
import com.septi.rescuu.model.RescueTeam;

import java.util.List;

public class ActiveTeamsAdapter extends RecyclerView.Adapter<ActiveTeamsAdapter.RescueTeamViewHolder> {
    private List<RescueTeam> rescueTeams;
    private OnItemClickListener listener;

    public ActiveTeamsAdapter(List<RescueTeam> rescueTeams) {
        this.rescueTeams = rescueTeams;
    }

    @NonNull
    @Override
    public RescueTeamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rescue_team, parent, false);
        return new RescueTeamViewHolder(view, listener, rescueTeams);
    }


    @Override
    public void onBindViewHolder(@NonNull RescueTeamViewHolder holder, int position) {
        RescueTeam team = rescueTeams.get(position);
        holder.bind(team);
    }

    @Override
    public int getItemCount() {
        return rescueTeams.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(RescueTeam team, int position);
    }

    static class RescueTeamViewHolder extends RecyclerView.ViewHolder {
        TextView nameView;
        TextView distanceView;
        TextView statusView;
        List<RescueTeam> rescueTeams;  // Hold reference to the list

        RescueTeamViewHolder(View itemView, final OnItemClickListener listener, List<RescueTeam> rescueTeams) {
            super(itemView);
            this.rescueTeams = rescueTeams;  // Initialize the list
            nameView = itemView.findViewById(R.id.tv_team_name);
            distanceView = itemView.findViewById(R.id.tv_distance);
            statusView = itemView.findViewById(R.id.tv_status);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(rescueTeams.get(position), position);
                }
            });
        }

        void bind(RescueTeam team) {
            nameView.setText(team.getName());
            distanceView.setText(team.getDistance());
            statusView.setText(team.getStatus());

            int statusColor = team.getStatus().equals("Tersedia") ?
                    R.color.status_available : R.color.status_busy;
            statusView.setTextColor(itemView.getContext().getColor(statusColor));
        }
    }

}