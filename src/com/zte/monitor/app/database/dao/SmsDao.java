package com.zte.monitor.app.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import com.zte.monitor.app.SystemConstants;
import com.zte.monitor.app.database.DbConstants;
import com.zte.monitor.app.model.SmsModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sylar on 8/26/14.
 * 短信数据库操作类
 */
public class SmsDao extends BaseDao {

    public SmsDao(Context context) {
        super(context);
    }


    /**
     * 保存
     */
    public void save(SmsModel smsModel) {
        ContentValues values = new ContentValues();
        values.put(DbConstants.COLUMN_SMS.DIRECTION, smsModel.direction);
        values.put(DbConstants.COLUMN_SMS.SC, smsModel.sc);
        values.put(DbConstants.COLUMN_SMS.IMSI, smsModel.imsi);
        values.put(DbConstants.COLUMN_SMS.PH_NUM, smsModel.phNum);
        values.put(DbConstants.COLUMN_SMS.SMS_CONTENT, smsModel.content);
        values.put(DbConstants.COLUMN_SMS.SMS_NEW_CONTENT, smsModel.newContent);
        values.put(DbConstants.COLUMN_SMS.UP_TIME, smsModel.upTime);
        values.put(DbConstants.COLUMN_SMS.COUNT_DOWN_NUM, smsModel.countdownNum);
        values.put(DbConstants.COLUMN_SMS.SMS_STATUS, smsModel.status);
        values.put(DbConstants.COLUMN_SMS.SMS_IS_ON_AUDIT, smsModel.onSmsAudit);
        database.insert(DbConstants.TABLE.sms_info, null, values);
    }

    /**
     * 更新
     */
    public void update(SmsModel smsModel) {
        ContentValues values = new ContentValues();
        values.put(DbConstants.COLUMN_SMS.DIRECTION, smsModel.direction);
        values.put(DbConstants.COLUMN_SMS.SC, smsModel.sc);
        values.put(DbConstants.COLUMN_SMS.IMSI, smsModel.imsi);
        values.put(DbConstants.COLUMN_SMS.PH_NUM, smsModel.phNum);
        values.put(DbConstants.COLUMN_SMS.SMS_CONTENT, smsModel.content);
        values.put(DbConstants.COLUMN_SMS.SMS_NEW_CONTENT, smsModel.newContent);
        values.put(DbConstants.COLUMN_SMS.UP_TIME, smsModel.upTime);
        values.put(DbConstants.COLUMN_SMS.COUNT_DOWN_NUM, smsModel.countdownNum);
        values.put(DbConstants.COLUMN_SMS.SMS_STATUS, smsModel.status);
        values.put(DbConstants.COLUMN_SMS.SMS_IS_ON_AUDIT, smsModel.onSmsAudit);
        database.update(DbConstants.TABLE.sms_info, values,
                DbConstants.COLUMN_SMS.IMSI + " =? and " + DbConstants.COLUMN_SMS.DIRECTION + " =? and " + DbConstants.COLUMN_SMS.UP_TIME + "=?",
                new String[]{smsModel.imsi, smsModel.direction + "", smsModel.upTime});
    }

