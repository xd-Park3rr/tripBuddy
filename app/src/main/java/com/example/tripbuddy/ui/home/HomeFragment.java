package com.example.tripbuddy.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.tripbuddy.R;
import com.example.tripbuddy.databinding.FragmentHomeBinding;
import com.google.android.material.transition.MaterialFadeThrough;
import com.example.tripbuddy.PrefsManager;
import com.example.tripbuddy.data.TripRepository;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEnterTransition(new MaterialFadeThrough());
        setExitTransition(new MaterialFadeThrough());
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        binding.btnPlanTrip.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.nav_trip_planner));
        binding.btnOpenGallery.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.nav_gallery));
        binding.btnViewBudget.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.nav_budget_summary));

        // Populate quick stats
    TripRepository repo = new TripRepository(requireContext());
    int trips = repo.getTripCount();
    double spent = repo.getTotalSpent();
    binding.tvStatTripsValue.setText(String.valueOf(trips));
    binding.tvStatBudget.setText(String.format("$%.2f", spent));

        String next = repo.getNextUpcomingTripDestination();
        if (next != null && !next.isEmpty()) {
            binding.tvNextTrip.setText(getString(R.string.home_next_trip_prefix) + " " + next);
        } else {
            binding.tvNextTrip.setText(getString(R.string.home_next_trip_fallback));
        }
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}