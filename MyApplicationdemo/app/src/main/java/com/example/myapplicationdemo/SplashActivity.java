package com.example.myapplicationdemo;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final long FADE_DURATION = 600L;
    private static final long STAY_DURATION = 1000L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        View center = findViewById(R.id.ll_center);
        center.animate()
                .alpha(1f)
                .translationYBy(-24f)
                .setDuration(FADE_DURATION)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() ->
                        center.postDelayed(this::goToMain, STAY_DURATION))
                .start();
    }

    @SuppressWarnings("deprecation")
    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN,
                    android.R.anim.fade_in, android.R.anim.fade_out);
        } else {
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
        finish();
    }
}
