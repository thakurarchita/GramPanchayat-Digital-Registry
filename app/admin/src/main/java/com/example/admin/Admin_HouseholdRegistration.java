package com.example.admin;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Admin_HouseholdRegistration extends AppCompatActivity {

    // UI Components
    private TextView tvHouseholdId;
    private EditText etHeadName, etAadhaar, etAddress, etMobile, etEmail, etIncome, etOccupation;
    private EditText etBankName, etAccountNumber, etIfscCode;
    private Spinner spinnerCaste;
    private RadioGroup rgHousingType, rgElectricity, rgToilet;
    private LinearLayout layoutMembersContainer;
    private Button btnAddMember, btnSave, btnUploadHouseholdImage;
    private ImageView ivHouseholdImage;

    // Data
    private List<View> memberViews = new ArrayList<>();
    private List<FamilyMemberData> familyMembersList = new ArrayList<>();
    private OkHttpClient client;

    // Image paths
    private String householdImagePath = null;
    private List<String> memberImagePaths = new ArrayList<>();

    // Image picker launchers
    private ActivityResultLauncher<Intent> householdImageLauncher;
    private ActivityResultLauncher<Intent> memberImageLauncher;
    private int currentMemberIndex = -1;

    // API URL
    private static final String API_URL = "http://192.168.147.188/grampanchayat_api/admin_register_household.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_household_registration);

        client = new OkHttpClient.Builder()
                .connectTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();

        initViews();
        setupToolbar();
        setupSpinners();
        setupClickListeners();
        setupImageLaunchers();
        generateHouseholdId();

        addNewMemberForm();
    }

    private void initViews() {
        tvHouseholdId = findViewById(R.id.tvHouseholdId);
        etHeadName = findViewById(R.id.etHeadName);
        etAadhaar = findViewById(R.id.etAadhaar);
        etAddress = findViewById(R.id.etAddress);
        etMobile = findViewById(R.id.etMobile);
        etEmail = findViewById(R.id.etEmail);  // Added email field
        etIncome = findViewById(R.id.etIncome);
        etOccupation = findViewById(R.id.etOccupation);
        etBankName = findViewById(R.id.etBankName);
        etAccountNumber = findViewById(R.id.etAccountNumber);
        etIfscCode = findViewById(R.id.etIfscCode);
        spinnerCaste = findViewById(R.id.spinnerCaste);
        rgHousingType = findViewById(R.id.rgHousingType);
        rgElectricity = findViewById(R.id.rgElectricity);
        rgToilet = findViewById(R.id.rgToilet);
        layoutMembersContainer = findViewById(R.id.layoutMembersContainer);
        btnAddMember = findViewById(R.id.btnAddMember);
        btnSave = findViewById(R.id.btnSave);
        btnUploadHouseholdImage = findViewById(R.id.btnUploadHouseholdImage);
        ivHouseholdImage = findViewById(R.id.ivHouseholdImage);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupSpinners() {
        String[] casteArray = {"General", "OBC", "SC", "ST", "Others"};
        ArrayAdapter<String> casteAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, casteArray);
        casteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCaste.setAdapter(casteAdapter);
    }

    private void setupClickListeners() {
        btnAddMember.setOnClickListener(v -> addNewMemberForm());
        btnSave.setOnClickListener(v -> validateAndSubmit());
        btnUploadHouseholdImage.setOnClickListener(v -> pickHouseholdImage());
    }

    private void setupImageLaunchers() {
        householdImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        householdImagePath = getRealPathFromURI(imageUri);
                        ivHouseholdImage.setImageURI(imageUri);
                        ivHouseholdImage.setVisibility(View.VISIBLE);
                    }
                }
        );

        memberImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null && currentMemberIndex >= 0) {
                        Uri imageUri = result.getData().getData();
                        String path = getRealPathFromURI(imageUri);
                        while (memberImagePaths.size() <= currentMemberIndex) {
                            memberImagePaths.add(null);
                        }
                        memberImagePaths.set(currentMemberIndex, path);

                        View memberView = memberViews.get(currentMemberIndex);
                        ImageView ivMemberImage = memberView.findViewById(R.id.ivMemberImage);
                        if (ivMemberImage != null) {
                            ivMemberImage.setImageURI(imageUri);
                            ivMemberImage.setVisibility(View.VISIBLE);
                        }
                    }
                }
        );
    }

    private String getRealPathFromURI(Uri uri) {
        String filePath = "";
        try {
            if (uri.getScheme().equals("content")) {
                String[] projection = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
                if (cursor != null) {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    cursor.moveToFirst();
                    filePath = cursor.getString(columnIndex);
                    cursor.close();
                }
            } else {
                filePath = uri.getPath();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return filePath;
    }

    private void pickHouseholdImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        householdImageLauncher.launch(intent);
    }

    private void pickMemberImage(int memberIndex) {
        currentMemberIndex = memberIndex;
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        memberImageLauncher.launch(intent);
    }

    private void generateHouseholdId() {
        tvHouseholdId.setText("Will be generated by system");
    }

    private void addNewMemberForm() {
        View memberView = getLayoutInflater().inflate(R.layout.item_family_member, null);

        setupMemberSpinners(memberView);
        setupDatePicker(memberView);
        setupMemberImageUpload(memberView, memberViews.size());

        ImageButton btnRemove = memberView.findViewById(R.id.btnRemoveMember);
        btnRemove.setOnClickListener(v -> {
            int position = memberViews.indexOf(memberView);
            layoutMembersContainer.removeView(memberView);
            memberViews.remove(memberView);
            if (position < memberImagePaths.size()) {
                memberImagePaths.remove(position);
            }
        });

        layoutMembersContainer.addView(memberView);
        memberViews.add(memberView);
        memberImagePaths.add(null);
    }

    private void setupMemberSpinners(View memberView) {
        Spinner spinnerGender = memberView.findViewById(R.id.spinnerGender);
        String[] genders = {"Male", "Female", "Other"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, genders);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);

        Spinner spinnerRelation = memberView.findViewById(R.id.spinnerRelation);
        String[] relations = {"Self", "Spouse", "Son", "Daughter", "Father", "Mother", "Other"};
        ArrayAdapter<String> relationAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, relations);
        relationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRelation.setAdapter(relationAdapter);

        Spinner spinnerMarital = memberView.findViewById(R.id.spinnerMaritalStatus);
        String[] marital = {"Unmarried", "Married", "Widowed", "Divorced"};
        ArrayAdapter<String> maritalAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, marital);
        maritalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMarital.setAdapter(maritalAdapter);
    }

    private void setupDatePicker(View memberView) {
        EditText etDob = memberView.findViewById(R.id.etMemberDob);
        etDob.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePicker = new DatePickerDialog(this,
                    (view, year, month, day) -> {
                        String date = year + "-" + String.format("%02d", (month + 1)) + "-" + String.format("%02d", day);
                        etDob.setText(date);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePicker.show();
        });
    }

    private void setupMemberImageUpload(View memberView, int index) {
        Button btnUpload = memberView.findViewById(R.id.btnUploadMemberImage);
        btnUpload.setOnClickListener(v -> pickMemberImage(index));
    }

    private void collectFamilyMembers() {
        familyMembersList.clear();

        for (int i = 0; i < memberViews.size(); i++) {
            View memberView = memberViews.get(i);

            EditText etName = memberView.findViewById(R.id.etMemberName);
            EditText etAadhaar = memberView.findViewById(R.id.etMemberAadhaar);
            EditText etAge = memberView.findViewById(R.id.etMemberAge);
            EditText etDob = memberView.findViewById(R.id.etMemberDob);
            EditText etOccupation = memberView.findViewById(R.id.etMemberOccupation);
            EditText etEducation = memberView.findViewById(R.id.etEducation);

            Spinner spinnerGender = memberView.findViewById(R.id.spinnerGender);
            Spinner spinnerRelation = memberView.findViewById(R.id.spinnerRelation);
            Spinner spinnerMarital = memberView.findViewById(R.id.spinnerMaritalStatus);

            String name = etName.getText().toString().trim();
            if (TextUtils.isEmpty(name)) {
                continue;
            }

            String ageStr = etAge.getText().toString().trim();
            int age = TextUtils.isEmpty(ageStr) ? 0 : Integer.parseInt(ageStr);

            FamilyMemberData member = new FamilyMemberData(
                    name,
                    etAadhaar.getText().toString().trim(),
                    age,
                    etDob.getText().toString().trim(),
                    spinnerGender.getSelectedItem().toString(),
                    spinnerRelation.getSelectedItem().toString(),
                    spinnerMarital.getSelectedItem().toString(),
                    etOccupation.getText().toString().trim(),
                    etEducation.getText().toString().trim(),
                    (i < memberImagePaths.size()) ? memberImagePaths.get(i) : null
            );
            familyMembersList.add(member);
        }
    }

    private static class FamilyMemberData {
        String name, aadhaar, dob, gender, relationship, maritalStatus, occupation, education, imagePath;
        int age;

        FamilyMemberData(String name, String aadhaar, int age, String dob, String gender,
                         String relationship, String maritalStatus, String occupation,
                         String education, String imagePath) {
            this.name = name;
            this.aadhaar = aadhaar;
            this.age = age;
            this.dob = dob;
            this.gender = gender;
            this.relationship = relationship;
            this.maritalStatus = maritalStatus;
            this.occupation = occupation;
            this.education = education;
            this.imagePath = imagePath;
        }
    }

    private void validateAndSubmit() {
        // Validate required fields
        String headName = etHeadName.getText().toString().trim();
        String aadhaar = etAadhaar.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String incomeStr = etIncome.getText().toString().trim();

        if (TextUtils.isEmpty(headName)) {
            etHeadName.setError("Head name required");
            return;
        }
        if (TextUtils.isEmpty(aadhaar) || aadhaar.length() != 12) {
            etAadhaar.setError("Valid 12-digit Aadhaar required");
            return;
        }
        if (TextUtils.isEmpty(address)) {
            etAddress.setError("Address required");
            return;
        }
        if (TextUtils.isEmpty(mobile) || mobile.length() != 10) {
            etMobile.setError("Valid 10-digit mobile required");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email address required");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Valid email address required");
            return;
        }
        if (TextUtils.isEmpty(incomeStr)) {
            etIncome.setError("Annual income required");
            return;
        }

        collectFamilyMembers();

        if (familyMembersList.isEmpty()) {
            Toast.makeText(this, "Please add at least one family member", Toast.LENGTH_SHORT).show();
            return;
        }

        showProgress(true);

        // Get radio button values
        String houseType = "Pucca";
        int selectedHousingId = rgHousingType.getCheckedRadioButtonId();
        if (selectedHousingId != -1) {
            RadioButton selectedHousing = findViewById(selectedHousingId);
            houseType = selectedHousing.getText().toString();
        }
        if (houseType.equals("Kaccha")) houseType = "Kutcha";

        String electricity = "No";
        int selectedElectricityId = rgElectricity.getCheckedRadioButtonId();
        if (selectedElectricityId != -1) {
            RadioButton selectedElectricity = findViewById(selectedElectricityId);
            electricity = selectedElectricity.getText().toString();
        }

        try {
            // Create JSON object for family members
            JSONArray membersArray = new JSONArray();
            for (FamilyMemberData member : familyMembersList) {
                JSONObject memberObj = new JSONObject();
                memberObj.put("member_name", member.name);
                memberObj.put("member_aadhaar", member.aadhaar != null ? member.aadhaar : "");
                memberObj.put("age", member.age);
                memberObj.put("dob", member.dob != null ? member.dob : "");
                memberObj.put("gender", member.gender);
                memberObj.put("relationship", member.relationship);
                memberObj.put("marital_status", member.maritalStatus != null ? member.maritalStatus : "");
                memberObj.put("occupation", member.occupation != null ? member.occupation : "");
                memberObj.put("education", member.education != null ? member.education : "");
                membersArray.put(memberObj);
            }

            // Create main JSON request
            JSONObject request = new JSONObject();
            request.put("head_name", headName);
            request.put("head_aadhaar", aadhaar);
            request.put("head_mobile", mobile);
            request.put("head_email", email);  // Added email field
            request.put("address", address);
            request.put("annual_income", incomeStr);
            request.put("caste", spinnerCaste.getSelectedItem().toString());
            request.put("house_type", houseType);
            request.put("occupation", etOccupation.getText().toString().trim());
            request.put("electricity", electricity);
            request.put("bank_name", etBankName.getText().toString().trim());
            request.put("account_number", etAccountNumber.getText().toString().trim());
            request.put("ifsc_code", etIfscCode.getText().toString().trim());
            request.put("family_members", membersArray);

            // Log the request
            android.util.Log.e("REGISTER", "Request: " + request.toString());

            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(request.toString(), JSON);

            Request httpRequest = new Request.Builder()
                    .url(API_URL)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(httpRequest).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        showProgress(false);
                        Toast.makeText(Admin_HouseholdRegistration.this,
                                "Network error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    runOnUiThread(() -> {
                        showProgress(false);
                        android.util.Log.e("REGISTER", "Response: " + responseBody);

                        try {
                            JSONObject jsonResponse = new JSONObject(responseBody);
                            if (jsonResponse.getBoolean("success")) {
                                String householdId = jsonResponse.optString("household_id", "N/A");
                                String userMobile = jsonResponse.optString("user_mobile", mobile);
                                new AlertDialog.Builder(Admin_HouseholdRegistration.this)
                                        .setTitle("Success")
                                        .setMessage("Household registered successfully!\n\nHousehold ID: " + householdId +
                                                "\nMobile: " + userMobile +
                                                "\n\nUser will receive OTP on email to activate account.")
                                        .setPositiveButton("OK", (dialog, which) -> finish())
                                        .show();
                            } else {
                                String errorMsg = jsonResponse.getString("message");
                                Toast.makeText(Admin_HouseholdRegistration.this, errorMsg, Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(Admin_HouseholdRegistration.this,
                                    "Error: " + responseBody, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });

        } catch (Exception e) {
            showProgress(false);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showProgress(boolean show) {
        if (show) {
            btnSave.setText("REGISTERING...");
            btnSave.setEnabled(false);
        } else {
            btnSave.setText("Register Household");
            btnSave.setEnabled(true);
        }
    }
}