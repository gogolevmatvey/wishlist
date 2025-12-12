package com.example.wishlist.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000; // 2 секунды

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Для Android до 12 скрываем статус бар
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
            );
        }

        // Для Android 12+ используем встроенный Splash Screen API
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Используем встроенный Splash Screen
            getSplashScreen().setOnExitAnimationListener(splashScreenView -> {
                // Анимация завершения сплеш-скрина
                splashScreenView.remove();
            });
        }

        new Handler().postDelayed(() -> {
            // Переходим на главный экран
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, SPLASH_DELAY);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Убираем анимацию при паузе
        overridePendingTransition(0, 0);
    }
}