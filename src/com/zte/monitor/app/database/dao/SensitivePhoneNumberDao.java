package com.zte.monitor.app.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import com.zte.monitor.app.database.DbConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sylar on 14-10-8.
 * 敏感号码数据库操作类
 */
public class SensitivePhoneNumberDao extends BaseDao {
    public SensitivePhoneNumberDao(Context context) {
        super(context);
    }

    /**
     * 获取列表
     *
     * @return
     */
    public List<String> getList() {
        List<String> result = new ArrayList<String>();
        String sql = "select * from " + DbConstants.TABLE.sensitive_phone_number;
        Cursor cursor = null;
        try {
            cursor = database.rawQuery(sql, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    result.add(cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_SENSITIVE_PHONE_NUMBER.SENSITIVE_PHONE_NUMBER)));
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }

    /**
     * 删除
     *
     * @param number
     */
    public void delete(String number) {
        database.delete(DbConstants.TABLE.sensitive_phone_number, DbConstants.COLUMN_SENSITIVE_PHONE_NUMBER.SENSITIVE_PHONE_NUMBER + "=?", new String[]{number});
    }

    /**
     * 添加或更新
     *
     * @param number
     */
    public void saveOrUpdate(String number) {
        ContentValues values = new ContentValues();
        values.put(DbConstants.COLUMN_SENSITIVE_PHONE_NUMBER.SENSITIVE_PHONE_NUMBER, number);
        if (isExists(number)) {
            database.update(DbConstants.TABLE.sensitive_phone_number, values, DbConstants.COLUMN_SENSITIVE_PHONE_NUMBER.SENSITIVE_PHONE_NUMBER + "=?", new String[]{number});
        } else {
            database.insert(DbConstants.TABLE.sensitive_phone_number, null, values);
        }
    }

    /**
     * 判断是否存在
     *
     * @param number
     * @return
     */
    public boolean isExists(String number) {
        boolean result = false;
        String sql = "select count(*) from " + DbConstants.TABLE.sensitive_phone_number + " where " + DbConstants.COLUMN_SENSITIVE_PHONE_NUMBER.SENSITIVE_PHONE_NUMBER
                + " like '%" + number + "%'";
        Cursor cursor = null;
        try {
            cursor = database.rawQuery(sql, null);
            if (cursor != null && cursor.moveToFirst()) {
                result = cursor.getLong(0) > 0;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }

    public void batchSave(List<String> sensitiveNumberList) {
        database.beginTransaction();
        try {
            for (String string : sensitiveNumberList) {
                saveOrUpdate(string);
            }
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
        }
    }

    public void clear() {
        database.delete(DbConstants.TABLE.sensitive_phone_number, null, null);
    }
}
