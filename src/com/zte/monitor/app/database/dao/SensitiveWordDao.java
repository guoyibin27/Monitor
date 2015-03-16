package com.zte.monitor.app.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import com.zte.monitor.app.database.DbConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sylar on 14-10-8.
 * 敏感词数据库操作类
 */
public class SensitiveWordDao extends BaseDao {
    public SensitiveWordDao(Context context) {
        super(context);
    }

    /**
     * 添加/更新
     *
     * @param word
     */
    public void saveOrUpdate(String word) {
        ContentValues values = new ContentValues();
        values.put(DbConstants.COLUMN_SENSITIVE_WORD.SENSITIVE_WORD, word);
        if (isExists(word)) {
            database.update(DbConstants.TABLE.sensitive_word, values, DbConstants.COLUMN_SENSITIVE_WORD.SENSITIVE_WORD + "=?", new String[]{word});
        } else {
            database.insert(DbConstants.TABLE.sensitive_word, null, values);
        }
    }

    private boolean isExists(String word) {
        boolean result = false;
        String sql = "select count(*) from " + DbConstants.TABLE.sensitive_word + " where " + DbConstants.COLUMN_SENSITIVE_WORD.SENSITIVE_WORD
                + "= ?";
        Cursor cursor = null;
        try {
            cursor = database.rawQuery(sql, new String[]{word});
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


    /**
     * 获取所有敏感词
     *
     * @return
     */
    public List<String> getList() {
        List<String> result = new ArrayList<String>();
        String sql = "select * from " + DbConstants.TABLE.sensitive_word;
        Cursor cursor = null;
        try {
            cursor = database.rawQuery(sql, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    result.add(cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_SENSITIVE_WORD.SENSITIVE_WORD)));
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
     * @param word
     */
    public void delete(String word) {
        database.delete(DbConstants.TABLE.sensitive_word, DbConstants.COLUMN_SENSITIVE_WORD.SENSITIVE_WORD + "=?", new String[]{word});
    }

    public void batchSave(List<String> sensitiveWordList) {
        database.beginTransaction();
        try {
            for (String string : sensitiveWordList) {
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
        database.delete(DbConstants.TABLE.sensitive_word, null, null);
    }
}
