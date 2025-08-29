package com.example.tripbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private EditText username;
    private EditText password;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int layoutId = getResources().getIdentifier("activity_login", "layout", getPackageName());
        if (layoutId != 0) setContentView(layoutId);
        int userId = getResources().getIdentifier("et_username", "id", getPackageName());
        int passId = getResources().getIdentifier("et_password", "id", getPackageName());
        int btnId = getResources().getIdentifier("btn_login", "id", getPackageName());
        username = userId != 0 ? findViewById(userId) : null;
        password = passId != 0 ? findViewById(passId) : null;
        Button login = btnId != 0 ? findViewById(btnId) : null;
        if (login != null) {
            login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String u = username != null ? username.getText().toString().trim() : "";
                    String p = password != null ? password.getText().toString().trim() : "";
                    if (TextUtils.isEmpty(u) || TextUtils.isEmpty(p)) {
                        Toast.makeText(LoginActivity.this, "Enter username and password", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // Simple demo auth; in real apps validate securely/server-side
                    PrefsManager prefs = new PrefsManager(LoginActivity.this);
                    prefs.setLoggedIn(true);
                    Intent i = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(i);
                    finish();
                }
            });
        }
    }
}
