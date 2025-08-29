package com.example.tripbuddy.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TripBuddyDbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "tripbuddy.db";
    private static final int DB_VERSION = 3;

    public static final String T_TRIPS = "trips";
    public static final String T_EXPENSES = "expenses";
    public static final String T_MEMORIES = "memories";
    public static final String T_TRIP_IMAGES = "trip_images";
    public static final String T_USERS = "users";

    public TripBuddyDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + T_TRIPS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "destination TEXT NOT NULL, " +
                "start_date TEXT, " +
                "end_date TEXT, " +
                "notes TEXT, " +
                "total REAL, " +
                "discount REAL, " +
                "total_after_discount REAL, " +
                "created_at INTEGER)"
        );

        db.execSQL("CREATE TABLE IF NOT EXISTS " + T_EXPENSES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "trip_id INTEGER NOT NULL, " +
                "name TEXT NOT NULL, " +
                "cost REAL NOT NULL, " +
                "FOREIGN KEY(trip_id) REFERENCES trips(id) ON DELETE CASCADE)"
        );

        db.execSQL("CREATE TABLE IF NOT EXISTS " + T_MEMORIES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT, " +
                "image_res_name TEXT, " +
                "image_uri TEXT, " +
                "created_at INTEGER)"
        );

    db.execSQL("CREATE TABLE IF NOT EXISTS " + T_TRIP_IMAGES + " (" +
        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "trip_id INTEGER NOT NULL, " +
        "image_uri TEXT NOT NULL, " +
        "caption TEXT, " +
        "created_at INTEGER, " +
        "FOREIGN KEY(trip_id) REFERENCES trips(id) ON DELETE CASCADE)"
    );

    db.execSQL("CREATE TABLE IF NOT EXISTS " + T_USERS + " (" +
        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "username TEXT UNIQUE NOT NULL, " +
        "password_hash TEXT NOT NULL, " +
        "first_name TEXT, " +
        "last_name TEXT, " +
        "initials TEXT, " +
        "created_at INTEGER)"
    );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + T_TRIP_IMAGES + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "trip_id INTEGER NOT NULL, " +
                    "image_uri TEXT NOT NULL, " +
                    "caption TEXT, " +
                    "created_at INTEGER, " +
                    "FOREIGN KEY(trip_id) REFERENCES trips(id) ON DELETE CASCADE)");
        }
    if (oldVersion < 3) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + T_USERS + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "username TEXT UNIQUE NOT NULL, " +
            "password_hash TEXT NOT NULL, " +
            "first_name TEXT, " +
            "last_name TEXT, " +
            "initials TEXT, " +
            "created_at INTEGER)");
    }
    }
}
