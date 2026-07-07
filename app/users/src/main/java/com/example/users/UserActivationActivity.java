package com.example.users;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UserActivationActivity extends BaseUserActivity {

    private TextInputEditText etMobile, etPassword, etConfirmPassword;
    private EditText otpBox1, otpBox2, otpBox3, otpBox4, otpBox5, otpBox6;
    private EditText etOtp;
    private Button btnCheckMobile, btnSendOtp, btnVerifyOtp, btnActivate;
    private TextView tvStep2, tvStep2Title, tvMobileHint, tvStep3, tvTimer, tvResendOtp;
    private TextView tvStep3Title, tvBackToLogin;
    private LinearLayout otpContainer;
    private ProgressBar progressBar;
    private View divider2;

    private OkHttpClient client;
    private String verifiedMobile;
    private CountDownTimer countDownTimer;
    private boolean isOtpVerified = false;

    private static final String BASE_URL = "http://192.168.147.188/grampanchayat_api/";
    private static final int SMS_PERMISSION_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_activation);

        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        initViews();
        setupListeners();
        setupOtpAutoMove();
        showStep1Only();
    }

    private void initViews() {
        etMobile = findViewById(R.id.etMobile);
        etOtp = findViewById(R.id.etOtp);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        otpBox1 = findViewById(R.id.otpBox1);
        otpBox2 = findViewById(R.id.otpBox2);
        otpBox3 = findViewById(R.id.otpBox3);
        otpBox4 = findViewById(R.id.otpBox4);
        otpBox5 = findViewById(R.id.otpBox5);
        otpBox6 = findViewById(R.id.otpBox6);

        btnCheckMobile = findViewById(R.id.btnCheckMobile);
        btnSendOtp = findViewById(R.id.btnSendOtp);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        btnActivate = findViewById(R.id.btnActivate);

        tvStep2 = findViewById(R.id.tvStep2);
        tvStep2Title = findViewById(R.id.tvStep2Title);
        tvMobileHint = findViewById(R.id.tvMobileHint);
        tvStep3 = findViewById(R.id.tvStep3);
        tvStep3Title = findViewById(R.id.tvStep3Title);
        tvTimer = findViewById(R.id.tvTimer);
        tvResendOtp = findViewById(R.id.tvResendOtp);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        otpContainer = findViewById(R.id.otpContainer);
        progressBar = findViewById(R.id.progressBar);
        divider2 = findViewById(R.id.divider2);
    }

    private void setupListeners() {
        btnCheckMobile.setOnClickListener(v -> checkMobile());
        btnSendOtp.setOnClickListener(v -> sendOtp());
        btnVerifyOtp.setOnClickListener(v -> verifyOtp());
        btnActivate.setOnClickListener(v -> activateAccount());
        tvResendOtp.setOnClickListener(v -> resendOtp());
        tvBackToLogin.setOnClickListener(v -> finish());
    }

    private void setupOtpAutoMove() {
        EditText[] otpBoxes = {otpBox1, otpBox2, otpBox3, otpBox4, otpBox5, otpBox6};

        for (int i = 0; i < otpBoxes.length; i++) {
            final int index = i;
            otpBoxes[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && index < 5) {
                        otpBoxes[index + 1].requestFocus();
                    }
                    updateHiddenOtpField();
                    if (getFullOtp().length() == 6 && !isOtpVerified) {
                        verifyOtp();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            otpBoxes[i].setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (otpBoxes[index].getText().toString().isEmpty() && index > 0) {
                        otpBoxes[index - 1].requestFocus();
                        otpBoxes[index - 1].setText("");
                        updateHiddenOtpField();
                    }
                }
                return false;
            });
        }
    }

    private String getFullOtp() {
        return otpBox1.getText().toString() +
                otpBox2.getText().toString() +
                otpBox3.getText().toString() +
                otpBox4.getText().toString() +
                otpBox5.getText().toString() +
                otpBox6.getText().toString();
    }

    private void updateHiddenOtpField() {
        String fullOtp = getFullOtp();
        if (etOtp != null) {
            etOtp.setText(fullOtp);
        }
    }

    private void autoFillOtp(String otp) {
        if (otp != null && otp.length() == 6) {
            otpBox1.setText(String.valueOf(otp.charAt(0)));
            otpBox2.setText(String.valueOf(otp.charAt(1)));
            otpBox3.setText(String.valueOf(otp.charAt(2)));
            otpBox4.setText(String.valueOf(otp.charAt(3)));
            otpBox5.setText(String.valueOf(otp.charAt(4)));
            otpBox6.setText(String.valueOf(otp.charAt(5)));
        }
    }

    private void showStep1Only() {
        etMobile.setEnabled(true);
        btnCheckMobile.setVisibility(View.VISIBLE);
        otpContainer.setVisibility(View.GONE);
        btnSendOtp.setVisibility(View.GONE);
        btnVerifyOtp.setVisibility(View.GONE);
        tvStep2.setVisibility(View.GONE);
        tvStep2Title.setVisibility(View.GONE);
        tvMobileHint.setVisibility(View.GONE);
        tvTimer.setVisibility(View.GONE);
        tvResendOtp.setVisibility(View.GONE);
        divider2.setVisibility(View.GONE);
        etPassword.setVisibility(View.GONE);
        etConfirmPassword.setVisibility(View.GONE);
        btnActivate.setVisibility(View.GONE);
        tvStep3.setVisibility(View.GONE);
        if (tvStep3Title != null) tvStep3Title.setVisibility(View.GONE);
    }

    private void checkMobile() {
        String mobile = etMobile.getText().toString().trim();

        if (TextUtils.isEmpty(mobile) || mobile.length() != 10) {
            Toast.makeText(this, "Valid 10-digit mobile number required", Toast.LENGTH_SHORT).show();
            return;
        }

        showProgress(true);

        try {
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("mobile", mobile);

            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(jsonRequest.toString(), JSON);

            Request request = new Request.Builder()
                    .url(BASE_URL + "check_mobile.php")
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        showProgress(false);
                        Toast.makeText(UserActivationActivity.this,
                                "Network error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    runOnUiThread(() -> {
                        showProgress(false);
                        try {
                            JSONObject jsonResponse = new JSONObject(responseBody);
                            boolean success = jsonResponse.getBoolean("success");

                            if (success) {
                                verifiedMobile = mobile;
                                Toast.makeText(UserActivationActivity.this,
                                        jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();

                                otpContainer.setVisibility(View.VISIBLE);
                                btnSendOtp.setVisibility(View.VISIBLE);
                                tvStep2.setVisibility(View.VISIBLE);
                                tvStep2Title.setVisibility(View.VISIBLE);
                                tvMobileHint.setVisibility(View.VISIBLE);
                                tvMobileHint.setText("OTP will be sent to " + mobile);
                                divider2.setVisibility(View.VISIBLE);
                                btnCheckMobile.setEnabled(false);
                                etMobile.setEnabled(false);
                            } else {
                                String message = jsonResponse.getString("message");
                                Toast.makeText(UserActivationActivity.this, message, Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(UserActivationActivity.this,
                                    "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });

        } catch (Exception e) {
            showProgress(false);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void sendOtp() {
        if (verifiedMobile == null) {
            Toast.makeText(this, "Please verify mobile first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check SMS permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    SMS_PERMISSION_CODE);
            Toast.makeText(this, "Please grant SMS permission to send OTP", Toast.LENGTH_LONG).show();
            return;
        }

        showProgress(true);
        startTimer(120); // 2 minutes = 120 seconds

        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("mobile", verifiedMobile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonRequest.toString(), JSON);

        Request request = new Request.Builder()
                .url(BASE_URL + "user_send_otp.php")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(UserActivationActivity.this,
                            "Network error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                runOnUiThread(() -> {
                    showProgress(false);
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        boolean success = jsonResponse.getBoolean("success");

                        if (success) {
                            String otp = jsonResponse.getString("otp");

                            // Send SMS using Android's SmsManager
                            sendSMSFromDevice(verifiedMobile, otp);

                            // Auto-fill OTP for testing
                            autoFillOtp(otp);

                            btnVerifyOtp.setVisibility(View.VISIBLE);
                            btnSendOtp.setEnabled(false);
                            tvResendOtp.setVisibility(View.VISIBLE);
                            isOtpVerified = false;

                        } else {
                            String message = jsonResponse.getString("message");
                            Toast.makeText(UserActivationActivity.this, message, Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(UserActivationActivity.this,
                                "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    // Send SMS directly from the Android device
    private void sendSMSFromDevice(String mobileNumber, String otp) {
        String message = "Your Gram Panchayat OTP for account activation is: " + otp + "\nValid for 2 minutes.";

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(mobileNumber, null, message, null, null);
            Toast.makeText(this, "OTP sent to " + mobileNumber, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to send SMS. OTP for testing: " + otp, Toast.LENGTH_LONG).show();
            autoFillOtp(otp);
        }
    }

    private void resendOtp() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        // Clear OTP boxes
        otpBox1.setText("");
        otpBox2.setText("");
        otpBox3.setText("");
        otpBox4.setText("");
        otpBox5.setText("");
        otpBox6.setText("");
        otpBox1.requestFocus();
        sendOtp();
    }

    private void startTimer(int seconds) {
        tvTimer.setVisibility(View.VISIBLE);
        tvResendOtp.setVisibility(View.GONE);
        tvResendOtp.setEnabled(false);

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(seconds * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long secondsLeft = millisUntilFinished / 1000;
                long minutes = secondsLeft / 60;
                long secs = secondsLeft % 60;
                tvTimer.setText(String.format("Resend OTP in %d:%02d", minutes, secs));
            }

            @Override
            public void onFinish() {
                tvTimer.setVisibility(View.GONE);
                tvResendOtp.setVisibility(View.VISIBLE);
                tvResendOtp.setEnabled(true);
                btnSendOtp.setEnabled(true);
            }
        }.start();
    }

    private void verifyOtp() {
        String enteredOtp = getFullOtp();

        if (enteredOtp.length() != 6) {
            Toast.makeText(this, "Enter complete 6-digit OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        showProgress(true);

        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("mobile", verifiedMobile);
            jsonRequest.put("otp", enteredOtp);
        } catch (Exception e) {
            e.printStackTrace();
        }

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonRequest.toString(), JSON);

        Request request = new Request.Builder()
                .url(BASE_URL + "verify_otp.php")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(UserActivationActivity.this,
                            "Verification failed", Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                runOnUiThread(() -> {
                    showProgress(false);
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        if (jsonResponse.getBoolean("success")) {
                            isOtpVerified = true;
                            Toast.makeText(UserActivationActivity.this,
                                    jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();

                            // Disable OTP boxes after verification
                            otpBox1.setEnabled(false);
                            otpBox2.setEnabled(false);
                            otpBox3.setEnabled(false);
                            otpBox4.setEnabled(false);
                            otpBox5.setEnabled(false);
                            otpBox6.setEnabled(false);
                            btnVerifyOtp.setEnabled(false);
                            btnSendOtp.setEnabled(false);

                            if (countDownTimer != null) {
                                countDownTimer.cancel();
                            }
                            tvTimer.setVisibility(View.GONE);
                            tvResendOtp.setVisibility(View.GONE);

                            // Show password setup section
                            tvStep3.setVisibility(View.VISIBLE);
                            if (tvStep3Title != null) tvStep3Title.setVisibility(View.VISIBLE);
                            etPassword.setVisibility(View.VISIBLE);
                            etConfirmPassword.setVisibility(View.VISIBLE);
                            btnActivate.setVisibility(View.VISIBLE);
                        } else {
                            String message = jsonResponse.getString("message");
                            Toast.makeText(UserActivationActivity.this, message, Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(UserActivationActivity.this,
                                "Error verifying OTP", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void activateAccount() {
        if (!isOtpVerified) {
            Toast.makeText(this, "Please verify OTP first", Toast.LENGTH_SHORT).show();
            return;
        }

        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Password required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 4) {
            Toast.makeText(this, "Password must be at least 4 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        showProgress(true);

        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("mobile", verifiedMobile);
            jsonRequest.put("password", password);
            jsonRequest.put("otp", getFullOtp());
        } catch (Exception e) {
            e.printStackTrace();
        }

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonRequest.toString(), JSON);

        Request request = new Request.Builder()
                .url(BASE_URL + "activate_account.php")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(UserActivationActivity.this,
                            "Activation failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                runOnUiThread(() -> {
                    showProgress(false);
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        if (jsonResponse.getBoolean("success")) {
                            new AlertDialog.Builder(UserActivationActivity.this)
                                    .setTitle("Success!")
                                    .setMessage("Account activated successfully! You can now login.")
                                    .setPositiveButton("Login", (dialog, which) -> finish())
                                    .setCancelable(false)
                                    .show();
                        } else {
                            String message = jsonResponse.getString("message");
                            Toast.makeText(UserActivationActivity.this, message, Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(UserActivationActivity.this,
                                "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "SMS permission required to send OTP", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnCheckMobile.setEnabled(!show);
        btnSendOtp.setEnabled(!show);
        btnVerifyOtp.setEnabled(!show);
        btnActivate.setEnabled(!show);
        etMobile.setEnabled(!show && verifiedMobile == null);
    }
}