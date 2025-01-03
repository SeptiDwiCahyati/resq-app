package com.septi.resq;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.septi.resq.utils.AppUtils;

public class MainActivity extends AppCompatActivity {
    private LottieAnimationView loadingAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Mengatur warna status bar agar sesuai dengan tema
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));

        loadingAnimation = findViewById(R.id.loading_animation);

        showLoading();

        new Handler().postDelayed(() -> {
            if (AppUtils.isFirstTimeLaunch(this)) {
                startActivity(new Intent(MainActivity.this, LandingActivity.class));
            } else {
                startActivity(new Intent(MainActivity.this, OverviewActivity.class));
            }
            finish();
        }, AppUtils.LOADING_DELAY);
    }

    private void showLoading() {
        loadingAnimation.setVisibility(View.VISIBLE);
        loadingAnimation.playAnimation();
    }
}
