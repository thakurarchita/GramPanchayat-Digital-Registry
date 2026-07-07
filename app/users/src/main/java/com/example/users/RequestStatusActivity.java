package com.example.users;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import org.json.JSONArray;
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

public class RequestStatusActivity extends BaseUserActivity {

    private LinearLayout layoutRequestsContainer;
    private TextView tvNoRequests;
    private ProgressBar progressBar;
    private ImageButton btnBack;
    private Toolbar toolbar;

    private OkHttpClient client;
    private UserSessionManager sessionManager;
    private static final String BASE_URL = "http://192.168.147.188/grampanchayat_api/";
    private String householdId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_status);

        client = new OkHttpClient();
        sessionManager = new UserSessionManager(this);
        householdId = sessionManager.getHouseholdId();

        initViews();
        setupToolbar();
        fetchRequests();
    }

    private void initViews() {
        layoutRequestsContainer = findViewById(R.id.layoutRequestsContainer);
        tvNoRequests = findViewById(R.id.tvNoRequests);
        progressBar = findViewById(R.id.progressBar);
        btnBack = findViewById(R.id.btnBack);
        toolbar = findViewById(R.id.toolbar);
    }

    private void setupToolbar() {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void fetchRequests() {
        showProgress(true);

        // Debug: Print the URL being called
        String url = BASE_URL + "get_user_requests.php?household_id=" + householdId;
        android.util.Log.e("REQUEST_STATUS", "Calling URL: " + url);

        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    showProgress(false);
                    android.util.Log.e("REQUEST_STATUS", "Network error: " + e.getMessage());
                    Toast.makeText(RequestStatusActivity.this,
                            "Network error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    tvNoRequests.setVisibility(View.VISIBLE);
                    tvNoRequests.setText("Network error: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                android.util.Log.e("REQUEST_STATUS", "Response: " + responseBody);

                runOnUiThread(() -> {
                    showProgress(false);
                    try {
                        if (responseBody == null || responseBody.isEmpty()) {
                            tvNoRequests.setVisibility(View.VISIBLE);
                            tvNoRequests.setText("Empty response from server");
                            return;
                        }

                        JSONObject jsonResponse = new JSONObject(responseBody);
                        if (jsonResponse.getBoolean("success")) {
                            JSONArray requests = jsonResponse.getJSONArray("requests");
                            int total = jsonResponse.optInt("total", 0);

                            if (requests.length() == 0) {
                                tvNoRequests.setVisibility(View.VISIBLE);
                                tvNoRequests.setText("No requests found.\nTotal: 0");
                            } else {
                                tvNoRequests.setVisibility(View.GONE);
                                displayRequests(requests);
                            }
                        } else {
                            tvNoRequests.setVisibility(View.VISIBLE);
                            String errorMsg = jsonResponse.getString("message");
                            tvNoRequests.setText("Error: " + errorMsg);
                            Toast.makeText(RequestStatusActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        tvNoRequests.setVisibility(View.VISIBLE);
                        tvNoRequests.setText("Error: " + e.getMessage() + "\nResponse: " + responseBody);
                        android.util.Log.e("REQUEST_STATUS", "Parse error: " + e.getMessage());
                        android.util.Log.e("REQUEST_STATUS", "Response was: " + responseBody);
                    }
                });
            }
        });
    }

    private void displayRequests(JSONArray requests) throws Exception {
        layoutRequestsContainer.removeAllViews();

        for (int i = 0; i < requests.length(); i++) {
            JSONObject request = requests.getJSONObject(i);

            String requestId = request.getString("request_id");
            String requestType = request.getString("request_type");
            String title = request.getString("title");
            String status = request.getString("status");
            String submittedDate = request.getString("submitted_date");
            String remarks = request.optString("admin_remarks", "");

            // Create card
            CardView requestCard = new CardView(this);
            CardView.LayoutParams cardParams = new CardView.LayoutParams(
                    CardView.LayoutParams.MATCH_PARENT,
                    CardView.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(0, 0, 0, 16);
            requestCard.setLayoutParams(cardParams);
            requestCard.setRadius(12);
            requestCard.setCardElevation(4);
            requestCard.setCardBackgroundColor(Color.WHITE);

            LinearLayout cardContent = new LinearLayout(this);
            cardContent.setOrientation(LinearLayout.VERTICAL);
            cardContent.setPadding(20, 16, 20, 16);

            // Header Row
            LinearLayout headerRow = new LinearLayout(this);
            headerRow.setOrientation(LinearLayout.HORIZONTAL);
            headerRow.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));

            TextView tvRequestType = new TextView(this);
            tvRequestType.setText(title);
            tvRequestType.setTextSize(14);
            tvRequestType.setTypeface(tvRequestType.getTypeface(), android.graphics.Typeface.BOLD);
            tvRequestType.setTextColor(0xFF2196F3);
            tvRequestType.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

            TextView tvStatus = new TextView(this);
            tvStatus.setText(status);
            tvStatus.setTextSize(12);
            tvStatus.setPadding(12, 4, 12, 4);
            tvStatus.setTextColor(Color.WHITE);

            android.graphics.drawable.GradientDrawable statusBg = new android.graphics.drawable.GradientDrawable();
            statusBg.setCornerRadius(20);
            if (status.equals("Pending")) {
                statusBg.setColor(0xFFFF9800);
            } else if (status.equals("Approved")) {
                statusBg.setColor(0xFF4CAF50);
            } else if (status.equals("Rejected")) {
                statusBg.setColor(0xFFF44336);
            } else {
                statusBg.setColor(0xFF9E9E9E);
            }
            tvStatus.setBackground(statusBg);

            headerRow.addView(tvRequestType);
            headerRow.addView(tvStatus);

            // Request ID
            TextView tvRequestIdView = new TextView(this);
            tvRequestIdView.setText("ID: " + requestId);
            tvRequestIdView.setTextSize(11);
            tvRequestIdView.setTextColor(Color.GRAY);
            tvRequestIdView.setPadding(0, 8, 0, 4);

            // Date
            TextView tvDate = new TextView(this);
            tvDate.setText("📅 " + formatDate(submittedDate));
            tvDate.setTextSize(12);
            tvDate.setTextColor(Color.GRAY);
            tvDate.setPadding(0, 0, 0, 8);

            cardContent.addView(headerRow);
            cardContent.addView(tvRequestIdView);
            cardContent.addView(tvDate);

            // Remarks
            if (remarks != null && !remarks.isEmpty() && !remarks.equals("null") && !remarks.equals("")) {
                TextView tvRemarks = new TextView(this);
                tvRemarks.setText("📝 " + remarks);
                tvRemarks.setTextSize(12);
                tvRemarks.setTextColor(Color.GRAY);
                tvRemarks.setPadding(0, 4, 0, 0);
                cardContent.addView(tvRemarks);
            }

            requestCard.addView(cardContent);
            layoutRequestsContainer.addView(requestCard);
        }
    }

    private String formatDate(String dateStr) {
        try {
            // Try different date formats
            SimpleDateFormat input1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat input2 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat output = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());

            Date date = null;
            try {
                date = input1.parse(dateStr);
            } catch (Exception e) {
                date = input2.parse(dateStr);
            }
            return output.format(date);
        } catch (Exception e) {
            return dateStr;
        }
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}