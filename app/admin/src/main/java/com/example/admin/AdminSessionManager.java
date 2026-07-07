package com.example.admin;

import android.content.Context;
import android.content.SharedPreferences;

public class AdminSessionManager {

    private static final String PREF_NAME = "AdminSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_FULL_NAME = "fullName";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    public AdminSessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void createLoginSession(String username, String fullName) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_FULL_NAME, fullName);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getUsername() {
        return pref.getString(KEY_USERNAME, "");
    }

    public String getFullName() {
        return pref.getString(KEY_FULL_NAME, "");
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}