package com.example.tripbuddy.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.tripbuddy.data.models.Memory;

import java.util.ArrayList;
import java.util.List;

public class MemoryRepository {
    private final TripBuddyDbHelper helper;

    public MemoryRepository(Context ctx) {
        helper = new TripBuddyDbHelper(ctx);
    }

    public long addMemory(String title, String imageResName, String imageUri) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("title", title);
        cv.put("image_res_name", imageResName);
        cv.put("image_uri", imageUri);
        cv.put("created_at", System.currentTimeMillis());
        return db.insert(TripBuddyDbHelper.T_MEMORIES, null, cv);
    }

    public List<Memory> getAllMemories() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.query(TripBuddyDbHelper.T_MEMORIES, new String[]{"id","title","image_res_name","image_uri","created_at"}, null, null, null, null, "created_at DESC");
        List<Memory> list = new ArrayList<>();
        try {
            while (c.moveToNext()) {
                Memory m = new Memory();
                m.id = c.getLong(0);
                m.title = c.getString(1);
                String resName = c.getString(2);
                String uri = c.getString(3);
                m.imageUri = (uri != null && !uri.isEmpty()) ? uri : (resName != null ? "res:" + resName : null);
                m.createdAt = c.getLong(4);
                list.add(m);
            }
        } finally {
            c.close();
        }
        return list;
    }

    // New: get memories between (inclusive) timestamps
    public List<Memory> getMemoriesBetween(long startInclusive, long endInclusive) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.query(TripBuddyDbHelper.T_MEMORIES,
                new String[]{"id","title","image_res_name","image_uri","created_at"},
                "created_at BETWEEN ? AND ?",
                new String[]{String.valueOf(startInclusive), String.valueOf(endInclusive)},
                null, null, "created_at DESC");
        List<Memory> list = new ArrayList<>();
        try {
            while (c.moveToNext()) {
                Memory m = new Memory();
                m.id = c.getLong(0);
                m.title = c.getString(1);
                String resName = c.getString(2);
                String uri = c.getString(3);
                m.imageUri = (uri != null && !uri.isEmpty()) ? uri : (resName != null ? "res:" + resName : null);
                m.createdAt = c.getLong(4);
                list.add(m);
            }
        } finally { c.close(); }
        return list;
    }

    // New: fetch a single memory by id
    public Memory getMemoryById(long id) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.query(TripBuddyDbHelper.T_MEMORIES,
                new String[]{"id","title","image_res_name","image_uri","created_at"},
                "id=?", new String[]{String.valueOf(id)}, null, null, null);
        try {
            if (c.moveToFirst()) {
                Memory m = new Memory();
                m.id = c.getLong(0);
                m.title = c.getString(1);
                String resName = c.getString(2);
                String uri = c.getString(3);
                m.imageUri = (uri != null && !uri.isEmpty()) ? uri : (resName != null ? "res:" + resName : null);
                m.createdAt = c.getLong(4);
                return m;
            }
        } finally { c.close(); }
        return null;
    }

    // New: remove previously seeded mock memories (resource-only entries)
    public int deleteMockMemories() {
        SQLiteDatabase db = helper.getWritableDatabase();
        // Heuristic: entries with image_res_name set and image_uri null/empty were seeded
        return db.delete(TripBuddyDbHelper.T_MEMORIES,
                "image_res_name IS NOT NULL AND (image_uri IS NULL OR image_uri='')",
                null);
    }
}

