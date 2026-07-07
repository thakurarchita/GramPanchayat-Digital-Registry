package com.example.users;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import okhttp3.*;

public class DeathRequestActivity extends BaseUserActivity {

    private TextView tvRequestId, tvHouseholdId, tvHeadName, tvMemberId, tvDob;
    private Spinner spinnerMember;
    private EditText etDateOfDeath, etPlaceOfDeath, etCertificateNo, etDeathReason;
    private Button btnSubmit;
    private ImageButton btnBack;
    private ProgressBar progressBar;
    private Toolbar toolbar;

    private OkHttpClient client;
    private UserSessionManager sessionManager;
    private static final String BASE_URL = "http://192.168.147.188/grampanchayat_api/";
    private String householdId;
    private List<Member> memberList = new ArrayList<>();

    private class Member {
        String memberId, memberName, dob, relationship;
        int age;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_death_request);

        client = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build();

        sessionManager = new UserSessionManager(this);
        householdId = sessionManager.getHouseholdId();

        initViews();
        setupToolbar();
        setupDatePicker();
        setupClickListeners();
        loadHouseholdDetails();
        generateRequestId();
        loadFamilyMembers();
    }

    private void initViews() {
        tvRequestId = findViewById(R.id.tvRequestId);
        tvHouseholdId = findViewById(R.id.tvHouseholdId);
        tvHeadName = findViewById(R.id.tvHeadName);
        tvMemberId = findViewById(R.id.tvMemberId);
        tvDob = findViewById(R.id.tvDob);
        spinnerMember = findViewById(R.id.spinnerMember);
        etDateOfDeath = findViewById(R.id.etDateOfDeath);
        etPlaceOfDeath = findViewById(R.id.etPlaceOfDeath);
        etCertificateNo = findViewById(R.id.etCertificateNo);
        etDeathReason = findViewById(R.id.etDeathReason);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);
        toolbar = findViewById(R.id.toolbar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupDatePicker() {
        etDateOfDeath.setOnClickListener(v -> showDatePicker(etDateOfDeath));
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

    private void setupClickListeners() {
        btnSubmit.setOnClickListener(v -> submitRequest());
        spinnerMember.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    Member selected = memberList.get(position - 1);
                    tvMemberId.setText(selected.memberId);
                    tvDob.setText(selected.dob != null && !selected.dob.isEmpty() ? selected.dob : "Not available");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadHouseholdDetails() {
        tvHouseholdId.setText(householdId);
        tvHeadName.setText(sessionManager.getFullName());
    }

    private void generateRequestId() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        tvRequestId.setText("DR" + sdf.format(new Date()));
    }

    private void loadFamilyMembers() {
        String url = BASE_URL + "get_family_members_list.php?household_id=" + householdId;

        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(DeathRequestActivity.this, "Failed to load members: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    setupEmptyMemberSpinner();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                runOnUiThread(() -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        if (jsonResponse.getBoolean("success")) {
                            JSONArray membersArray = jsonResponse.getJSONArray("members");
                            memberList.clear();

                            List<String> memberNames = new ArrayList<>();
                            memberNames.add("Select Member");

                            for (int i = 0; i < membersArray.length(); i++) {
                                JSONObject memberObj = membersArray.getJSONObject(i);
                                Member member = new Member();
                                member.memberId = memberObj.getString("member_id");
                                member.memberName = memberObj.getString("member_name");
                                member.age = memberObj.optInt("age", 0);
                                member.dob = memberObj.optString("date_of_birth", "Not available");
                                member.relationship = memberObj.getString("relationship");
                                memberList.add(member);
                                memberNames.add(member.memberName + " (" + member.relationship + ")");
                            }

                            ArrayAdapter<String> adapter = new ArrayAdapter<>(DeathRequestActivity.this,
                                    android.R.layout.simple_spinner_item, memberNames);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerMember.setAdapter(adapter);
                        } else {
                            setupEmptyMemberSpinner();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        setupEmptyMemberSpinner();
                    }
                });
            }
        });
    }

    private void setupEmptyMemberSpinner() {
        List<String> memberNames = new ArrayList<>();
        memberNames.add("No members found");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, memberNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMember.setAdapter(adapter);
        spinnerMember.setEnabled(false);
    }

    private void submitRequest() {
        int selectedPosition = spinnerMember.getSelectedItemPosition();
        if (selectedPosition == 0 || memberList.isEmpty()) {
            Toast.makeText(this, "Please select a member", Toast.LENGTH_SHORT).show();
            return;
        }

        Member selectedMember = memberList.get(selectedPosition - 1);
        String dateOfDeath = etDateOfDeath.getText().toString().trim();
        String placeOfDeath = etPlaceOfDeath.getText().toString().trim();
        String certificateNo = etCertificateNo.getText().toString().trim();
        String deathReason = etDeathReason.getText().toString().trim();

        if (TextUtils.isEmpty(dateOfDeath)) {
            etDateOfDeath.setError("Date of death required");
            return;
        }
        if (TextUtils.isEmpty(placeOfDeath)) {
            etPlaceOfDeath.setError("Place of death required");
            return;
        }
        if (TextUtils.isEmpty(certificateNo)) {
            etCertificateNo.setError("Certificate number required");
            return;
        }
        if (TextUtils.isEmpty(deathReason)) {
            etDeathReason.setError("Reason of death required");
            return;
        }

        showProgress(true);

        try {
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("request_id", tvRequestId.getText().toString());
            jsonRequest.put("household_id", householdId);
            jsonRequest.put("member_id", selectedMember.memberId);
            jsonRequest.put("member_name", selectedMember.memberName);
            jsonRequest.put("date_of_death", dateOfDeath);
            jsonRequest.put("place_of_death", placeOfDeath);
            jsonRequest.put("death_certificate_number", certificateNo);
            jsonRequest.put("death_reason", deathReason);
            jsonRequest.put("requested_by", sessionManager.getMobile());

            android.util.Log.e("DEATH_REQ", "Sending: " + jsonRequest.toString());

            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(jsonRequest.toString(), JSON);

            Request request = new Request.Builder()
                    .url(BASE_URL + "user_submit_death_request.php")
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        showProgress(false);
                        Toast.makeText(DeathRequestActivity.this, "Network error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    android.util.Log.e("DEATH_REQ", "Response: " + responseBody);

                    runOnUiThread(() -> {
                        showProgress(false);
                        try {
                            if (responseBody.trim().startsWith("<")) {
                                Toast.makeText(DeathRequestActivity.this, "Server error", Toast.LENGTH_LONG).show();
                                return;
                            }

                            JSONObject jsonResponse = new JSONObject(responseBody);
                            if (jsonResponse.getBoolean("success")) {
                                new AlertDialog.Builder(DeathRequestActivity.this)
                                        .setTitle("Success")
                                        .setMessage(jsonResponse.getString("message"))
                                        .setPositiveButton("OK", (dialog, which) -> finish())
                                        .show();
                            } else {
                                String errorMsg = jsonResponse.optString("message", "Unknown error");
                                Toast.makeText(DeathRequestActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(DeathRequestActivity.this, "Error: " + responseBody, Toast.LENGTH_LONG).show();
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
        btnSubmit.setText(show ? "SUBMITTING..." : "Submit Death Request");
    }
}