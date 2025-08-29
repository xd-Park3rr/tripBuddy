package com.example.tripbuddy.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.tripbuddy.PrefsManager;

public class SettingsFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        int layoutId = getResources().getIdentifier("fragment_settings", "layout", requireContext().getPackageName());
        View v = inflater.inflate(layoutId != 0 ? layoutId : android.R.layout.simple_list_item_1, container, false);
        PrefsManager prefs = new PrefsManager(requireContext());

        int swId = getResources().getIdentifier("sw_music", "id", requireContext().getPackageName());
        Switch swMusic = swId != 0 ? v.findViewById(swId) : null;
        if (swMusic != null) {
            swMusic.setChecked(prefs.isMusicEnabled());
            swMusic.setOnCheckedChangeListener((buttonView, isChecked) -> prefs.setMusicEnabled(isChecked));
        }

        int spThemeId = getResources().getIdentifier("sp_theme", "id", requireContext().getPackageName());
        Spinner theme = spThemeId != 0 ? v.findViewById(spThemeId) : null;
        int themesArrayId = getResources().getIdentifier("themes_array", "array", requireContext().getPackageName());
        if (theme != null && themesArrayId != 0) {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(), themesArrayId, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            theme.setAdapter(adapter);
            int mode = prefs.getThemeMode();
            int pos = 0; // system
            if (mode == AppCompatDelegate.MODE_NIGHT_NO) pos = 1; else if (mode == AppCompatDelegate.MODE_NIGHT_YES) pos = 2;
            theme.setSelection(pos);
            theme.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                    int m = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                    if (position == 1) m = AppCompatDelegate.MODE_NIGHT_NO; else if (position == 2) m = AppCompatDelegate.MODE_NIGHT_YES;
                    prefs.setThemeMode(m);
                    AppCompatDelegate.setDefaultNightMode(m);
                }
                @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
            });
        }

        int spLangId = getResources().getIdentifier("sp_language", "id", requireContext().getPackageName());
        Spinner lang = spLangId != 0 ? v.findViewById(spLangId) : null;
        int languagesArrayId = getResources().getIdentifier("languages_array", "array", requireContext().getPackageName());
        if (lang != null && languagesArrayId != 0) {
            ArrayAdapter<CharSequence> langAdapter = ArrayAdapter.createFromResource(requireContext(), languagesArrayId, android.R.layout.simple_spinner_item);
            langAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            lang.setAdapter(langAdapter);
            String cur = prefs.getLanguage();
            lang.setSelection("en".equals(cur) ? 0 : 1);
            lang.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                    prefs.setLanguage(position == 0 ? "en" : "fr");
                }
                @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
            });
        }

        return v;
    }
}
