package com.example.tripbuddy.ui.gallery;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;

import com.example.tripbuddy.PrefsManager;
import com.example.tripbuddy.data.MemoryRepository;
import com.example.tripbuddy.data.models.Memory;

public class PhotoViewerActivity extends AppCompatActivity {
    private MediaPlayer player;
    private long[] ids;
    private int index;
    private ImageView iv; private TextView tv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int layoutId = getResources().getIdentifier("activity_photo_viewer", "layout", getPackageName());
        if (layoutId != 0) setContentView(layoutId);
        int ivId = getResources().getIdentifier("iv_full", "id", getPackageName());
        int tvId = getResources().getIdentifier("tv_title", "id", getPackageName());
        int prevId = getResources().getIdentifier("btn_prev", "id", getPackageName());
        int nextId = getResources().getIdentifier("btn_next", "id", getPackageName());
        int closeId = getResources().getIdentifier("btn_close", "id", getPackageName());
        iv = ivId != 0 ? findViewById(ivId) : null;
        tv = tvId != 0 ? findViewById(tvId) : null;
        ImageButton btnPrev = prevId != 0 ? findViewById(prevId) : null;
        ImageButton btnNext = nextId != 0 ? findViewById(nextId) : null;
        ImageButton btnClose = closeId != 0 ? findViewById(closeId) : null;
        String uri = getIntent().getStringExtra("imageUri");
        String title = getIntent().getStringExtra("title");
        String transitionName = getIntent().getStringExtra("transitionName");
        if (iv != null && transitionName != null) ViewCompat.setTransitionName(iv, transitionName);
        ids = getIntent().getLongArrayExtra("memoryIds");
        index = getIntent().getIntExtra("startIndex", -1);
        if (ids != null && ids.length > 0 && index >= 0) {
            loadByIndex(index);
        } else {
            // fallback single
            applyImageAndTitle(uri, title);
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

        if (btnPrev != null) btnPrev.setOnClickListener(v -> navigate(-1));
        if (btnNext != null) btnNext.setOnClickListener(v -> navigate(1));
        if (btnClose != null) btnClose.setOnClickListener(v -> finishWithAnim());
        if (iv != null) iv.setOnClickListener(v -> navigate(1));
        if (tv != null) tv.setOnClickListener(v -> navigate(1));
    }

    private void navigate(int delta) {
        if (ids == null || ids.length == 0) { finishWithAnim(); return; }
        index += delta;
        if (index < 0) index = ids.length - 1;
        if (index >= ids.length) index = 0;
        loadByIndex(index);
    }

    private void loadByIndex(int i) {
        MemoryRepository repo = new MemoryRepository(this);
        Memory m = repo.getMemoryById(ids[i]);
        if (m != null) {
            applyImageAndTitle(m.imageUri, m.title);
        }
    }

    private void applyImageAndTitle(String uri, String title) {
        if (tv != null) tv.setText(title != null ? title : "");
        if (iv != null) {
            if (uri != null && uri.startsWith("res:")) {
                String resName = uri.substring(4);
                int resId = getResources().getIdentifier(resName, "drawable", getPackageName());
                if (resId == 0) resId = getResources().getIdentifier("ic_app", "mipmap", getPackageName());
                if (resId != 0) iv.setImageResource(resId);
            } else if (uri != null) {
                iv.setImageURI(Uri.parse(uri));
            } else {
                int resId = getResources().getIdentifier("ic_app", "mipmap", getPackageName());
                if (resId != 0) iv.setImageResource(resId);
            }
        }
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
