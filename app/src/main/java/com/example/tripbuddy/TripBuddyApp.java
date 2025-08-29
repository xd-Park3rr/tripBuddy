package com.example.tripbuddy;

import android.app.Application;

import com.google.android.material.color.DynamicColors;

public class TripBuddyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DynamicColors.applyToActivitiesIfAvailable(this);
    }
}

