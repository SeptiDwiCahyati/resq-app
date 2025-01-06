package com.septi.resq;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

public class FeatureDetailBottomSheet extends BottomSheetDialogFragment {
    private final String feature;

    public FeatureDetailBottomSheet(String feature) {
        this.feature = feature;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_feature_detail, container, false);

        TextView titleText = view.findViewById(R.id.featureTitle);
        TextView descriptionText = view.findViewById(R.id.featureDescription);
        ImageView featureIcon = view.findViewById(R.id.featureIcon);
        MaterialButton closeButton = view.findViewById(R.id.closeButton);

        if (feature.equals("emergency")) {
            titleText.setText("Panggilan Darurat");
            descriptionText.setText(
                    "• Terintegrasi langsung dengan layanan darurat 112/911\n" +
                            "• Pengiriman lokasi otomatis ke pusat bantuan\n" +
                            "• Panggilan darurat dengan satu sentuhan\n" +
                            "• Riwayat panggilan darurat\n" +
                            "• Kontak darurat yang bisa dikustomisasi"
            );
            featureIcon.setImageResource(R.drawable.ic_emergency);
        } else if (feature.equals("tracking")) {
            titleText.setText("Pelacakan Tim");
            descriptionText.setText(
                    "• Pantau lokasi tim penyelamat secara real-time\n" +
                            "• Estimasi waktu kedatangan\n" +
                            "• Status dan progress tim rescue\n" +
                            "• Rute optimal untuk tim penyelamat\n" +
                            "• Notifikasi status terkini"
            );
            featureIcon.setImageResource(R.drawable.ic_tracking);
        }

        closeButton.setOnClickListener(v -> dismiss());

        return view;
    }
}