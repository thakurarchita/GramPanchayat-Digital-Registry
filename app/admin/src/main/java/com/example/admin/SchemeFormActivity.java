package com.example.admin;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import org.json.JSONObject;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import okhttp3.*;

public class SchemeFormActivity extends AppCompatActivity {

    private TextView tvSchemeId;
    private EditText etSchemeName, etDepartment, etDescription, etBenefitAmount;
    private EditText etStartDate, etEndDate, etMinIncome, etMaxIncome, etMinAge, etMaxAge;
    private EditText etPortalUrl;  // NEW FIELD
    private RadioGroup rgSchemeCategory;
    private Spinner spinnerBenefitType, spinnerGender, spinnerCaste;
    private CheckBox cbAadhaar, cbIncome, cbCaste, cbResidence, cbAge, cbBank, cbPhoto;
    private Button btnSubmit, btnUpdate, btnDelete;
    private ProgressBar progressBar;

    private OkHttpClient client;
    private static final String BASE_URL = "http://192.168.147.188/grampanchayat_api/";
    private String schemeId = null;

    private String[] benefitTypes = {"Financial", "Subsidy", "Loan", "Material", "Other"};
    private String[] genders = {"Any", "Male", "Female", "Transgender"};
    private String[] castes = {"All", "General", "OBC", "SC", "ST", "Other"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheme_form);

        client = new OkHttpClient();
        initViews();
        setupToolbar();
        setupSpinners();
        setupDatePickers();

        // Get scheme_id from intent
        if (getIntent().hasExtra("scheme_id")) {
            schemeId = getIntent().getStringExtra("scheme_id");
        }

