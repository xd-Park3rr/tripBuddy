package com.example.tripbuddy.ui.trip;

import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.tripbuddy.PrefsManager;
import com.example.tripbuddy.R;
import com.example.tripbuddy.data.TripRepository;
import com.example.tripbuddy.data.models.Expense;
import com.example.tripbuddy.data.models.Trip;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.transition.MaterialFadeThrough;
import androidx.core.util.Pair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class TripPlannerFragment extends Fragment {

    private EditText etDestination, etStart, etEnd, etNotes, etExpName, etExpCost, etBudget;
    private LinearLayout expenseList;
    private TextView tvTotal, tvDiscount, tvTotalAfter, tvBudgetPct;
    private ProgressBar pbBudget;
    private final List<Expense> expenses = new ArrayList<>();
    private ImageView ivCoverPreview;
    private Uri pickedImageUri;
    private ActivityResultLauncher<String> pickImageLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_trip_planner, container, false);
        etDestination = v.findViewById(R.id.et_destination);
        etStart = v.findViewById(R.id.et_start);
        etEnd = v.findViewById(R.id.et_end);
        etNotes = v.findViewById(R.id.et_notes);
        etExpName = v.findViewById(R.id.et_expense_name);
        etExpCost = v.findViewById(R.id.et_expense_cost);
        etBudget = v.findViewById(R.id.et_budget);
        expenseList = v.findViewById(R.id.ll_expenses);
        tvTotal = v.findViewById(R.id.tv_total);
        tvDiscount = v.findViewById(R.id.tv_discount);
        tvTotalAfter = v.findViewById(R.id.tv_total_after);
        pbBudget = v.findViewById(R.id.pb_budget);
        tvBudgetPct = v.findViewById(R.id.tv_budget_pct);
        ivCoverPreview = v.findViewById(R.id.iv_cover_preview);

        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                pickedImageUri = uri;
                ivCoverPreview.setImageURI(uri);
            } else {
                Toast.makeText(getContext(), R.string.image_picker_error, Toast.LENGTH_SHORT).show();
            }
        });

        v.findViewById(R.id.btn_pick_image).setOnClickListener(view -> pickImageLauncher.launch("image/*"));
        v.findViewById(R.id.btn_remove_image).setOnClickListener(view -> {
            pickedImageUri = null;
            ivCoverPreview.setImageDrawable(null);
        });

    v.findViewById(R.id.btn_add_expense).setOnClickListener(view -> addManualExpense());

    // Hook date range picker for start/end fields
    View.OnClickListener dateClicker = vv -> showDateRangePicker();
    etStart.setFocusable(false);
    etEnd.setFocusable(false);
    etStart.setOnClickListener(dateClicker);
    etEnd.setOnClickListener(dateClicker);
        // Predefined activities
        initPredefined(v);
        v.findViewById(R.id.btn_save).setOnClickListener(view -> saveTrip(view));

        if (etBudget != null) {
            etBudget.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) { updateBudgetProgress(); }
                @Override public void afterTextChanged(Editable s) { updateBudgetProgress(); }
            });
        }
        updateTotals();
        updateBudgetProgress();
        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEnterTransition(new MaterialFadeThrough());
        setExitTransition(new MaterialFadeThrough());
    }

    private void initPredefined(View v) {
        setCheckListener(v, R.id.cb_city_tour, 50);
        setCheckListener(v, R.id.cb_museum_pass, 30);
        setCheckListener(v, R.id.cb_airport_transfer, 25);
    }

    private void showDateRangePicker() {
        // Constrain to today forward (optional)
    CalendarConstraints.Builder constraints = new CalendarConstraints.Builder();
    long todayUtc = MaterialDatePicker.todayInUtcMilliseconds();
    constraints.setValidator(DateValidatorPointForward.from(todayUtc));
        MaterialDatePicker<Pair<Long, Long>> picker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText(R.string.select_trip_dates)
        .setCalendarConstraints(constraints.build())
        .setSelection(new androidx.core.util.Pair<>(todayUtc, todayUtc))
                .build();
        picker.addOnPositiveButtonClickListener(selection -> {
            if (selection == null) return;
            Long start = selection.first;
            Long end = selection.second;
            if (start != null) etStart.setText(com.example.tripbuddy.util.DateUtils.formatYMD(start));
            if (end != null) etEnd.setText(com.example.tripbuddy.util.DateUtils.formatYMD(end));
        });
        picker.show(getParentFragmentManager(), "trip_dates");
    }

    private void setCheckListener(View v, int id, double cost) {
        CheckBox cb = v.findViewById(id);
        cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String label = cb.getText().toString();
            if (isChecked) {
                expenses.add(new Expense(label, cost));
            } else {
                for (Iterator<Expense> it = expenses.iterator(); it.hasNext();) {
                    Expense e = it.next();
                    if (label.equals(e.name) && Math.abs(e.cost - cost) < 0.0001) { it.remove(); break; }
                }
            }
            refreshExpenseList();
            updateTotals();
            updateBudgetProgress();
        });
    }

    private void addManualExpense() {
        String name = etExpName.getText().toString().trim();
        String costStr = etExpCost.getText().toString().trim();
        if (TextUtils.isEmpty(name)) { Toast.makeText(getContext(), R.string.invalid_expense, Toast.LENGTH_SHORT).show(); return; }
        double cost;
        try { cost = Double.parseDouble(costStr); } catch (Exception e) { Toast.makeText(getContext(), R.string.invalid_expense, Toast.LENGTH_SHORT).show(); return; }
        expenses.add(new Expense(name, cost));
        etExpName.setText(""); etExpCost.setText("");
        refreshExpenseList();
        updateTotals();
        updateBudgetProgress();
    }

    private double currentTotal() {
        double sum = 0; for (Expense e : expenses) sum += e.cost; return sum;
    }

    private void updateBudgetProgress() {
        if (pbBudget == null || tvBudgetPct == null) return;
        double total = currentTotal();
        double plan = 0;
        try { plan = etBudget != null ? Double.parseDouble(etBudget.getText().toString().trim()) : 0; } catch (Exception ignored) {}
        int pct = 0;
        if (plan > 0) {
            double raw = (total / plan) * 100.0;
            pct = (int) Math.max(0, Math.min(100, Math.round(raw)));
        }
        pbBudget.setProgress(pct);
        tvBudgetPct.setText(getString(R.string.budget_progress, pct));
    }

    private void refreshExpenseList() {
        expenseList.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (int i = 0; i < expenses.size(); i++) {
            Expense e = expenses.get(i);
            View row = inflater.inflate(R.layout.row_expense, expenseList, false);
            ((TextView)row.findViewById(R.id.tv_expense_name)).setText(e.name);
            ((TextView)row.findViewById(R.id.tv_expense_cost)).setText(String.format(Locale.getDefault(), "R%.2f", e.cost));
            int index = i;
            row.findViewById(R.id.btn_remove).setOnClickListener(v -> {
                expenses.remove(index);
                refreshExpenseList();
                updateTotals();
                updateBudgetProgress();
            });
            expenseList.addView(row);
        }
    }

    private void updateTotals() {
        double sum = 0;
        for (Expense e : expenses) sum += e.cost;
        PrefsManager prefs = new PrefsManager(requireContext());
        int trips = prefs.getTripCount();
        boolean eligible = trips >= 3;
        double discount = eligible ? sum * 0.10 : 0;
        double after = sum - discount;
        tvTotal.setText(getString(R.string.budget_line, getString(R.string.total), sum));
        tvDiscount.setText(getString(R.string.budget_line, getString(R.string.discount), discount));
        tvTotalAfter.setText(getString(R.string.budget_line, getString(R.string.total_after_discount), after));
        if (eligible) {
            tvDiscount.setVisibility(View.VISIBLE);
        }
        updateBudgetProgress();
    }

    private void saveTrip(View view) {
        String destination = etDestination.getText().toString().trim();
        if (TextUtils.isEmpty(destination)) { Toast.makeText(getContext(), R.string.destination, Toast.LENGTH_SHORT).show(); return; }
        double sum = 0; for (Expense e: expenses) sum += e.cost;
        PrefsManager prefs = new PrefsManager(requireContext());
        boolean eligible = prefs.getTripCount() >= 3;
        double discount = eligible ? sum * 0.10 : 0;
        Trip t = new Trip();
        t.destination = destination;
        t.startDate = etStart.getText().toString().trim();
        t.endDate = etEnd.getText().toString().trim();
        t.notes = etNotes.getText().toString().trim();
        t.imageUri = pickedImageUri != null ? pickedImageUri.toString() : null;
        t.expenses = new ArrayList<>(expenses);
        t.total = sum;
        t.discount = discount;
        t.totalAfterDiscount = sum - discount;
        TripRepository repo = new TripRepository(requireContext());
        long id = repo.saveTrip(t);
        if (pickedImageUri != null) {
            repo.addTripImage(id, pickedImageUri.toString(), null);
        }
        prefs.setLastTripId(id);
        prefs.incrementTripCount();
        Toast.makeText(getContext(), R.string.save_trip, Toast.LENGTH_SHORT).show();
        Navigation.findNavController(view).navigate(R.id.nav_budget_summary);
    }
}
