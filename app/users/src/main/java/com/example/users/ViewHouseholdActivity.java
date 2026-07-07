package com.example.users;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ViewHouseholdActivity extends BaseUserActivity {

    // Views
    private ImageView ivProfileImage;
    private TextView tvHouseholdId, tvHeadName;
    private TextView tvAddress, tvVillage, tvTaluka, tvDistrict, tvState, tvPincode;
    private TextView tvMobile, tvAltMobile, tvEmail;
    private TextView tvCaste, tvIncome, tvOccupation, tvHouseType, tvBPLStatus;
    private TextView tvAccountHolder, tvBankName, tvAccountNumber, tvIfscCode;
    private TextView tvElectricity, tvWater, tvGas, tvToilet;
    private ImageButton btnBack;

    private OkHttpClient client;
    private UserSessionManager sessionManager;
    private static final String BASE_URL = "http://192.168.147.188/grampanchayat_api/";
    private static final String IMAGE_BASE_URL = "http://192.168.147.188/grampanchayat_api/uploads/";
    private String householdId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_household);

        client = new OkHttpClient();
        sessionManager = new UserSessionManager(this);
        householdId = sessionManager.getHouseholdId();

        initViews();
        setupClickListeners();
        fetchHouseholdDetails();
    }

    private void initViews() {
        ivProfileImage = findViewById(R.id.ivProfileImage);
        tvHouseholdId = findViewById(R.id.tvHouseholdId);
        tvHeadName = findViewById(R.id.tvHeadName);
        tvAddress = findViewById(R.id.tvAddress);
        tvVillage = findViewById(R.id.tvVillage);
        tvTaluka = findViewById(R.id.tvTaluka);
        tvDistrict = findViewById(R.id.tvDistrict);
        tvState = findViewById(R.id.tvState);
        tvPincode = findViewById(R.id.tvPincode);
        tvMobile = findViewById(R.id.tvMobile);
        tvAltMobile = findViewById(R.id.tvAltMobile);
        tvEmail = findViewById(R.id.tvEmail);
        tvCaste = findViewById(R.id.tvCaste);
        tvIncome = findViewById(R.id.tvIncome);
        tvOccupation = findViewById(R.id.tvOccupation);
        tvHouseType = findViewById(R.id.tvHouseType);
        tvBPLStatus = findViewById(R.id.tvBPLStatus);
        tvAccountHolder = findViewById(R.id.tvAccountHolder);
        tvBankName = findViewById(R.id.tvBankName);
        tvAccountNumber = findViewById(R.id.tvAccountNumber);
        tvIfscCode = findViewById(R.id.tvIfscCode);
        tvElectricity = findViewById(R.id.tvElectricity);
        tvWater = findViewById(R.id.tvWater);
        tvGas = findViewById(R.id.tvGas);
        tvToilet = findViewById(R.id.tvToilet);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void fetchHouseholdDetails() {
        String url = BASE_URL + "user_get_household_details.php?household_id=" + householdId;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(ViewHouseholdActivity.this,
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

                            String imageName = data.optString("profile_image", "default_household.png");
                            String imageUrl = IMAGE_BASE_URL + imageName;
                            Glide.with(ViewHouseholdActivity.this)
                                    .load(imageUrl)
                                    .circleCrop()
                                    .placeholder(R.drawable.circle_blue_bg)
                                    .error(R.drawable.circle_blue_bg)
                                    .into(ivProfileImage);

                            tvHouseholdId.setText(householdId);
                            tvHeadName.setText(data.optString("head_name", "N/A"));
                            tvAddress.setText(data.optString("address", "N/A"));
                            tvVillage.setText(data.optString("village", "N/A"));
                            tvTaluka.setText(data.optString("taluka", "N/A"));
                            tvDistrict.setText(data.optString("district", "N/A"));
                            tvState.setText(data.optString("state", "Maharashtra"));
                            tvPincode.setText(data.optString("pincode", "N/A"));
                            tvMobile.setText(data.optString("head_mobile", "N/A"));
                            tvAltMobile.setText(data.optString("alt_mobile", "Not Available"));
                            tvEmail.setText(data.optString("email", "Not Available"));
                            tvCaste.setText(data.optString("caste", "N/A"));
                            tvIncome.setText("₹ " + data.optString("annual_income", "0"));
                            tvOccupation.setText(data.optString("occupation", "Not Specified"));
                            tvHouseType.setText(data.optString("house_type", "N/A"));
                            tvBPLStatus.setText(data.optInt("bpl_status", 0) == 1 ? "Yes" : "No");
                            tvAccountHolder.setText(data.optString("head_name", "N/A"));
                            tvBankName.setText(data.optString("bank_name", "Not Available"));
                            tvAccountNumber.setText(data.optString("account_number", "Not Available"));
                            tvIfscCode.setText(data.optString("ifsc_code", "Not Available"));
                            tvElectricity.setText(data.optInt("electricity", 0) == 1 ? "✅ Yes" : "❌ No");
                            tvWater.setText(data.optInt("water_connection", 0) == 1 ? "✅ Yes" : "❌ No");
                            tvGas.setText(data.optInt("gas_connection", 0) == 1 ? "✅ Yes" : "❌ No");
                            tvToilet.setText(data.optInt("toilet", 0) == 1 ? "✅ Yes" : "❌ No");

                        } else {
                            Toast.makeText(ViewHouseholdActivity.this,
                                    jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(ViewHouseholdActivity.this,
                                "Error loading data", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}