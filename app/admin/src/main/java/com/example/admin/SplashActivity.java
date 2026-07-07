package com.example.admin;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SplashActivity extends AppCompatActivity {
        private AdminSessionManager sessionManager;
        private static final int SPLASH_DELAY = 2000;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_splash);

            sessionManager = new AdminSessionManager(this);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (sessionManager.isLoggedIn()) {
                        startActivity(new Intent(SplashActivity.this, AdminDashboardActivity.class));
                    } else {
                        startActivity(new Intent(SplashActivity.this, AdminLoginActivity.class));
                    }
                    finish();
                }
            }, SPLASH_DELAY);
        }
    }