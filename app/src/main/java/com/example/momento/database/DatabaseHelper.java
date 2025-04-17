package com.example.momento.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.momento.models.Event;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "momento.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_EVENTS = "events";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_LOCATION = "location";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_IMAGE_URI = "image_uri";

    private static final String CREATE_TABLE_EVENTS =
            "CREATE TABLE " + TABLE_EVENTS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TITLE + " TEXT, " +
                    COLUMN_DATE + " TEXT, " +
                    COLUMN_LOCATION + " TEXT, " +
                    COLUMN_DESCRIPTION + " TEXT, " +
                    COLUMN_CATEGORY + " TEXT, " +
                    COLUMN_IMAGE_URI + " TEXT);";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_EVENTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
        onCreate(db);
    }

    public void insertEvent(Event event) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, event.getTitle());
        values.put(COLUMN_DATE, event.getDate());
        values.put(COLUMN_LOCATION, event.getLocation());
        values.put(COLUMN_DESCRIPTION, event.getDescription());
        values.put(COLUMN_CATEGORY, event.getCategory());
        values.put(COLUMN_IMAGE_URI, event.getImageUri());

        db.insert(TABLE_EVENTS, null, values);
        db.close();
    }

    public List<Event> getAllEvents() {
        List<Event> eventList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_EVENTS, null, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String title = getColumnValue(cursor, COLUMN_TITLE, "Default Title");
                String date = getColumnValue(cursor, COLUMN_DATE, "Default Date");
                String location = getColumnValue(cursor, COLUMN_LOCATION, "Default Location");
                String description = getColumnValue(cursor, COLUMN_DESCRIPTION, "Default Description");
                String category = getColumnValue(cursor, COLUMN_CATEGORY, "Default Category");
                String imageUri = getColumnValue(cursor, COLUMN_IMAGE_URI, "");

                Event event = new Event(id, title, date, location, description, category, imageUri);
                eventList.add(event);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return eventList;
    }

    private String getColumnValue(Cursor cursor, String columnName, String defaultValue) {
        try {
            int columnIndex = cursor.getColumnIndexOrThrow(columnName);
            return cursor.getString(columnIndex);
        } catch (IllegalArgumentException e) {
            Log.w("DatabaseHelper", columnName + " not found, using default value");
            return defaultValue;
        }
    }

    public Event getEventById(int eventId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_EVENTS, null, COLUMN_ID + "=?",
                new String[]{String.valueOf(eventId)},
                null, null, null);

        Event event = null;
        if (cursor != null && cursor.moveToFirst()) {
            event = new Event(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URI))
            );
            cursor.close();
        }
        db.close();
        return event;
    }

    public boolean deleteEvent(int eventId) {
        // 1) Load the Event first (getEventById opens & closes its own DB handle)
        Event event = getEventById(eventId);

        // 2) Now open the writable DB for deletion
        SQLiteDatabase db = this.getWritableDatabase();

        // 3) Delete the associated local image file, if any
        if (event != null && event.getImageUri() != null) {
            try {
                File imageFile = new File(Uri.parse(event.getImageUri()).getPath());
                if (imageFile.exists()) {
                    imageFile.delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 4) Delete the row from the table
        int rows = db.delete(TABLE_EVENTS,
                COLUMN_ID + "=?",
                new String[]{ String.valueOf(eventId) });

        // 5) Close this writable DB
        db.close();

        return rows > 0;
    }
}
