package com.example.users;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.bumptech.glide.Glide;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ViewFamilyMembersActivity extends BaseUserActivity {

    private TextView tvHouseholdInfo, tvTotalMembers;
    private LinearLayout layoutMembersContainer;
    private ImageButton btnBack;

    private OkHttpClient client;
    private UserSessionManager sessionManager;
    private static final String BASE_URL = "http://192.168.147.188/grampanchayat_api/";
    private static final String IMAGE_BASE_URL = "http://192.168.147.188/grampanchayat_api/uploads/";
    private String householdId;
    private String headName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_family_members);

        client = new OkHttpClient();
        sessionManager = new UserSessionManager(this);
        householdId = sessionManager.getHouseholdId();

        initViews();
        setupClickListeners();
        fetchMembers();
    }

    private void initViews() {
        tvHouseholdInfo = findViewById(R.id.tvHouseholdInfo);
        tvTotalMembers = findViewById(R.id.tvTotalMembers);
        layoutMembersContainer = findViewById(R.id.layoutMembersContainer);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void fetchMembers() {
        String url = BASE_URL + "user_get_family_members.php?household_id=" + householdId;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(ViewFamilyMembersActivity.this,
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
                            headName = data.getString("head_name");
                            JSONArray members = data.getJSONArray("members");

                            tvHouseholdInfo.setText(headName);
                            tvTotalMembers.setText("Total Members: " + members.length());

                            displayMembers(members);
                        } else {
                            Toast.makeText(ViewFamilyMembersActivity.this,
                                    jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(ViewFamilyMembersActivity.this,
                                "Error loading members", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void displayMembers(JSONArray members) throws Exception {
        layoutMembersContainer.removeAllViews();

        for (int i = 0; i < members.length(); i++) {
            JSONObject member = members.getJSONObject(i);

            final String memberId = member.optString("member_id", "");
            final String name = member.getString("member_name");
            final int age = member.getInt("age");
            final String gender = member.getString("gender");
            final String relationship = member.getString("relationship");
            final String education = member.optString("education", "Not specified");
            final String occupation = member.optString("occupation", "Not specified");
            final String aadhaar = member.optString("member_aadhaar", "");
            final String dob = member.optString("date_of_birth", "");
            final String marital = member.optString("marital_status", "");
            final String profileImage = member.optString("profile_image", "");

            // Create member card
            CardView memberCard = new CardView(this);
            CardView.LayoutParams cardParams = new CardView.LayoutParams(
                    CardView.LayoutParams.MATCH_PARENT,
                    CardView.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(0, 0, 0, 16);
            memberCard.setLayoutParams(cardParams);
            memberCard.setRadius(12);
            memberCard.setCardElevation(4);
            memberCard.setCardBackgroundColor(getColor(android.R.color.white));
            memberCard.setClickable(true);
            memberCard.setFocusable(true);

            // Set click listener to open member details
            memberCard.setOnClickListener(v -> {
                Intent intent = new Intent(ViewFamilyMembersActivity.this, ViewMemberDetailsActivity.class);
                intent.putExtra("member_id", memberId);
                intent.putExtra("member_name", name);
                intent.putExtra("member_age", String.valueOf(age));
                intent.putExtra("member_gender", gender);
                intent.putExtra("member_relation", relationship);
                intent.putExtra("member_education", education);
                intent.putExtra("member_occupation", occupation);
                intent.putExtra("member_aadhaar", aadhaar);
                intent.putExtra("member_dob", dob);
                intent.putExtra("member_marital", marital);
                intent.putExtra("household_id", householdId);
                intent.putExtra("head_name", headName);
                startActivity(intent);
            });

            LinearLayout cardContent = new LinearLayout(this);
            cardContent.setOrientation(LinearLayout.HORIZONTAL);
            cardContent.setPadding(16, 16, 16, 16);

            // Member Image (instead of emoji)
            ImageView ivMemberImage = new ImageView(this);
            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(60, 60);
            imageParams.setMargins(0, 0, 16, 0);
            ivMemberImage.setLayoutParams(imageParams);
            ivMemberImage.setScaleType(ImageView.ScaleType.CENTER_CROP);

            String imageUrl = IMAGE_BASE_URL + profileImage;
            if (profileImage != null && !profileImage.isEmpty()) {
                Glide.with(this)
                        .load(imageUrl)
                        .circleCrop()
                        .placeholder(R.drawable.circle_blue_bg)
                        .error(R.drawable.circle_blue_bg)
                        .into(ivMemberImage);
            } else {
                // Fallback to emoji if no image
                ivMemberImage.setImageResource(R.drawable.circle_blue_bg);
            }

            // Details container
            LinearLayout detailsContainer = new LinearLayout(this);
            detailsContainer.setOrientation(LinearLayout.VERTICAL);
            detailsContainer.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

            TextView tvName = new TextView(this);
            tvName.setText(name);
            tvName.setTextSize(16);
            tvName.setTypeface(tvName.getTypeface(), android.graphics.Typeface.BOLD);
            tvName.setTextColor(getColor(android.R.color.black));

            TextView tvDetails = new TextView(this);
            tvDetails.setText("Age: " + age + " | " + gender + " | " + relationship);
            tvDetails.setTextSize(14);
            tvDetails.setTextColor(getColor(android.R.color.darker_gray));

            TextView tvEducation = new TextView(this);
            tvEducation.setText("📚 Education: " + education);
            tvEducation.setTextSize(12);
            tvEducation.setTextColor(getColor(android.R.color.darker_gray));

            detailsContainer.addView(tvName);
            detailsContainer.addView(tvDetails);
            detailsContainer.addView(tvEducation);

            cardContent.addView(ivMemberImage);
            cardContent.addView(detailsContainer);

            // Add "Head" badge for head of family
            if (relationship.equalsIgnoreCase("Self")) {
                TextView tvBadge = new TextView(this);
                tvBadge.setText("Head");
                tvBadge.setTextSize(12);
                tvBadge.setTextColor(Color.WHITE);

                GradientDrawable badgeBg = new GradientDrawable();
                badgeBg.setColor(getColor(R.color.blue_primary));
                badgeBg.setCornerRadius(20);
                tvBadge.setBackground(badgeBg);
                tvBadge.setPadding(16, 8, 16, 8);

                cardContent.addView(tvBadge);
            }

            // Add chevron arrow indicator
            TextView tvArrow = new TextView(this);
            tvArrow.setText("›");
            tvArrow.setTextSize(24);
            tvArrow.setTextColor(getColor(android.R.color.darker_gray));
            tvArrow.setPadding(16, 0, 0, 0);
            cardContent.addView(tvArrow);

            memberCard.addView(cardContent);
            layoutMembersContainer.addView(memberCard);
        }
    }
}