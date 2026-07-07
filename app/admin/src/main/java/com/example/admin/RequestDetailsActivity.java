package com.example.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import org.json.JSONObject;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import okhttp3.*;
public class RequestDetailsActivity extends AppCompatActivity {


        private TextView tvRequestId, tvRequestType, tvRequestDate, tvStatus;
        private TextView tvHouseholdId, tvHeadName;
        private LinearLayout layoutDetails;
        private EditText etRemarks;
        private Button btnApprove, btnReject;
        private ImageButton btnBack;
        private ProgressBar progressBar;
        private Toolbar toolbar;

        private OkHttpClient client;
        private AdminSessionManager sessionManager;
        private static final String BASE_URL = "http://192.168.147.188/grampanchayat_api/";

        private String requestId, requestType, householdId, headName, requestDate, requestStatus, requestData;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_request_details);

            client = new OkHttpClient();
            sessionManager = new AdminSessionManager(this);

            // Get data from intent
            requestId = getIntent().getStringExtra("request_id");
            requestType = getIntent().getStringExtra("request_type");
            householdId = getIntent().getStringExtra("household_id");
            headName = getIntent().getStringExtra("head_name");
            requestDate = getIntent().getStringExtra("request_date");
            requestStatus = getIntent().getStringExtra("request_status");
            requestData = getIntent().getStringExtra("request_data");

            initViews();
            setupToolbar();
            setupClickListeners();
            displayRequestInfo();
            displayRequestDetails();
        }

        private void initViews() {
            tvRequestId = findViewById(R.id.tvRequestId);
            tvRequestType = findViewById(R.id.tvRequestType);
            tvRequestDate = findViewById(R.id.tvRequestDate);
            tvStatus = findViewById(R.id.tvStatus);
            tvHouseholdId = findViewById(R.id.tvHouseholdId);
            tvHeadName = findViewById(R.id.tvHeadName);
            layoutDetails = findViewById(R.id.layoutDetails);
            etRemarks = findViewById(R.id.etRemarks);
            btnApprove = findViewById(R.id.btnApprove);
            btnReject = findViewById(R.id.btnReject);
            btnBack = findViewById(R.id.btnBack);
            progressBar = findViewById(R.id.progressBar);
            toolbar = findViewById(R.id.toolbar);
        }

        private void setupToolbar() {
            setSupportActionBar(toolbar);
            btnBack.setOnClickListener(v -> finish());
        }

        private void setupClickListeners() {
            btnApprove.setOnClickListener(v -> updateRequestStatus("approved"));
            btnReject.setOnClickListener(v -> updateRequestStatus("rejected"));
        }

        private void displayRequestInfo() {
            tvRequestId.setText(requestId);

            String typeDisplay = "";
            switch (requestType) {
                case "birth": typeDisplay = "Birth Addition Request"; break;
                case "death": typeDisplay = "Death Request"; break;
                case "correction": typeDisplay = "Correction Request"; break;
            }
            tvRequestType.setText(typeDisplay);

            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
                Date date = inputFormat.parse(requestDate);
                tvRequestDate.setText(outputFormat.format(date));
            } catch (Exception e) {
                tvRequestDate.setText(requestDate);
            }

            tvHouseholdId.setText(householdId);
            tvHeadName.setText(headName);

            if (requestStatus.equals("pending")) {
                tvStatus.setText("Pending");
                tvStatus.setTextColor(getColor(android.R.color.holo_orange_light));
                btnApprove.setEnabled(true);
                btnReject.setEnabled(true);
            } else if (requestStatus.equals("approved")) {
                tvStatus.setText("Approved");
                tvStatus.setTextColor(getColor(android.R.color.holo_green_light));
                btnApprove.setEnabled(false);
                btnReject.setEnabled(false);
            } else {
                tvStatus.setText("Rejected");
                tvStatus.setTextColor(getColor(android.R.color.holo_red_light));
                btnApprove.setEnabled(false);
                btnReject.setEnabled(false);
            }
        }

        private void displayRequestDetails() {
            try {
                JSONObject data = new JSONObject(requestData);

                if (requestType.equals("birth")) {
                    addDetailRow("Child Name", data.optString("child_name", "N/A"));
                    addDetailRow("Gender", data.optString("gender", "N/A"));
                    addDetailRow("Date of Birth", data.optString("date_of_birth", "N/A"));
                    addDetailRow("Place of Birth", data.optString("place_of_birth", "N/A"));
                    addDetailRow("Father Name", data.optString("father_name", "N/A"));
                    addDetailRow("Mother Name", data.optString("mother_name", "N/A"));
                    addDetailRow("Birth Certificate No.", data.optString("birth_certificate_number", "N/A"));

                } else if (requestType.equals("death")) {
                    addDetailRow("Member Name", data.optString("member_name", "N/A"));
                    addDetailRow("Date of Death", data.optString("date_of_death", "N/A"));
                    addDetailRow("Place of Death", data.optString("place_of_death", "N/A"));
                    addDetailRow("Death Certificate No.", data.optString("death_certificate_number", "N/A"));
                    addDetailRow("Death Reason", data.optString("death_reason", "N/A"));

                } else if (requestType.equals("correction")) {
                    addDetailRow("Field to Correct", data.optString("field_name", "N/A"));
                    addDetailRow("Old Value", data.optString("old_value", "N/A"));
                    addDetailRow("New Value", data.optString("new_value", "N/A"));
                    addDetailRow("Reason", data.optString("reason", "N/A"));
                }

            } catch (Exception e) {
                e.printStackTrace();
                TextView errorView = new TextView(this);
                errorView.setText("Error loading request details");
                errorView.setTextColor(getColor(android.R.color.holo_red_light));
                layoutDetails.addView(errorView);
            }
        }

        private void addDetailRow(String label, String value) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, 0, 0, 16);

            TextView tvLabel = new TextView(this);
            tvLabel.setText(label + ":");
            tvLabel.setTextSize(14);
            tvLabel.setTextColor(getColor(android.R.color.darker_gray));
            tvLabel.setLayoutParams(new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 0.4f));

            TextView tvValue = new TextView(this);
            tvValue.setText(value);
            tvValue.setTextSize(14);
            tvValue.setTextColor(getColor(android.R.color.black));
            tvValue.setLayoutParams(new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 0.6f));

            row.addView(tvLabel);
            row.addView(tvValue);
            layoutDetails.addView(row);
        }

        private void updateRequestStatus(String status) {
            String remarks = etRemarks.getText().toString().trim();

            new AlertDialog.Builder(this)
                    .setTitle(status.toUpperCase() + " REQUEST")
                    .setMessage("Are you sure you want to " + status + " this request?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        showProgress(true);
                        updateStatusAPI(status, remarks);
                    })
                    .setNegativeButton("No", null)
                    .show();
        }

    private void updateStatusAPI(String status, String remarks) {
        try {
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("request_id", requestId);
            jsonRequest.put("request_type", requestType);
            jsonRequest.put("status", status);
            jsonRequest.put("admin_remarks", remarks);
            jsonRequest.put("admin_id", sessionManager.getUsername());

            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(jsonRequest.toString(), JSON);

            // Log the request
            Log.d("REQUEST_BODY", jsonRequest.toString());

            Request request = new Request.Builder()
                    .url(BASE_URL + "update_request_status.php")
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        showProgress(false);
                        Toast.makeText(RequestDetailsActivity.this,
                                "Network error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();

                    // Log the response
                    Log.d("API_RESPONSE", responseBody);

                    runOnUiThread(() -> {
                        showProgress(false);
                        try {
                            JSONObject jsonResponse = new JSONObject(responseBody);
                            if (jsonResponse.getBoolean("success")) {
                                Toast.makeText(RequestDetailsActivity.this,
                                        "Request " + status + " successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(RequestDetailsActivity.this,
                                        jsonResponse.getString("message"), Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(RequestDetailsActivity.this,
                                    "Error: " + responseBody, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });

        } catch (Exception e) {
            showProgress(false);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
        private void showProgress(boolean show) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            btnApprove.setEnabled(!show);
            btnReject.setEnabled(!show);
        }
    }