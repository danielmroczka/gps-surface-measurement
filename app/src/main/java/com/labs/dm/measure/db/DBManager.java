package com.labs.dm.measure.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.labs.dm.measure.domain.Measurement;
import com.labs.dm.measure.domain.Position;

import java.text.SimpleDateFormat;
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

        for (Position pos : list) {
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
        Cursor cursor = readableDatabase.rawQuery("SELECT id, created, count(p.id_measurement) from MEASUREMENT m, POINTS p where m.id = p.id_measurement group by id, created", null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isLast()) {
                Map<String, String> map = new HashMap<>();
                map.put("id", String.valueOf(cursor.getLong(0)));

                String formattedDate = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date(cursor.getLong(1)));

                map.put("created", formattedDate + " (" + cursor.getLong(2) + ")");
                list.add(map);
                cursor.moveToNext();
            }
        }

        cursor.close();

        return list;
    }

    public void delete(String id) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.delete("POINTS", "id_measurement=?", new String[]{id});
            db.delete("MEASUREMENT", "id=?", new String[]{id});
        } finally {
            db.close();
        }
    }

    public Measurement getMeasurement(String id) {
        SQLiteDatabase readableDatabase = getReadableDatabase();
        Cursor cursor = readableDatabase.rawQuery("SELECT created from MEASUREMENT m where m.id=" + id, null);
        Measurement measurement = null;
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            measurement = new Measurement(new Date(cursor.getLong(0)));
        }

        cursor.close();
        readableDatabase.close();
        return measurement;

    }

    public List<Position> getPoints(String id) {
        List<Position> list = new ArrayList<>();

        SQLiteDatabase readableDatabase = getReadableDatabase();
        Cursor cursor = readableDatabase.rawQuery("SELECT latitude, longitude from POINTS p where p.id_measurement=" + id, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isLast()) {
                list.add(new Position(cursor.getDouble(0), cursor.getDouble(1)));
                cursor.moveToNext();
            }
        }

        cursor.close();
        readableDatabase.close();
        return list;
    }
}
