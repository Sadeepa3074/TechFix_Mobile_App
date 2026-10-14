package com.example.techfix_mobiel_app.activities.member3.database;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.example.techfix_mobiel_app.R;
import com.example.techfix_mobiel_app.activities.member2.database.dao.AppointmentDao;
import com.example.techfix_mobiel_app.activities.member2.database.entities.AppointmentEntity;
import com.example.techfix_mobiel_app.database.AppDatabase;

import java.io.File;
import java.util.concurrent.Executors;

public class CustomerReviewActivity extends AppCompatActivity {

    private ImageView ivRepairProof;
    private TextView tvReviewPrice, tvReviewStatus;
    private MaterialButton btnProceedToPay;
    private AppointmentEntity currentBooking;
    private int bookingId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_review);

        bookingId = getIntent().getIntExtra("BOOKING_ID", -1);
        findViewById(R.id.btnBackReview).setOnClickListener(v -> finish());

        ivRepairProof = findViewById(R.id.ivRepairProof);
        tvReviewPrice = findViewById(R.id.tvReviewPrice);
        tvReviewStatus = findViewById(R.id.tvReviewStatus);
        btnProceedToPay = findViewById(R.id.btnProceedToPay);

        loadBookingData();

        btnProceedToPay.setOnClickListener(v -> {
            if (currentBooking != null) {
                Intent intent = new Intent(this, PaymentGatewayActivity.class);
                intent.putExtra("BOOKING_ID", currentBooking.id);
                intent.putExtra("AMOUNT", currentBooking.price);
                startActivity(intent);
            }
        });
    }

    private void loadBookingData() {
        if (bookingId == -1) return;

        AppointmentDao dao = AppDatabase.getInstance(this).appointmentDao();
        Executors.newSingleThreadExecutor().execute(() -> {
            currentBooking = dao.getAppointmentById(bookingId);
            runOnUiThread(() -> {
                if (currentBooking != null) {
                    tvReviewPrice.setText("Total Due: $" + (int) currentBooking.price);
                    tvReviewStatus.setText("Payment: " + (currentBooking.paymentStatus != null ? currentBooking.paymentStatus : "Unpaid"));

                    if (currentBooking.completionPhotoPath != null) {
                        File imgFile = new File(currentBooking.completionPhotoPath);
                        if (imgFile.exists()) {
                            ivRepairProof.setImageBitmap(BitmapFactory.decodeFile(imgFile.getAbsolutePath()));
                        }
                    }

                    if ("Paid".equalsIgnoreCase(currentBooking.paymentStatus)) {
                        btnProceedToPay.setText("Already Paid");
                        btnProceedToPay.setEnabled(false);
                    }
                }
            });
        });
    }
}