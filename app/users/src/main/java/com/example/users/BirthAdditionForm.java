package com.example.users;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import org.json.JSONObject;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import okhttp3.*;

public class BirthAdditionForm extends BaseUserActivity {

    private TextView tvRequestId, tvHouseholdId, tvHeadName;
    private EditText etChildName, etPlaceOfBirth, etFatherName, etMotherName;
    private EditText etCertificateNo, etIssueDate, etDob, etAadhaar;
    private RadioGroup rgGender;
    private Spinner spinnerRelation;
    private Button btnSubmit;
    private ImageButton btnBack;
    private ProgressBar progressBar;
    private Toolbar toolbar;

    private OkHttpClient client;
    private UserSessionManager sessionManager;
    private static final String BASE_URL = "http://192.168.147.188/grampanchayat_api/";
    private String householdId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_birth_addition_form);

        client = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build();

        sessionManager = new UserSessionManager(this);
        householdId = sessionManager.getHouseholdId();

        initViews();
        setupToolbar();
        setupDatePickers();
        setupRelationSpinner();
        loadHouseholdDetails();
        generateRequestId();
        btnSubmit.setOnClickListener(v -> submitRequest());
        btnBack.setOnClickListener(v -> finish());
    }

    private void initViews() {
        tvRequestId = findViewById(R.id.tvRequestId);
        tvHouseholdId = findViewById(R.id.tvHouseholdId);
        tvHeadName = findViewById(R.id.tvHeadName);
        etChildName = findViewById(R.id.etChildName);
        rgGender = findViewById(R.id.rgGender);
        etDob = findViewById(R.id.etDob);
        etPlaceOfBirth = findViewById(R.id.etPlaceOfBirth);
        spinnerRelation = findViewById(R.id.spinnerRelation);
        etAadhaar = findViewById(R.id.etAadhaar);
        etFatherName = findViewById(R.id.etFatherName);
        etMotherName = findViewById(R.id.etMotherName);
        etCertificateNo = findViewById(R.id.etCertificateNo);
        etIssueDate = findViewById(R.id.etIssueDate);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);
        toolbar = findViewById(R.id.toolbar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
    }

    private void setupRelationSpinner() {
        String[] relations = {"Son", "Daughter", "Child"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, relations);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRelation.setAdapter(adapter);
    }

    private void setupDatePickers() {
        etDob.setOnClickListener(v -> showDatePicker(etDob));
        etIssueDate.setOnClickListener(v -> showDatePicker(etIssueDate));
    }

    private void showDatePicker(EditText target) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(this,
                (view, year, month, day) -> {
                    String date = day + "/" + (month + 1) + "/" + year;
                    target.setText(date);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePicker.show();
    }

    private void loadHouseholdDetails() {
        tvHouseholdId.setText(householdId);
        tvHeadName.setText(sessionManager.getFullName());
    }

    private void generateRequestId() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        tvRequestId.setText("BR" + sdf.format(new Date()));
    }

    private String getSelectedGender() {
        int selectedId = rgGender.getCheckedRadioButtonId();
        if (selectedId == R.id.rbMale) return "Male";
        if (selectedId == R.id.rbFemale) return "Female";
        return "Male";
    }

    private void submitRequest() {
        String childName = etChildName.getText().toString().trim();
        String gender = getSelectedGender();
        String dob = etDob.getText().toString().trim();
        String placeOfBirth = etPlaceOfBirth.getText().toString().trim();
        String relation = spinnerRelation.getSelectedItem().toString();
        String aadhaar = etAadhaar.getText().toString().trim();
        String fatherName = etFatherName.getText().toString().trim();
        String motherName = etMotherName.getText().toString().trim();
        String certificateNo = etCertificateNo.getText().toString().trim();
        String issueDate = etIssueDate.getText().toString().trim();

        if (TextUtils.isEmpty(childName)) {
            etChildName.setError("Child name required");
            return;
        }
        if (TextUtils.isEmpty(dob)) {
            etDob.setError("Date of birth required");
            return;
        }
        if (TextUtils.isEmpty(placeOfBirth)) {
            etPlaceOfBirth.setError("Place of birth required");
            return;
        }
        if (TextUtils.isEmpty(fatherName)) {
            etFatherName.setError("Father name required");
            return;
        }
        if (TextUtils.isEmpty(motherName)) {
            etMotherName.setError("Mother name required");
            return;
        }
        if (TextUtils.isEmpty(certificateNo)) {
            etCertificateNo.setError("Certificate number required");
            return;
        }
        if (TextUtils.isEmpty(issueDate)) {
            etIssueDate.setError("Date of issue required");
            return;
        }

        // Validate Aadhaar if provided
        if (!TextUtils.isEmpty(aadhaar) && aadhaar.length() != 12) {
            etAadhaar.setError("Aadhaar must be 12 digits");
            return;
        }

        showProgress(true);

        try {
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("request_id", tvRequestId.getText().toString());
            jsonRequest.put("household_id", householdId);
            jsonRequest.put("child_name", childName);
            jsonRequest.put("gender", gender);
            jsonRequest.put("date_of_birth", dob);
            jsonRequest.put("place_of_birth", placeOfBirth);
            jsonRequest.put("relationship", relation);
            jsonRequest.put("aadhaar_number", aadhaar);
            jsonRequest.put("father_name", fatherName);
            jsonRequest.put("mother_name", motherName);
            jsonRequest.put("birth_certificate_number", certificateNo);
            jsonRequest.put("date_of_issue", issueDate);
            jsonRequest.put("requested_by", sessionManager.getMobile());

            android.util.Log.e("BIRTH_REQ", "Sending: " + jsonRequest.toString());

            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(jsonRequest.toString(), JSON);

            Request request = new Request.Builder()
                    .url(BASE_URL + "user_submit_birth_request.php")
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        showProgress(false);
                        Toast.makeText(BirthAdditionForm.this, "Network error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    android.util.Log.e("BIRTH_REQ", "Response: " + responseBody);

                    runOnUiThread(() -> {
                        showProgress(false);
                        try {
                            JSONObject jsonResponse = new JSONObject(responseBody);
                            if (jsonResponse.getBoolean("success")) {
                                new AlertDialog.Builder(BirthAdditionForm.this)
                                        .setTitle("Success")
                                        .setMessage(jsonResponse.getString("message"))
                                        .setPositiveButton("OK", (dialog, which) -> finish())
                                        .show();
                            } else {
                                String errorMsg = jsonResponse.optString("message", "Unknown error");
                                Toast.makeText(BirthAdditionForm.this, errorMsg, Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(BirthAdditionForm.this, "Error: " + responseBody, Toast.LENGTH_LONG).show();
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
        btnSubmit.setEnabled(!show);
        btnSubmit.setText(show ? "SUBMITTING..." : "Submit Birth Request");
    }
}