package com.example.techfix_mobiel_app.activities.member1.database;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.techfix_mobiel_app.R;
import com.google.android.material.button.MaterialButton;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvProfileEmail, tvProfileRole;
    private MaterialButton btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvProfileRole = findViewById(R.id.tvProfileRole);
        btnLogout = findViewById(R.id.btnLogout);

        // Load user info
        SharedPreferences prefs = getSharedPreferences("TechFixPrefs", MODE_PRIVATE);
        String userEmail = prefs.getString("logged_email", "customer@techfix.com");

        tvProfileEmail.setText(userEmail);
        tvProfileRole.setText("Customer Account");

        btnLogout.setOnClickListener(v -> {
            // Reuse the existing 'prefs' variable instead of redeclaring it to avoid a compilation error
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}