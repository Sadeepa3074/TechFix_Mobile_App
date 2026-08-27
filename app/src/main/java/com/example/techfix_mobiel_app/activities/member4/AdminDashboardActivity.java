package com.example.techfix_mobiel_app.activities.member4;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.techfix_mobiel_app.R;

public class AdminDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        Button btnManageInventory = findViewById(R.id.btnManageInventory);
        btnManageInventory.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, ManageInventoryActivity.class);
            startActivity(intent);
        });
    }
}