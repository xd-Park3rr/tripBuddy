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
import com.google.android.material.datepicker.MaterialDatePicker;

import java.util.List;
import java.util.TimeZone;

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
        binding.fabAdd.setOnClickListener(v -> openDateFilter());
        // Remove any previously seeded mock data
        repo.deleteMockMemories();
        // Clear range when chip close clicked
        binding.chipRange.setOnCloseIconClickListener(v -> {
            binding.chipRange.setVisibility(View.GONE);
            refresh();
        });
        refresh();
        return root;
    }

    private void openDateFilter() {
        // Build a date range picker with no past restriction for memories
        MaterialDatePicker.Builder<androidx.core.util.Pair<Long, Long>> builder =
                MaterialDatePicker.Builder.dateRangePicker();
        builder.setTitleText(R.string.gallery_title);
        MaterialDatePicker<androidx.core.util.Pair<Long, Long>> picker = builder.build();
        picker.addOnPositiveButtonClickListener(sel -> {
            if (sel != null) {
                Long start = sel.first; Long end = sel.second;
                if (start != null && end != null) {
                    applyDateRange(start, end);
                }
            }
        });
        picker.show(getParentFragmentManager(), "memories_range");
    }

    private void refresh() {
        List<Memory> list = repo.getAllMemories();
        adapter.submitList(list);
        binding.tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
    binding.chipRange.setVisibility(View.GONE);
    }

    private void applyDateRange(long startUtc, long endUtc) {
        // Normalize if needed; keep inclusive
        List<Memory> list = repo.getMemoriesBetween(startUtc, endUtc);
        adapter.submitList(list);
        binding.tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
    java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("yyyy-MM-dd");
    String label = fmt.format(new java.util.Date(startUtc)) + " to " + fmt.format(new java.util.Date(endUtc));
    binding.chipRange.setText(label);
    binding.chipRange.setVisibility(View.VISIBLE);
    }

    private void openMemory(Memory m, View sharedView) {
        Intent i = new Intent(requireContext(), com.example.tripbuddy.ui.gallery.PhotoViewerActivity.class);
    // Build id array from current (potentially filtered) adapter list
    List<Memory> current = adapter.getCurrentItems();
        long[] ids = new long[current.size()];
        int startIndex = 0;
        for (int idx = 0; idx < current.size(); idx++) {
            ids[idx] = current.get(idx).id;
            if (current.get(idx).id == m.id) startIndex = idx;
        }
        i.putExtra("memoryIds", ids);
        i.putExtra("startIndex", startIndex);
        // Also include direct data as fallback
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