package com.example.tripbuddy;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_splash);
        PrefsManager prefs = new PrefsManager(this);
        Intent next;
        if (!prefs.isLoggedIn()) {
            next = new Intent(this, LoginActivity.class);
        } else {
            next = new Intent(this, MainActivity.class);
        }
        startActivity(next);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // if we ever get resumed while not logged in, ensure we go to Login
        PrefsManager prefs = new PrefsManager(this);
        if (!prefs.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }
}
