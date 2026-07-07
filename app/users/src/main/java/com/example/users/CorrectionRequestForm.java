package com.example.users;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import okhttp3.*;

public class CorrectionRequestForm extends BaseUserActivity {

    private TextView tvRequestId, tvHouseholdId, tvApplicantName, tvMobile, tvRequestDate;
    private LinearLayout layoutRequestsContainer;
    private Button btnAddRequest, btnSubmit;
    private ImageButton btnBack;
    private ProgressBar progressBar;
    private Toolbar toolbar;

    private OkHttpClient client;
    private UserSessionManager sessionManager;
    private static final String BASE_URL = "http://192.168.147.188/grampanchayat_api/";
    private String householdId;
    private List<View> requestViews = new ArrayList<>();

    private String[] correctionFields = {
            "head_name", "head_mobile", "address", "annual_income", "caste"
    };

    private String[] fieldDisplayNames = {
            "Head Name", "Mobile Number", "Address", "Annual Income", "Caste"
    };

    private String[] docTypes = {"Aadhaar Card", "Birth Certificate", "Income Certificate",
            "Caste Certificate", "Residence Proof", "Other"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_correction_request_form);

        client = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build();

        sessionManager = new UserSessionManager(this);
        householdId = sessionManager.getHouseholdId();

        initViews();
        setupToolbar();
        setupListeners();
        loadHouseholdDetails();
        generateRequestId();

