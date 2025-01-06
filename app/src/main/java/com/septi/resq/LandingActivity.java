package com.septi.resq;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.septi.resq.utils.AppUtils;

public class LandingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

        if (!AppUtils.isFirstTimeLaunch(this)) {
            startActivity(new Intent(LandingActivity.this, OverviewActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.landing_screen);

        View emergencyCard = findViewById(R.id.emergencyCard);
        View trackingCard = findViewById(R.id.trackingCard);
        MaterialButton getStartedButton = findViewById(R.id.getStartedButton);

        emergencyCard.setOnClickListener(v -> showFeatureDetail("emergency"));
        trackingCard.setOnClickListener(v -> showFeatureDetail("tracking"));

        getStartedButton.setOnClickListener(v -> {
            AppUtils.setFirstTimeLaunch(this, false);
            startActivity(new Intent(LandingActivity.this, OverviewActivity.class));
            finish();
        });
    }

    private void showFeatureDetail(String feature) {
        FeatureDetailBottomSheet bottomSheet = new FeatureDetailBottomSheet(feature);
        bottomSheet.show(getSupportFragmentManager(), "FeatureDetail");
    }
}