package com.example.tripbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tripbuddy.data.AuthRepository;

public class SignUpActivity extends AppCompatActivity {
    private EditText etFirst, etLast, etUsername, etPassword;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int layoutId = getResources().getIdentifier("activity_signup", "layout", getPackageName());
        if (layoutId != 0) setContentView(layoutId);

        int idFirst = getResources().getIdentifier("et_first", "id", getPackageName());
        int idLast = getResources().getIdentifier("et_last", "id", getPackageName());
        int idUser = getResources().getIdentifier("et_username", "id", getPackageName());
        int idPass = getResources().getIdentifier("et_password", "id", getPackageName());
        int idBtn = getResources().getIdentifier("btn_signup", "id", getPackageName());

        etFirst = idFirst != 0 ? findViewById(idFirst) : null;
        etLast = idLast != 0 ? findViewById(idLast) : null;
        etUsername = idUser != 0 ? findViewById(idUser) : null;
        etPassword = idPass != 0 ? findViewById(idPass) : null;
        Button btn = idBtn != 0 ? findViewById(idBtn) : null;

        if (btn != null) {
            btn.setOnClickListener(v -> {
                String first = etFirst != null ? etFirst.getText().toString().trim() : "";
                String last = etLast != null ? etLast.getText().toString().trim() : "";
                String user = etUsername != null ? etUsername.getText().toString().trim() : "";
                String pass = etPassword != null ? etPassword.getText().toString().trim() : "";
                if (TextUtils.isEmpty(user) || TextUtils.isEmpty(pass)) {
                    showErrorDialog("Missing info", "Username and password are required.");
                    return;
                }
                AuthRepository repo = new AuthRepository(this);
                long id = repo.signUp(user, pass, first, last);
                if (id <= 0) {
                    showErrorDialog("Sign up failed", "That username may already exist. Please try a different one.");
                    return;
                }
                Toast.makeText(this, "Account created. Please log in.", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(this, LoginActivity.class);
                startActivity(i);
                finish();
            });
        }
    }

    private void showErrorDialog(String title, String message) {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (d, which) -> d.dismiss())
                .show();
    }
}
