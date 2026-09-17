package com.example.techfix_mobiel_app.activities.member1.database;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.techfix_mobiel_app.R;
import com.example.techfix_mobiel_app.activities.member1.database.entities.UserEntity;
import com.example.techfix_mobiel_app.network.ApiService;
import com.example.techfix_mobiel_app.network.RetrofitClient;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText etUsername, etEmail, etPassword;
    private final String selectedRole = "Customer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etUsername = findViewById(R.id.etRegUsername);
        etEmail = findViewById(R.id.etRegEmail);
        etPassword = findViewById(R.id.etRegPassword);
        Button btnRegisterSubmit = findViewById(R.id.btnRegisterSubmit);

        btnRegisterSubmit.setOnClickListener(v -> performRegistration());
    }

    private void performRegistration() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        UserEntity newUser = new UserEntity(username, email, password, selectedRole, 0);

        ApiService apiService = RetrofitClient.getApiService();
        apiService.registerUser(newUser).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> result = response.body();
                    String status = (String) result.get("status");

                    if ("success".equals(status)) {
                        Toast.makeText(RegisterActivity.this, "Customer Registration Successful!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        String message = (String) result.get("message");
                        Toast.makeText(RegisterActivity.this, message != null ? message : "Registration failed", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "Server error during registration", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e("API_ERROR", "Failed to connect to server: " + t.getMessage());
                Toast.makeText(RegisterActivity.this, "Connection failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}