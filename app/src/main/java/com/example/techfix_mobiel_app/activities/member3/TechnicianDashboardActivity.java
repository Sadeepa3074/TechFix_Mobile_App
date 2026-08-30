package com.example.techfix_mobiel_app.activities.member3;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.techfix_mobiel_app.R;

public class TechnicianDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_technician_dashboard);

        Button btnUpdateRepair = findViewById(R.id.btnUpdateRepair);
        btnUpdateRepair.setOnClickListener(v -> {
            Intent intent = new Intent(TechnicianDashboardActivity.this, UpdateRepairStatusActivity.class);
            startActivity(intent);
        });
    }
}