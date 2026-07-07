package com.example.users;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.example.users.R;
import com.example.users.UserSessionManager;
public class UserSplashActivity extends BaseUserActivity {

        private UserSessionManager sessionManager;
        private static final int SPLASH_DELAY = 2000;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_user_splash);

            sessionManager = new UserSessionManager(this);

            new Handler().postDelayed(() -> {
                if (sessionManager.isLoggedIn()) {
                    startActivity(new Intent(UserSplashActivity.this, UserDashboardActivity.class));
                } else {
                    startActivity(new Intent(UserSplashActivity.this, UserLoginActivity.class));
                }
                finish();
            }, SPLASH_DELAY);
        }
    }