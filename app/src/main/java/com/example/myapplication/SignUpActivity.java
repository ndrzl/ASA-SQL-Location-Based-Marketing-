package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import com.google.firebase.messaging.FirebaseMessaging;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SignUpActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private TextInputEditText nameEditText, usernameEditText, phoneEditText, addressEditText, emailEditText,
            passwordEditText, confirmPasswordEditText;
    private TextInputLayout nameLayout, usernameLayout, phoneLayout, addressLayout, emailLayout, passwordLayout,
            confirmPasswordLayout;

    private final ActivityResultLauncher<String> getContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    profileImageView.setImageURI(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        profileImageView = findViewById(R.id.profileImageView);
        nameEditText = findViewById(R.id.nameEditText);
        usernameEditText = findViewById(R.id.usernameEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        addressEditText = findViewById(R.id.addressEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);

        nameLayout = findViewById(R.id.nameLayout);
        usernameLayout = findViewById(R.id.usernameLayout);
        phoneLayout = findViewById(R.id.phoneLayout);
        addressLayout = findViewById(R.id.addressLayout);
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout);

        Button uploadButton = findViewById(R.id.uploadButton);
        Spinner genderSpinner = findViewById(R.id.genderSpinner);
        Spinner daySpinner = findViewById(R.id.daySpinner);
        Spinner monthSpinner = findViewById(R.id.monthSpinner);
        Spinner yearSpinner = findViewById(R.id.yearSpinner);
        Button createAccountButton = findViewById(R.id.createAccountButton);
        TextView loginRedirectText = findViewById(R.id.loginRedirectText);

        // Setup Gender Spinner
        String[] genders = { "Male", "Female", "Other" };
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genders);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(genderAdapter);

        // Setup Day Spinner (1-31)
        List<String> days = new ArrayList<>();
        for (int d = 1; d <= 31; d++)
            days.add(String.valueOf(d));
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, days);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySpinner.setAdapter(dayAdapter);

        // Setup Month Spinner
        String[] months = { "January", "February", "March", "April", "May", "June", "July", "August", "September",
                "October", "November", "December" };
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(monthAdapter);

        // Setup Year Spinner (Current year back to 1900)
        List<String> years = new ArrayList<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int y = currentYear; y >= 1900; y--)
            years.add(String.valueOf(y));
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(yearAdapter);

        uploadButton.setOnClickListener(v -> getContent.launch("image/*"));

        createAccountButton.setOnClickListener(v -> {
            clearErrors();

            String fullName = nameEditText.getText().toString().trim();
            String username = usernameEditText.getText().toString().trim();
            String phone = phoneEditText.getText().toString().trim();
            String address = addressEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString();
            String confirmPassword = confirmPasswordEditText.getText().toString();

            boolean isValid = true;

            if (fullName.isEmpty()) {
                nameLayout.setError("Full name is required");
                isValid = false;
            }
            if (username.isEmpty()) {
                usernameLayout.setError("Username is required");
                isValid = false;
            }
            if (phone.isEmpty()) {
                phoneLayout.setError("Phone number is required");
                isValid = false;
            }
            if (address.isEmpty()) {
                addressLayout.setError("Address is required");
                isValid = false;
            }
            if (email.isEmpty()) {
                emailLayout.setError("Email is required");
                isValid = false;
            }

            if (password.isEmpty()) {
                passwordLayout.setError("Password is required");
                isValid = false;
            } else if (!isValidPassword(password)) {
                passwordLayout.setError("Must be at least 8 characters with letters, numbers, and special characters.");
                isValid = false;
            }

            if (confirmPassword.isEmpty()) {
                confirmPasswordLayout.setError("Please confirm your password");
                isValid = false;
            } else if (!password.equals(confirmPassword)) {
                confirmPasswordLayout.setError("Passwords do not match!");
                isValid = false;
            }

            if (isValid) {
                String selectedGender = genderSpinner.getSelectedItem().toString();
                String selectedDay = daySpinner.getSelectedItem().toString();
                String selectedMonth = monthSpinner.getSelectedItem().toString();
                String selectedYear = yearSpinner.getSelectedItem().toString();
                String dob = selectedDay + "/" + selectedMonth + "/" + selectedYear;

                // Fetch real FCM token before signing up
                FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                    String fcmToken = "";
                    if (task.isSuccessful() && task.getResult() != null) {
                        fcmToken = task.getResult();
                    }
                    final String finalFcmToken = fcmToken;

                    JSONObject jsonBody = new JSONObject();
                    try {
                        jsonBody.put("username", username);
                        jsonBody.put("email", email);
                        jsonBody.put("password", password);
                        jsonBody.put("fullName", fullName);
                        jsonBody.put("phone", phone);
                        jsonBody.put("address", address);
                        jsonBody.put("gender", selectedGender);
                        jsonBody.put("dob", dob);
                        jsonBody.put("fcm_token", finalFcmToken);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error building request", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    OkHttpClient client = new OkHttpClient();
                    RequestBody body = RequestBody.create(
                            jsonBody.toString(),
                            MediaType.parse("application/json"));
                    Request request = new Request.Builder()
                            .url("http://172.20.10.3:3000/signup")
                            .post(body)
                            .build();

                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            runOnUiThread(() -> Toast
                                    .makeText(SignUpActivity.this, "Network error: " + e.getMessage(), Toast.LENGTH_SHORT)
                                    .show());
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String responseBody = response.body().string();
                            runOnUiThread(() -> {
                                if (response.isSuccessful()) {
                                    // ✅ Save password so login can verify it
                                    SharedPreferences userPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                                    userPrefs.edit().putString(username, password).apply();

                                    // Save session
                                    SharedPreferences sessionPref = getSharedPreferences("AppSession", MODE_PRIVATE);
                                    sessionPref.edit().putString("currentUser", username).apply();

                                    Toast.makeText(SignUpActivity.this,
                                            "Welcome " + username + "! Account Created. Please login.", Toast.LENGTH_LONG)
                                            .show();
                                    Intent intent = new Intent(SignUpActivity.this, ThirdActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(SignUpActivity.this, "Signup failed: " + responseBody,
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                        } // ← closes onResponse
                    }); // ← closes enqueue / Callback
                }); // ← closes getToken
            } else {
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            }
        }); // ← closes createAccountButton.setOnClickListener

        loginRedirectText.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, ThirdActivity.class);
            startActivity(intent);
        });
    } // ← closes onCreate

    private void clearErrors() {
        nameLayout.setError(null);
        usernameLayout.setError(null);
        phoneLayout.setError(null);
        addressLayout.setError(null);
        emailLayout.setError(null);
        passwordLayout.setError(null);
        confirmPasswordLayout.setError(null);
    }

    private boolean isValidPassword(String password) {
        Pattern pattern = Pattern.compile("^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[^a-zA-Z\\d\\s]).{8,}$");
        return pattern.matcher(password).matches();
    }
} // ← closes class