package com.septi.resq.utils;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.septi.resq.R;

public class EmergencyStatusHelper {
    public static void updateProgressStatus(View view, String status) {
        ImageView dotWaiting = view.findViewById(R.id.status_dot_waiting);
        ImageView dotOtw = view.findViewById(R.id.status_dot_otw);
        ImageView dotArrived = view.findViewById(R.id.status_dot_arrived);
        ImageView dotReturning = view.findViewById(R.id.status_dot_returning);

        View lineWaitingToOtw = view.findViewById(R.id.line_waiting_to_otw);
        View lineOtwToArrived = view.findViewById(R.id.line_otw_to_arrived);
        View lineArrivedToReturning = view.findViewById(R.id.line_arrived_to_returning);

        TextView textWaiting = view.findViewById(R.id.status_text_waiting);
        TextView textOtw = view.findViewById(R.id.status_text_otw);
        TextView textArrived = view.findViewById(R.id.status_text_arrived);
        TextView textReturning = view.findViewById(R.id.status_text_returning);

        // Reset all to inactive
        dotWaiting.setImageResource(R.drawable.circle_progress_inactive);
        dotOtw.setImageResource(R.drawable.circle_progress_inactive);
        dotArrived.setImageResource(R.drawable.circle_progress_inactive);
        dotReturning.setImageResource(R.drawable.circle_progress_inactive);

        lineWaitingToOtw.setBackgroundColor(view.getContext().getColor(R.color.progress_line_inactive));
        lineOtwToArrived.setBackgroundColor(view.getContext().getColor(R.color.progress_line_inactive));
        lineArrivedToReturning.setBackgroundColor(view.getContext().getColor(R.color.progress_line_inactive));

        textWaiting.setTextColor(view.getContext().getColor(R.color.status_inactive));
        textOtw.setTextColor(view.getContext().getColor(R.color.status_inactive));
        textArrived.setTextColor(view.getContext().getColor(R.color.status_inactive));
        textReturning.setTextColor(view.getContext().getColor(R.color.status_inactive));

        switch (status) {
            case "MENUNGGU":
                dotWaiting.setImageResource(R.drawable.circle_progress_active);
                textWaiting.setTextColor(view.getContext().getColor(R.color.status_active));
                break;

            case "IN_PROGRESS":
                dotWaiting.setImageResource(R.drawable.circle_progress_completed);
                dotOtw.setImageResource(R.drawable.circle_progress_active);
                lineWaitingToOtw.setBackgroundColor(view.getContext().getColor(R.color.progress_line_active));
                textOtw.setTextColor(view.getContext().getColor(R.color.status_active));
                break;

            case "ARRIVED":
                dotWaiting.setImageResource(R.drawable.circle_progress_completed);
                dotOtw.setImageResource(R.drawable.circle_progress_completed);
                dotArrived.setImageResource(R.drawable.circle_progress_active);
                lineWaitingToOtw.setBackgroundColor(view.getContext().getColor(R.color.progress_line_active));
                lineOtwToArrived.setBackgroundColor(view.getContext().getColor(R.color.progress_line_active));
                textArrived.setTextColor(view.getContext().getColor(R.color.status_active));
                break;

            case "RETURNING":
                dotWaiting.setImageResource(R.drawable.circle_progress_completed);
                dotOtw.setImageResource(R.drawable.circle_progress_completed);
                dotArrived.setImageResource(R.drawable.circle_progress_completed);
                dotReturning.setImageResource(R.drawable.circle_progress_active);
                lineWaitingToOtw.setBackgroundColor(view.getContext().getColor(R.color.progress_line_active));
                lineOtwToArrived.setBackgroundColor(view.getContext().getColor(R.color.progress_line_active));
                lineArrivedToReturning.setBackgroundColor(view.getContext().getColor(R.color.progress_line_active));
                textReturning.setTextColor(view.getContext().getColor(R.color.status_active));
                break;
        }
    }
}