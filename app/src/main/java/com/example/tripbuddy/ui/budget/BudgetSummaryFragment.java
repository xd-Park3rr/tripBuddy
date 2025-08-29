package com.example.tripbuddy.ui.budget;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.tripbuddy.PrefsManager;
import com.example.tripbuddy.R;
import com.example.tripbuddy.data.TripRepository;
import com.example.tripbuddy.data.models.Expense;
import com.example.tripbuddy.data.models.Trip;
import com.google.android.material.transition.MaterialFadeThrough;

import java.util.Locale;

public class BudgetSummaryFragment extends Fragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEnterTransition(new MaterialFadeThrough());
        setExitTransition(new MaterialFadeThrough());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_budget_summary, container, false);
        TextView tvTitle = v.findViewById(R.id.tv_title);
        TextView tvTotals = v.findViewById(R.id.tv_totals);
        LinearLayout ll = v.findViewById(R.id.ll_expenses);
        PrefsManager prefs = new PrefsManager(requireContext());
        long lastId = prefs.getLastTripId();
        if (lastId == -1) {
            tvTitle.setText(R.string.no_trip_summary);
            return v;
        }
        TripRepository repo = new TripRepository(requireContext());
        Trip t = repo.getTrip(lastId);
        if (t == null) {
            tvTitle.setText(R.string.no_trip_summary);
            return v;
        }
        String start = t.startDate != null ? t.startDate : "";
        String end = t.endDate != null ? t.endDate : "";
        tvTitle.setText(getString(R.string.trip_title_format, t.destination, start, end));

        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.budget_line, getString(R.string.total), t.total)).append("\n")
          .append(getString(R.string.budget_line, getString(R.string.discount), t.discount)).append("\n")
          .append(getString(R.string.budget_line, getString(R.string.total_after_discount), t.totalAfterDiscount));
        tvTotals.setText(sb.toString());

        LayoutInflater inf = LayoutInflater.from(getContext());
        for (Expense e : t.expenses) {
            View row = inf.inflate(R.layout.row_expense, ll, false);
            ((TextView)row.findViewById(R.id.tv_expense_name)).setText(e.name);
            ((TextView)row.findViewById(R.id.tv_expense_cost)).setText(String.format(Locale.getDefault(), "R%.2f", e.cost));
            row.findViewById(R.id.btn_remove).setVisibility(View.GONE);
            ll.addView(row);
        }
        return v;
    }
}
