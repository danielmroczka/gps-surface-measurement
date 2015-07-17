package com.labs.dm.gpssurfacemeasurement;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by daniel on 2015-07-16.
 */
public class DBManager extends SQLiteOpenHelper {

    public DBManager(Context context, String name) {
        super(context, name, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // CREATE TABLE
        db.execSQL("create table MEASUREMENT(id INTEGER PRIMARY KEY, created DATETIME DEFAULT CURRENT_TIMESTAMP )");
        db.execSQL("create table POINTS(id_measurement INTEGER, latitude REAL, longitude REAL, FOREIGN KEY(id_measurement) REFERENCES measurement(id))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void save(List<Position> list) {
        SQLiteDatabase writableDatabase = getWritableDatabase();
        ContentValues content = new ContentValues();
        content.put("created", new Date().getTime());
        long id = writableDatabase.insertOrThrow("MEASUREMENT", null, content);

        for (Position pos:list) {
            content = new ContentValues();
            content.put("latitude", pos.getLatitude());
            content.put("longitude", pos.getLongitude());
            content.put("id_measurement", id);
            writableDatabase.insertOrThrow("POINTS", null, content);
        }

        writableDatabase.close();
    }

    public List<Map<String, String>> list() {
        List<Map<String, String>> list = new ArrayList<>();
        SQLiteDatabase readableDatabase = getReadableDatabase();
        Cursor cursor = readableDatabase.rawQuery("SELECT id, created from MEASUREMENT", null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isLast()) {
                Map<String, String> map = new HashMap<>();
                map.put("id", String.valueOf(cursor.getLong(0)));
                map.put("created", new Date(cursor.getLong(1)).toString());
                list.add(map);
                cursor.moveToNext();
            }
        }

        cursor.close();

        return list;
    }
}
