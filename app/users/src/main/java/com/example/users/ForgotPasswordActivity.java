package com.example.users;

import android.os.Bundle;
import android.os.CountDownTimer;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.android.material.textfield.TextInputEditText;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputEditText etMobile, etNewPassword, etConfirmPassword;
    private EditText otpBox1, otpBox2, otpBox3, otpBox4, otpBox5, otpBox6;
    private Button btnSendOtp, btnVerifyOtp, btnResetPassword;
    private TextView tvResendOtp, tvBackToLogin, tvTimer;
    private ProgressBar progressBar;
    private LinearLayout otpSection, resetPasswordSection;
    private CardView mobileCard, otpCard;
    private OkHttpClient client;
    private String userId = "";
    private CountDownTimer countDownTimer;

    private static final String BASE_URL = "http://192.168.147.188/grampanchayat_api/";
    private static final String FORGOT_PASSWORD_URL = BASE_URL + "forgot_password.php";
    private static final String VERIFY_OTP_URL = BASE_URL + "verify_otp.php";
    private static final String RESET_PASSWORD_URL = BASE_URL + "reset_password.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        client = new OkHttpClient();
        initViews();
        setupListeners();
        setupOtpAutoMove();
    }

    private void initViews() {
        etMobile = findViewById(R.id.etMobile);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        otpBox1 = findViewById(R.id.otpBox1);
        otpBox2 = findViewById(R.id.otpBox2);
        otpBox3 = findViewById(R.id.otpBox3);
        otpBox4 = findViewById(R.id.otpBox4);
        otpBox5 = findViewById(R.id.otpBox5);
        otpBox6 = findViewById(R.id.otpBox6);

        btnSendOtp = findViewById(R.id.btnSendOtp);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        btnResetPassword = findViewById(R.id.btnResetPassword);

        tvResendOtp = findViewById(R.id.tvResendOtp);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);
        tvTimer = findViewById(R.id.tvTimer);
        progressBar = findViewById(R.id.progressBar);

        otpSection = findViewById(R.id.otpSection);
        resetPasswordSection = findViewById(R.id.resetPasswordSection);

        mobileCard = findViewById(R.id.mobileCard);
        otpCard = findViewById(R.id.otpCard);

        otpSection.setVisibility(View.GONE);
        resetPasswordSection.setVisibility(View.GONE);
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
                    // Auto-verify when all 6 digits are entered
                    if (getFullOtp().length() == 6) {
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

    private void clearOtpBoxes() {
        otpBox1.setText("");
        otpBox2.setText("");
        otpBox3.setText("");
        otpBox4.setText("");
        otpBox5.setText("");
        otpBox6.setText("");
        otpBox1.requestFocus();
    }

    private void setupListeners() {
        btnSendOtp.setOnClickListener(v -> sendOtp());
        btnVerifyOtp.setOnClickListener(v -> verifyOtp());
        btnResetPassword.setOnClickListener(v -> resetPassword());
        tvResendOtp.setOnClickListener(v -> {
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
            clearOtpBoxes();
            sendOtp();
        });

        if (tvBackToLogin != null) {
            tvBackToLogin.setOnClickListener(v -> finish());
        }
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
                tvTimer.setText("Resend OTP in " + (millisUntilFinished / 1000) + "s");
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

    private void sendOtp() {
        String mobile = etMobile.getText().toString().trim();

        if (TextUtils.isEmpty(mobile)) {
            etMobile.setError("Mobile number required");
            return;
        }

        if (mobile.length() != 10) {
            etMobile.setError("Enter valid 10-digit mobile number");
            return;
        }

        showProgress(true);
        startTimer(60);

        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("mobile", mobile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonRequest.toString(), JSON);

        Request request = new Request.Builder()
                .url(FORGOT_PASSWORD_URL)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(ForgotPasswordActivity.this,
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

                        // Log the response for debugging
                        android.util.Log.e("FORGOT_PASSWORD", "Send OTP Response: " + responseBody);

                        if (success) {
                            userId = jsonResponse.getString("user_id");
                            String otp = jsonResponse.optString("otp", "");
                            String fullName = jsonResponse.optString("full_name", "");

                            android.util.Log.e("FORGOT_PASSWORD", "User ID: " + userId);
                            android.util.Log.e("FORGOT_PASSWORD", "Received OTP: " + otp);

                            // Auto-fill OTP into the boxes
                            if (!otp.isEmpty()) {
                                autoFillOtp(otp);
                                Toast.makeText(ForgotPasswordActivity.this,
                                        "OTP: " + otp, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(ForgotPasswordActivity.this,
                                        "OTP sent to your mobile", Toast.LENGTH_SHORT).show();
                            }

                            // Show OTP section
                            mobileCard.setCardElevation(0);
                            otpSection.setVisibility(View.VISIBLE);
                            btnSendOtp.setEnabled(false);

                        } else {
                            String message = jsonResponse.getString("message");
                            Toast.makeText(ForgotPasswordActivity.this, message, Toast.LENGTH_LONG).show();
                            if (countDownTimer != null) {
                                countDownTimer.cancel();
                            }
                            tvTimer.setVisibility(View.GONE);
                            tvResendOtp.setVisibility(View.GONE);
                            btnSendOtp.setEnabled(true);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(ForgotPasswordActivity.this,
                                "Error: " + responseBody, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void verifyOtp() {
        String enteredOtp = getFullOtp();

        android.util.Log.e("FORGOT_PASSWORD", "=====================================");
        android.util.Log.e("FORGOT_PASSWORD", "VERIFYING OTP");
        android.util.Log.e("FORGOT_PASSWORD", "User ID: " + userId);
        android.util.Log.e("FORGOT_PASSWORD", "Entered OTP: " + enteredOtp);
        android.util.Log.e("FORGOT_PASSWORD", "OTP Length: " + enteredOtp.length());
        android.util.Log.e("FORGOT_PASSWORD", "=====================================");

        if (enteredOtp.length() != 6) {
            Toast.makeText(this, "Please enter complete 6-digit OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        showProgress(true);

        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("user_id", userId);
            jsonRequest.put("otp", enteredOtp);
        } catch (Exception e) {
            e.printStackTrace();
        }

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonRequest.toString(), JSON);

        Request request = new Request.Builder()
                .url(VERIFY_OTP_URL)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(ForgotPasswordActivity.this,
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
                        String message = jsonResponse.getString("message");

                        android.util.Log.e("FORGOT_PASSWORD", "Verify Response: " + responseBody);

                        Toast.makeText(ForgotPasswordActivity.this, message, Toast.LENGTH_LONG).show();

                        if (success) {
                            // Disable OTP boxes after successful verification
                            otpBox1.setEnabled(false);
                            otpBox2.setEnabled(false);
                            otpBox3.setEnabled(false);
                            otpBox4.setEnabled(false);
                            otpBox5.setEnabled(false);
                            otpBox6.setEnabled(false);
                            btnVerifyOtp.setEnabled(false);

                            if (countDownTimer != null) {
                                countDownTimer.cancel();
                            }
                            tvTimer.setVisibility(View.GONE);
                            tvResendOtp.setVisibility(View.GONE);

                            // Show reset password section
                            otpCard.setCardElevation(0);
                            resetPasswordSection.setVisibility(View.VISIBLE);

                        } else {
                            // Clear OTP boxes for retry
                            clearOtpBoxes();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        android.util.Log.e("FORGOT_PASSWORD", "Parse Error: " + responseBody);
                        Toast.makeText(ForgotPasswordActivity.this,
                                "Error: " + responseBody, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void resetPassword() {
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        android.util.Log.e("FORGOT_PASSWORD", "Resetting password for user: " + userId);

        if (TextUtils.isEmpty(newPassword)) {
            etNewPassword.setError("Password required");
            return;
        }

        if (newPassword.length() < 4) {
            etNewPassword.setError("Password must be at least 4 characters");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            return;
        }

        showProgress(true);

        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("user_id", userId);
            jsonRequest.put("new_password", newPassword);
        } catch (Exception e) {
            e.printStackTrace();
        }

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonRequest.toString(), JSON);

        Request request = new Request.Builder()
                .url(RESET_PASSWORD_URL)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(ForgotPasswordActivity.this,
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
                        String message = jsonResponse.getString("message");

                        android.util.Log.e("FORGOT_PASSWORD", "Reset Response: " + responseBody);

                        Toast.makeText(ForgotPasswordActivity.this, message, Toast.LENGTH_LONG).show();

                        if (success) {
                            new AlertDialog.Builder(ForgotPasswordActivity.this)
                                    .setTitle("Success!")
                                    .setMessage("Password reset successful! Please login with your new password.")
                                    .setPositiveButton("Login", (dialog, which) -> finish())
                                    .setCancelable(false)
                                    .show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(ForgotPasswordActivity.this,
                                "Error: " + responseBody, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSendOtp.setEnabled(!show);
        btnVerifyOtp.setEnabled(!show);
        btnResetPassword.setEnabled(!show);
    }
}