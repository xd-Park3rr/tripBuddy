package com.example.tripbuddy;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public class PrefsManager {
    private static final String PREFS = "tripbuddy_prefs";
    private static final String KEY_LOGGED_IN = "logged_in";
    private static final String KEY_TRIP_COUNT = "trip_count";
    private static final String KEY_MUSIC_ENABLED = "music_enabled";
    private static final String KEY_THEME_MODE = "theme_mode"; // -1 follow system, 1 light, 2 dark
    private static final String KEY_LANGUAGE = "language"; // e.g., en, fr
    private static final String KEY_LAST_TRIP_ID = "last_trip_id";
    private static final String KEY_GALLERY_SEEDED = "gallery_seeded";
    private static final String KEY_ONBOARDED = "onboarded";

    private final SharedPreferences prefs;

    public PrefsManager(Context ctx) {
        prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_LOGGED_IN, false);
    }

    public void setLoggedIn(boolean value) {
        prefs.edit().putBoolean(KEY_LOGGED_IN, value).apply();
    }

    public int getTripCount() {
        return prefs.getInt(KEY_TRIP_COUNT, 0);
    }

    public void incrementTripCount() {
        prefs.edit().putInt(KEY_TRIP_COUNT, getTripCount() + 1).apply();
    }

    public boolean isMusicEnabled() {
        return prefs.getBoolean(KEY_MUSIC_ENABLED, true);
    }

    public void setMusicEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_MUSIC_ENABLED, enabled).apply();
    }

    public int getThemeMode() {
        return prefs.getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    public void setThemeMode(int mode) {
        prefs.edit().putInt(KEY_THEME_MODE, mode).apply();
    }

    public String getLanguage() {
        return prefs.getString(KEY_LANGUAGE, "en");
    }

    public void setLanguage(String lang) {
        prefs.edit().putString(KEY_LANGUAGE, lang).apply();
    }

    public long getLastTripId() {
        return prefs.getLong(KEY_LAST_TRIP_ID, -1);
    }

    public void setLastTripId(long id) {
        prefs.edit().putLong(KEY_LAST_TRIP_ID, id).apply();
    }

    public boolean isGallerySeeded() {
        return prefs.getBoolean(KEY_GALLERY_SEEDED, false);
    }

    public void setGallerySeeded(boolean seeded) {
        prefs.edit().putBoolean(KEY_GALLERY_SEEDED, seeded).apply();
    }

    public boolean isOnboarded() {
        return prefs.getBoolean(KEY_ONBOARDED, false);
    }

    public void setOnboarded(boolean value) {
        prefs.edit().putBoolean(KEY_ONBOARDED, value).apply();
    }
}
