package com.example.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AdminLoginActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    private AdminSessionManager sessionManager;
    private OkHttpClient client;

    private static final String API_URL = "http://192.168.147.188/grampanchayat_api/admin_login.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        sessionManager = new AdminSessionManager(this);
        client = new OkHttpClient();

        // Check if already logged in
        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(AdminLoginActivity.this, AdminDashboardActivity.class));
            finish();
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> performLogin());
    }

    private void performLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Debug logs
        android.util.Log.d("AdminLogin", "=== LOGIN ATTEMPT ===");
        android.util.Log.d("AdminLogin", "API_URL: " + API_URL);
        android.util.Log.d("AdminLogin", "Username: " + username);
        android.util.Log.d("AdminLogin", "Password length: " + password.length());

        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Username required");
            etUsername.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password required");
            etPassword.requestFocus();
            return;
        }

        showProgress(true);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("username", username);
            jsonObject.put("password", password);
            android.util.Log.d("AdminLogin", "JSON Request: " + jsonObject.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonObject.toString(), JSON);

        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                android.util.Log.e("AdminLogin", "Network Error: " + e.getMessage());
                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(AdminLoginActivity.this,
                            "Network error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                android.util.Log.d("AdminLogin", "Response Code: " + response.code());
                android.util.Log.d("AdminLogin", "Response Body: " + responseBody);

                runOnUiThread(() -> {
                    showProgress(false);
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        boolean success = jsonResponse.getBoolean("success");

                        if (success) {
                            String fullName = jsonResponse.getString("full_name");
                            sessionManager.createLoginSession(username, fullName);
                            Toast.makeText(AdminLoginActivity.this,
                                    "Welcome " + fullName, Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(AdminLoginActivity.this,
                                    AdminDashboardActivity.class));
                            finish();
                        } else {
                            String message = jsonResponse.getString("message");
                            Toast.makeText(AdminLoginActivity.this,
                                    message, Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(AdminLoginActivity.this,
                                "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!show);
        btnLogin.setText(show ? "LOGGING IN..." : "LOGIN");
    }
}