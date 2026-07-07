package com.example.admin;

import android.content.Intent;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import okhttp3.*;

public class AdminReportsActivity extends AppCompatActivity {

    private TextView tvTotalHouseholdsCard, tvTotalMembersCard;
    private TextView tvMaleCount, tvFemaleCount, tvOtherCount;
    private TextView tvMalePercent, tvFemalePercent, tvOtherPercent;
    private View viewMaleBar, viewFemaleBar;
    private TextView tvBPLCount, tvAPLCount, tvBPLPercentage;
    private TextView tvGeneralCount, tvOBCCount, tvSCCount, tvSTCount, tvOtherCasteCount;
    private TextView tvAge0to18, tvAge19to35, tvAge36to60, tvAge60plus;
    private TextView tvPuccaCount, tvSemiPuccaCount, tvKutchaCount, tvApartmentCount;
    private TextView tvElectricityPercent, tvWaterPercent, tvGasPercent;
    private Button btnExport;
    private ProgressBar progressBar;
    private ImageButton btnBack;
    private Toolbar toolbar;

    private OkHttpClient client;
    private AdminSessionManager sessionManager;
    private static final String BASE_URL = "http://192.168.147.188/grampanchayat_api/";

    // Store statistics data for export
    private JSONObject statisticsData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_reports);

        client = new OkHttpClient();
        sessionManager = new AdminSessionManager(this);

        initViews();
        setupToolbar();
        setupClickListeners();  // Add this line
        fetchStatistics();
    }

    private void initViews() {
        tvTotalHouseholdsCard = findViewById(R.id.tvTotalHouseholdsCard);
        tvTotalMembersCard = findViewById(R.id.tvTotalMembersCard);

        tvMaleCount = findViewById(R.id.tvMaleCount);
        tvFemaleCount = findViewById(R.id.tvFemaleCount);
        tvOtherCount = findViewById(R.id.tvOtherCount);
        tvMalePercent = findViewById(R.id.tvMalePercent);
        tvFemalePercent = findViewById(R.id.tvFemalePercent);
        tvOtherPercent = findViewById(R.id.tvOtherPercent);
        viewMaleBar = findViewById(R.id.viewMaleBar);
        viewFemaleBar = findViewById(R.id.viewFemaleBar);

        tvBPLCount = findViewById(R.id.tvBPLCount);
        tvAPLCount = findViewById(R.id.tvAPLCount);
        tvBPLPercentage = findViewById(R.id.tvBPLPercentage);

        tvGeneralCount = findViewById(R.id.tvGeneralCount);
        tvOBCCount = findViewById(R.id.tvOBCCount);
        tvSCCount = findViewById(R.id.tvSCCount);
        tvSTCount = findViewById(R.id.tvSTCount);
        tvOtherCasteCount = findViewById(R.id.tvOtherCasteCount);

        tvAge0to18 = findViewById(R.id.tvAge0to18);
        tvAge19to35 = findViewById(R.id.tvAge19to35);
        tvAge36to60 = findViewById(R.id.tvAge36to60);
        tvAge60plus = findViewById(R.id.tvAge60plus);

        tvPuccaCount = findViewById(R.id.tvPuccaCount);
        tvSemiPuccaCount = findViewById(R.id.tvSemiPuccaCount);
        tvKutchaCount = findViewById(R.id.tvKutchaCount);
        tvApartmentCount = findViewById(R.id.tvApartmentCount);

        tvElectricityPercent = findViewById(R.id.tvElectricityPercent);
        tvWaterPercent = findViewById(R.id.tvWaterPercent);
        tvGasPercent = findViewById(R.id.tvGasPercent);

        btnExport = findViewById(R.id.btnExport);
        progressBar = findViewById(R.id.progressBar);
        btnBack = findViewById(R.id.btnBack);
        toolbar = findViewById(R.id.toolbar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupClickListeners() {
        btnExport.setOnClickListener(v -> exportToPDF());
    }

    private void fetchStatistics() {
        showProgress(true);

        String url = BASE_URL + "get_village_statistics.php";

        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(AdminReportsActivity.this,
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
                            statisticsData = jsonResponse.getJSONObject("data");
                            updateUI(statisticsData);
                        } else {
                            Toast.makeText(AdminReportsActivity.this,
                                    jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(AdminReportsActivity.this,
                                "Error loading statistics", Toast.LENGTH_SHORT).show();
                    } finally {
                        showProgress(false);
                    }
                });
            }
        });
    }

    private void updateUI(JSONObject data) throws Exception {
        // Basic Counts
        tvTotalHouseholdsCard.setText(String.valueOf(data.getInt("total_households")));
        tvTotalMembersCard.setText(String.valueOf(data.getInt("total_members")));

        // Gender Statistics
        JSONObject gender = data.getJSONObject("gender");
        int male = gender.getInt("male");
        int female = gender.getInt("female");
        int other = gender.getInt("other");
        int total = male + female + other;

        tvMaleCount.setText(String.valueOf(male));
        tvFemaleCount.setText(String.valueOf(female));
        tvOtherCount.setText(String.valueOf(other));

        if (total > 0) {
            tvMalePercent.setText(String.format("%.1f%%", (male * 100.0 / total)));
            tvFemalePercent.setText(String.format("%.1f%%", (female * 100.0 / total)));
            tvOtherPercent.setText(String.format("%.1f%%", (other * 100.0 / total)));

            LinearLayout.LayoutParams maleParams = (LinearLayout.LayoutParams) viewMaleBar.getLayoutParams();
            maleParams.weight = (float) (male * 100.0 / total);
            viewMaleBar.setLayoutParams(maleParams);

            LinearLayout.LayoutParams femaleParams = (LinearLayout.LayoutParams) viewFemaleBar.getLayoutParams();
            femaleParams.weight = (float) (female * 100.0 / total);
            viewFemaleBar.setLayoutParams(femaleParams);
        }

        // Economic Status
        JSONObject economic = data.getJSONObject("economic");
        tvBPLCount.setText(String.valueOf(economic.getInt("bpl")));
        tvAPLCount.setText(String.valueOf(economic.getInt("apl")));
        int bpl = economic.getInt("bpl");
        int apl = economic.getInt("apl");
        if (bpl + apl > 0) {
            tvBPLPercentage.setText(String.format("%.1f%%", (bpl * 100.0 / (bpl + apl))));
        }

        // Caste Distribution
        JSONObject caste = data.getJSONObject("caste");
        tvGeneralCount.setText(String.valueOf(caste.getInt("general")));
        tvOBCCount.setText(String.valueOf(caste.getInt("obc")));
        tvSCCount.setText(String.valueOf(caste.getInt("sc")));
        tvSTCount.setText(String.valueOf(caste.getInt("st")));
        tvOtherCasteCount.setText(String.valueOf(caste.getInt("others")));

        // Age Distribution
        JSONObject age = data.getJSONObject("age");
        tvAge0to18.setText(String.valueOf(age.getInt("0_18")));
        tvAge19to35.setText(String.valueOf(age.getInt("19_35")));
        tvAge36to60.setText(String.valueOf(age.getInt("36_60")));
        tvAge60plus.setText(String.valueOf(age.getInt("60_plus")));

        // Housing Type
        JSONObject housing = data.getJSONObject("housing");
        tvPuccaCount.setText(String.valueOf(housing.getInt("pucca")));
        tvSemiPuccaCount.setText(String.valueOf(housing.getInt("semi_pucca")));
        tvKutchaCount.setText(String.valueOf(housing.getInt("kutcha")));
        tvApartmentCount.setText(String.valueOf(housing.getInt("apartment")));

        // Facilities Coverage
        JSONObject facilities = data.getJSONObject("facilities");
        tvElectricityPercent.setText(String.format("%.1f%%", facilities.getDouble("electricity")));
        tvWaterPercent.setText(String.format("%.1f%%", facilities.getDouble("water")));
        tvGasPercent.setText(String.format("%.1f%%", facilities.getDouble("gas")));
    }

    private void exportToPDF() {
        if (statisticsData == null) {
            Toast.makeText(this, "No data to export. Please wait for data to load.", Toast.LENGTH_SHORT).show();
            return;
        }

        showProgress(true);

        try {
            // Create PDF document
            PdfDocument pdfDocument = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create(); // A4 size
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);

            android.graphics.Canvas canvas = page.getCanvas();
            android.graphics.Paint paint = new android.graphics.Paint();
            paint.setColor(android.graphics.Color.BLACK);
            paint.setTextSize(14);

            float x = 50;
            float y = 80;
            int lineHeight = 30;

            // Title
            paint.setTextSize(22);
            paint.setFakeBoldText(true);
            canvas.drawText("Gram Panchayat Village Statistics Report", x, y, paint);
            y += lineHeight + 10;

            // Date
            paint.setTextSize(12);
            paint.setFakeBoldText(false);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
            canvas.drawText("Generated on: " + sdf.format(new Date()), x, y, paint);
            y += lineHeight + 10;

            // Separator
            paint.setStrokeWidth(2);
            canvas.drawLine(x, y, 545, y, paint);
            y += lineHeight;

            // Basic Statistics
            paint.setTextSize(14);
            paint.setFakeBoldText(true);
            canvas.drawText("BASIC STATISTICS", x, y, paint);
            y += lineHeight;
            paint.setFakeBoldText(false);

            canvas.drawText("Total Households: " + tvTotalHouseholdsCard.getText().toString(), x + 20, y, paint);
            y += lineHeight;
            canvas.drawText("Total Members: " + tvTotalMembersCard.getText().toString(), x + 20, y, paint);
            y += lineHeight + 10;

            // Gender Distribution
            paint.setFakeBoldText(true);
            canvas.drawText("GENDER DISTRIBUTION", x, y, paint);
            y += lineHeight;
            paint.setFakeBoldText(false);

            canvas.drawText("Male: " + tvMaleCount.getText().toString() + " (" + tvMalePercent.getText().toString() + ")", x + 20, y, paint);
            y += lineHeight;
            canvas.drawText("Female: " + tvFemaleCount.getText().toString() + " (" + tvFemalePercent.getText().toString() + ")", x + 20, y, paint);
            y += lineHeight;
            canvas.drawText("Other: " + tvOtherCount.getText().toString() + " (" + tvOtherPercent.getText().toString() + ")", x + 20, y, paint);
            y += lineHeight + 10;

            // Economic Status
            paint.setFakeBoldText(true);
            canvas.drawText("ECONOMIC STATUS", x, y, paint);
            y += lineHeight;
            paint.setFakeBoldText(false);

            canvas.drawText("BPL Families: " + tvBPLCount.getText().toString() + " (" + tvBPLPercentage.getText().toString() + ")", x + 20, y, paint);
            y += lineHeight;
            canvas.drawText("APL Families: " + tvAPLCount.getText().toString(), x + 20, y, paint);
            y += lineHeight + 10;

            // Caste Distribution
            paint.setFakeBoldText(true);
            canvas.drawText("CASTE DISTRIBUTION", x, y, paint);
            y += lineHeight;
            paint.setFakeBoldText(false);

            canvas.drawText("General: " + tvGeneralCount.getText().toString(), x + 20, y, paint);
            y += lineHeight;
            canvas.drawText("OBC: " + tvOBCCount.getText().toString(), x + 20, y, paint);
            y += lineHeight;
            canvas.drawText("SC: " + tvSCCount.getText().toString(), x + 20, y, paint);
            y += lineHeight;
            canvas.drawText("ST: " + tvSTCount.getText().toString(), x + 20, y, paint);
            y += lineHeight;
            canvas.drawText("Others: " + tvOtherCasteCount.getText().toString(), x + 20, y, paint);
            y += lineHeight + 10;

            // Age Distribution
            paint.setFakeBoldText(true);
            canvas.drawText("AGE DISTRIBUTION", x, y, paint);
            y += lineHeight;
            paint.setFakeBoldText(false);

            canvas.drawText("0-18 Years: " + tvAge0to18.getText().toString(), x + 20, y, paint);
            y += lineHeight;
            canvas.drawText("19-35 Years: " + tvAge19to35.getText().toString(), x + 20, y, paint);
            y += lineHeight;
            canvas.drawText("36-60 Years: " + tvAge36to60.getText().toString(), x + 20, y, paint);
            y += lineHeight;
            canvas.drawText("60+ Years: " + tvAge60plus.getText().toString(), x + 20, y, paint);
            y += lineHeight + 10;

            // Housing Type
            paint.setFakeBoldText(true);
            canvas.drawText("HOUSING TYPE", x, y, paint);
            y += lineHeight;
            paint.setFakeBoldText(false);

            canvas.drawText("Pucca: " + tvPuccaCount.getText().toString(), x + 20, y, paint);
            y += lineHeight;
            canvas.drawText("Semi-Pucca: " + tvSemiPuccaCount.getText().toString(), x + 20, y, paint);
            y += lineHeight;
            canvas.drawText("Kutcha: " + tvKutchaCount.getText().toString(), x + 20, y, paint);
            y += lineHeight;
            canvas.drawText("Apartment: " + tvApartmentCount.getText().toString(), x + 20, y, paint);
            y += lineHeight + 10;

            // Facilities Coverage
            paint.setFakeBoldText(true);
            canvas.drawText("FACILITIES COVERAGE", x, y, paint);
            y += lineHeight;
            paint.setFakeBoldText(false);

            canvas.drawText("Electricity: " + tvElectricityPercent.getText().toString(), x + 20, y, paint);
            y += lineHeight;
            canvas.drawText("Water Connection: " + tvWaterPercent.getText().toString(), x + 20, y, paint);
            y += lineHeight;
            canvas.drawText("Gas Connection: " + tvGasPercent.getText().toString(), x + 20, y, paint);

            pdfDocument.finishPage(page);

            // Save PDF
            String fileName = "GramPanchayat_Report_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".pdf";
            File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File pdfFile = new File(downloadsFolder, fileName);

            pdfDocument.writeTo(new FileOutputStream(pdfFile));
            pdfDocument.close();

            showProgress(false);
            Toast.makeText(this, "PDF saved to Downloads folder: " + fileName, Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            showProgress(false);
            e.printStackTrace();
            Toast.makeText(this, "Error creating PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showProgress(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (btnExport != null) {
            btnExport.setEnabled(!show);
        }
    }
}