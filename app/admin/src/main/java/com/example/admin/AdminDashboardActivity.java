package com.example.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView tvWelcome, tvDate, tvTotalHouseholds, tvTotalSchemes, tvPendingRequests, tvPendingBadge, tvLogout;
    private CardView cardRegisterHousehold, cardViewRequests, cardManageSchemes, cardViewEligible, cardReports;
    private AdminSessionManager sessionManager;
    private OkHttpClient client;
    private static final String API_URL = "http://192.168.147.188/grampanchayat_api/admin_dashboard_stats.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        sessionManager = new AdminSessionManager(this);
        client = new OkHttpClient();

        // Check login status
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, AdminLoginActivity.class));
            finish();
            return;
        }

        initViews();
        setupClickListeners();
        setWelcomeMessage();
        setCurrentDate();
        fetchDashboardStats();
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        tvDate = findViewById(R.id.tvDate);
        tvTotalHouseholds = findViewById(R.id.tvTotalHouseholds);
        tvTotalSchemes = findViewById(R.id.tvTotalSchemes);
        tvPendingRequests = findViewById(R.id.tvPendingRequests);
        tvPendingBadge = findViewById(R.id.tvPendingBadge);
        tvLogout = findViewById(R.id.tvLogout);

        cardRegisterHousehold = findViewById(R.id.cardRegisterHousehold);
        cardViewRequests = findViewById(R.id.cardViewRequests);
        cardManageSchemes = findViewById(R.id.cardManageSchemes);
        cardViewEligible = findViewById(R.id.cardViewEligible);
        cardReports = findViewById(R.id.cardReports);
    }

    private void setupClickListeners() {
        // Register Household
        cardRegisterHousehold.setOnClickListener(v -> {
            startActivity(new Intent(this, Admin_HouseholdRegistration.class));
        });

        // View Requests
        cardViewRequests.setOnClickListener(v -> {
            startActivity(new Intent(this, ViewRequestActivity.class));
        });

        // Manage Schemes
        cardManageSchemes.setOnClickListener(v -> {
            startActivity(new Intent(this, ViewSchemeActivity.class));
        });

        // View Eligible
        cardViewEligible.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminViewEligibleActivity.class));
        });

        // Reports
        cardReports.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminReportsActivity.class));
        });

        // Logout
        tvLogout.setOnClickListener(v -> logout());
    }

    private void setWelcomeMessage() {
        String fullName = sessionManager.getFullName();
        tvWelcome.setText("Welcome back, " + fullName + "!");
    }

    private void setCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault());
        tvDate.setText(sdf.format(new Date()));
    }

    private void fetchDashboardStats() {
        Request request = new Request.Builder().url(API_URL).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    tvTotalHouseholds.setText("0");
                    tvTotalSchemes.setText("0");
                    tvPendingRequests.setText("0");
                    Toast.makeText(AdminDashboardActivity.this,
                            "Network error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                runOnUiThread(() -> {
                    try {
                        JSONObject json = new JSONObject(responseBody);
                        if (json.getBoolean("success")) {
                            JSONObject data = json.getJSONObject("data");
                            int households = data.getInt("total_households");
                            int schemes = data.getInt("total_schemes");
                            int pending = data.getInt("pending_requests");

                            tvTotalHouseholds.setText(String.valueOf(households));
                            tvTotalSchemes.setText(String.valueOf(schemes));
                            tvPendingRequests.setText(String.valueOf(pending));

                            if (pending > 0) {
                                tvPendingBadge.setVisibility(View.VISIBLE);
                                tvPendingBadge.setText(pending + " Pending");
                            } else {
                                tvPendingBadge.setVisibility(View.GONE);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    private void logout() {
        sessionManager.logout();
        startActivity(new Intent(this, AdminLoginActivity.class));
        finish();
    }
}