        if (schemeId != null && !schemeId.isEmpty()) {
            // Edit Mode - Show UPDATE and DELETE buttons
            btnSubmit.setVisibility(View.GONE);
            btnUpdate.setVisibility(View.VISIBLE);
            btnDelete.setVisibility(View.VISIBLE);
            loadSchemeData(schemeId);
        } else {
            // Add Mode - Show ADD button only
            btnSubmit.setVisibility(View.VISIBLE);
            btnUpdate.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);
            generateSchemeId();
        }

        btnSubmit.setOnClickListener(v -> addScheme());
        btnUpdate.setOnClickListener(v -> updateScheme());
        btnDelete.setOnClickListener(v -> confirmDelete());
    }

    private void initViews() {
        tvSchemeId = findViewById(R.id.tvSchemeId);
        etSchemeName = findViewById(R.id.etSchemeName);
        etDepartment = findViewById(R.id.etDepartment);
        etDescription = findViewById(R.id.etDescription);
        etBenefitAmount = findViewById(R.id.etBenefitAmount);
        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        etMinIncome = findViewById(R.id.etMinIncome);
        etMaxIncome = findViewById(R.id.etMaxIncome);
        etMinAge = findViewById(R.id.etMinAge);
        etMaxAge = findViewById(R.id.etMaxAge);
        etPortalUrl = findViewById(R.id.etPortalUrl);  // NEW FIELD
        rgSchemeCategory = findViewById(R.id.rgSchemeCategory);
        spinnerBenefitType = findViewById(R.id.spinnerBenefitType);
        spinnerGender = findViewById(R.id.spinnerGender);
        spinnerCaste = findViewById(R.id.spinnerCaste);
        cbAadhaar = findViewById(R.id.cbAadhaar);
        cbIncome = findViewById(R.id.cbIncome);
        cbCaste = findViewById(R.id.cbCaste);
        cbResidence = findViewById(R.id.cbResidence);
        cbAge = findViewById(R.id.cbAge);
        cbBank = findViewById(R.id.cbBank);
        cbPhoto = findViewById(R.id.cbPhoto);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupSpinners() {
        ArrayAdapter<String> benefitAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, benefitTypes);
        benefitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBenefitType.setAdapter(benefitAdapter);

        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, genders);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);

        ArrayAdapter<String> casteAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, castes);
        casteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCaste.setAdapter(casteAdapter);
    }

    private void setupDatePickers() {
        etStartDate.setOnClickListener(v -> showDatePicker(etStartDate));
        etEndDate.setOnClickListener(v -> showDatePicker(etEndDate));
    }

    private void showDatePicker(EditText target) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(this,
                (view, year, month, day) -> {
                    String date = year + "-" + (month + 1) + "-" + day;
                    target.setText(date);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePicker.show();
    }

    private void generateSchemeId() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        String id = "SCH" + sdf.format(new java.util.Date());
        tvSchemeId.setText(id);
    }

    private void loadSchemeData(String id) {
        showProgress(true);
        String url = BASE_URL + "get_scheme.php?scheme_id=" + id;

        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(SchemeFormActivity.this, "Failed to load: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                runOnUiThread(() -> {
                    showProgress(false);
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        if (jsonResponse.getBoolean("success")) {
                            JSONObject data = jsonResponse.getJSONObject("data");
                            displaySchemeData(data);
                        } else {
                            Toast.makeText(SchemeFormActivity.this, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(SchemeFormActivity.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }
        });
    }

    private void displaySchemeData(JSONObject data) throws Exception {
        tvSchemeId.setText(data.optString("scheme_id"));
        etSchemeName.setText(data.optString("scheme_name"));
        etDepartment.setText(data.optString("department_name"));
        etDescription.setText(data.optString("description"));
        etBenefitAmount.setText(data.optString("benefit_amount"));
        etStartDate.setText(data.optString("application_start_date"));
        etEndDate.setText(data.optString("application_end_date"));
        etMinIncome.setText(data.optString("min_income"));
        etMaxIncome.setText(data.optString("max_income"));
        etMinAge.setText(data.optString("min_age"));
        etMaxAge.setText(data.optString("max_age"));
        etPortalUrl.setText(data.optString("portal_url"));  // NEW FIELD

        String category = data.optString("scheme_category");
        if (category.equals("Household-Based")) {
            rgSchemeCategory.check(R.id.rbHouseholdBased);
        } else {
            rgSchemeCategory.check(R.id.rbIndividualBased);
        }

        String benefitType = data.optString("benefit_type");
        for (int i = 0; i < benefitTypes.length; i++) {
            if (benefitTypes[i].equals(benefitType)) {
                spinnerBenefitType.setSelection(i);
                break;
            }
        }

        String gender = data.optString("gender_restriction");
        for (int i = 0; i < genders.length; i++) {
            if (genders[i].equals(gender)) {
                spinnerGender.setSelection(i);
                break;
            }
        }

        String caste = data.optString("caste_restriction");
        for (int i = 0; i < castes.length; i++) {
            if (castes[i].equals(caste)) {
                spinnerCaste.setSelection(i);
                break;
            }
        }

        String documents = data.optString("required_documents");
        if (documents != null) {
            cbAadhaar.setChecked(documents.contains("Aadhaar"));
            cbIncome.setChecked(documents.contains("Income"));
            cbCaste.setChecked(documents.contains("Caste"));
            cbResidence.setChecked(documents.contains("Residence"));
            cbAge.setChecked(documents.contains("Age"));
            cbBank.setChecked(documents.contains("Bank"));
            cbPhoto.setChecked(documents.contains("Photo"));
        }
    }

    private String getSelectedDocuments() {
        StringBuilder docs = new StringBuilder();
        if (cbAadhaar.isChecked()) docs.append("Aadhaar Card, ");
        if (cbIncome.isChecked()) docs.append("Income Certificate, ");
        if (cbCaste.isChecked()) docs.append("Caste Certificate, ");
        if (cbResidence.isChecked()) docs.append("Residence Proof, ");
        if (cbAge.isChecked()) docs.append("Age Proof, ");
        if (cbBank.isChecked()) docs.append("Bank Passbook, ");
        if (cbPhoto.isChecked()) docs.append("Passport Size Photo, ");

        String result = docs.toString();
        if (result.endsWith(", ")) {
            result = result.substring(0, result.length() - 2);
        }
        return result;
    }

    private void addScheme() {
        if (!validateInputs()) return;
        showProgress(true);

        try {
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("scheme_id", tvSchemeId.getText().toString());
            jsonRequest.put("scheme_name", etSchemeName.getText().toString().trim());
            jsonRequest.put("scheme_category", getSelectedCategory());
            jsonRequest.put("department_name", etDepartment.getText().toString().trim());
            jsonRequest.put("description", etDescription.getText().toString().trim());
            jsonRequest.put("benefit_type", benefitTypes[spinnerBenefitType.getSelectedItemPosition()]);
            jsonRequest.put("benefit_amount", etBenefitAmount.getText().toString().trim());
            jsonRequest.put("application_start_date", etStartDate.getText().toString().trim());
            jsonRequest.put("application_end_date", etEndDate.getText().toString().trim());
            jsonRequest.put("required_documents", getSelectedDocuments());
            jsonRequest.put("min_income", etMinIncome.getText().toString().trim());
            jsonRequest.put("max_income", etMaxIncome.getText().toString().trim());
            jsonRequest.put("min_age", etMinAge.getText().toString().trim());
            jsonRequest.put("max_age", etMaxAge.getText().toString().trim());
            jsonRequest.put("gender_restriction", genders[spinnerGender.getSelectedItemPosition()]);
            jsonRequest.put("caste_restriction", castes[spinnerCaste.getSelectedItemPosition()]);
            jsonRequest.put("scheme_status", "Active");
            jsonRequest.put("portal_url", etPortalUrl.getText().toString().trim());  // NEW FIELD

            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(jsonRequest.toString(), JSON);

            Request request = new Request.Builder()
                    .url(BASE_URL + "add_scheme.php")
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        showProgress(false);
                        Toast.makeText(SchemeFormActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    runOnUiThread(() -> {
                        showProgress(false);
                        try {
                            JSONObject jsonResponse = new JSONObject(responseBody);
                            if (jsonResponse.getBoolean("success")) {
                                Toast.makeText(SchemeFormActivity.this, "Scheme added successfully!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent();
                                intent.putExtra("refresh", true);
                                setResult(RESULT_OK, intent);
                                finish();
                            } else {
                                Toast.makeText(SchemeFormActivity.this, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(SchemeFormActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

        } catch (Exception e) {
            showProgress(false);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateScheme() {
        if (!validateInputs()) return;
        showProgress(true);

        try {
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("scheme_id", schemeId);
            jsonRequest.put("scheme_name", etSchemeName.getText().toString().trim());
            jsonRequest.put("scheme_category", getSelectedCategory());
            jsonRequest.put("department_name", etDepartment.getText().toString().trim());
            jsonRequest.put("description", etDescription.getText().toString().trim());
            jsonRequest.put("benefit_type", benefitTypes[spinnerBenefitType.getSelectedItemPosition()]);
            jsonRequest.put("benefit_amount", etBenefitAmount.getText().toString().trim());
            jsonRequest.put("application_start_date", etStartDate.getText().toString().trim());
            jsonRequest.put("application_end_date", etEndDate.getText().toString().trim());
            jsonRequest.put("required_documents", getSelectedDocuments());
            jsonRequest.put("min_income", etMinIncome.getText().toString().trim());
            jsonRequest.put("max_income", etMaxIncome.getText().toString().trim());
            jsonRequest.put("min_age", etMinAge.getText().toString().trim());
            jsonRequest.put("max_age", etMaxAge.getText().toString().trim());
            jsonRequest.put("gender_restriction", genders[spinnerGender.getSelectedItemPosition()]);
            jsonRequest.put("caste_restriction", castes[spinnerCaste.getSelectedItemPosition()]);
            jsonRequest.put("scheme_status", "Active");
            jsonRequest.put("portal_url", etPortalUrl.getText().toString().trim());  // NEW FIELD

            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(jsonRequest.toString(), JSON);

            Request request = new Request.Builder()
                    .url(BASE_URL + "update_scheme.php")
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        showProgress(false);
                        Toast.makeText(SchemeFormActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    runOnUiThread(() -> {
                        showProgress(false);
                        try {
                            JSONObject jsonResponse = new JSONObject(responseBody);
                            if (jsonResponse.getBoolean("success")) {
                                Toast.makeText(SchemeFormActivity.this, "Scheme updated successfully!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent();
                                intent.putExtra("refresh", true);
                                setResult(RESULT_OK, intent);
                                finish();
                            } else {
                                Toast.makeText(SchemeFormActivity.this, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(SchemeFormActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

        } catch (Exception e) {
            showProgress(false);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String getSelectedCategory() {
        int selectedId = rgSchemeCategory.getCheckedRadioButtonId();
        if (selectedId == R.id.rbHouseholdBased) {
            return "Household-Based";
        } else {
            return "Individual-Based";
        }
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Scheme")
                .setMessage("Are you sure you want to delete this scheme?")
                .setPositiveButton("Delete", (dialog, which) -> deleteScheme())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteScheme() {
        showProgress(true);

        try {
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("scheme_id", schemeId);

            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(jsonRequest.toString(), JSON);

            Request request = new Request.Builder()
                    .url(BASE_URL + "delete_scheme.php")
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        showProgress(false);
                        Toast.makeText(SchemeFormActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    runOnUiThread(() -> {
                        showProgress(false);
                        try {
                            JSONObject jsonResponse = new JSONObject(responseBody);
                            if (jsonResponse.getBoolean("success")) {
                                Toast.makeText(SchemeFormActivity.this, "Scheme deleted successfully!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent();
                                intent.putExtra("refresh", true);
                                setResult(RESULT_OK, intent);
                                finish();
                            } else {
                                Toast.makeText(SchemeFormActivity.this, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(SchemeFormActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

        } catch (Exception e) {
            showProgress(false);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateInputs() {
        if (TextUtils.isEmpty(etSchemeName.getText().toString().trim())) {
            etSchemeName.setError("Scheme name required");
            return false;
        }
        if (TextUtils.isEmpty(etDepartment.getText().toString().trim())) {
            etDepartment.setError("Department name required");
            return false;
        }
        return true;
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSubmit.setEnabled(!show);
        btnUpdate.setEnabled(!show);
        btnDelete.setEnabled(!show);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}