    /**
     * 判断是否存在
     */
    public boolean isExits(SmsModel smsModel) {
        boolean result = false;
        Cursor cursor = null;
        try {
            String sql = "select count(*) from " + DbConstants.TABLE.sms_info + " where "
                    + DbConstants.COLUMN_SMS.IMSI + " =? and " + DbConstants.COLUMN_SMS.DIRECTION + " =?";
            cursor = database.rawQuery(sql, new String[]{smsModel.imsi, smsModel.direction + ""});
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
     * 获取短信列表
     */
    public List<SmsModel> getSmsList() {
        List<SmsModel> result = null;
        Cursor cursor = null;
        try {
            String sql = "select * from " + DbConstants.TABLE.sms_info + " where " + DbConstants.COLUMN_SMS.SMS_IS_ON_AUDIT + "= 1 order by date(" + DbConstants.COLUMN_SMS.UP_TIME + ") desc, time(" + DbConstants.COLUMN_SMS.UP_TIME + ") desc";
            cursor = database.rawQuery(sql, null);
            if (cursor != null) {
                result = new ArrayList<SmsModel>();
                while (cursor.moveToNext()) {
                    SmsModel smsModel = new SmsModel();
                    smsModel.direction = Byte.parseByte(cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_SMS.DIRECTION)));
                    smsModel.imsi = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_SMS.IMSI));
                    smsModel.phNum = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_SMS.PH_NUM));
                    smsModel.content = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_SMS.SMS_CONTENT));
                    smsModel.newContent = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_SMS.SMS_NEW_CONTENT));
                    smsModel.upTime = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_SMS.UP_TIME));
                    smsModel.sc = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_SMS.SC));
                    smsModel.countdownNum = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_SMS.COUNT_DOWN_NUM));
                    smsModel.status = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_SMS.SMS_STATUS));
                    smsModel.onSmsAudit = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_SMS.SMS_IS_ON_AUDIT));
                    result.add(smsModel);
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
     * 根据IMSI获取短信内容
     */
    public SmsModel getSmsModelByImsi(String imsi) {
        SmsModel smsModel = null;
        Cursor cursor = null;
        try {
            String sql = "select * from " + DbConstants.TABLE.sms_info + " where " + DbConstants.COLUMN_SMS.IMSI + "=? order by " + DbConstants.COLUMN_SMS.UP_TIME + " DESC";
            cursor = database.rawQuery(sql, new String[]{imsi});
            if (cursor != null && cursor.moveToFirst()) {
                smsModel = new SmsModel();
                smsModel.direction = Byte.parseByte(cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_SMS.DIRECTION)));
                smsModel.imsi = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_SMS.IMSI));
                smsModel.phNum = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_SMS.PH_NUM));
                smsModel.content = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_SMS.SMS_CONTENT));
                smsModel.newContent = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_SMS.SMS_NEW_CONTENT));
                smsModel.upTime = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_SMS.UP_TIME));
                smsModel.sc = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_SMS.SC));
                smsModel.countdownNum = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_SMS.COUNT_DOWN_NUM));
                smsModel.status = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_SMS.SMS_STATUS));
                smsModel.onSmsAudit = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_SMS.SMS_IS_ON_AUDIT));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return smsModel;
    }

    /**
     * 获取新到短信列表
     *
     * @return
     */
    public List<SmsModel> getNewArrivedSmsList() {
        List<SmsModel> result = null;
        Cursor cursor = null;
        try {
            String sql = "select * from " + DbConstants.TABLE.sms_info + " where " + DbConstants.COLUMN_SMS.SMS_STATUS + "=? order by " + DbConstants.COLUMN_SMS.UP_TIME + " desc";
            cursor = database.rawQuery(sql, new String[]{String.valueOf(SystemConstants.SMS_STATUS.NEW_ARRIVE)});
            if (cursor != null) {
                result = new ArrayList<SmsModel>();
                while (cursor.moveToNext()) {
                    SmsModel smsModel = new SmsModel();
                    smsModel.direction = Byte.parseByte(cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_SMS.DIRECTION)));
                    smsModel.imsi = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_SMS.IMSI));
                    smsModel.phNum = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_SMS.PH_NUM));
                    smsModel.content = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_SMS.SMS_CONTENT));
                    smsModel.newContent = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_SMS.SMS_NEW_CONTENT));
                    smsModel.upTime = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_SMS.UP_TIME));
                    smsModel.sc = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_SMS.SC));
                    smsModel.countdownNum = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_SMS.COUNT_DOWN_NUM));
                    smsModel.status = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_SMS.SMS_STATUS));
                    smsModel.onSmsAudit = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_SMS.SMS_IS_ON_AUDIT));
                    result.add(smsModel);
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
     * 批量更新
     */
    public void batchUpdate(List<SmsModel> smsModelList) {
        database.beginTransaction();
        try {
            for (SmsModel smsModel : smsModelList) {
                update(smsModel);
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }

    }

    /**
     * 根据关键字搜索短信
     *
     * @param keyword
     * @return
     */
    public List<SmsModel> searchSms(String keyword) {
        List<SmsModel> result = null;
        Cursor cursor = null;
        try {
            StringBuffer stringBuffer = new StringBuffer("select * from ");
            stringBuffer.append(DbConstants.TABLE.sms_info).append(" where ").append(DbConstants.COLUMN_SMS.IMSI).append(" like ").append(" '%").append(keyword).append("%' ")
                    .append(" or ").append(DbConstants.COLUMN_SMS.PH_NUM).append(" like ").append(" '%").append(keyword).append("%' ")
                    .append(" or ").append(DbConstants.COLUMN_SMS.SMS_CONTENT).append(" like ").append(" '%").append(keyword).append("%' ")
                    .append(" or ").append(DbConstants.COLUMN_SMS.SMS_NEW_CONTENT).append(" like ").append(" '%").append(keyword).append("%' ")
                    .append(" order by ").append(DbConstants.COLUMN_SMS.UP_TIME).append(" desc");
            Log.e("TAG", stringBuffer.toString());
            cursor = database.rawQuery(stringBuffer.toString(), null);
            if (cursor != null) {
                result = new ArrayList<SmsModel>();
                while (cursor.moveToNext()) {
                    SmsModel smsModel = new SmsModel();
                    smsModel.direction = Byte.parseByte(cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_SMS.DIRECTION)));
                    smsModel.imsi = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_SMS.IMSI));
                    smsModel.phNum = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_SMS.PH_NUM));
                    smsModel.content = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_SMS.SMS_CONTENT));
                    smsModel.newContent = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_SMS.SMS_NEW_CONTENT));
                    smsModel.upTime = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_SMS.UP_TIME));
                    smsModel.sc = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_SMS.SC));
                    smsModel.countdownNum = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_SMS.COUNT_DOWN_NUM));
                    smsModel.status = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_SMS.SMS_STATUS));
                    smsModel.onSmsAudit = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_SMS.SMS_IS_ON_AUDIT));
                    result.add(smsModel);
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
     * 清空
     */
    public void clear() {
        database.delete(DbConstants.TABLE.sms_info, null, null);
    }
}
