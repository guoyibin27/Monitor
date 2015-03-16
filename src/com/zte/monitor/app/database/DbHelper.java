package com.zte.monitor.app.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by sylar on 14-3-12.
 */
public class DbHelper extends SQLiteOpenHelper {

    public DbHelper(Context context) {
        super(context, DbConstants.DB_NAME, null, DbConstants.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            db.execSQL(DbConstants.create_server_info_table.toString());
            db.execSQL(DbConstants.insert_default_server_info.toString());
            db.execSQL(DbConstants.create_user_info_table.toString());
            db.execSQL(DbConstants.create_sms_table.toString());
            db.execSQL(DbConstants.create_bts_state_table.toString());
            db.execSQL(DbConstants.create_monitor_line_table.toString());
            db.execSQL(DbConstants.create_setting_table.toString());
            db.execSQL(DbConstants.create_sensitive_phone_number_table.toString());
            db.execSQL(DbConstants.create_sensitive_word_table.toString());
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(this.getClass().getName(), e.getMessage());
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
