package com.example.momento.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.momento.models.Category;
import com.example.momento.models.Event;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database configutation
    private static final String DATABASE_NAME = "momento.db";
    private static final int DATABASE_VERSION = 2;

    // Events table and columns
    private static final String TABLE_EVENTS = "events";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_LOCATION = "location";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_IMAGE_URI = "image_uri";

    // Categories table and columns
    private static final String TABLE_CATEGORIES = "categories";
    private static final String COLUMN_CATEGORY_ID = "id";
    private static final String COLUMN_CATEGORY_NAME = "name";

    // SQL statement to create the events table
    private static final String CREATE_TABLE_EVENTS =
            "CREATE TABLE " + TABLE_EVENTS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TITLE + " TEXT, " +
                    COLUMN_DATE + " TEXT, " +
                    COLUMN_LOCATION + " TEXT, " +
                    COLUMN_CATEGORY + " TEXT, " +
                    COLUMN_IMAGE_URI + " TEXT);";

    // SQL statement to create the categories table
    private static final String CREATE_TABLE_CATEGORIES =
            "CREATE TABLE " + TABLE_CATEGORIES + " (" +
                    COLUMN_CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_CATEGORY_NAME + " TEXT);";

    // Constructor
    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Executes statements to create events and categories tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_EVENTS);
        db.execSQL(CREATE_TABLE_CATEGORIES);
    }

    // Statement to drop existing tables and recreate them.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        onCreate(db);
    }

    // Inserts a new event into the events table
    public void insertEvent(Event event) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, event.getTitle());
        values.put(COLUMN_DATE, event.getDate());
        values.put(COLUMN_LOCATION, event.getLocation());
        values.put(COLUMN_CATEGORY, event.getCategory());
        values.put(COLUMN_IMAGE_URI, event.getImageUri());

        db.insert(TABLE_EVENTS, null, values);
        db.close();
    }

    // Retrieves all events from the events table
    public List<Event> getAllEvents() {
        List<Event> eventList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Query all columns from the events table
        Cursor cursor = db.query(TABLE_EVENTS, null, null, null, null, null, null);

        // Iterate through result and construct the events
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String title = getColumnValue(cursor, COLUMN_TITLE, "Default Title");
                String date = getColumnValue(cursor, COLUMN_DATE, "Default Date");
                String location = getColumnValue(cursor, COLUMN_LOCATION, "Default Location");
                String category = getColumnValue(cursor, COLUMN_CATEGORY, "Default Category");
                String imageUri = getColumnValue(cursor, COLUMN_IMAGE_URI, "");

                Event event = new Event(id, title, date, location, category, imageUri);
                eventList.add(event);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return eventList;
    }

    // Gets the column value in a string
    private String getColumnValue(Cursor cursor, String columnName, String defaultValue) {
        try {
            int columnIndex = cursor.getColumnIndexOrThrow(columnName);
            return cursor.getString(columnIndex);
        } catch (IllegalArgumentException e) {
            Log.w("DatabaseHelper", columnName + " not found, using default value");
            return defaultValue;
        }
    }

    // Retrieves an Event by ID
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
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URI))
            );
            cursor.close();
        }
        db.close();
        return event;
    }

    // Updates an event record
    public boolean updateEvent(Event event) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE,       event.getTitle());
        values.put(COLUMN_DATE,        event.getDate());
        values.put(COLUMN_LOCATION,    event.getLocation());
        values.put(COLUMN_CATEGORY,    event.getCategory());
        values.put(COLUMN_IMAGE_URI,   event.getImageUri());

        int rows = db.update(TABLE_EVENTS,
                values,
                COLUMN_ID + " = ?",
                new String[]{ String.valueOf(event.getId()) });

        db.close();
        return rows > 0;
    }

    // Deletes an image by its ID and removes its image from storage
    public boolean deleteEvent(int eventId) {
        // Load event to delete its image
        Event event = getEventById(eventId);
        SQLiteDatabase db = this.getWritableDatabase();

        // If the event has an image URI attempt to delete the image from storage
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

        // Remove the event record
        int rows = db.delete(TABLE_EVENTS,
                COLUMN_ID + "=?",
                new String[]{ String.valueOf(eventId) });

        db.close();

        return rows > 0;
    }

    // Inserts a new category into the categories table
    public long insertCategory(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CATEGORY_NAME, name);
        return db.insert(TABLE_CATEGORIES, null, values);
    }

    // Retrieves all categories from the events table
    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CATEGORIES, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Category category = new Category();
                category.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_ID)));
                category.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_NAME)));
                categories.add(category);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return categories;
    }

    // Checks id a category is used by any events
    public boolean isCategoryUsed(int categoryId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String categoryName = null;

        // Lookup category name by ID
        Cursor catCursor = db.rawQuery("SELECT name FROM categories WHERE id = ?", new String[]{String.valueOf(categoryId)});
        if (catCursor.moveToFirst()) {
            categoryName = catCursor.getString(0);
        }
        catCursor.close();

        if (categoryName == null) return false;

        // Check for any events using that category
        Cursor eventCursor = db.rawQuery("SELECT * FROM events WHERE category = ?", new String[]{categoryName});
        boolean isUsed = eventCursor.moveToFirst();
        eventCursor.close();

        return isUsed;
    }

    // Updates a category record
    public void updateCategory(int id, String newName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CATEGORY_NAME, newName);
        db.update(TABLE_CATEGORIES, values, COLUMN_CATEGORY_ID + " = ?", new String[]{String.valueOf(id)});
    }

    // Deletes a category record by ID
    public void deleteCategory(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CATEGORIES, COLUMN_CATEGORY_ID + " = ?", new String[]{String.valueOf(id)});
    }
}
