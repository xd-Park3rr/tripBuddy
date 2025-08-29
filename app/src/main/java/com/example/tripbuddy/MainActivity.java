package com.example.tripbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tripbuddy.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

    setSupportActionBar(binding.appBarMain.toolbar);
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
    mAppBarConfiguration = new AppBarConfiguration.Builder(
        R.id.nav_home, R.id.nav_gallery,
        R.id.nav_trip_planner, R.id.nav_budget_summary, R.id.nav_settings)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

    // Bottom navigation removed to keep navigation solely in the sidebar

        // Enhance drawer header: show dynamic trip count and initials
        View header = navigationView.getHeaderView(0);
    if (header != null) {
            TextView tvTrips = header.findViewById(R.id.tv_trip_count);
            TextView tvInitials = header.findViewById(R.id.tv_avatar_initials);
            TextView tvHeaderTitle = header.findViewById(R.id.tv_header_title);
            PrefsManager prefs = new PrefsManager(this);
            if (tvTrips != null) {
                int tripCount = new com.example.tripbuddy.data.TripRepository(this).getTripCount();
                tvTrips.setText("Trips: " + tripCount);
            }
            long uid = prefs.getUserId();
            com.example.tripbuddy.data.AuthRepository.User u = null;
            if (uid > 0) {
                u = new com.example.tripbuddy.data.AuthRepository(this).getUserById(uid);
            }
            if (tvInitials != null) {
                String initials = (u != null && u.initials != null && !u.initials.isEmpty()) ? u.initials : "TB";
                tvInitials.setText(initials);
            }
            if (tvHeaderTitle != null && u != null) {
                String fullName = (u.firstName != null ? u.firstName : "").trim() +
                        (u.lastName != null && !u.lastName.isEmpty() ? " " + u.lastName.trim() : "");
                if (!fullName.trim().isEmpty()) {
                    tvHeaderTitle.setText(fullName.trim());
                }
            }
        }

        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_logout) {
                new PrefsManager(MainActivity.this).setLoggedIn(false);
                Intent i = new Intent(MainActivity.this, LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();
                return true;
            }
            boolean handled = NavigationUI.onNavDestinationSelected(item, navController);
            if (handled) drawer.closeDrawers();
            return handled;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // If user was auto-logged out while app was in background, route to Login
        PrefsManager prefs = new PrefsManager(this);
        if (!prefs.isLoggedIn()) {
            Intent i = new Intent(this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        }
    }
}