package com.example.techfix_mobiel_app.activities.member3;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.techfix_mobiel_app.R;

public class UpdateRepairStatusActivity extends AppCompatActivity {

    private EditText etStatus, etNotes;
    private ImageView ivDevicePhoto;
    private Button btnCapture, btnSubmit;
    private Bitmap capturedBitmap;

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        capturedBitmap = (Bitmap) extras.get("data");
                        ivDevicePhoto.setImageBitmap(capturedBitmap);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_repair_status);

        etStatus = findViewById(R.id.etStatus);
        etNotes = findViewById(R.id.etNotes);
        ivDevicePhoto = findViewById(R.id.ivDevicePhoto);
        btnCapture = findViewById(R.id.btnCapturePhoto);
        btnSubmit = findViewById(R.id.btnSubmitUpdate);

        btnCapture.setOnClickListener(v -> {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                cameraLauncher.launch(takePictureIntent);
            } else {
                Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show();
            }
        });

        btnSubmit.setOnClickListener(v -> {
            String status = etStatus.getText().toString().trim();
            if (status.isEmpty()) {
                Toast.makeText(this, "Please enter a status", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "Repair status updated successfully!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}