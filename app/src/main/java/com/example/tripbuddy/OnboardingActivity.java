package com.example.tripbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class OnboardingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);
        TextView tv = findViewById(R.id.tv_tagline);
        tv.setText(getString(R.string.tagline_travel_buddy));
        Button btn = findViewById(R.id.btn_get_started);
        btn.setOnClickListener(v -> {
            PrefsManager prefs = new PrefsManager(this);
            prefs.setOnboarded(true);
            Intent next = new Intent(this, prefs.isLoggedIn() ? MainActivity.class : LoginActivity.class);
            startActivity(next);
            finish();
        });
    }
}
