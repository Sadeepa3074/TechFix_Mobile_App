package com.example.techfix_mobiel_app.activities.member1.database;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.techfix_mobiel_app.R;
import com.example.techfix_mobiel_app.activities.member1.database.entities.UserEntity;
import com.example.techfix_mobiel_app.activities.member4.AdminDashboardActivity;
import com.example.techfix_mobiel_app.activities.member2.database.CustomerDashboardActivity;
import com.example.techfix_mobiel_app.network.ApiService;
import com.example.techfix_mobiel_app.network.RetrofitClient;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if user is already logged in before loading UI
        SharedPreferences prefs = getSharedPreferences("TechFixPrefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("is_logged_in", false);

        if (isLoggedIn) {
            String role = prefs.getString("user_role", "Customer");
            navigateToDashboard(role);
            return;
        }

        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView btnGoToRegister = findViewById(R.id.btnGoToRegister);

        btnLogin.setOnClickListener(v -> performLogin());

        btnGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void performLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        UserEntity loginPayload = new UserEntity("", email, password, "", 0);

        ApiService apiService = RetrofitClient.getApiService();
        apiService.loginUser(loginPayload).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> result = response.body();
                    String status = (String) result.get("status");

                    if ("success".equals(status)) {
                        Map<String, Object> userData = (Map<String, Object>) result.get("user");

                        String username = (String) userData.get("username");
                        String userEmail = (String) userData.get("email");
                        String role = (String) userData.get("role");

                        Toast.makeText(LoginActivity.this, "Welcome " + username + "!", Toast.LENGTH_SHORT).show();

                        SharedPreferences prefs = getSharedPreferences("TechFixPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean("is_logged_in", true);
                        editor.putString("logged_email", userEmail);
                        editor.putString("logged_username", username);
                        editor.putString("user_role", role);
                        editor.apply();

                        navigateToDashboard(role);
                    } else {
                        String message = (String) result.get("message");
                        Toast.makeText(LoginActivity.this, message != null ? message : "Invalid email or password", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Server error during login", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Connection failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToDashboard(String role) {
        Intent intent = null;

        if (role != null) {
            if (role.equalsIgnoreCase("Admin")) {
                intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
            } else if (role.equalsIgnoreCase("Customer")) {
                intent = new Intent(LoginActivity.this, CustomerDashboardActivity.class);
            }
        }

        if (intent != null) {
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(LoginActivity.this, "Error: Unknown role assigned.", Toast.LENGTH_SHORT).show();
        }
    }
}