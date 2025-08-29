package com.example.tripbuddy.ui.gallery;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;

import com.example.tripbuddy.PrefsManager;

public class PhotoViewerActivity extends AppCompatActivity {
    private MediaPlayer player;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int layoutId = getResources().getIdentifier("activity_photo_viewer", "layout", getPackageName());
        if (layoutId != 0) setContentView(layoutId);
        int ivId = getResources().getIdentifier("iv_full", "id", getPackageName());
        int tvId = getResources().getIdentifier("tv_title", "id", getPackageName());
        ImageView iv = ivId != 0 ? findViewById(ivId) : null;
        TextView tv = tvId != 0 ? findViewById(tvId) : null;
        String uri = getIntent().getStringExtra("imageUri");
        String title = getIntent().getStringExtra("title");
        String transitionName = getIntent().getStringExtra("transitionName");
        if (iv != null && transitionName != null) ViewCompat.setTransitionName(iv, transitionName);
        if (tv != null) tv.setText(title != null ? title : "");
        if (iv != null) {
            if (uri != null && uri.startsWith("res:")) {
                String resName = uri.substring(4);
                int resId = getResources().getIdentifier(resName, "drawable", getPackageName());
                if (resId == 0) resId = getResources().getIdentifier("ic_launcher", "mipmap", getPackageName());
                if (resId != 0) iv.setImageResource(resId);
            } else if (uri != null) {
                iv.setImageURI(Uri.parse(uri));
            } else {
                int resId = getResources().getIdentifier("ic_launcher", "mipmap", getPackageName());
                if (resId != 0) iv.setImageResource(resId);
            }
        }

        PrefsManager prefs = new PrefsManager(this);
        if (prefs.isMusicEnabled()) {
            int musicRes = getResources().getIdentifier("memory_music", "raw", getPackageName());
            if (musicRes != 0) {
                try {
                    player = MediaPlayer.create(this, musicRes);
                    if (player != null) {
                        player.setLooping(true);
                        player.start();
                    }
                } catch (Exception ignored) {}
            }
        }

        if (iv != null) iv.setOnClickListener(v -> finishWithAnim());
        if (tv != null) tv.setOnClickListener(v -> finishWithAnim());
    }

    private void finishWithAnim() {
        finish();
        try {
            int fadeIn = getResources().getIdentifier("fade_in", "anim", getPackageName());
            int fadeOut = getResources().getIdentifier("fade_out", "anim", getPackageName());
            if (fadeIn != 0 && fadeOut != 0) overridePendingTransition(fadeIn, fadeOut);
        } catch (Exception ignored) {}
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            try {
                if (player.isPlaying()) player.stop();
            } catch (Exception ignored) {}
            player.release();
            player = null;
        }
    }
}
