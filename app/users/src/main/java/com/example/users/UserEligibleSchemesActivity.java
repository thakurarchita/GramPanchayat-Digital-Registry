package com.example.users;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.*;

public class UserEligibleSchemesActivity extends BaseUserActivity {

    private LinearLayout layoutHouseholdSchemes, layoutIndividualSchemes;
    private TextView tvNoHouseholdSchemes, tvNoIndividualSchemes;
    private TextView tvHouseholdCount, tvIndividualCount;
    private TextView tvHouseholdId, tvHeadName;
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
        setContentView(R.layout.activity_user_eligible_schemes);

        client = new OkHttpClient();
        sessionManager = new UserSessionManager(this);
        householdId = sessionManager.getHouseholdId();

        initViews();
        setupToolbar();
        fetchHouseholdDetails();
        fetchEligibleSchemes();
    }

    private void initViews() {
        layoutHouseholdSchemes = findViewById(R.id.layoutHouseholdSchemes);
        layoutIndividualSchemes = findViewById(R.id.layoutIndividualSchemes);
        tvNoHouseholdSchemes = findViewById(R.id.tvNoHouseholdSchemes);
        tvNoIndividualSchemes = findViewById(R.id.tvNoIndividualSchemes);
        progressBar = findViewById(R.id.progressBar);
        btnBack = findViewById(R.id.btnBack);
        toolbar = findViewById(R.id.toolbar);
        tvHouseholdCount = findViewById(R.id.tvHouseholdCount);
        tvIndividualCount = findViewById(R.id.tvIndividualCount);
        tvHouseholdId = findViewById(R.id.tvHouseholdId);
        tvHeadName = findViewById(R.id.tvHeadName);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        btnBack.setOnClickListener(v -> finish());
    }

    private void fetchHouseholdDetails() {
        String url = BASE_URL + "get_household_details.php?household_id=" + householdId;

        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    tvHouseholdId.setText("ID: " + householdId);
                    tvHeadName.setText("Failed to load");
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
                            String headName = data.optString("head_name", "N/A");
                            String address = data.optString("address", "");
                            String village = data.optString("village", "");

                            tvHouseholdId.setText("ID: " + householdId);
                            tvHeadName.setText(headName);
                        } else {
                            tvHouseholdId.setText("ID: " + householdId);
                            tvHeadName.setText("Household Details");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        tvHouseholdId.setText("ID: " + householdId);
                        tvHeadName.setText("Household Details");
                    }
                });
            }
        });
    }

    private void fetchEligibleSchemes() {
        showProgress(true);

        String url = BASE_URL + "get_eligible_schemes.php?household_id=" + householdId;

        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(UserEligibleSchemesActivity.this,
                            "Network error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

                            // Clear existing views
                            layoutHouseholdSchemes.removeAllViews();
                            layoutIndividualSchemes.removeAllViews();

                            // Display Household Schemes
                            JSONArray householdSchemes = data.optJSONArray("household_schemes");
                            if (householdSchemes == null || householdSchemes.length() == 0) {
                                tvNoHouseholdSchemes.setVisibility(View.VISIBLE);
                                tvHouseholdCount.setText("0 schemes");
                            } else {
                                tvNoHouseholdSchemes.setVisibility(View.GONE);
                                tvHouseholdCount.setText(householdSchemes.length() + " scheme" + (householdSchemes.length() != 1 ? "s" : ""));
                                for (int i = 0; i < householdSchemes.length(); i++) {
                                    JSONObject scheme = householdSchemes.getJSONObject(i);
                                    addSchemeCard(layoutHouseholdSchemes, scheme, false, null);
                                }
                            }

                            // Display Individual Schemes
                            JSONArray individualSchemes = data.optJSONArray("individual_schemes");
                            if (individualSchemes == null || individualSchemes.length() == 0) {
                                tvNoIndividualSchemes.setVisibility(View.VISIBLE);
                                tvIndividualCount.setText("0 schemes");
                            } else {
                                tvNoIndividualSchemes.setVisibility(View.GONE);
                                tvIndividualCount.setText(individualSchemes.length() + " scheme" + (individualSchemes.length() != 1 ? "s" : ""));
                                for (int i = 0; i < individualSchemes.length(); i++) {
                                    JSONObject scheme = individualSchemes.getJSONObject(i);
                                    String memberName = scheme.optString("member_name", "Family Member");
                                    addSchemeCard(layoutIndividualSchemes, scheme, true, memberName);
                                }
                            }

                        } else {
                            String message = jsonResponse.getString("message");
                            Toast.makeText(UserEligibleSchemesActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(UserEligibleSchemesActivity.this,
                                "Error loading data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    } finally {
                        showProgress(false);
                    }
                });
            }
        });
    }

    private void addSchemeCard(LinearLayout container, JSONObject scheme, boolean isIndividual, String memberName) {
        try {
            View cardView = LayoutInflater.from(this).inflate(R.layout.item_scheme_card, null);

            TextView tvSchemeName = cardView.findViewById(R.id.tvSchemeName);
            TextView tvCategoryBadge = cardView.findViewById(R.id.tvCategoryBadge);
            TextView tvBenefit = cardView.findViewById(R.id.tvBenefit);
            TextView tvEligibilitySummary = cardView.findViewById(R.id.tvEligibilitySummary);
            TextView tvMemberNameView = cardView.findViewById(R.id.tvMemberName);
            TextView tvDepartment = cardView.findViewById(R.id.tvDepartment);
            Button btnApply = cardView.findViewById(R.id.btnApply);

            String schemeName = getStringSafe(scheme, "scheme_name");
            String category = getStringSafe(scheme, "scheme_category");
            String benefitType = getStringSafe(scheme, "benefit_type");
            double amount = getDoubleSafe(scheme, "benefit_amount");
            String portalUrl = getStringSafe(scheme, "portal_url");
            String department = getStringSafe(scheme, "department_name");

            tvSchemeName.setText(schemeName);

            // Set badge text and background color
            if (category.equals("Household-Based") || category.equals("Household")) {
                tvCategoryBadge.setText("Household");
                tvCategoryBadge.setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
            } else {
                tvCategoryBadge.setText("Individual");
                tvCategoryBadge.setBackgroundColor(ContextCompat.getColor(this, R.color.blue_primary));
            }

            // Add padding to badge
            tvCategoryBadge.setPadding(20, 8, 20, 8);

            // Set department if available
            if (tvDepartment != null && !department.isEmpty()) {
                tvDepartment.setText("🏢 " + department);
                tvDepartment.setVisibility(View.VISIBLE);
            }

            if (amount > 0) {
                tvBenefit.setText("💰 " + benefitType + ": ₹" + amount);
            } else {
                tvBenefit.setText("💰 " + benefitType);
            }

            // Show eligibility as "Eligible" since API already filters
            tvEligibilitySummary.setText("✅ Eligible");
            tvEligibilitySummary.setTextColor(ContextCompat.getColor(this, R.color.green));

            if (isIndividual && memberName != null && !memberName.isEmpty()) {
                tvMemberNameView.setVisibility(View.VISIBLE);
                tvMemberNameView.setText("👤 For: " + memberName);
            } else {
                tvMemberNameView.setVisibility(View.GONE);
            }

            // Make card clickable to open details activity
            cardView.setOnClickListener(v -> {
                Intent intent = new Intent(UserEligibleSchemesActivity.this, SchemeDetailsActivity.class);
                intent.putExtra("scheme_name", schemeName);
                intent.putExtra("scheme_category", category);
                intent.putExtra("benefit_type", benefitType);
                intent.putExtra("benefit_amount", amount);
                intent.putExtra("department_name", department);
                intent.putExtra("portal_url", portalUrl);
                intent.putExtra("is_individual", isIndividual);
                if (isIndividual && memberName != null && !memberName.isEmpty()) {
                    intent.putExtra("member_name", memberName);
                }
                startActivity(intent);
            });

            // Apply button
            btnApply.setOnClickListener(v -> {
                if (portalUrl != null && !portalUrl.isEmpty()) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(portalUrl)));
                } else {
                    Toast.makeText(UserEligibleSchemesActivity.this,
                            "Portal URL not available", Toast.LENGTH_SHORT).show();
                }
            });

            container.addView(cardView);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getStringSafe(JSONObject obj, String key) {
        try {
            return obj.has(key) ? obj.getString(key) : "";
        } catch (Exception e) {
            return "";
        }
    }

    private int getIntSafe(JSONObject obj, String key) {
        try {
            return obj.has(key) ? obj.getInt(key) : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private double getDoubleSafe(JSONObject obj, String key) {
        try {
            return obj.has(key) ? obj.getDouble(key) : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}