package com.septi.resq.adapter;

import static com.septi.resq.model.Emergency.EmergencyStatus.MENUNGGU;
import static com.septi.resq.model.Emergency.EmergencyStatus.PROSES;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.septi.resq.R;
import com.septi.resq.model.Emergency;
import com.septi.resq.model.Report;

import java.util.ArrayList;
import java.util.List;

public class RecentReportsAdapter extends RecyclerView.Adapter<RecentReportsAdapter.ReportViewHolder> {
    private List<Report> reports;
    private OnItemClickListener listener;

    public RecentReportsAdapter(List<Report> reports) {
        this.reports = reports != null ? reports : new ArrayList<>();
    }

    public void updateReports(List<Report> newReports) {
        this.reports = newReports != null ? newReports : new ArrayList<>();
        notifyDataSetChanged();
    }

    public List<Report> getReports() {
        return new ArrayList<>(reports);
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(view, listener, reports);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        Report report = reports.get(position);
        holder.bind(report);
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(Report report, int position);
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleView;
        private final TextView locationView;
        private final TextView timestampView;
        private final TextView statusView;

        ReportViewHolder(View itemView, final OnItemClickListener listener, List<Report> reports) {
            super(itemView);
            titleView = itemView.findViewById(R.id.tv_report_title);
            locationView = itemView.findViewById(R.id.tv_location);
            timestampView = itemView.findViewById(R.id.tv_timestamp);
            statusView = itemView.findViewById(R.id.tv_status);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(reports.get(position), position);
                }
            });
        }

        void bind(Report report) {
            if (report != null) {
                if (titleView != null) titleView.setText(report.getTitle());
                if (locationView != null) locationView.setText(report.getLocation());
                if (timestampView != null) timestampView.setText(report.getTimestamp());
                if (statusView != null) {
                    statusView.setText(getStatusText(report.getStatus()));
                    statusView.setTextColor(getStatusColor(report.getStatus()));
                }
            }
        }

        private String getStatusText(Emergency.EmergencyStatus status) {
            if (status == null) return "";

            switch (status) {
                case MENUNGGU:
                    return "Menunggu";
                case PROSES:
                    return "Dalam Proses";
                case SELESAI:
                    return "Selesai";
                default:
                    return "";
            }
        }

        private int getStatusColor(Emergency.EmergencyStatus status) {
            Context context = itemView.getContext();
            if (status == null) return ContextCompat.getColor(context, R.color.text_secondary);

            switch (status) {
                case MENUNGGU:
                    return ContextCompat.getColor(context, R.color.status_pending);
                case PROSES:
                    return ContextCompat.getColor(context, R.color.status_in_progress);
                case SELESAI:
                    return ContextCompat.getColor(context, R.color.status_resolved);
                default:
                    return ContextCompat.getColor(context, R.color.text_secondary);
            }
        }
    }
}