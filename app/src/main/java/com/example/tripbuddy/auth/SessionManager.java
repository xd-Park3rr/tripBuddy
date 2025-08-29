package com.example.tripbuddy.auth;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.example.tripbuddy.PrefsManager;

public class SessionManager implements Application.ActivityLifecycleCallbacks {
    private final Application app;
    private int activityRefs = 0;
    private boolean isChangingConfig = false;

    public SessionManager(Application app) {
        this.app = app;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) { }

    @Override
    public void onActivityStarted(Activity activity) {
        if (++activityRefs == 1 && !isChangingConfig) {
            // App returns to foreground
        }
    }

    @Override
    public void onActivityResumed(Activity activity) { }

    @Override
    public void onActivityPaused(Activity activity) { }

    @Override
    public void onActivityStopped(Activity activity) {
        isChangingConfig = activity.isChangingConfigurations();
        if (--activityRefs == 0 && !isChangingConfig) {
            // App goes to background: enforce logout
            PrefsManager prefs = new PrefsManager(activity);
            prefs.setLoggedIn(false);
            prefs.setUserId(-1);
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) { }

    @Override
    public void onActivityDestroyed(Activity activity) { }
}
