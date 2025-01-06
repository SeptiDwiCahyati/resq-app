package com.septi.resq;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.septi.resq.utils.AppUtils;

public class MainActivity extends AppCompatActivity {
    private LottieAnimationView lottieAnimation;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable navigateRunnable = () -> {
        if (AppUtils.isFirstTimeLaunch(this)) {
            startActivity(new Intent(MainActivity.this, LandingActivity.class));
        } else {
            startActivity(new Intent(MainActivity.this, OverviewActivity.class));
        }
        finish();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setStatusBarColor(R.color.colorPrimaryDark);

        lottieAnimation = findViewById(R.id.loading_animation);
        showLoading();

        handler.postDelayed(navigateRunnable, 3000);
    }

    private void setStatusBarColor(int colorResId) {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(colorResId));
    }

    private void showLoading() {
        lottieAnimation.setVisibility(View.VISIBLE);
        lottieAnimation.playAnimation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(navigateRunnable);
    }
}
