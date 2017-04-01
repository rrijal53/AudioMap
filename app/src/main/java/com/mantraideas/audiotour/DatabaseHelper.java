package com.mantraideas.audiotour;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.support.annotation.Keep;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rowsun on 9/27/16.
 */
@Keep
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TABLE_LANDMARKS = "landmarks";
    private static final String ID = "_id";
    private static final String COLUMN_NAME = "title";
    private static final String COLUMN_LAT = "lat";
    private static final String COLUMN_LNG = "lng";

    private static final String CREATE_TABLE_LANDMARKS = "CREATE TABLE " + TABLE_LANDMARKS +
            " (" +
            ID + " INTEGER PRIMARY KEY AUTOINCREMENT ," +
            COLUMN_NAME + " TEXT ," +
            COLUMN_LAT + " TEXT ," +
            COLUMN_LNG + " TEXT "
            + ");";
    private Context mContext;
    private static final String DB_NAME = "Lestoury";
    private static final int DB_VERSION = 1;


    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(CREATE_TABLE_LANDMARKS);
            Utilities.log("Table Created.......................");
        } catch (SQLException exception) {
            Utilities.log("Error" + exception);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("DROP " + TABLE_LANDMARKS + " IF EXISTS;");
            onCreate(db);
        } catch (SQLException exception) {
            Utilities.log(exception + "");
        }
    }

    public boolean addLandmark(Landmarks m) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, m.title);
        values.put(COLUMN_LAT, m.geo_lat);
        values.put(COLUMN_LNG, m.geo_lng);
        SQLiteDatabase db = getWritableDatabase();
        db.insert(TABLE_LANDMARKS, null, values);
        db.close();
        return true;
    }

    public void updateLandmarks(Landmarks m) {
        SQLiteDatabase db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME, m.title);
            values.put(COLUMN_LAT, m.geo_lat);
            values.put(COLUMN_LNG, m.geo_lng);
            db.update(TABLE_LANDMARKS, values, COLUMN_NAME + "=?", new String[]{m.title});
            db.close();
    }

    public List<Landmarks> getLandmarks() {
        List<Landmarks> result = new ArrayList<>();
        String sql = "Select * from " + TABLE_LANDMARKS;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            do {
                Landmarks s = new Landmarks(cursor.getString(1), cursor.getDouble(2), cursor.getDouble(3));
                result.add(s);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return result;
    }

    public boolean isExists(String table, String id) {
        String sql = "Select * from " + table + " where " + COLUMN_NAME + "='" + COLUMN_NAME +"'";
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            return true;
        }
        cursor.close();
        db.close();
        return false;

    }

    public void deleteByName(String name) {
        String sql = "DELETE FROM " + TABLE_LANDMARKS + " WHERE "+COLUMN_NAME+"='" + name+ "'" ;
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(sql);
        db.close();
    }

    public void deleteAllData() {
        String sql = "DELETE FROM " + TABLE_LANDMARKS + " ";
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(sql);
        db.close();
    }


    public boolean exportToCsv() {


        File file   = null;
        File root   = Environment.getExternalStorageDirectory();
        File dir    =   new File (root.getAbsolutePath() + "/whatsupdata");
        dir.mkdirs();
        FileOutputStream out   =   null;
        file   =   new File(dir, "Data.csv");
        String sql = "Select * from " + TABLE_LANDMARKS;
        try {
            out = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            do {
                Landmarks s = new Landmarks(cursor.getString(1), cursor.getDouble(2), cursor.getDouble(3));

                String combinedString = s.title + "," + s.geo_lat + "," + s.geo_lng + "\n";
                if (root.canWrite()){
                    try {
                        out.write(combinedString.getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


            } while (cursor.moveToNext());
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        cursor.close();
        db.close();
        return true;


    }
}