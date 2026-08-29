package com.example.techfix_mobiel_app.activities.member3;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.techfix_mobiel_app.R;
import com.example.techfix_mobiel_app.activities.member3.database.AppDatabase;
import com.example.techfix_mobiel_app.activities.member3.database.entities.RepairJobEntity;

import java.util.concurrent.Executors;

public class UpdateRepairStatusActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 100;
    private EditText etDeviceName, etStatus;
    private Button btnCapturePhoto, btnSaveJob;
    private ImageView imgPreview;
    private AppDatabase db;
    private String currentPhotoPath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_repair_status);

        db = AppDatabase.getInstance(this);

        etDeviceName = findViewById(R.id.etDeviceName);
        etStatus = findViewById(R.id.etStatus);
        btnCapturePhoto = findViewById(R.id.btnCapturePhoto);
        btnSaveJob = findViewById(R.id.btnSaveJob);
        imgPreview = findViewById(R.id.imgPreview);

        btnCapturePhoto.setOnClickListener(v -> openCamera());
        btnSaveJob.setOnClickListener(v -> saveJobToDatabase());
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
            imgPreview.setImageBitmap(imageBitmap);
            currentPhotoPath = "captured_image_placeholder";
        }
    }

    private void saveJobToDatabase() {
        String device = etDeviceName.getText().toString().trim();
        String status = etStatus.getText().toString().trim();

        if (device.isEmpty() || status.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        RepairJobEntity job = new RepairJobEntity(device, status, currentPhotoPath);

        Executors.newSingleThreadExecutor().execute(() -> {
            db.repairJobDao().insertJob(job);
            runOnUiThread(() -> {
                Toast.makeText(getApplicationContext(), "Repair Job Saved!", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}