package com.example.users;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
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
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ViewMemberDetailsActivity extends BaseUserActivity {

    private ImageView ivMemberImage;
    private TextView tvMemberName, tvRelationBadge;
    private TextView tvDetailName, tvDetailAadhaar, tvDetailAge, tvDetailDob;
    private TextView tvDetailGender, tvDetailRelation, tvDetailMarital;
    private TextView tvDetailEducation, tvDetailOccupation;
    private TextView tvDetailHouseholdId, tvDetailHeadName;
    private ImageButton btnBack;

    private OkHttpClient client;
    private static final String BASE_URL = "http://192.168.147.188/grampanchayat_api/";
    private static final String IMAGE_BASE_URL = "http://192.168.147.188/grampanchayat_api/uploads/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_member_details);

        client = new OkHttpClient();
        initViews();
        setupClickListeners();

        // Get member ID from intent
        String memberId = getIntent().getStringExtra("member_id");

        if (memberId != null && !memberId.isEmpty()) {
            fetchMemberDetailsFromApi(memberId);
        } else {
            displayFromIntent();
        }
    }

    private void fetchMemberDetailsFromApi(String memberId) {
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("member_id", memberId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonRequest.toString(), JSON);

        Request request = new Request.Builder()
                .url(BASE_URL + "user_get_member_details.php")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(ViewMemberDetailsActivity.this,
                            "Network error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
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
                            displayMemberDetails(data);
                        } else {
                            Toast.makeText(ViewMemberDetailsActivity.this,
                                    jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(ViewMemberDetailsActivity.this,
                                "Error loading data", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }
        });
    }

    private void displayFromIntent() {
        String name = getIntent().getStringExtra("member_name");
        String age = getIntent().getStringExtra("member_age");
        String gender = getIntent().getStringExtra("member_gender");
        String relation = getIntent().getStringExtra("member_relation");
        String education = getIntent().getStringExtra("member_education");
        String occupation = getIntent().getStringExtra("member_occupation");
        String aadhaar = getIntent().getStringExtra("member_aadhaar");
        String dob = getIntent().getStringExtra("member_dob");
        String marital = getIntent().getStringExtra("member_marital");
        String householdId = getIntent().getStringExtra("household_id");
        String headName = getIntent().getStringExtra("head_name");

        displayMemberDetails(name, age, gender, relation, education,
                occupation, aadhaar, dob, marital, householdId, headName);
    }

    private void displayMemberDetails(JSONObject data) throws Exception {
        String name = data.getString("member_name");
        String age = String.valueOf(data.getInt("age"));
        String gender = data.getString("gender");
        String relation = data.getString("relationship");
        String education = data.optString("education", "Not Available");
        String occupation = data.optString("occupation", "Not Available");
        String aadhaar = data.optString("member_aadhaar", "Not Available");
        String dob = data.optString("date_of_birth", "Not Available");
        String marital = data.optString("marital_status", "Not Available");
        String householdId = data.getString("household_id");
        String headName = data.getString("head_name");
        String profileImage = data.optString("profile_image", "");

        displayMemberDetails(name, age, gender, relation, education,
                occupation, aadhaar, dob, marital, householdId, headName, profileImage);
    }

    private void displayMemberDetails(String name, String age, String gender, String relation,
                                      String education, String occupation, String aadhaar,
                                      String dob, String marital, String householdId, String headName) {
        displayMemberDetails(name, age, gender, relation, education, occupation,
                aadhaar, dob, marital, householdId, headName, "");
    }

    private void displayMemberDetails(String name, String age, String gender, String relation,
                                      String education, String occupation, String aadhaar,
                                      String dob, String marital, String householdId,
                                      String headName, String profileImage) {

        // Load member image
        if (profileImage != null && !profileImage.isEmpty()) {
            String imageUrl = IMAGE_BASE_URL + profileImage;
            Glide.with(this)
                    .load(imageUrl)
                    .circleCrop()
                    .placeholder(R.drawable.circle_blue_bg)
                    .error(R.drawable.circle_blue_bg)
                    .into(ivMemberImage);
        } else {
            ivMemberImage.setImageResource(R.drawable.circle_blue_bg);
        }

        tvMemberName.setText(name);
        tvRelationBadge.setText(relation);

        tvDetailName.setText(name);
        tvDetailAadhaar.setText(aadhaar != null && !aadhaar.isEmpty() && !aadhaar.equals("Not Available") ? aadhaar : "Not Available");
        tvDetailAge.setText(age + " years");
        tvDetailDob.setText(dob != null && !dob.isEmpty() && !dob.equals("Not Available") ? dob : "Not Available");
        tvDetailGender.setText(gender);
        tvDetailRelation.setText(relation);
        tvDetailMarital.setText(marital != null && !marital.isEmpty() && !marital.equals("Not Available") ? marital : "Not Available");
        tvDetailEducation.setText(education != null && !education.isEmpty() && !education.equals("Not Available") ? education : "Not Available");
        tvDetailOccupation.setText(occupation != null && !occupation.isEmpty() && !occupation.equals("Not Available") ? occupation : "Not Available");
        tvDetailHouseholdId.setText(householdId != null ? householdId : "N/A");
        tvDetailHeadName.setText(headName != null ? headName : "N/A");
    }

    private void initViews() {
        ivMemberImage = findViewById(R.id.ivMemberImage);
        tvMemberName = findViewById(R.id.tvMemberName);
        tvRelationBadge = findViewById(R.id.tvRelationBadge);
        tvDetailName = findViewById(R.id.tvDetailName);
        tvDetailAadhaar = findViewById(R.id.tvDetailAadhaar);
        tvDetailAge = findViewById(R.id.tvDetailAge);
        tvDetailDob = findViewById(R.id.tvDetailDob);
        tvDetailGender = findViewById(R.id.tvDetailGender);
        tvDetailRelation = findViewById(R.id.tvDetailRelation);
        tvDetailMarital = findViewById(R.id.tvDetailMarital);
        tvDetailEducation = findViewById(R.id.tvDetailEducation);
        tvDetailOccupation = findViewById(R.id.tvDetailOccupation);
        tvDetailHouseholdId = findViewById(R.id.tvDetailHouseholdId);
        tvDetailHeadName = findViewById(R.id.tvDetailHeadName);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }
}