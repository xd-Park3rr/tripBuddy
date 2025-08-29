package com.example.tripbuddy.util;

import android.text.TextUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtils {
    // Accept common formats used in the app; prefer yyyy-MM-dd
    private static final String[] FORMATS = new String[] {
            "yyyy-MM-dd",
            "MM/dd/yyyy",
            "MMM d, yyyy"
    };

    public static long parseToUtcMillis(String s) {
        if (TextUtils.isEmpty(s)) return Long.MIN_VALUE;
        for (String f : FORMATS) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(f, Locale.getDefault());
                sdf.setLenient(true);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date d = sdf.parse(s);
                if (d != null) return d.getTime();
            } catch (ParseException ignored) {}
        }
        return Long.MIN_VALUE;
    }

    public static String formatYMD(long utcMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date(utcMillis));
    }

    public static long startOfDayUtc(long utcMillis) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTimeInMillis(utcMillis);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    public static long endOfDayUtc(long utcMillis) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTimeInMillis(utcMillis);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTimeInMillis();
    }

    public static boolean rangesOverlap(long aStart, long aEnd, long bStart, long bEnd) {
        return aStart <= bEnd && bStart <= aEnd;
    }
}
