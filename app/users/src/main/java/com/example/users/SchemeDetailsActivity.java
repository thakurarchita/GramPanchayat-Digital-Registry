package com.example.users;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.util.Locale;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
public class SchemeDetailsActivity extends BaseUserActivity {

        private TextView tvSchemeName, tvCategory, tvCategoryBadge;
        private TextView tvBenefitType, tvBenefitAmount, tvBenefitsList;
        private TextView tvEligibility, tvDocuments, tvProcess;
        private TextView tvDepartment, tvApplicationDates;
        private TextView tvMemberName;
        private Button btnApplyNow;
        private ImageButton btnBack;
        private Toolbar toolbar;
        private CardView cardMemberInfo;  // Changed from LinearLayout to CardView

        private String portalUrl = "";

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_scheme_details);

            initViews();
            setupToolbar();
            loadSchemeData();
        }

        private void initViews() {
            tvSchemeName = findViewById(R.id.tvSchemeName);
            tvCategory = findViewById(R.id.tvCategory);
            tvCategoryBadge = findViewById(R.id.tvCategoryBadge);
            tvBenefitType = findViewById(R.id.tvBenefitType);
            tvBenefitAmount = findViewById(R.id.tvBenefitAmount);
            tvBenefitsList = findViewById(R.id.tvBenefitsList);
            tvEligibility = findViewById(R.id.tvEligibility);
            tvDocuments = findViewById(R.id.tvDocuments);
            tvProcess = findViewById(R.id.tvProcess);
            tvDepartment = findViewById(R.id.tvDepartment);
            tvApplicationDates = findViewById(R.id.tvApplicationDates);
            tvMemberName = findViewById(R.id.tvMemberName);
            btnApplyNow = findViewById(R.id.btnApplyNow);
            btnBack = findViewById(R.id.btnBack);
            toolbar = findViewById(R.id.toolbar);
            cardMemberInfo = findViewById(R.id.cardMemberInfo);  // This is a CardView
        }

        private void setupToolbar() {
            setSupportActionBar(toolbar);
            btnBack.setOnClickListener(v -> finish());
        }

        private void loadSchemeData() {
            Intent intent = getIntent();

            String schemeName = intent.getStringExtra("scheme_name");
            String category = intent.getStringExtra("scheme_category");
            String benefitType = intent.getStringExtra("benefit_type");
            double benefitAmount = intent.getDoubleExtra("benefit_amount", 0);
            String department = intent.getStringExtra("department_name");
            String startDate = intent.getStringExtra("start_date");
            String endDate = intent.getStringExtra("end_date");
            String documents = intent.getStringExtra("required_documents");
            String process = intent.getStringExtra("application_process");
            String benefits = intent.getStringExtra("benefits");
            portalUrl = intent.getStringExtra("portal_url");

            int minIncome = intent.getIntExtra("min_income", 0);
            int maxIncome = intent.getIntExtra("max_income", 0);
            int minAge = intent.getIntExtra("min_age", 0);
            int maxAge = intent.getIntExtra("max_age", 0);
            String gender = intent.getStringExtra("gender_restriction");
            String caste = intent.getStringExtra("caste_restriction");

            String memberName = intent.getStringExtra("member_name");
            boolean isIndividual = intent.getBooleanExtra("is_individual", false);

            tvSchemeName.setText(schemeName);
            tvCategory.setText(category != null && category.equals("Household-Based") ? "Household Scheme" : "Individual Scheme");

            if (category != null && category.equals("Household-Based")) {
                tvCategoryBadge.setBackgroundColor(0xFFFF9800);
                tvCategoryBadge.setText("Household");
            } else {
                tvCategoryBadge.setBackgroundColor(0xFF2196F3);
                tvCategoryBadge.setText("Individual");
            }

            tvBenefitType.setText(benefitType != null ? benefitType : "Financial");
            if (benefitAmount > 0) {
                tvBenefitAmount.setText("₹ " + String.format("%,.0f", benefitAmount));
            } else {
                tvBenefitAmount.setText("As per scheme norms");
            }

            if (benefits != null && !benefits.isEmpty()) {
                tvBenefitsList.setText(benefits.replace("\\n", "\n"));
            } else {
                tvBenefitsList.setText("• " + (benefitType != null ? benefitType : "Financial") + " benefit");
            }

            // Eligibility
            StringBuilder eligibility = new StringBuilder();
            if (maxIncome > 0) eligibility.append("• Maximum Income: ₹").append(String.format("%,d", maxIncome)).append("\n");
            if (minIncome > 0) eligibility.append("• Minimum Income: ₹").append(String.format("%,d", minIncome)).append("\n");
            if (minAge > 0 && maxAge > 0) eligibility.append("• Age: ").append(minAge).append(" - ").append(maxAge).append(" years\n");
            else if (minAge > 0) eligibility.append("• Minimum Age: ").append(minAge).append(" years\n");
            else if (maxAge > 0) eligibility.append("• Maximum Age: ").append(maxAge).append(" years\n");
            if (gender != null && !gender.equals("Any") && !gender.isEmpty()) eligibility.append("• Gender: ").append(gender).append("\n");
            if (caste != null && !caste.equals("All") && !caste.isEmpty()) eligibility.append("• Caste: ").append(caste).append("\n");
            if (eligibility.length() == 0) eligibility.append("• As per scheme guidelines");
            tvEligibility.setText(eligibility.toString());

            // Documents
            if (documents != null && !documents.isEmpty()) {
                tvDocuments.setText(documents.replace(",", "\n• "));
                if (!tvDocuments.getText().toString().startsWith("•")) {
                    tvDocuments.setText("• " + tvDocuments.getText().toString());
                }
            } else {
                tvDocuments.setText("• Contact department office");
            }

            // Process
            if (process != null && !process.isEmpty()) {
                tvProcess.setText(process.replace("\\n", "\n"));
            } else {
                tvProcess.setText("1. Visit department office\n2. Fill application form\n3. Submit documents\n4. Get approval");
            }

            tvDepartment.setText(department != null ? department : "Concerned Department");

            if (startDate != null && endDate != null && !startDate.isEmpty() && !endDate.isEmpty()) {
                tvApplicationDates.setText(formatDate(startDate) + " - " + formatDate(endDate));
            } else {
                tvApplicationDates.setText("Open throughout the year");
            }

            if (isIndividual && memberName != null && !memberName.isEmpty()) {
                cardMemberInfo.setVisibility(View.VISIBLE);
                tvMemberName.setText(memberName);
            }

            btnApplyNow.setOnClickListener(v -> {
                if (portalUrl != null && !portalUrl.isEmpty()) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(portalUrl)));
                } else {
                    Toast.makeText(SchemeDetailsActivity.this,
                            "Portal URL not available. Visit department office.", Toast.LENGTH_LONG).show();
                }
            });
        }

        private String formatDate(String date) {
            try {
                SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat output = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                return output.format(input.parse(date));
            } catch (Exception e) {
                return date;
            }
        }
    }