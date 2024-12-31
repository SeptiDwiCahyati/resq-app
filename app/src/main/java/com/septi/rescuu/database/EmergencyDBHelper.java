package com.septi.rescuu.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.septi.rescuu.model.Emergency;

import java.util.ArrayList;
import java.util.List;

public class EmergencyDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "emergency.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_EMERGENCY = "emergencies";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    private static final String CREATE_TABLE_EMERGENCY =
            "CREATE TABLE " + TABLE_EMERGENCY + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_LATITUDE + " REAL NOT NULL, " +
                    COLUMN_LONGITUDE + " REAL NOT NULL, " +
                    COLUMN_TYPE + " TEXT NOT NULL, " +
                    COLUMN_DESCRIPTION + " TEXT, " +
                    COLUMN_TIMESTAMP + " TEXT NOT NULL);";

    public EmergencyDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_EMERGENCY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EMERGENCY);
        onCreate(db);
    }

    public long insertEmergency( Emergency emergency) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_LATITUDE, emergency.getLatitude());
        values.put(COLUMN_LONGITUDE, emergency.getLongitude());
        values.put(COLUMN_TYPE, emergency.getType());
        values.put(COLUMN_DESCRIPTION, emergency.getDescription());
        values.put(COLUMN_TIMESTAMP, emergency.getTimestamp());
        return db.insert(TABLE_EMERGENCY, null, values);
    }

    public List<Emergency> getAllEmergencies() {
        List<Emergency> emergencies = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_EMERGENCY, null, null, null, null, null, COLUMN_TIMESTAMP + " DESC");

        while (cursor.moveToNext()) {
            Emergency emergency = new Emergency(
                    cursor.getDouble(cursor.getColumnIndex(COLUMN_LATITUDE)),
                    cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGITUDE)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_TYPE)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_TIMESTAMP))
            );
            emergency.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));
            emergencies.add(emergency);
        }
        cursor.close();
        return emergencies;
    }
}