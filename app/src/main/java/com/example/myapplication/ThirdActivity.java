package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

public class ThirdActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third);

        TextInputEditText usernameEditText = findViewById(R.id.usernameLoginEditText);
        TextInputEditText passwordEditText = findViewById(R.id.passwordLoginEditText);
        Button loginButton = findViewById(R.id.loginButton);
        TextView forgotPasswordText = findViewById(R.id.forgotPasswordLoginText);
        TextView registerRedirectText = findViewById(R.id.registerRedirectText);
        View loadingOverlay = findViewById(R.id.loadingOverlay);

        loginButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
                return;
            }

            // Retrieve stored password
            SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            String storedPassword = preferences.getString(username, null);

            if (storedPassword == null) {
                Toast.makeText(this, "Username not found. Please Sign Up first!", Toast.LENGTH_LONG).show();
            } else if (!storedPassword.equals(password)) {
                Toast.makeText(this, "Incorrect password. Please try again.", Toast.LENGTH_LONG).show();
            } else {
                // SUCCESS: Start Loading
                loadingOverlay.setVisibility(View.VISIBLE);
                
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    // Save session
                    SharedPreferences sessionPref = getSharedPreferences("AppSession", MODE_PRIVATE);
                    sessionPref.edit().putString("currentUser", username).apply();

                    Toast.makeText(this, "Welcome back, " + username + "!", Toast.LENGTH_SHORT).show();
                    
                    Intent intent = new Intent(ThirdActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                }, 1500); // 1.5 second delay for better feel
            }
        });

        forgotPasswordText.setOnClickListener(v -> {
            Intent intent = new Intent(ThirdActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        registerRedirectText.setOnClickListener(v -> {
            Intent intent = new Intent(ThirdActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }
}