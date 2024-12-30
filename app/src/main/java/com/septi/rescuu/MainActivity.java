package com.septi.rescuu;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import com.airbnb.lottie.LottieAnimationView;

public class MainActivity extends AppCompatActivity {
    private LottieAnimationView loadingAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadingAnimation = findViewById(R.id.loading_animation);
        showLoading();

        new Handler().postDelayed(() -> {
            if (AppUtils.isFirstTimeLaunch(this)) {
                startActivity(new Intent(MainActivity.this, LandingActivity.class));
            } else {
                startActivity(new Intent(MainActivity.this, DashboardActivity.class));
            }
            finish();
        }, AppUtils.LOADING_DELAY);
    }

    private void showLoading() {
        loadingAnimation.setVisibility(View.VISIBLE);
        loadingAnimation.playAnimation();
    }
}
