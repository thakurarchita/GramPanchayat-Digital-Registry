package com.example.users;


import android.content.Context;
import android.content.SharedPreferences;

public class UserSessionManager {

    private static final String PREF_NAME = "UserSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_MOBILE = "mobile";
    private static final String KEY_FULL_NAME = "fullName";
    private static final String KEY_HOUSEHOLD_ID = "householdId";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    public UserSessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void createLoginSession(String mobile, String fullName, String householdId) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_MOBILE, mobile);
        editor.putString(KEY_FULL_NAME, fullName);
        editor.putString(KEY_HOUSEHOLD_ID, householdId);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getMobile() {
        return pref.getString(KEY_MOBILE, "");
    }

    public String getFullName() {
        return pref.getString(KEY_FULL_NAME, "");
    }

    public String getHouseholdId() {
        return pref.getString(KEY_HOUSEHOLD_ID, "");
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}