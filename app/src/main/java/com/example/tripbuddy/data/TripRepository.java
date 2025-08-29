package com.example.tripbuddy.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.tripbuddy.data.models.Expense;
import com.example.tripbuddy.data.models.Trip;

import java.util.ArrayList;
import java.util.List;

public class TripRepository {
    private final TripBuddyDbHelper helper;

    public TripRepository(Context ctx) {
        helper = new TripBuddyDbHelper(ctx);
    }

    public long saveTrip(Trip trip) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("destination", trip.destination);
        cv.put("start_date", trip.startDate);
        cv.put("end_date", trip.endDate);
        cv.put("notes", trip.notes);
        cv.put("total", trip.total);
        cv.put("discount", trip.discount);
        cv.put("total_after_discount", trip.totalAfterDiscount);
        cv.put("created_at", System.currentTimeMillis());
        long id = db.insert(TripBuddyDbHelper.T_TRIPS, null, cv);
        for (Expense e : trip.expenses) {
            ContentValues ce = new ContentValues();
            ce.put("trip_id", id);
            ce.put("name", e.name);
            ce.put("cost", e.cost);
            db.insert(TripBuddyDbHelper.T_EXPENSES, null, ce);
        }
        return id;
    }

    public Trip getTrip(long id) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.query(TripBuddyDbHelper.T_TRIPS,
                new String[]{"id","destination","start_date","end_date","notes","total","discount","total_after_discount","created_at"},
                "id=?",
                new String[]{String.valueOf(id)}, null, null, null);
        try {
            if (c.moveToFirst()) {
                Trip t = new Trip();
                t.id = c.getLong(0);
                t.destination = c.getString(1);
                t.startDate = c.getString(2);
                t.endDate = c.getString(3);
                t.notes = c.getString(4);
                t.total = c.getDouble(5);
                t.discount = c.getDouble(6);
                t.totalAfterDiscount = c.getDouble(7);
                t.createdAt = c.getLong(8);
                t.expenses = getExpensesForTrip(id);
                return t;
            }
        } finally { c.close(); }
        return null;
    }

    public List<Expense> getExpensesForTrip(long tripId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.query(TripBuddyDbHelper.T_EXPENSES,
                new String[]{"id","trip_id","name","cost"},
                "trip_id=?",
                new String[]{String.valueOf(tripId)}, null, null, null);
        List<Expense> list = new ArrayList<>();
        try {
            while (c.moveToNext()) {
                Expense e = new Expense();
                e.id = c.getLong(0);
                e.tripId = c.getLong(1);
                e.name = c.getString(2);
                e.cost = c.getDouble(3);
                list.add(e);
            }
        } finally { c.close(); }
        return list;
    }

    public int getTripCount() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + TripBuddyDbHelper.T_TRIPS, null);
        try {
            if (c.moveToFirst()) return c.getInt(0);
        } finally { c.close(); }
        return 0;
    }

    public double getTotalSpent() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT IFNULL(SUM(cost),0) FROM " + TripBuddyDbHelper.T_EXPENSES, null);
        try {
            if (c.moveToFirst()) return c.getDouble(0);
        } finally { c.close(); }
        return 0.0;
    }

    public void addTripImage(long tripId, String imageUri, String caption) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("trip_id", tripId);
        cv.put("image_uri", imageUri);
        cv.put("caption", caption);
        cv.put("created_at", System.currentTimeMillis());
        db.insert(TripBuddyDbHelper.T_TRIP_IMAGES, null, cv);
    }

    public String getCoverImageForTrip(long tripId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.query(TripBuddyDbHelper.T_TRIP_IMAGES,
                new String[]{"image_uri"},
                "trip_id=?",
                new String[]{String.valueOf(tripId)},
                null, null, "created_at DESC", "1");
        try {
            if (c.moveToFirst()) return c.getString(0);
        } finally { c.close(); }
        return null;
    }

    public String getLatestImageUri() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.query(TripBuddyDbHelper.T_TRIP_IMAGES,
                new String[]{"image_uri"},
                null, null, null, null, "created_at DESC", "1");
        try {
            if (c.moveToFirst()) return c.getString(0);
        } finally { c.close(); }
        return null;
    }

    public String getNextUpcomingTripDestination() {
        // Simple heuristic: latest created trip's destination
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.query(TripBuddyDbHelper.T_TRIPS,
                new String[]{"destination"}, null, null, null, null, "created_at DESC", "1");
        try {
            if (c.moveToFirst()) return c.getString(0);
        } finally { c.close(); }
        return null;
    }
}

