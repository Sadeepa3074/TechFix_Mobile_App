package com.example.techfix_mobiel_app.activities.member3.database;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.techfix_mobiel_app.activities.member2.database.TrackStatusActivity;
import com.google.android.material.button.MaterialButton;
import com.example.techfix_mobiel_app.R;
import com.example.techfix_mobiel_app.activities.member2.database.dao.AppointmentDao;
import com.example.techfix_mobiel_app.activities.member2.database.entities.AppointmentEntity;
import com.example.techfix_mobiel_app.database.AppDatabase;
import com.example.techfix_mobiel_app.network.ApiService;
import com.example.techfix_mobiel_app.network.RetrofitClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentGatewayActivity extends AppCompatActivity {

    private EditText etCardNumber, etExpiry, etCvv;
    private MaterialButton btnPayNow;
    private int bookingId = -1;
    private double amount = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_gateway);

        bookingId = getIntent().getIntExtra("BOOKING_ID", -1);

        amount = getIntent().getDoubleExtra("AMOUNT", 0.0);
        if (amount == 0.0) {
            amount = getIntent().getDoubleExtra("TOTAL_AMOUNT", 0.0);
        }

        findViewById(R.id.btnBackPayment).setOnClickListener(v -> finish());

        etCardNumber = findViewById(R.id.etCardNumber);
        etExpiry = findViewById(R.id.etExpiry);
        etCvv = findViewById(R.id.etCvv);
        btnPayNow = findViewById(R.id.btnPayNow);

        btnPayNow.setText("Pay $" + (int) amount);

        btnPayNow.setOnClickListener(v -> {
            String card = etCardNumber.getText().toString().trim();
            if (card.isEmpty() || card.length() < 15) {
                Toast.makeText(this, "Enter a valid card number", Toast.LENGTH_SHORT).show();
                return;
            }

            if (bookingId != -1) {
                // Appointment Booking Payment Flow
                Executors.newSingleThreadExecutor().execute(() -> {
                    AppointmentDao dao = AppDatabase.getInstance(this).appointmentDao();
                    AppointmentEntity booking = dao.getAppointmentById(bookingId);
                    if (booking != null) {
                        booking.paymentStatus = "Paid";
                        booking.status = "Completed";
                        dao.update(booking);

                        runOnUiThread(() -> {
                            Toast.makeText(this, "Payment Successful! Moved to history.", Toast.LENGTH_LONG).show();

                            Intent intent = new Intent(PaymentGatewayActivity.this, TrackStatusActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                            finish();
                        });
                    }
                });
            } else {
                // Spare Parts Checkout Payment Flow
                // 1. Accumulate total spare parts revenue immediately in persistent preferences
                SharedPreferences revenuePrefs = getSharedPreferences("TechFixRevenuePrefs", MODE_PRIVATE);
                float currentRevenue = revenuePrefs.getFloat("TOTAL_SPARE_PARTS_REVENUE", 0f);
                revenuePrefs.edit()
                        .putFloat("TOTAL_SPARE_PARTS_REVENUE", currentRevenue + (float) amount)
                        .apply();

                // 2. Deduct quantities from MySQL database via Retrofit
                SharedPreferences prefs = getSharedPreferences("TechFixPrefs", MODE_PRIVATE);
                String cartJson = prefs.getString("CART_ITEMS_JSON", "[]");

                try {
                    JSONArray array = new JSONArray(cartJson);
                    ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        int partId = obj.getInt("id");
                        int purchasedQty = obj.getInt("qty");

                        Map<String, Integer> payload = new HashMap<>();
                        payload.put("id", partId);
                        payload.put("qty", purchasedQty);

                        apiService.reduceStock(payload).enqueue(new Callback<Map<String, Object>>() {
                            @Override
                            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                                // Handled on server
                            }

                            @Override
                            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                                // Handle failure if needed
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // 3. Clear user active cart
                prefs.edit()
                        .remove("CART_ITEMS_JSON")
                        .putInt("CART_COUNT", 0)
                        .apply();

                Toast.makeText(this, "Payment Successful! Spare parts reserved.", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(PaymentGatewayActivity.this, CustomerSparePartsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });
    }
}