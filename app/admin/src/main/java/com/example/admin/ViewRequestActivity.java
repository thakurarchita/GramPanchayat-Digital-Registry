package com.example.admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import okhttp3.*;

public class ViewRequestActivity extends AppCompatActivity {

    private ListView listViewRequests;
    private Button btnAll, btnBirth, btnDeath, btnCorrection;
    private ImageButton btnBack;
    private ProgressBar progressBar;
    private Toolbar toolbar;

    private OkHttpClient client;
    private AdminSessionManager sessionManager;
    private static final String BASE_URL = "http://192.168.147.188/grampanchayat_api/";
    private List<RequestItem> requestList = new ArrayList<>();
    private String currentFilter = "all";

    private class RequestItem {
        String requestId, requestType, requestStatus, householdId, headName;
        String requestDate, requestData;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_request);

        client = new OkHttpClient();
        sessionManager = new AdminSessionManager(this);

        initViews();
        setupToolbar();
        setupClickListeners();
        fetchRequests();
    }

    private void initViews() {
        listViewRequests = findViewById(R.id.listViewRequests);
        btnAll = findViewById(R.id.btnAll);
        btnBirth = findViewById(R.id.btnBirth);
        btnDeath = findViewById(R.id.btnDeath);
        btnCorrection = findViewById(R.id.btnCorrection);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);
        toolbar = findViewById(R.id.toolbar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupClickListeners() {
        btnAll.setOnClickListener(v -> filterRequests("all"));
        btnBirth.setOnClickListener(v -> filterRequests("birth"));
        btnDeath.setOnClickListener(v -> filterRequests("death"));
        btnCorrection.setOnClickListener(v -> filterRequests("correction"));
    }

    private void filterRequests(String type) {
        currentFilter = type;
        fetchRequests();

        resetButtonStyles();

        if (type.equals("all"))
            btnAll.setBackgroundTintList(getColorStateList(android.R.color.holo_orange_dark));
        else if (type.equals("birth"))
            btnBirth.setBackgroundTintList(getColorStateList(android.R.color.holo_orange_dark));
        else if (type.equals("death"))
            btnDeath.setBackgroundTintList(getColorStateList(android.R.color.holo_orange_dark));
        else
            btnCorrection.setBackgroundTintList(getColorStateList(android.R.color.holo_orange_dark));
    }

    private void resetButtonStyles() {
        btnAll.setBackgroundTintList(getColorStateList(android.R.color.darker_gray));
        btnBirth.setBackgroundTintList(getColorStateList(android.R.color.darker_gray));
        btnDeath.setBackgroundTintList(getColorStateList(android.R.color.darker_gray));
        btnCorrection.setBackgroundTintList(getColorStateList(android.R.color.darker_gray));
    }

    private void fetchRequests() {
        showProgress(true);

        String url = BASE_URL + "get_all_requests.php?type=" + currentFilter;

        Log.d("API_URL", url);  // Debug

        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(ViewRequestActivity.this,
                            "Network error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();

                Log.d("API_RESPONSE", responseBody); // Debug

                runOnUiThread(() -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);

                        if (jsonResponse.optBoolean("success")) {

                            JSONArray requests = jsonResponse.optJSONArray("requests");
                            requestList.clear();

                            if (requests != null) {
                                for (int i = 0; i < requests.length(); i++) {

                                    JSONObject obj = requests.getJSONObject(i);

                                    RequestItem item = new RequestItem();
                                    item.requestId = obj.optString("request_id", "");
                                    item.requestType = obj.optString("request_type", "");
                                    item.requestStatus = obj.optString("status", "pending");
                                    item.householdId = obj.optString("household_id", "");
                                    item.headName = obj.optString("head_name", "");
                                    item.requestDate = obj.optString("created_at", "");
                                    item.requestData = obj.optString("request_data", "{}");

                                    requestList.add(item);
                                }
                            }

                            displayRequests();

                        } else {
                            Toast.makeText(ViewRequestActivity.this,
                                    jsonResponse.optString("message", "Error"),
                                    Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(ViewRequestActivity.this,
                                "Error parsing data", Toast.LENGTH_SHORT).show();
                    } finally {
                        showProgress(false);
                    }
                });
            }
        });
    }

    private void displayRequests() {

        if (requestList.isEmpty()) {
            Toast.makeText(this, "No requests found", Toast.LENGTH_SHORT).show();
        }

        ArrayAdapter<RequestItem> adapter = new ArrayAdapter<RequestItem>(this,
                android.R.layout.simple_list_item_1, requestList) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext())
                            .inflate(R.layout.item_request_list, parent, false);
                }

                RequestItem item = requestList.get(position);

                TextView tvRequestId = convertView.findViewById(R.id.tvRequestId);
                TextView tvRequestType = convertView.findViewById(R.id.tvRequestType);
                TextView tvHouseholdId = convertView.findViewById(R.id.tvHouseholdId);
                TextView tvStatus = convertView.findViewById(R.id.tvStatus);
                TextView tvDate = convertView.findViewById(R.id.tvDate);

                tvRequestId.setText(item.requestId);

                // Type badge
                String typeText = item.requestType.toUpperCase();
                int typeColor;

                switch (item.requestType) {
                    case "birth":
                        typeColor = getColor(android.R.color.holo_green_light);
                        break;
                    case "death":
                        typeColor = getColor(android.R.color.holo_red_light);
                        break;
                    default:
                        typeColor = getColor(android.R.color.holo_orange_light);
                }

                tvRequestType.setText(typeText);
                tvRequestType.setBackgroundColor(typeColor);

                tvHouseholdId.setText("Household: " + item.householdId);

                // Status
                if ("approved".equals(item.requestStatus)) {
                    tvStatus.setText("Approved");
                    tvStatus.setTextColor(getColor(android.R.color.holo_green_light));
                } else if ("rejected".equals(item.requestStatus)) {
                    tvStatus.setText("Rejected");
                    tvStatus.setTextColor(getColor(android.R.color.holo_red_light));
                } else {
                    tvStatus.setText("Pending");
                    tvStatus.setTextColor(getColor(android.R.color.holo_orange_light));
                }

                // Date format
                try {
                    SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    SimpleDateFormat out = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    Date date = in.parse(item.requestDate);
                    tvDate.setText("Requested on: " + out.format(date));
                } catch (Exception e) {
                    tvDate.setText("Requested on: " + item.requestDate);
                }

                convertView.setOnClickListener(v -> {
                    Intent intent = new Intent(ViewRequestActivity.this, RequestDetailsActivity.class);
                    intent.putExtra("request_id", item.requestId);
                    intent.putExtra("request_type", item.requestType);
                    intent.putExtra("household_id", item.householdId);
                    intent.putExtra("head_name", item.headName);
                    intent.putExtra("request_date", item.requestDate);
                    intent.putExtra("request_status", item.requestStatus);
                    intent.putExtra("request_data", item.requestData);
                    startActivity(intent);
                });

                return convertView;
            }
        };

        listViewRequests.setAdapter(adapter);
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchRequests();
    }
}
