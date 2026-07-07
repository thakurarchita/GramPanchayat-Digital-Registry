package com.example.admin;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class AddEditSchemeActivity extends AppCompatActivity {

    // Fields
    EditText etSchemeName, etDepartment, etDescription;
    EditText etBenefitAmount, etStartDate, etEndDate;
    EditText etMinIncome, etMaxIncome, etMinAge, etMaxAge;

    Spinner spinnerBenefitType, spinnerGender, spinnerCaste;

    RadioGroup rgSchemeCategory;
    RadioButton rbHouseholdBased, rbIndividualBased;

    CheckBox cbAadhaar, cbIncome, cbCaste, cbResidence, cbAge, cbBank, cbPhoto;

    Button btnUpdate, btnDelete;
    ImageButton btnBack;

    TextView tvSchemeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheme_form);

        // Initialize views
        initViews();

        // Date pickers
        setupDatePicker(etStartDate);
        setupDatePicker(etEndDate);

        // Button actions
        btnUpdate.setOnClickListener(v -> updateScheme());
        btnDelete.setOnClickListener(v -> confirmDelete());

        btnBack.setOnClickListener(v -> finish());
    }

    private void initViews() {
        tvSchemeId = findViewById(R.id.tvSchemeId);

        etSchemeName = findViewById(R.id.etSchemeName);
        etDepartment = findViewById(R.id.etDepartment);
        etDescription = findViewById(R.id.etDescription);

        etBenefitAmount = findViewById(R.id.etBenefitAmount);
        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);

        etMinIncome = findViewById(R.id.etMinIncome);
        etMaxIncome = findViewById(R.id.etMaxIncome);
        etMinAge = findViewById(R.id.etMinAge);
        etMaxAge = findViewById(R.id.etMaxAge);

        spinnerBenefitType = findViewById(R.id.spinnerBenefitType);
        spinnerGender = findViewById(R.id.spinnerGender);
        spinnerCaste = findViewById(R.id.spinnerCaste);

        rgSchemeCategory = findViewById(R.id.rgSchemeCategory);
        rbHouseholdBased = findViewById(R.id.rbHouseholdBased);
        rbIndividualBased = findViewById(R.id.rbIndividualBased);

        cbAadhaar = findViewById(R.id.cbAadhaar);
        cbIncome = findViewById(R.id.cbIncome);
        cbCaste = findViewById(R.id.cbCaste);
        cbResidence = findViewById(R.id.cbResidence);
        cbAge = findViewById(R.id.cbAge);
        cbBank = findViewById(R.id.cbBank);
        cbPhoto = findViewById(R.id.cbPhoto);

        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);
        btnBack = findViewById(R.id.btnBack);

    }

    // 📅 Date Picker
    private void setupDatePicker(EditText editText) {
        editText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, y, m, d) -> editText.setText(d + "/" + (m + 1) + "/" + y),
                    year, month, day
            );

            datePickerDialog.show();
        });
    }

    // 🔄 Update Function
    private void updateScheme() {
        String name = etSchemeName.getText().toString().trim();
        String department = etDepartment.getText().toString().trim();

        if (name.isEmpty() || department.isEmpty()) {
            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String category = rbHouseholdBased.isChecked() ? "Household" : "Individual";

        String documents = "";
        if (cbAadhaar.isChecked()) documents += "Aadhaar, ";
        if (cbIncome.isChecked()) documents += "Income, ";
        if (cbCaste.isChecked()) documents += "Caste, ";
        if (cbResidence.isChecked()) documents += "Residence, ";
        if (cbAge.isChecked()) documents += "Age, ";
        if (cbBank.isChecked()) documents += "Bank, ";
        if (cbPhoto.isChecked()) documents += "Photo, ";

        // Remove last comma
        if (!documents.isEmpty()) {
            documents = documents.substring(0, documents.length() - 2);
        }

        // TODO: Replace with API call
        Toast.makeText(this, "Scheme Updated Successfully", Toast.LENGTH_LONG).show();
    }

    // ❌ Delete Confirmation
    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Scheme")
                .setMessage("Are you sure you want to delete this scheme?")
                .setPositiveButton("Yes", (dialog, which) -> deleteScheme())
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteScheme() {
        // TODO: Replace with API call
        Toast.makeText(this, "Scheme Deleted", Toast.LENGTH_SHORT).show();
        finish();
    }
}