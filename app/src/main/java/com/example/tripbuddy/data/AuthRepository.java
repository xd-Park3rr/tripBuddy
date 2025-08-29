package com.example.tripbuddy.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AuthRepository {
    private final TripBuddyDbHelper helper;

    public static class User {
        public long id;
        public String username;
        public String initials;
    public String firstName;
    public String lastName;
    }

    public AuthRepository(Context ctx) {
        this.helper = new TripBuddyDbHelper(ctx);
    }

    public long signUp(String username, String password, String firstName, String lastName) {
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) return -1;
        String initials = buildInitials(firstName, lastName, username);
        String hash = sha256(password);
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("username", username);
        cv.put("password_hash", hash);
        cv.put("first_name", firstName);
        cv.put("last_name", lastName);
        cv.put("initials", initials);
        cv.put("created_at", System.currentTimeMillis());
        return db.insert(TripBuddyDbHelper.T_USERS, null, cv);
    }

    public User login(String username, String password) {
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) return null;
        String hash = sha256(password);
        SQLiteDatabase db = helper.getReadableDatabase();
        // Support login by username or initials
        String where = "(username=? OR initials=?) AND password_hash=?";
        String[] args = new String[]{username, username, hash};
        try (Cursor c = db.query(TripBuddyDbHelper.T_USERS,
                new String[]{"id","username","initials","first_name","last_name"},
                where,
                args, null, null, null)) {
            if (c.moveToFirst()) {
                User u = new User();
                u.id = c.getLong(0);
                u.username = c.getString(1);
                u.initials = c.getString(2);
                u.firstName = c.getString(3);
                u.lastName = c.getString(4);
                return u;
            }
        }
        return null;
    }

    public User getUserById(long id) {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.query(TripBuddyDbHelper.T_USERS,
                new String[]{"id","username","initials","first_name","last_name"},
                "id=?", new String[]{String.valueOf(id)}, null, null, null)) {
            if (c.moveToFirst()) {
                User u = new User();
                u.id = c.getLong(0);
                u.username = c.getString(1);
                u.initials = c.getString(2);
                u.firstName = c.getString(3);
                u.lastName = c.getString(4);
                return u;
            }
        }
        return null;
    }

    private static String buildInitials(String first, String last, String fallbackUsername) {
        StringBuilder sb = new StringBuilder();
        if (!TextUtils.isEmpty(first)) sb.append(Character.toUpperCase(first.charAt(0)));
        if (!TextUtils.isEmpty(last)) sb.append(Character.toUpperCase(last.charAt(0)));
        if (sb.length() == 0 && !TextUtils.isEmpty(fallbackUsername)) {
            sb.append(Character.toUpperCase(fallbackUsername.charAt(0)));
        }
        return sb.toString();
    }

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] out = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : out) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return input; // fallback (not ideal)
        }
    }
}
