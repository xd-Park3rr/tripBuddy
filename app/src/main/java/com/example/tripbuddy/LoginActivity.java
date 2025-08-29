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
    int signUpId = getResources().getIdentifier("btn_goto_signup", "id", getPackageName());
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
                    com.example.tripbuddy.data.AuthRepository repo = new com.example.tripbuddy.data.AuthRepository(LoginActivity.this);
                    com.example.tripbuddy.data.AuthRepository.User user = repo.login(u, p);
                    if (user == null) {
                        Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // Require initials to be non-empty and alphabetic before continuing
                    if (TextUtils.isEmpty(user.initials) || !user.initials.matches("[A-Z]{1,3}")) {
                        Toast.makeText(LoginActivity.this, "Initials invalid for login", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    PrefsManager prefs = new PrefsManager(LoginActivity.this);
                    prefs.setLoggedIn(true);
                    prefs.setUserId(user.id);
                    Intent i = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(i);
                    finish();
                }
            });
        }

        Button goSignUp = signUpId != 0 ? findViewById(signUpId) : null;
        if (goSignUp != null) {
            goSignUp.setOnClickListener(v -> {
                Intent i = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(i);
            });
        }
    }
}
