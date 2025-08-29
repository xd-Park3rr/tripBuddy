package com.example.tripbuddy.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.tripbuddy.R;
import com.example.tripbuddy.databinding.FragmentHomeBinding;
import com.example.tripbuddy.util.DateUtils;
import com.google.android.material.transition.MaterialFadeThrough;
import com.kizitonwose.calendar.view.CalendarView;
import com.kizitonwose.calendar.view.ViewContainer;
import com.kizitonwose.calendar.core.CalendarDay;
import com.kizitonwose.calendar.core.CalendarMonth;
import com.kizitonwose.calendar.core.DayOwner;
import com.kizitonwose.calendar.core.OutDateStyle;
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale;
import com.example.tripbuddy.PrefsManager;
import com.example.tripbuddy.data.TripRepository;

public class HomeFragment extends Fragment {
    private DayTripsAdapter dayTripsAdapter;

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
    binding.tvStatBudget.setText(String.format("R%.2f", spent));

        String next = repo.getNextUpcomingTripDestination();
        if (next != null && !next.isEmpty()) {
            binding.tvNextTrip.setText(getString(R.string.home_next_trip_prefix) + " " + next);
        } else {
            binding.tvNextTrip.setText(getString(R.string.home_next_trip_fallback));
        }

        // Load latest trip cover image into hero with Glide, fallback to default
        // Calendar: show trips for selected date
        dayTripsAdapter = new DayTripsAdapter();
    binding.rvDayTrips.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvDayTrips.setAdapter(dayTripsAdapter);
        dayTripsAdapter.setOnItemClick(v1 -> {
            int pos = binding.rvDayTrips.getChildAdapterPosition(v1);
            if (pos >= 0) {
                com.example.tripbuddy.data.models.Trip t = ((DayTripsAdapter) binding.rvDayTrips.getAdapter()).data.get(pos);
                // Persist selected trip id so BudgetSummaryFragment can load it
                new com.example.tripbuddy.PrefsManager(requireContext()).setLastTripId(t.id);
                Navigation.findNavController(v1).navigate(R.id.nav_budget_summary);
            }
        });
    CalendarView calendarView = binding.calendarView;
    setupCalendar(calendarView);

        long now = System.currentTimeMillis();
        updateMonthIndicators(now);
        renderTripsForDay(now);
        binding.tvSelectedDay.setText(getString(R.string.selected_day_prefix, com.example.tripbuddy.util.DateUtils.formatYMD(now)));

