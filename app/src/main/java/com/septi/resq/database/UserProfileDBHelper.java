package com.septi.resq.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.septi.resq.model.UserProfile;

public class UserProfileDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "UserProfile.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_USER = "user_profile";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_PHOTO_URI = "photo_uri";

    private static final String CREATE_USER_TABLE = "CREATE TABLE " + TABLE_USER + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_NAME + " TEXT,"
            + COLUMN_EMAIL + " TEXT,"
            + COLUMN_PHONE + " TEXT,"
            + COLUMN_PHOTO_URI + " TEXT"
            + ")";

    public UserProfileDBHelper( Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        onCreate(db);
    }

    // CRUD Operations
    public long insertProfile( UserProfile profile) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME, profile.getName());
        values.put(COLUMN_EMAIL, profile.getEmail());
        values.put(COLUMN_PHONE, profile.getPhone());
        values.put(COLUMN_PHOTO_URI, profile.getPhotoUri());

        long id = db.insert(TABLE_USER, null, values);
        db.close();
        return id;
    }

    public UserProfile getProfile(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        UserProfile profile = null;

        Cursor cursor = db.query(TABLE_USER,
                new String[]{COLUMN_ID, COLUMN_NAME, COLUMN_EMAIL, COLUMN_PHONE, COLUMN_PHOTO_URI},
                COLUMN_ID + "=?", new String[]{String.valueOf(id)},
                null, null, null);

        if (cursor.moveToFirst()) {
            profile = new UserProfile(
                    cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHOTO_URI))
            );
            cursor.close();
        }
        db.close();
        return profile;
    }

    public boolean updateProfile(UserProfile profile) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME, profile.getName());
        values.put(COLUMN_EMAIL, profile.getEmail());
        values.put(COLUMN_PHONE, profile.getPhone());
        values.put(COLUMN_PHOTO_URI, profile.getPhotoUri());

        int rowsAffected = db.update(TABLE_USER, values,
                COLUMN_ID + "=?", new String[]{String.valueOf(profile.getId())});
        db.close();
        return rowsAffected > 0;
    }

}