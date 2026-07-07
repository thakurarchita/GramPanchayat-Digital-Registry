package com.example.users;

import android.os.Bundle;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public abstract class BaseUserActivity extends AppCompatActivity {

    private FloatingChatFragment chatFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Add floating chat fragment after content is set
        getWindow().getDecorView().post(() -> {
            if (!isFinishing()) {
                addChatFragment();
            }
        });

        // Modern back button handling using OnBackPressedDispatcher
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Check if chat is open and close it first
                Fragment fragment = getSupportFragmentManager().findFragmentByTag("FloatingChat");
                if (fragment instanceof FloatingChatFragment) {
                    FloatingChatFragment chatFragment = (FloatingChatFragment) fragment;
                    if (chatFragment.isChatVisible()) {
                        chatFragment.closeChat();
                        return; // Don't close activity, just close chat
                    }
                }
                // If chat is not visible, finish activity
                finish();
            }
        });
    }

    private void addChatFragment() {
        if (getSupportFragmentManager().findFragmentByTag("FloatingChat") == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            chatFragment = new FloatingChatFragment();
            transaction.add(android.R.id.content, chatFragment, "FloatingChat");
            transaction.commit();
        }
    }
}