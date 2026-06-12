package com.example.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private TextInputEditText editFullName, editPhone, editAddress, editEmail;
    private Spinner editGenderSpinner, editYearSpinner, editMonthSpinner, editDaySpinner;
    private String currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        editFullName = findViewById(R.id.editFullName);
        editPhone = findViewById(R.id.editPhone);
        editAddress = findViewById(R.id.editAddress);
        editEmail = findViewById(R.id.editEmail);
        editGenderSpinner = findViewById(R.id.editGenderSpinner);
        editYearSpinner = findViewById(R.id.editYearSpinner);
        editMonthSpinner = findViewById(R.id.editMonthSpinner);
        editDaySpinner = findViewById(R.id.editDaySpinner);
        Button updateButton = findViewById(R.id.updateProfileButton);

        setupSpinners();

        // Get current user session
        SharedPreferences sessionPref = getSharedPreferences("AppSession", MODE_PRIVATE);
        currentUser = sessionPref.getString("currentUser", null);

        if (currentUser != null) {
            loadUserProfile();
        }

        updateButton.setOnClickListener(v -> saveUserProfile());
    }

    private void setupSpinners() {
        // Setup Gender Spinner
        String[] genders = {"Male", "Female", "Other"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genders);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editGenderSpinner.setAdapter(genderAdapter);

        // Setup Day Spinner (1-31)
        List<String> days = new ArrayList<>();
        for (int d = 1; d <= 31; d++) days.add(String.valueOf(d));
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, days);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editDaySpinner.setAdapter(dayAdapter);

        // Setup Month Spinner
        String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editMonthSpinner.setAdapter(monthAdapter);

        // Setup Year Spinner
        List<String> years = new ArrayList<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int y = currentYear; y >= 1900; y--) years.add(String.valueOf(y));
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editYearSpinner.setAdapter(yearAdapter);
    }

    private void loadUserProfile() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        editFullName.setText(prefs.getString(currentUser + "_fullName", ""));
        editPhone.setText(prefs.getString(currentUser + "_phone", ""));
        editAddress.setText(prefs.getString(currentUser + "_address", ""));
        editEmail.setText(prefs.getString(currentUser + "_email", ""));
        
        editGenderSpinner.setSelection(prefs.getInt(currentUser + "_gender", 0));
        
        // Load Year
        String savedYear = prefs.getString(currentUser + "_year", "");
        if (!savedYear.isEmpty()) {
            ArrayAdapter adapter = (ArrayAdapter) editYearSpinner.getAdapter();
            editYearSpinner.setSelection(adapter.getPosition(savedYear));
        }

        // Load Month
        editMonthSpinner.setSelection(prefs.getInt(currentUser + "_month", 0));

        // Load Day
        String savedDay = prefs.getString(currentUser + "_day", "");
        if (!savedDay.isEmpty()) {
            ArrayAdapter adapter = (ArrayAdapter) editDaySpinner.getAdapter();
            editDaySpinner.setSelection(adapter.getPosition(savedDay));
        }
    }

    private void saveUserProfile() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        editor.putString(currentUser + "_fullName", editFullName.getText().toString().trim());
        editor.putString(currentUser + "_phone", editPhone.getText().toString().trim());
        editor.putString(currentUser + "_address", editAddress.getText().toString().trim());
        editor.putString(currentUser + "_email", editEmail.getText().toString().trim());
        
        editor.putInt(currentUser + "_gender", editGenderSpinner.getSelectedItemPosition());
        editor.putString(currentUser + "_year", (String) editYearSpinner.getSelectedItem());
        editor.putInt(currentUser + "_month", editMonthSpinner.getSelectedItemPosition());
        editor.putString(currentUser + "_day", (String) editDaySpinner.getSelectedItem());

        editor.apply();

        Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }
}