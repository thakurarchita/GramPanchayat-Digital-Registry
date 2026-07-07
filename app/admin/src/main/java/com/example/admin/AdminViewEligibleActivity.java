package com.example.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.*;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AdminViewEligibleActivity extends AppCompatActivity {


        private Spinner spinnerHousehold;
        private Button btnSearch;
        private LinearLayout layoutHouseholdSchemes, layoutIndividualSchemes;
        private TextView tvHouseholdName, tvHouseholdId, tvNoHouseholdSchemes, tvNoIndividualSchemes;
        private ProgressBar progressBar;
        private ImageButton btnBack;
        private Toolbar toolbar;

        private OkHttpClient client;
        private AdminSessionManager sessionManager;
        private static final String BASE_URL = "http://192.168.147.188/grampanchayat_api/";
        private List<HouseholdItem> householdList = new ArrayList<>();

        private class HouseholdItem {
            String householdId, headName;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_admin_view_eligible);

            client = new OkHttpClient();
            sessionManager = new AdminSessionManager(this);

            initViews();
            setupToolbar();
            setupListeners();
            loadHouseholds();
        }

        private void initViews() {
            spinnerHousehold = findViewById(R.id.spinnerHousehold);
            btnSearch = findViewById(R.id.btnSearch);
            layoutHouseholdSchemes = findViewById(R.id.layoutHouseholdSchemes);
            layoutIndividualSchemes = findViewById(R.id.layoutIndividualSchemes);
            tvHouseholdName = findViewById(R.id.tvHouseholdName);
            tvHouseholdId = findViewById(R.id.tvHouseholdId);
            tvNoHouseholdSchemes = findViewById(R.id.tvNoHouseholdSchemes);
            tvNoIndividualSchemes = findViewById(R.id.tvNoIndividualSchemes);
            progressBar = findViewById(R.id.progressBar);
            btnBack = findViewById(R.id.btnBack);
            toolbar = findViewById(R.id.toolbar);
        }

        private void setupToolbar() {
            setSupportActionBar(toolbar);
            btnBack.setOnClickListener(v -> finish());
        }

        private void setupListeners() {
            btnSearch.setOnClickListener(v -> {
                int position = spinnerHousehold.getSelectedItemPosition();
                if (position > 0) {
                    HouseholdItem selected = householdList.get(position - 1);
                    fetchEligibleSchemes(selected.householdId);
                } else {
                    Toast.makeText(this, "Please select a household", Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void loadHouseholds() {
            String url = BASE_URL + "get_all_households.php";

            Request request = new Request.Builder().url(url).get().build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(AdminViewEligibleActivity.this,
                            "Failed to load households", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    runOnUiThread(() -> {
                        try {
                            JSONObject jsonResponse = new JSONObject(responseBody);
                            if (jsonResponse.getBoolean("success")) {
                                JSONArray households = jsonResponse.getJSONArray("households");
                                householdList.clear();

                                List<String> householdNames = new ArrayList<>();
                                householdNames.add("Select Household");

                                for (int i = 0; i < households.length(); i++) {
                                    JSONObject obj = households.getJSONObject(i);
                                    HouseholdItem item = new HouseholdItem();
                                    item.householdId = obj.getString("household_id");
                                    item.headName = obj.getString("head_name");
                                    householdList.add(item);
                                    householdNames.add(item.headName + " (" + item.householdId + ")");
                                }

                                ArrayAdapter<String> adapter = new ArrayAdapter<>(AdminViewEligibleActivity.this,
                                        android.R.layout.simple_spinner_item, householdNames);
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spinnerHousehold.setAdapter(adapter);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            });
        }

        private void fetchEligibleSchemes(String householdId) {
            showProgress(true);
            layoutHouseholdSchemes.removeAllViews();
            layoutIndividualSchemes.removeAllViews();
            tvNoHouseholdSchemes.setVisibility(View.GONE);
            tvNoIndividualSchemes.setVisibility(View.GONE);

            String url = BASE_URL + "get_eligible_schemes.php?household_id=" + householdId;

            Request request = new Request.Builder().url(url).get().build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        showProgress(false);
                        Toast.makeText(AdminViewEligibleActivity.this,
                                "Network error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                                JSONObject household = data.getJSONObject("household");

                                tvHouseholdName.setText(household.getString("head_name"));
                                tvHouseholdId.setText(household.getString("household_id"));

                                // Display Household Schemes
                                JSONArray householdSchemes = data.getJSONArray("household_schemes");
                                if (householdSchemes.length() == 0) {
                                    tvNoHouseholdSchemes.setVisibility(View.VISIBLE);
                                } else {
                                    for (int i = 0; i < householdSchemes.length(); i++) {
                                        JSONObject scheme = householdSchemes.getJSONObject(i);
                                        addSchemeCard(layoutHouseholdSchemes, scheme, false, null);
                                    }
                                }

                                // Display Individual Schemes
                                JSONArray individualSchemes = data.getJSONArray("individual_schemes");
                                if (individualSchemes.length() == 0) {
                                    tvNoIndividualSchemes.setVisibility(View.VISIBLE);
                                } else {
                                    for (int i = 0; i < individualSchemes.length(); i++) {
                                        JSONObject scheme = individualSchemes.getJSONObject(i);
                                        String memberName = scheme.optString("member_name", "Unknown");
                                        addSchemeCard(layoutIndividualSchemes, scheme, true, memberName);
                                    }
                                }
                            } else {
                                Toast.makeText(AdminViewEligibleActivity.this,
                                        jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(AdminViewEligibleActivity.this,
                                    "Error loading data", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }

        private void addSchemeCard(LinearLayout container, JSONObject scheme, boolean isIndividual, String memberName) throws Exception {
            CardView card = new CardView(this);
            CardView.LayoutParams params = new CardView.LayoutParams(
                    CardView.LayoutParams.MATCH_PARENT,
                    CardView.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, 16);
            card.setLayoutParams(params);
            card.setRadius(12);
            card.setCardElevation(4);
            card.setContentPadding(20, 20, 20, 20);

            LinearLayout content = new LinearLayout(this);
            content.setOrientation(LinearLayout.VERTICAL);

            // Scheme Name
            TextView tvSchemeName = new TextView(this);
            tvSchemeName.setText(scheme.getString("scheme_name"));
            tvSchemeName.setTextSize(16);
            tvSchemeName.setTextColor(getColor(android.R.color.black));
            tvSchemeName.setTypeface(tvSchemeName.getTypeface(), android.graphics.Typeface.BOLD);
            content.addView(tvSchemeName);

            // Department
            TextView tvDept = new TextView(this);
            tvDept.setText("🏢 " + scheme.getString("department_name"));
            tvDept.setTextSize(12);
            tvDept.setTextColor(getColor(android.R.color.darker_gray));
            tvDept.setPadding(0, 8, 0, 8);
            content.addView(tvDept);

            // Benefit
            TextView tvBenefit = new TextView(this);
            String benefitType = scheme.getString("benefit_type");
            double amount = scheme.getDouble("benefit_amount");
            if (amount > 0) {
                tvBenefit.setText("💰 " + benefitType + ": ₹" + amount);
            } else {
                tvBenefit.setText("💰 " + benefitType);
            }
            tvBenefit.setTextSize(14);
            tvBenefit.setTextColor(getColor(android.R.color.black));
            content.addView(tvBenefit);

            // For individual schemes, show member name
            if (isIndividual && memberName != null) {
                TextView tvMember = new TextView(this);
                tvMember.setText("👤 For: " + memberName);
                tvMember.setTextSize(12);
                tvMember.setTextColor(getColor(R.color.blue_primary));
                content.addView(tvMember);
            }

            card.addView(content);
            container.addView(card);
        }

        private void showProgress(boolean show) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            btnSearch.setEnabled(!show);
        }
    }