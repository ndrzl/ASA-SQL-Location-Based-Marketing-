package com.example.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.regex.Pattern;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputEditText usernameEditText, newPasswordEditText, confirmNewPasswordEditText;
    private TextInputLayout usernameLayout, newPasswordLayout, confirmNewPasswordLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        

        usernameEditText = findViewById(R.id.usernameResetEditText);
        newPasswordEditText = findViewById(R.id.newPasswordEditText);
        confirmNewPasswordEditText = findViewById(R.id.confirmNewPasswordEditText);
        
        usernameLayout = findViewById(R.id.usernameResetLayout);
        newPasswordLayout = findViewById(R.id.newPasswordLayout);
        confirmNewPasswordLayout = findViewById(R.id.confirmNewPasswordLayout);
        
        Button resetButton = findViewById(R.id.resetButton);

        resetButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String newPassword = newPasswordEditText.getText().toString();
            String confirmNewPassword = confirmNewPasswordEditText.getText().toString();

            clearErrors();

            if (username.isEmpty()) {
                usernameLayout.setError("Username is required");
            } else if (newPassword.isEmpty()) {
                newPasswordLayout.setError("New password is required");
            } else if (!isValidPassword(newPassword)) {
                newPasswordLayout.setError("Must be at least 8 characters with letters, numbers, and special characters.");
            } else if (!newPassword.equals(confirmNewPassword)) {
                confirmNewPasswordLayout.setError("Passwords do not match!");
            } else {
                updatePassword(username, newPassword);
            }
        });
    }

    private void clearErrors() {
        usernameLayout.setError(null);
        newPasswordLayout.setError(null);
        confirmNewPasswordLayout.setError(null);
    }

    private void updatePassword(String username, String newPassword) {
        SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        
        // Check if user exists
        if (!preferences.contains(username)) {
            Toast.makeText(this, "Username not found. Cannot reset password.", Toast.LENGTH_LONG).show();
            return;
        }

        // Update the password in SharedPreferences
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(username, newPassword);
        editor.apply();

        Toast.makeText(this, "Password updated successfully! Please login.", Toast.LENGTH_LONG).show();
        finish(); // Go back to login page
    }

    private boolean isValidPassword(String password) {
        Pattern pattern = Pattern.compile("^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[^a-zA-Z\\d\\s]).{8,}$");
        return pattern.matcher(password).matches();
    }
}