        // Next upcoming trip indicator
        {
            java.util.List<com.example.tripbuddy.data.models.Trip> all = new TripRepository(requireContext()).getAllTrips();
            long soonest = Long.MAX_VALUE; String label = null; String when = null;
            for (com.example.tripbuddy.data.models.Trip t : all) {
                long start = com.example.tripbuddy.util.DateUtils.parseToUtcMillis(t.startDate);
                if (start >= now && start < soonest) { soonest = start; label = t.destination; when = t.startDate; }
            }
            if (label != null) {
                binding.tvNextEvent.setText(getString(R.string.next_event_prefix, label, when));
                binding.tvNextEvent.setVisibility(View.VISIBLE);
            } else {
                binding.tvNextEvent.setVisibility(View.GONE);
            }
        }
    String latestImage = repo.getLatestImageUri();
        if (latestImage != null && !latestImage.isEmpty()) {
            Glide.with(this)
                    .load(latestImage)
                    .placeholder(R.drawable.bg_travel_beach)
                    .error(R.drawable.bg_travel_beach)
                    .centerCrop()
                    .into(binding.ivHero);
        } else {
            binding.ivHero.setImageResource(R.drawable.bg_travel_beach);
        }
        return root;
    }

    private void setupCalendar(CalendarView calendarView) {
        java.time.YearMonth currentMonth = java.time.YearMonth.now();
        java.time.DayOfWeek firstDayOfWeek = firstDayOfWeekFromLocale();
        // Show 12 months back and 12 months forward.
        java.time.YearMonth startMonth = currentMonth.minusMonths(12);
        java.time.YearMonth endMonth = currentMonth.plusMonths(12);
        calendarView.setMonthScrollListener(month -> {
            // Update month indicators when month changes
            java.time.LocalDate anchor = month.getYearMonth().atDay(15);
            long millis = java.util.Date.from(anchor.atStartOfDay(java.time.ZoneOffset.UTC)).getTime();
            updateMonthIndicators(millis);
            return null;
        });
        calendarView.setup(startMonth, endMonth, firstDayOfWeek);
        calendarView.scrollToMonth(currentMonth);

        // Collect booked days into a set for quick lookup
        final java.util.HashSet<java.time.LocalDate> orangeDays = new java.util.HashSet<>();
        final java.util.HashSet<java.time.LocalDate> blueDays = new java.util.HashSet<>();
        TripRepository repo = new TripRepository(requireContext());
        java.util.List<com.example.tripbuddy.data.models.Trip> all = repo.getAllTrips();
        for (com.example.tripbuddy.data.models.Trip t : all) {
            long ts = com.example.tripbuddy.util.DateUtils.parseToUtcMillis(t.startDate);
            long te = com.example.tripbuddy.util.DateUtils.parseToUtcMillis(t.endDate);
            if (ts == Long.MIN_VALUE) continue;
            if (te < ts) te = ts;
            java.time.LocalDate start = java.time.Instant.ofEpochMilli(ts).atZone(java.time.ZoneOffset.UTC).toLocalDate();
            java.time.LocalDate end = java.time.Instant.ofEpochMilli(te).atZone(java.time.ZoneOffset.UTC).toLocalDate();
            java.time.LocalDate walk = start;
            while (!walk.isAfter(end)) {
                if ((walk.getDayOfMonth() % 2) == 0) blueDays.add(walk); else orangeDays.add(walk);
                walk = walk.plusDays(1);
            }
        }

        final int orange = getResources().getColor(R.color.tb_sunset);
        final int blue = getResources().getColor(R.color.tb_sky);
        final int lightText = getResources().getColor(R.color.white);

        class DayViewContainer extends ViewContainer {
            final android.widget.TextView textView;
            java.time.LocalDate date;
            DayViewContainer(@NonNull View view) {
                super(view);
                textView = view.findViewById(R.id.tvDayText);
                view.setOnClickListener(v -> {
                    if (date != null && (date.getMonthValue() == calendarView.getMonth().getYearMonth().getMonthValue())) {
                        long millis = java.util.Date.from(date.atStartOfDay(java.time.ZoneOffset.UTC)).getTime();
                        renderTripsForDay(millis);
                        updateMonthIndicators(millis);
                        binding.tvSelectedDay.setText(getString(R.string.selected_day_prefix, com.example.tripbuddy.util.DateUtils.formatYMD(millis)));
                    }
                });
            }
        }

        calendarView.setDayBinder(new com.kizitonwose.calendar.view.DayBinder<DayViewContainer>() {
            @NonNull
            @Override
            public DayViewContainer create(@NonNull View view) {
                return new DayViewContainer(view);
            }

            @Override
            public void bind(@NonNull DayViewContainer container, @NonNull CalendarDay day) {
                container.date = day.getDate();
                android.widget.TextView tv = container.textView;
                tv.setText(String.valueOf(container.date.getDayOfMonth()));

                // Reset to default background/text
                tv.setBackground(requireContext().getDrawable(R.drawable.bg_calendar_day_default));
                tv.setTextColor(getResources().getColor(android.R.color.secondary_text_dark));

                if (day.getOwner() == DayOwner.THIS_MONTH) {
                    // Highlight booked days
                    if (orangeDays.contains(container.date)) {
                        android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
                        gd.setColor(orange);
                        gd.setCornerRadius(24f);
                        tv.setBackground(gd);
                        tv.setTextColor(lightText);
                    } else if (blueDays.contains(container.date)) {
                        android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
                        gd.setColor(blue);
                        gd.setCornerRadius(24f);
                        tv.setBackground(gd);
                        tv.setTextColor(lightText);
                    }
                    tv.setAlpha(1f);
                } else {
                    tv.setAlpha(0.3f);
                }
            }
        });

        calendarView.setMonthHeaderBinder(new com.kizitonwose.calendar.view.MonthHeaderFooterBinder<ViewContainer>() {
            @NonNull
            @Override
            public ViewContainer create(@NonNull View view) {
                return new ViewContainer(view) {};
            }

            @Override
            public void bind(@NonNull ViewContainer container, @NonNull CalendarMonth month) {
                android.widget.TextView title = container.getView().findViewById(R.id.tvMonthTitle);
                java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy");
                title.setText(month.getYearMonth().format(fmt));
            }
        });
    }

    private void renderTripsForDay(long dayUtc) {
        TripRepository repo = new TripRepository(requireContext());
        java.util.List<com.example.tripbuddy.data.models.Trip> trips = repo.getTripsForDay(dayUtc);
        dayTripsAdapter.setData(trips);
        binding.tvCalendarEmpty.setVisibility(trips.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void updateMonthIndicators(long anchorUtc) {
        TripRepository repo = new TripRepository(requireContext());
        // Collect days in the currently visible month that have trips
        java.util.Calendar cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"));
        cal.setTimeInMillis(anchorUtc);
        long firstOfMonth;
        long endOfMonth;
        {
            cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
            firstOfMonth = cal.getTimeInMillis();
            cal.add(java.util.Calendar.MONTH, 1);
            cal.add(java.util.Calendar.MILLISECOND, -1);
            endOfMonth = cal.getTimeInMillis();
        }
        java.util.List<com.example.tripbuddy.data.models.Trip> all = repo.getAllTrips();
        java.util.Set<Integer> days = new java.util.TreeSet<>();
        for (com.example.tripbuddy.data.models.Trip t : all) {
            long ts = com.example.tripbuddy.util.DateUtils.parseToUtcMillis(t.startDate);
            long te = com.example.tripbuddy.util.DateUtils.parseToUtcMillis(t.endDate);
            if (te < ts) te = ts;
            if (com.example.tripbuddy.util.DateUtils.rangesOverlap(ts, te, firstOfMonth, endOfMonth)) {
                long from = Math.max(ts, firstOfMonth);
                long to = Math.min(te, endOfMonth);
                java.util.Calendar walk = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"));
                walk.setTimeInMillis(from);
                while (walk.getTimeInMillis() <= to) {
                    days.add(walk.get(java.util.Calendar.DAY_OF_MONTH));
                    walk.add(java.util.Calendar.DAY_OF_MONTH, 1);
                }
            }
        }
        if (days.isEmpty()) {
            binding.tvMonthIndicators.setVisibility(View.GONE);
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("Trip days this month: ");
            boolean first = true;
            for (Integer d : days) {
                if (!first) sb.append(", ");
                sb.append(d);
                first = false;
            }
            binding.tvMonthIndicators.setText(sb.toString());
            binding.tvMonthIndicators.setVisibility(View.VISIBLE);
        }
    }

    private static class DayTripsAdapter extends RecyclerView.Adapter<DayTripsAdapter.VH> {
        private final java.util.List<com.example.tripbuddy.data.models.Trip> data = new java.util.ArrayList<>();
        private android.view.View.OnClickListener onItemClick;

        void setOnItemClick(android.view.View.OnClickListener l) { this.onItemClick = l; }

        void setData(java.util.List<com.example.tripbuddy.data.models.Trip> newData) {
            data.clear();
            if (newData != null) data.addAll(newData);
            notifyDataSetChanged();
        }

        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            android.view.View v = android.view.LayoutInflater.from(parent.getContext()).inflate(R.layout.row_day_trip, parent, false);
            if (onItemClick != null) v.setOnClickListener(onItemClick);
            return new VH(v);
        }

        @Override public void onBindViewHolder(@NonNull VH h, int i) {
            com.example.tripbuddy.data.models.Trip t = data.get(i);
            h.dest.setText(t.destination);
            String s = t.startDate != null ? t.startDate : "";
            String e = t.endDate != null ? t.endDate : "";
            if (!s.isEmpty() || !e.isEmpty()) h.dates.setText(s + (e.isEmpty()?"":" → " + e));
            else h.dates.setText("");
        }

        @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
            final android.widget.TextView dest; final android.widget.TextView dates; final android.widget.ImageView cover;
            VH(@NonNull android.view.View v) { super(v); dest = v.findViewById(R.id.tv_dest); dates = v.findViewById(R.id.tv_dates); cover = v.findViewById(R.id.iv_cover); }
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}