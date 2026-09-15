package com.example.techfix_mobiel_app.activities.member3.database;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.example.techfix_mobiel_app.R;

public class AboutUsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        // 1. Back Button Action to return to the dashboard
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // 2. Social Media Links Click Listeners
        View btnFacebook = findViewById(R.id.btnFacebook);
        if (btnFacebook != null) {
            btnFacebook.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/techfix"));
                startActivity(intent);
            });
        }

        View btnInstagram = findViewById(R.id.btnInstagram);
        if (btnInstagram != null) {
            btnInstagram.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/techfix"));
                startActivity(intent);
            });
        }

        View btnTiktok = findViewById(R.id.btnTiktok);
        if (btnTiktok != null) {
            btnTiktok.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.tiktok.com/@techfix"));
                startActivity(intent);
            });
        }
    }
}