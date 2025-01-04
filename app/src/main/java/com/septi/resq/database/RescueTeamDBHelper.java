package com.septi.resq.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.septi.resq.model.RescueTeam;

import java.util.ArrayList;
import java.util.List;

public class RescueTeamDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "RescueTeam.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_RESCUE_TEAMS = "rescue_teams";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_LATITUDE = "latitude";
    private static final String COLUMN_LONGITUDE = "longitude";
    private static final String COLUMN_CONTACT = "contact_number";
    private static final String COLUMN_DISTANCE = "distance";
    private static final String COLUMN_AVAILABLE = "is_available";

    public RescueTeamDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TEAMS_TABLE = "CREATE TABLE " + TABLE_RESCUE_TEAMS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_LATITUDE + " REAL,"
                + COLUMN_LONGITUDE + " REAL,"
                + COLUMN_CONTACT + " TEXT,"
                + COLUMN_DISTANCE + " REAL,"
                + COLUMN_AVAILABLE + " INTEGER" + ")";
        db.execSQL(CREATE_TEAMS_TABLE);

        // Insert sample data
        insertSampleData(db);
    }

    private void insertSampleData(SQLiteDatabase db) {
        String[][] sampleData = {
                {"Basarnas Pontianak", "0.0319", "109.3250", "0561-55555555"}, // Pontianak coordinates
                {"PMI Pontianak", "0.0332", "109.3345", "0561-44444444"},
                {"Relawan Pontianak", "0.0325", "109.3308", "0561-33333333"},
                {"Basarnas Sintang", "0.0716", "111.4950", "0565-55555555"}, // Sintang coordinates
                {"PMI Sintang", "0.0730", "111.4975", "0565-44444444"},
                {"Relawan Sintang", "0.0709", "111.4948", "0565-33333333"},
                {"Basarnas Sanggau", "0.1246", "110.5642", "0564-55555555"}, // Sanggau coordinates
                {"PMI Sanggau", "0.1275", "110.5678", "0564-44444444"},
                {"Relawan Sanggau", "0.1258", "110.5653", "0564-33333333"},
                {"Basarnas Ketapang", "-1.8536", "109.9779", "0534-55555555"}, // Ketapang coordinates
                {"PMI Ketapang", "-1.8502", "109.9824", "0534-44444444"},
                {"Relawan Ketapang", "-1.8528", "109.9796", "0534-33333333"}
        };

        for (String[] data : sampleData) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME, data[0]);
            values.put(COLUMN_LATITUDE, Double.parseDouble(data[1]));
            values.put(COLUMN_LONGITUDE, Double.parseDouble(data[2]));
            values.put(COLUMN_CONTACT, data[3]);
            values.put(COLUMN_AVAILABLE, 1);
            db.insert(TABLE_RESCUE_TEAMS, null, values);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RESCUE_TEAMS);
        onCreate(db);
    }

    public List<RescueTeam> getAllTeams() {
        List<RescueTeam> teams = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_RESCUE_TEAMS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                RescueTeam team = new RescueTeam(
                        cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LATITUDE)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LONGITUDE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTACT)),
                        cursor.isNull(cursor.getColumnIndexOrThrow(COLUMN_DISTANCE)) ? null :
                                cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_DISTANCE)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_AVAILABLE)) == 1
                );
                teams.add(team);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return teams;
    }

    public List<RescueTeam> getAvailableTeams() {
        List<RescueTeam> teams = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_RESCUE_TEAMS +
                " WHERE " + COLUMN_AVAILABLE + " = 1";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                RescueTeam team = new RescueTeam(
                        cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LATITUDE)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LONGITUDE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTACT)),
                        cursor.isNull(cursor.getColumnIndexOrThrow(COLUMN_DISTANCE)) ? null :
                                cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_DISTANCE)),
                        true
                );
                teams.add(team);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return teams;
    }
}
