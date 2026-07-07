package com.example.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SchemeListActivity extends AppCompatActivity {

    private RecyclerView rvSchemes;
    private SchemeAdapter adapter;
    private List<Scheme> schemeList = new ArrayList<>();
    private MaterialButton btnAdd;
    private OkHttpClient client;
    private static final String BASE_URL = "http://192.168.147.188/grampanchayat_api/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheme_list);

        client = new OkHttpClient();

        rvSchemes = findViewById(R.id.rvSchemes);
        btnAdd = findViewById(R.id.btnAddNewScheme);

        rvSchemes.setLayoutManager(new LinearLayoutManager(this));

        adapter = new SchemeAdapter(schemeList, scheme -> {
            Intent intent = new Intent(SchemeListActivity.this, SchemeFormActivity.class);
            intent.putExtra("scheme_id", scheme.getScheme_id());
            startActivity(intent);
        });

        rvSchemes.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(SchemeListActivity.this, SchemeFormActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSchemes();
    }

    private void loadSchemes() {
        String url = BASE_URL + "get_all_schemes.php";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(SchemeListActivity.this, "Network Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                runOnUiThread(() -> {
                    try {
                        JSONObject json = new JSONObject(responseBody);
                        if (json.getBoolean("success")) {
                            JSONArray data = json.getJSONArray("data");
                            schemeList.clear();
                            for (int i = 0; i < data.length(); i++) {
                                JSONObject obj = data.getJSONObject(i);
                                Scheme scheme = new Scheme();
                                scheme.setScheme_id(obj.getString("scheme_id"));
                                scheme.setScheme_name(obj.getString("scheme_name"));
                                scheme.setScheme_category(obj.optString("scheme_category"));
                                scheme.setDepartment_name(obj.optString("department_name"));
                                scheme.setDescription(obj.optString("description"));
                                scheme.setBenefit_type(obj.optString("benefit_type"));
                                scheme.setBenefit_amount(obj.optString("benefit_amount"));
                                scheme.setApplication_start_date(obj.optString("application_start_date"));
                                scheme.setApplication_end_date(obj.optString("application_end_date"));
                                scheme.setRequired_documents(obj.optString("required_documents"));
                                scheme.setMin_income(obj.optString("min_income"));
                                scheme.setMax_income(obj.optString("max_income"));
                                scheme.setMin_age(obj.optInt("min_age"));
                                scheme.setMax_age(obj.optInt("max_age"));
                                scheme.setGender_restriction(obj.optString("gender_restriction"));
                                scheme.setCaste_restriction(obj.optString("caste_restriction"));
                                scheme.setScheme_status(obj.optString("scheme_status"));
                                schemeList.add(scheme);
                            }
                            adapter.updateList(schemeList);
                            Toast.makeText(SchemeListActivity.this, "Loaded " + schemeList.size() + " schemes", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SchemeListActivity.this, "Failed to load schemes", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(SchemeListActivity.this, "Parse Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}