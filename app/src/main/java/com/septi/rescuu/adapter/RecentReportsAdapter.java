package com.septi.rescuu.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.septi.rescuu.R;
import com.septi.rescuu.model.Report;

import java.util.List;

public class RecentReportsAdapter extends RecyclerView.Adapter<RecentReportsAdapter.ReportViewHolder> {
    private List<Report> reports;
    private OnItemClickListener listener;

    public RecentReportsAdapter(List<Report> reports) {
        this.reports = reports;
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
        TextView titleView;
        TextView locationView;
        TextView timestampView;
        List<Report> reports;  // Add a reference to the reports list

        ReportViewHolder(View itemView, final OnItemClickListener listener, List<Report> reports) {
            super(itemView);
            this.reports = reports;  // Initialize the reports list
            titleView = itemView.findViewById(R.id.tv_report_title);
            locationView = itemView.findViewById(R.id.tv_location);
            timestampView = itemView.findViewById(R.id.tv_timestamp);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(reports.get(position), position);
                }
            });
        }


    void bind(Report report) {
            titleView.setText(report.getTitle());
            locationView.setText(report.getLocation());
            timestampView.setText(report.getTimestamp());
        }
    }
}