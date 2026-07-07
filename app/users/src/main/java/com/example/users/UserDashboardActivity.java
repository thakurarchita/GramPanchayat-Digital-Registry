package com.example.users;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UserDashboardActivity extends BaseUserActivity {

    private TextView tvWelcome, tvHouseholdInfo, tvPendingCount, tvLogout;
    private CardView cardViewHousehold, cardViewMembers, cardEligibleSchemes, cardSubmitRequest, cardRequestStatus;

    private UserSessionManager sessionManager;
    private OkHttpClient client;
    private static final String BASE_URL = "http://192.168.147.188/grampanchayat_api/";
    private String householdId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        sessionManager = new UserSessionManager(this);
        client = new OkHttpClient();

        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(UserDashboardActivity.this, UserLoginActivity.class));
            finish();
            return;
        }

        initViews();
        setupClickListeners();
        loadUserData();
        loadPendingRequestsCount();  // Fixed method name
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        tvHouseholdInfo = findViewById(R.id.tvHouseholdInfo);
        tvPendingCount = findViewById(R.id.tvPendingCount);
        tvLogout = findViewById(R.id.tvLogout);

        cardViewHousehold = findViewById(R.id.cardViewHousehold);
        cardViewMembers = findViewById(R.id.cardViewMembers);
        cardEligibleSchemes = findViewById(R.id.cardEligibleSchemes);
        cardSubmitRequest = findViewById(R.id.cardSubmitRequest);
        cardRequestStatus = findViewById(R.id.cardRequestStatus);
    }

    private void setupClickListeners() {
        cardViewHousehold.setOnClickListener(v -> {
            startActivity(new Intent(UserDashboardActivity.this, ViewHouseholdActivity.class));
        });

        cardViewMembers.setOnClickListener(v -> {
            startActivity(new Intent(UserDashboardActivity.this, ViewFamilyMembersActivity.class));
        });

        cardEligibleSchemes.setOnClickListener(v -> {
            Intent intent = new Intent(UserDashboardActivity.this, UserEligibleSchemesActivity.class);
            startActivity(intent);
        });

        cardSubmitRequest.setOnClickListener(v -> {
            Intent intent = new Intent(UserDashboardActivity.this, SubmitRequestActivity.class);
            startActivity(intent);
        });

        cardRequestStatus.setOnClickListener(v -> {
            Intent intent = new Intent(UserDashboardActivity.this, RequestStatusActivity.class);
            startActivity(intent);
        });

        tvLogout.setOnClickListener(v -> logout());
    }

    private void loadUserData() {
        String fullName = sessionManager.getFullName();
        householdId = sessionManager.getHouseholdId();

        // Log the household ID
        android.util.Log.e("DASHBOARD", "Household ID: " + householdId);

        tvWelcome.setText("Welcome, " + fullName + "!");

        if (householdId != null && !householdId.isEmpty()) {
            tvHouseholdInfo.setText("Household ID: " + householdId);
            fetchHouseholdDetails();
            loadPendingRequestsCount(); // Move this here instead of onCreate
        } else {
            tvHouseholdInfo.setText("No household assigned");
        }
    }
    private void loadPendingRequestsCount() {
        if (householdId == null || householdId.isEmpty()) {
            tvPendingCount.setText("0 Pending");
            return;
        }

        String url = BASE_URL + "get_pending_requests_count.php?household_id=" + householdId;

        // Log the URL
        android.util.Log.e("PENDING_COUNT", "URL: " + url);

        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    tvPendingCount.setText("0 Pending");
                    android.util.Log.e("PENDING_COUNT", "Error: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                // Log the response
                android.util.Log.e("PENDING_COUNT", "Response: " + responseBody);

                runOnUiThread(() -> {
                    try {
                        JSONObject json = new JSONObject(responseBody);
                        if (json.getBoolean("success")) {
                            int pendingCount = json.getInt("pending_count");
                            android.util.Log.e("PENDING_COUNT", "Pending count: " + pendingCount);
                            if (pendingCount > 0) {
                                tvPendingCount.setText(pendingCount + " Pending");
                            } else {
                                tvPendingCount.setText("0 Pending");
                            }
                        } else {
                            tvPendingCount.setText("0 Pending");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        tvPendingCount.setText("0 Pending");
                    }
                });
            }
        });
    }
    private void fetchHouseholdDetails() {
        String url = BASE_URL + "get_household_details.php?household_id=" + householdId;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(UserDashboardActivity.this,
                            "Failed to load household details", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                runOnUiThread(() -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        if (jsonResponse.getBoolean("success")) {
                            JSONObject data = jsonResponse.getJSONObject("data");
                            String headName = data.getString("head_name");
                            String address = data.getString("address");

                            tvHouseholdInfo.setText(headName + " | " + address);
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
        Intent intent = new Intent(UserDashboardActivity.this, UserLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}