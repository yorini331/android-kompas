package com.example.compas;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "compass.db";
    private static final int DATABASE_VERSION = 2;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE compass_data (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "azimuth REAL NOT NULL," +
                "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "latitude REAL," +
                "longitude REAL," +
                "accuracy REAL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS compass_data");
        onCreate(db);
    }
}
