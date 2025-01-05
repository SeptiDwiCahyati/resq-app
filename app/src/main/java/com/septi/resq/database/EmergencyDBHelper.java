package com.septi.resq.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.septi.resq.model.Emergency;

import java.util.ArrayList;
import java.util.List;

public class EmergencyDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "emergency.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_EMERGENCY = "emergencies";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_PHOTO_PATH = "photo_path";

    public static final String COLUMN_STATUS = "status";

    private static final String CREATE_TABLE_EMERGENCY =
            "CREATE TABLE " + TABLE_EMERGENCY + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_LATITUDE + " REAL NOT NULL, " +
                    COLUMN_LONGITUDE + " REAL NOT NULL, " +
                    COLUMN_TYPE + " TEXT NOT NULL, " +
                    COLUMN_DESCRIPTION + " TEXT, " +
                    COLUMN_TIMESTAMP + " TEXT NOT NULL, " +
                    COLUMN_PHOTO_PATH + " TEXT, " +
                    COLUMN_STATUS + " TEXT NOT NULL DEFAULT 'MENUNGGU');";

    public EmergencyDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_EMERGENCY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_EMERGENCY +
                    " ADD COLUMN " + COLUMN_PHOTO_PATH + " TEXT;");
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_EMERGENCY +
                    " ADD COLUMN " + COLUMN_STATUS + " TEXT NOT NULL DEFAULT 'MENUNGGU';");
        }
    }

    public boolean deleteEmergency(long id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_EMERGENCY, COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}) > 0;
    }

    public boolean updateEmergency(Emergency emergency) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TYPE, emergency.getType());
        values.put(COLUMN_DESCRIPTION, emergency.getDescription());
        values.put(COLUMN_LATITUDE, emergency.getLatitude());
        values.put(COLUMN_LONGITUDE, emergency.getLongitude());
        values.put(COLUMN_PHOTO_PATH, emergency.getPhotoPath());
        values.put(COLUMN_STATUS, emergency.getStatus().name());

        return db.update(TABLE_EMERGENCY, values,
                COLUMN_ID + "=?",
                new String[]{String.valueOf(emergency.getId())}) > 0;
    }

    public long insertEmergency(Emergency emergency) {
        // Check for existing data with same timestamp
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_EMERGENCY,
                null,
                COLUMN_TIMESTAMP + " = ? AND " +
                        COLUMN_LATITUDE + " = ? AND " +
                        COLUMN_LONGITUDE + " = ?",
                new String[]{
                        emergency.getTimestamp(),
                        String.valueOf(emergency.getLatitude()),
                        String.valueOf(emergency.getLongitude())
                },
                null, null, null);

        if (cursor.getCount() > 0) {
            cursor.close();
            return -1;
        }
        cursor.close();

        db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_LATITUDE, emergency.getLatitude());
        values.put(COLUMN_LONGITUDE, emergency.getLongitude());
        values.put(COLUMN_TYPE, emergency.getType());
        values.put(COLUMN_DESCRIPTION, emergency.getDescription());
        values.put(COLUMN_TIMESTAMP, emergency.getTimestamp());
        values.put(COLUMN_PHOTO_PATH, emergency.getPhotoPath());
        values.put(COLUMN_STATUS, emergency.getStatus().name());

        return db.insert(TABLE_EMERGENCY, null, values);
    }


    @SuppressLint("Range")
    public Emergency getEmergencyById(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_EMERGENCY, null, COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            Emergency emergency = new Emergency(
                    cursor.getDouble(cursor.getColumnIndex(COLUMN_LATITUDE)),
                    cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGITUDE)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_TYPE)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_TIMESTAMP)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_PHOTO_PATH))
            );
            emergency.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));
            emergency.setStatus(Emergency.EmergencyStatus.valueOf(
                    cursor.getString(cursor.getColumnIndex(COLUMN_STATUS))));
            cursor.close();
            return emergency;
        }

        if (cursor != null) {
            cursor.close();
        }
        return null;
    }


    @SuppressLint("Range")
    public List<Emergency> getAllEmergencies() {
        List<Emergency> emergencies = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_EMERGENCY, null, null, null, null, null,
                COLUMN_TIMESTAMP + " DESC");

        while (cursor.moveToNext()) {
            Emergency emergency = new Emergency(
                    cursor.getDouble(cursor.getColumnIndex(COLUMN_LATITUDE)),
                    cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGITUDE)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_TYPE)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_TIMESTAMP)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_PHOTO_PATH))
            );
            emergency.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));
            emergency.setStatus(Emergency.EmergencyStatus.valueOf(
                    cursor.getString(cursor.getColumnIndex(COLUMN_STATUS))));
            emergencies.add(emergency);
        }
        cursor.close();
        return emergencies;
    }
}