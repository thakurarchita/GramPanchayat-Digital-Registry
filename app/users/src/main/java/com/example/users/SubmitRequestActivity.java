package com.example.users;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class SubmitRequestActivity extends BaseUserActivity {

    private ImageButton btnBack;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_request);

        initViews();
        setupToolbar();
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        toolbar = findViewById(R.id.toolbar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    // Method to open Birth Request Form
    public void openBirthRequest(View view) {
        Intent intent = new Intent(SubmitRequestActivity.this, BirthAdditionForm.class);
        startActivity(intent);
    }

    // Method to open Death Request Form
    public void openDeathRequest(View view) {
        Intent intent = new Intent(SubmitRequestActivity.this, DeathRequestActivity.class);
        startActivity(intent);
    }

    // Method to open Correction Request Form
    public void openCorrectionRequest(View view) {
        Intent intent = new Intent(SubmitRequestActivity.this, CorrectionRequestForm.class);
        startActivity(intent);
    }
}