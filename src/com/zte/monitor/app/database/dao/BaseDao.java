package com.zte.monitor.app.database.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.zte.monitor.app.database.SQLiteManager;

/**
 * Created by Sylar on 14-9-16.
 */
public class BaseDao {
    protected SQLiteDatabase database;

    public BaseDao(Context context) {
        database = SQLiteManager.getInstance(context).getReadableDatabase();
    }
}
