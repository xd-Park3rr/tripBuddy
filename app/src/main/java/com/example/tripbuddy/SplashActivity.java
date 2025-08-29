package com.example.tripbuddy;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PrefsManager prefs = new PrefsManager(this);
        Intent next;
        if (!prefs.isOnboarded()) {
            next = new Intent(this, OnboardingActivity.class);
        } else if (prefs.isLoggedIn()) {
            next = new Intent(this, MainActivity.class);
        } else {
            next = new Intent(this, LoginActivity.class);
        }
        startActivity(next);
        finish();
    }
}
