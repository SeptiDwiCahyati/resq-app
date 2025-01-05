// TrackingDBHelper.java
package com.septi.resq.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.septi.resq.model.TrackingStatus;

public class TrackingDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "tracking.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_TRACKING = "tracking";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TEAM_ID = "team_id";
    public static final String COLUMN_EMERGENCY_ID = "emergency_id";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_CURRENT_LAT = "current_lat";
    public static final String COLUMN_CURRENT_LON = "current_lon";
    public static final String COLUMN_DESTINATION_LAT = "destination_lat";
    public static final String COLUMN_DESTINATION_LON = "destination_lon";
    public static final String COLUMN_ROUTE_INDEX = "route_index";

    public TrackingDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TRACKING_TABLE = "CREATE TABLE " + TABLE_TRACKING + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_TEAM_ID + " INTEGER, "
                + COLUMN_EMERGENCY_ID + " INTEGER, "
                + COLUMN_STATUS + " TEXT, "
                + COLUMN_CURRENT_LAT + " DOUBLE, "
                + COLUMN_CURRENT_LON + " DOUBLE, "
                + COLUMN_DESTINATION_LAT + " DOUBLE, "
                + COLUMN_DESTINATION_LON + " DOUBLE, "
                + COLUMN_ROUTE_INDEX + " INTEGER" + ")";
        db.execSQL(CREATE_TRACKING_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRACKING);
        onCreate(db);
    }

    public long insertTracking(TrackingStatus status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TEAM_ID, status.getTeamId());
        values.put(COLUMN_EMERGENCY_ID, status.getEmergencyId());
        values.put(COLUMN_STATUS, status.getStatus());
        values.put(COLUMN_CURRENT_LAT, status.getCurrentLat());
        values.put(COLUMN_CURRENT_LON, status.getCurrentLon());
        values.put(COLUMN_DESTINATION_LAT, status.getDestinationLat());
        values.put(COLUMN_DESTINATION_LON, status.getDestinationLon());
        values.put(COLUMN_ROUTE_INDEX, status.getRouteIndex());
        return db.insert(TABLE_TRACKING, null, values);
    }

    public TrackingStatus getLastTrackingStatus(long teamId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TRACKING,
                null,
                COLUMN_TEAM_ID + " = ?",
                new String[]{String.valueOf(teamId)},
                null,
                null,
                COLUMN_ID + " DESC",  // Order by ID descending to get the latest
                "1");  // Limit 1 to get only the most recent

        TrackingStatus status = null;
        if (cursor.moveToFirst()) {
            status = new TrackingStatus();
            status.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));
            status.setTeamId(cursor.getLong(cursor.getColumnIndex(COLUMN_TEAM_ID)));
            status.setEmergencyId(cursor.getLong(cursor.getColumnIndex(COLUMN_EMERGENCY_ID)));
            status.setStatus(cursor.getString(cursor.getColumnIndex(COLUMN_STATUS)));
            status.setCurrentLat(cursor.getDouble(cursor.getColumnIndex(COLUMN_CURRENT_LAT)));
            status.setCurrentLon(cursor.getDouble(cursor.getColumnIndex(COLUMN_CURRENT_LON)));
            status.setDestinationLat(cursor.getDouble(cursor.getColumnIndex(COLUMN_DESTINATION_LAT)));
            status.setDestinationLon(cursor.getDouble(cursor.getColumnIndex(COLUMN_DESTINATION_LON)));
            status.setRouteIndex(cursor.getInt(cursor.getColumnIndex(COLUMN_ROUTE_INDEX)));
        }
        cursor.close();
        return status;
    }

    public boolean updateTracking(TrackingStatus status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_STATUS, status.getStatus());
        values.put(COLUMN_CURRENT_LAT, status.getCurrentLat());
        values.put(COLUMN_CURRENT_LON, status.getCurrentLon());
        values.put(COLUMN_ROUTE_INDEX, status.getRouteIndex());

        return db.update(TABLE_TRACKING, values,
                COLUMN_TEAM_ID + " = ? AND " + COLUMN_EMERGENCY_ID + " = ?",
                new String[]{String.valueOf(status.getTeamId()),
                        String.valueOf(status.getEmergencyId())}) > 0;
    }

    public TrackingStatus getActiveTracking(long teamId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TRACKING,
                null,
                COLUMN_TEAM_ID + " = ? AND " + COLUMN_STATUS + " != ?",
                new String[]{String.valueOf(teamId), "COMPLETED"},
                null, null, null);

        TrackingStatus status = null;
        if (cursor.moveToFirst()) {
            status = new TrackingStatus();
            status.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));
            status.setTeamId(cursor.getLong(cursor.getColumnIndex(COLUMN_TEAM_ID)));
            status.setEmergencyId(cursor.getLong(cursor.getColumnIndex(COLUMN_EMERGENCY_ID)));
            status.setStatus(cursor.getString(cursor.getColumnIndex(COLUMN_STATUS)));
            status.setCurrentLat(cursor.getDouble(cursor.getColumnIndex(COLUMN_CURRENT_LAT)));
            status.setCurrentLon(cursor.getDouble(cursor.getColumnIndex(COLUMN_CURRENT_LON)));
            status.setDestinationLat(cursor.getDouble(cursor.getColumnIndex(COLUMN_DESTINATION_LAT)));
            status.setDestinationLon(cursor.getDouble(cursor.getColumnIndex(COLUMN_DESTINATION_LON)));
            status.setRouteIndex(cursor.getInt(cursor.getColumnIndex(COLUMN_ROUTE_INDEX)));
        }
        cursor.close();
        return status;
    }
}