        addNewRequestForm();
    }

    private void initViews() {
        tvRequestId = findViewById(R.id.tvRequestId);
        tvHouseholdId = findViewById(R.id.tvHouseholdId);
        tvApplicantName = findViewById(R.id.tvApplicantName);
        tvMobile = findViewById(R.id.tvMobile);
        tvRequestDate = findViewById(R.id.tvRequestDate);
        layoutRequestsContainer = findViewById(R.id.layoutRequestsContainer);
        btnAddRequest = findViewById(R.id.btnAddRequest);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);
        toolbar = findViewById(R.id.toolbar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupListeners() {
        btnAddRequest.setOnClickListener(v -> addNewRequestForm());
        btnSubmit.setOnClickListener(v -> submitAllRequests());
    }

    private void loadHouseholdDetails() {
        tvHouseholdId.setText(householdId);
        tvApplicantName.setText(sessionManager.getFullName());
        tvMobile.setText(sessionManager.getMobile());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        tvRequestDate.setText(sdf.format(new Date()));
    }

    private void generateRequestId() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        tvRequestId.setText("CR" + sdf.format(new Date()));
    }

    private void addNewRequestForm() {
        View requestView = getLayoutInflater().inflate(R.layout.item_correction_request, null);

        Spinner spinnerField = requestView.findViewById(R.id.spinnerField);
        ArrayAdapter<String> fieldAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, fieldDisplayNames);
        fieldAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerField.setAdapter(fieldAdapter);

        Spinner spinnerDocType = requestView.findViewById(R.id.spinnerDocType);
        ArrayAdapter<String> docAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, docTypes);
        docAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDocType.setAdapter(docAdapter);

        spinnerField.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fetchOldValue(requestView, position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        ImageButton btnRemove = requestView.findViewById(R.id.btnRemoveRequest);
        if (requestViews.size() > 0) {
            btnRemove.setVisibility(View.VISIBLE);
            btnRemove.setOnClickListener(v -> {
                layoutRequestsContainer.removeView(requestView);
                requestViews.remove(requestView);
            });
        } else {
            btnRemove.setVisibility(View.GONE);
        }

        layoutRequestsContainer.addView(requestView);
        requestViews.add(requestView);
    }

    private void fetchOldValue(View requestView, int fieldPosition) {
        String selectedField = correctionFields[fieldPosition];
        TextView tvOldValue = requestView.findViewById(R.id.tvOldValue);
        tvOldValue.setText("Loading...");

        String url = BASE_URL + "get_household_field_value.php?household_id=" + householdId + "&field=" + selectedField;

        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> tvOldValue.setText("Network error"));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                runOnUiThread(() -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        if (jsonResponse.getBoolean("success")) {
                            tvOldValue.setText(jsonResponse.getString("old_value"));
                        } else {
                            tvOldValue.setText("Not available");
                        }
                    } catch (Exception e) {
                        tvOldValue.setText("Error");
                    }
                });
            }
        });
    }

    private void submitAllRequests() {
        if (requestViews.isEmpty()) {
            Toast.makeText(this, "Please add at least one correction request", Toast.LENGTH_SHORT).show();
            return;
        }

        showProgress(true);

        try {
            // Submit each request individually
            for (int i = 0; i < requestViews.size(); i++) {
                View requestView = requestViews.get(i);

                Spinner spinnerField = requestView.findViewById(R.id.spinnerField);
                EditText etNewValue = requestView.findViewById(R.id.etNewValue);
                EditText etReason = requestView.findViewById(R.id.etReason);
                Spinner spinnerDocType = requestView.findViewById(R.id.spinnerDocType);
                EditText etDocNumber = requestView.findViewById(R.id.etDocNumber);
                TextView tvOldValue = requestView.findViewById(R.id.tvOldValue);

                String fieldName = correctionFields[spinnerField.getSelectedItemPosition()];
                String newValue = etNewValue.getText().toString().trim();
                String reason = etReason.getText().toString().trim();
                String oldValue = tvOldValue.getText().toString().trim();
                String docType = spinnerDocType.getSelectedItem().toString();
                String docNumber = etDocNumber.getText().toString().trim();

                if (TextUtils.isEmpty(newValue)) {
                    Toast.makeText(this, "New value required for request " + (i + 1), Toast.LENGTH_SHORT).show();
                    showProgress(false);
                    return;
                }
                if (TextUtils.isEmpty(reason)) {
                    Toast.makeText(this, "Reason required for request " + (i + 1), Toast.LENGTH_SHORT).show();
                    showProgress(false);
                    return;
                }

                JSONObject requestObj = new JSONObject();
                requestObj.put("household_id", householdId);
                requestObj.put("member_id", "");
                requestObj.put("field_name", fieldName);
                requestObj.put("old_value", oldValue);
                requestObj.put("new_value", newValue);
                requestObj.put("reason", reason);
                requestObj.put("document_type", docType);
                requestObj.put("document_number", docNumber);

                android.util.Log.e("CORRECTION_REQ", "Sending: " + requestObj.toString());

                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(requestObj.toString(), JSON);

                Request request = new Request.Builder()
                        .url(BASE_URL + "submit_correction_request.php")
                        .post(body)
                        .addHeader("Content-Type", "application/json")
                        .build();

                final int requestNum = i + 1;
                final int totalRequests = requestViews.size();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(() -> {
                            showProgress(false);
                            Toast.makeText(CorrectionRequestForm.this,
                                    "Request " + requestNum + " failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String responseBody = response.body().string();
                        android.util.Log.e("CORRECTION_REQ", "Response " + requestNum + ": " + responseBody);

                        if (requestNum == totalRequests) {
                            runOnUiThread(() -> {
                                showProgress(false);
                                try {
                                    JSONObject jsonResponse = new JSONObject(responseBody);
                                    if (jsonResponse.getBoolean("success")) {
                                        new AlertDialog.Builder(CorrectionRequestForm.this)
                                                .setTitle("Success")
                                                .setMessage("All " + totalRequests + " correction request(s) submitted successfully")
                                                .setPositiveButton("OK", (dialog, which) -> finish())
                                                .show();
                                    } else {
                                        String message = jsonResponse.optString("message", "Unknown error");
                                        Toast.makeText(CorrectionRequestForm.this, "Error: " + message, Toast.LENGTH_LONG).show();
                                    }
                                } catch (Exception e) {
                                    Toast.makeText(CorrectionRequestForm.this, "Error: " + responseBody, Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                });
            }

        } catch (Exception e) {
            showProgress(false);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSubmit.setEnabled(!show);
        btnSubmit.setText(show ? "SUBMITTING..." : "Submit All Correction Requests");
    }
}