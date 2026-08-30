package com.example.techfix_mobiel_app.activities.member2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.techfix_mobiel_app.R;
import com.example.techfix_mobiel_app.activities.member2.database.Member2Database;
import com.example.techfix_mobiel_app.activities.member2.database.entities.QuotationEntity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.concurrent.Executors;

public class QuotationActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private EditText etCategory, etDescription, etQuantity;
    private TextView tvLocation;
    private Button btnGetLocation, btnSubmitQuote;
    private Member2Database db;
    private FusedLocationProviderClient fusedLocationClient;

    private double currentLat = 0.0;
    private double currentLng = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quotation);

        db = Member2Database.getInstance(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        etCategory = findViewById(R.id.etCategory);
        etDescription = findViewById(R.id.etDescription);
        etQuantity = findViewById(R.id.etQuantity);
        tvLocation = findViewById(R.id.tvLocation);
        btnGetLocation = findViewById(R.id.btnGetLocation);
        btnSubmitQuote = findViewById(R.id.btnSubmitQuote);

        btnGetLocation.setOnClickListener(v -> fetchLocation());
        btnSubmitQuote.setOnClickListener(v -> submitQuotation());
    }

    private void fetchLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                currentLat = location.getLatitude();
                currentLng = location.getLongitude();
                tvLocation.setText("Location: " + currentLat + ", " + currentLng);
                Toast.makeText(this, "Location captured successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Unable to fetch location. Turn on GPS.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchLocation();
        } else {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    private void submitQuotation() {
        String category = etCategory.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String qtyStr = etQuantity.getText().toString().trim();

        if (category.isEmpty() || description.isEmpty() || qtyStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int quantity = Integer.parseInt(qtyStr);

        Executors.newSingleThreadExecutor().execute(() -> {
            QuotationEntity quote = new QuotationEntity(category, description, quantity, "Pending", 0.0);
            db.quotationDao().insertQuotation(quote);
            runOnUiThread(() -> {
                Toast.makeText(QuotationActivity.this, "Quotation Submitted with GPS Data!", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}