package com.zte.monitor.app.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by sylar on 14-3-12.
 */
public class SQLiteManager {

    private static volatile SQLiteManager instance = null;
    private DbHelper dbHelper;

    private SQLiteDatabase writableDatabase;
    private SQLiteDatabase readableDatabase;

    private SQLiteManager(Context context) {
        dbHelper = new DbHelper(context);
        writableDatabase = dbHelper.getWritableDatabase();
        readableDatabase = dbHelper.getReadableDatabase();
    }

    public static SQLiteManager getInstance(Context context) {
        if (instance == null) {
            synchronized (SQLiteManager.class) {
                if (instance == null)
                    instance = new SQLiteManager(context);
            }
        }
        return instance;
    }

    public SQLiteDatabase getWritableDatabase() {
        return writableDatabase;
    }

    public SQLiteDatabase getReadableDatabase() {
        return readableDatabase;
    }

    public void release() {
        writableDatabase.close();
        readableDatabase.close();
        writableDatabase = null;
        readableDatabase = null;
        dbHelper = null;
        instance = null;
    }
}
