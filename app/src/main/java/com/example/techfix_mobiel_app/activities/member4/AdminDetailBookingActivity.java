package com.example.techfix_mobiel_app.activities.member4;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.example.techfix_mobiel_app.R;
import com.example.techfix_mobiel_app.activities.member2.database.dao.AppointmentDao;
import com.example.techfix_mobiel_app.activities.member2.database.entities.AppointmentEntity;
import com.example.techfix_mobiel_app.database.AppDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Executors;

public class AdminDetailBookingActivity extends AppCompatActivity {

    private TextView tvDetailRepairId, tvDetailBranch, tvDetailDevice, tvDetailCustomer, tvDetailIssue, tvDetailDescription, tvDetailPrice, tvDetailPayment;
    private LinearLayout layoutPriceSection, layoutCameraSection, layoutPaymentSection;
    private EditText etQuotationPrice;
    private MaterialButton btnSaveQuotation, btnCapturePhoto, btnPickGallery, btnUploadAndReady, btnTogglePayment;
    private ImageView ivCapturedPreview;

    private AppointmentDao appointmentDao;
    private int bookingId;
    private AppointmentEntity currentBooking;
    private Bitmap capturedBitmap;
    private static final int REQUEST_CAMERA_CODE = 101;
    private static final int REQUEST_GALLERY_CODE = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_detail_booking);

        appointmentDao = AppDatabase.getInstance(this).appointmentDao();
        bookingId = getIntent().getIntExtra("BOOKING_ID", -1);

        findViewById(R.id.btnBackDetail).setOnClickListener(v -> finish());

        tvDetailRepairId = findViewById(R.id.tvDetailRepairId);
        tvDetailBranch = findViewById(R.id.tvDetailBranch);
        tvDetailDevice = findViewById(R.id.tvDetailDevice);
        tvDetailCustomer = findViewById(R.id.tvDetailCustomer);
        tvDetailIssue = findViewById(R.id.tvDetailIssue);
        tvDetailDescription = findViewById(R.id.tvDetailDescription);
        tvDetailPrice = findViewById(R.id.tvDetailPrice);
        tvDetailPayment = findViewById(R.id.tvDetailPayment);

        layoutPriceSection = findViewById(R.id.layoutPriceSection);
        layoutCameraSection = findViewById(R.id.layoutCameraSection);
        layoutPaymentSection = findViewById(R.id.layoutPaymentSection);

        etQuotationPrice = findViewById(R.id.etQuotationPrice);
        btnSaveQuotation = findViewById(R.id.btnSaveQuotation);

        btnCapturePhoto = findViewById(R.id.btnCapturePhoto);
        btnPickGallery = findViewById(R.id.btnPickGallery);
        btnUploadAndReady = findViewById(R.id.btnUploadAndReady);
        ivCapturedPreview = findViewById(R.id.ivCapturedPreview);
        btnTogglePayment = findViewById(R.id.btnTogglePayment);

        loadBookingDetails();

        // Save price and set status to Awaiting Customer so it remains in Admin's Pending tab
        btnSaveQuotation.setOnClickListener(v -> {
            String priceStr = etQuotationPrice.getText().toString().trim();
            if (priceStr.isEmpty()) {
                Toast.makeText(this, "Please enter a valid price", Toast.LENGTH_SHORT).show();
                return;
            }

            if (currentBooking != null) {
                try {
                    double price = Double.parseDouble(priceStr);
                    currentBooking.price = price;
                    currentBooking.status = "Awaiting Customer";

                    Executors.newSingleThreadExecutor().execute(() -> {
                        appointmentDao.update(currentBooking);
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Quotation sent to customer for review!", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    });
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid number format", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Launch Camera Intent
        btnCapturePhoto.setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            try {
                startActivityForResult(intent, REQUEST_CAMERA_CODE);
            } catch (Exception e) {
                Toast.makeText(this, "Camera not available. Try choosing from gallery.", Toast.LENGTH_LONG).show();
            }
        });

        // Launch Gallery Intent as a reliable fallback for emulators
        btnPickGallery.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_GALLERY_CODE);
        });

        // Save photo file path to database, change status to Ready, and finish
        btnUploadAndReady.setOnClickListener(v -> {
            if (currentBooking != null) {
                currentBooking.status = "Ready";

                if (capturedBitmap != null) {
                    String savedPath = saveImageToInternalStorage(capturedBitmap);
                    if (savedPath != null) {
                        currentBooking.completionPhotoPath = savedPath;
                    }
                }

                Executors.newSingleThreadExecutor().execute(() -> {
                    appointmentDao.update(currentBooking);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Repair marked as Ready & photo uploaded!", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                });
            }
        });

        btnTogglePayment.setOnClickListener(v -> togglePaymentStatus());
    }

    private String saveImageToInternalStorage(Bitmap bitmap) {
        try {
            File cachePath = new File(getCacheDir(), "images");
            cachePath.mkdirs();
            File file = new File(cachePath, "repair_" + System.currentTimeMillis() + ".png");
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void loadBookingDetails() {
        if (bookingId == -1) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            currentBooking = appointmentDao.getAppointmentById(bookingId);

            runOnUiThread(() -> {
                if (currentBooking != null) {
                    tvDetailRepairId.setText("TF-000" + currentBooking.id);

                    String branchInfo = currentBooking.branchName != null && !currentBooking.branchName.isEmpty() ? currentBooking.branchName :
                            (currentBooking.assignedBranchId == 2 ? "Branch: TechFix - Galle Branch" : "Branch: TechFix - Colombo Branch");
                    tvDetailBranch.setText(branchInfo);

                    tvDetailDevice.setText(currentBooking.deviceName);

                    String custName = currentBooking.customerName != null && !currentBooking.customerName.isEmpty() ? currentBooking.customerName : "Customer #" + currentBooking.customerId;
                    tvDetailCustomer.setText("Customer: " + custName);

                    tvDetailIssue.setText("Issue: " + currentBooking.issueType);

                    tvDetailDescription.setText("Description: " + (currentBooking.description != null && !currentBooking.description.isEmpty() ? currentBooking.description : "No description provided."));

                    tvDetailPrice.setText("$" + (int) currentBooking.price);
                    tvDetailPayment.setText(currentBooking.paymentStatus != null ? currentBooking.paymentStatus : "Unpaid");

                    String status = currentBooking.status != null ? currentBooking.status : "";

                    if (status.equalsIgnoreCase("Pending") || status.equalsIgnoreCase("Awaiting Customer")) {
                        layoutPriceSection.setVisibility(View.VISIBLE);
                        layoutCameraSection.setVisibility(View.GONE);
                        layoutPaymentSection.setVisibility(View.GONE);
                        if (currentBooking.price > 0) {
                            etQuotationPrice.setText(String.valueOf((int) currentBooking.price));
                        }

                        // ADDED: Gray out and disable input/button if price was already sent
                        if (status.equalsIgnoreCase("Awaiting Customer")) {
                            etQuotationPrice.setEnabled(false);
                            etQuotationPrice.setAlpha(0.5f);
                            btnSaveQuotation.setEnabled(false);
                            btnSaveQuotation.setText("Price Already Sent");
                            btnSaveQuotation.setAlpha(0.6f);
                        } else {
                            etQuotationPrice.setEnabled(true);
                            etQuotationPrice.setAlpha(1.0f);
                            btnSaveQuotation.setEnabled(true);
                            btnSaveQuotation.setText("Save & Set Price");
                            btnSaveQuotation.setAlpha(1.0f);
                        }
                    } else {
                        layoutPriceSection.setVisibility(View.GONE);
                        layoutPaymentSection.setVisibility(View.VISIBLE);

                        if (status.equalsIgnoreCase("Approved") || status.equalsIgnoreCase("In Repair")) {
                            layoutCameraSection.setVisibility(View.VISIBLE);
                        } else {
                            layoutCameraSection.setVisibility(View.GONE);
                        }
                    }
                }
            });
        });
    }

    private void togglePaymentStatus() {
        if (currentBooking == null) return;

        String nextPayment = "Paid".equalsIgnoreCase(currentBooking.paymentStatus) ? "Unpaid" : "Paid";
        currentBooking.paymentStatus = nextPayment;

        Executors.newSingleThreadExecutor().execute(() -> {
            appointmentDao.update(currentBooking);
            runOnUiThread(() -> {
                tvDetailPayment.setText(nextPayment);
                Toast.makeText(this, "Payment marked as " + nextPayment, Toast.LENGTH_SHORT).show();
            });
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_CAMERA_CODE) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    capturedBitmap = (Bitmap) extras.get("data");
                    if (capturedBitmap != null) {
                        ivCapturedPreview.setImageBitmap(capturedBitmap);
                        ivCapturedPreview.setVisibility(View.VISIBLE);
                        btnUploadAndReady.setVisibility(View.VISIBLE);
                        Toast.makeText(this, "Photo captured successfully!", Toast.LENGTH_SHORT).show();
                    }
                }
            } else if (requestCode == REQUEST_GALLERY_CODE) {
                Uri imageUri = data.getData();
                try {
                    capturedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    if (capturedBitmap != null) {
                        ivCapturedPreview.setImageBitmap(capturedBitmap);
                        ivCapturedPreview.setVisibility(View.VISIBLE);
                        btnUploadAndReady.setVisibility(View.VISIBLE);
                        Toast.makeText(this, "Photo loaded from gallery!", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}