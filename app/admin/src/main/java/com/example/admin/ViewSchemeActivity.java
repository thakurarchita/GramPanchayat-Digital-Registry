package com.example.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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

public class ViewSchemeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SchemeAdapter adapter;
    private List<Scheme> schemeList = new ArrayList<>();
    private Button btnAddScheme;
    private OkHttpClient client;
    private static final String BASE_URL = "http://192.168.147.188/grampanchayat_api/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheme_list);

        client = new OkHttpClient();

        // Initialize views
        Toolbar toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.rvSchemes);
        btnAddScheme = findViewById(R.id.btnAddNewScheme);

        // Setup toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Manage Schemes");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SchemeAdapter(schemeList, scheme -> {
            Intent intent = new Intent(ViewSchemeActivity.this, SchemeFormActivity.class);
            intent.putExtra("scheme_id", scheme.getScheme_id());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        // Setup Add Button
        btnAddScheme.setOnClickListener(v -> {
            Intent intent = new Intent(ViewSchemeActivity.this, SchemeFormActivity.class);
            startActivity(intent);
        });

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
                    Toast.makeText(ViewSchemeActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                runOnUiThread(() -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        if (jsonResponse.getBoolean("success")) {
                            JSONArray schemesArray = jsonResponse.getJSONArray("data");
                            schemeList.clear();
                            for (int i = 0; i < schemesArray.length(); i++) {
                                JSONObject obj = schemesArray.getJSONObject(i);
                                Scheme scheme = new Scheme();
                                scheme.setScheme_id(obj.optString("scheme_id"));
                                scheme.setScheme_name(obj.optString("scheme_name"));
                                schemeList.add(scheme);
                            }
                            adapter.updateList(schemeList);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(ViewSchemeActivity.this, "Parse Error", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSchemes();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}