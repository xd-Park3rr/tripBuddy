package com.example.tripbuddy.ui.gallery;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.tripbuddy.PrefsManager;
import com.example.tripbuddy.R;
import com.example.tripbuddy.data.MemoryRepository;
import com.example.tripbuddy.data.models.Memory;
import com.example.tripbuddy.databinding.FragmentGalleryBinding;
import com.google.android.material.transition.MaterialFadeThrough;

import java.util.List;

public class GalleryFragment extends Fragment {

    private FragmentGalleryBinding binding;
    private MemoryRepository repo;
    private MemoryAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEnterTransition(new MaterialFadeThrough());
        setExitTransition(new MaterialFadeThrough());
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        repo = new MemoryRepository(requireContext());
        adapter = new MemoryAdapter(requireContext(), this::openMemory);
        binding.rvGallery.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        int spacing = getResources().getDimensionPixelSize(R.dimen.grid_spacing);
        binding.rvGallery.addItemDecoration(new GridSpacingItemDecoration(2, spacing, true));
        binding.rvGallery.setAdapter(adapter);
        binding.fabAdd.setOnClickListener(v -> addSampleMemory());
        seedIfNeeded();
        refresh();
        return root;
    }

    private void seedIfNeeded() {
        PrefsManager prefs = new PrefsManager(requireContext());
        if (!prefs.isGallerySeeded()) {
            repo.addMemory("Coastal Sunset", "bg_travel_beach", null);
            repo.addMemory("High Peaks", "bg_travel_mountain", null);
            repo.addMemory("City Lights", "bg_travel_header", null);
            prefs.setGallerySeeded(true);
        }
    }

    private void refresh() {
        List<Memory> list = repo.getAllMemories();
        adapter.submitList(list);
        binding.tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void addSampleMemory() {
        repo.addMemory("New Journey", "ic_menu_gallery", null);
        refresh();
    }

    private void openMemory(Memory m, View sharedView) {
        Intent i = new Intent(requireContext(), com.example.tripbuddy.ui.gallery.PhotoViewerActivity.class);
        i.putExtra("imageUri", m.imageUri);
        i.putExtra("title", m.title);
        String transitionName = ViewCompat.getTransitionName(sharedView);
        if (transitionName != null) i.putExtra("transitionName", transitionName);
        ActivityOptionsCompat opts = ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), Pair.create(sharedView, transitionName));
        startActivity(i, opts.toBundle());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}