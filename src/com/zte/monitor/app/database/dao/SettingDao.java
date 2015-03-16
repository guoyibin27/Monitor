package com.zte.monitor.app.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import com.zte.monitor.app.SystemConstants;
import com.zte.monitor.app.database.DbConstants;
import com.zte.monitor.app.model.SettingModel;

/**
 * Created by Sylar on 14-9-17.
 * 服务器参数设置数据库操作类
 */
public class SettingDao extends BaseDao {
    public SettingDao(Context context) {
        super(context);
    }

    /**
     * 保存/更新
     *
     * @param settingModel
     */
    public void saveOrUpdate(SettingModel settingModel) {
        ContentValues values = new ContentValues();
        values.put(DbConstants.COLUMN_SETTING.TAKE_OVER_MODE, settingModel.takeOverMode);
        values.put(DbConstants.COLUMN_SETTING.AUTO_SEND_SMS_INTERVAL, settingModel.autoSendSmsInterval);
        values.put(DbConstants.COLUMN_SETTING.CMCC_MONITOR_COUNT, settingModel.cmccMonitorCount);
        values.put(DbConstants.COLUMN_SETTING.CUCC_MONITOR_COUNT, settingModel.cuccMonitorCount);
        values.put(DbConstants.COLUMN_SETTING.AUTO_SEND_SMS, settingModel.autoSendSms);
        values.put(DbConstants.COLUMN_SETTING.STATION_FREQUENCY, settingModel.stationFrequency);
        values.put(DbConstants.COLUMN_SETTING.REGION_FREQUENCY, settingModel.regionFrequency);
        values.put(DbConstants.COLUMN_SETTING.OPERATOR, settingModel.operator);
        values.put(DbConstants.COLUMN_SETTING.INTENSITY, settingModel.intensity);
        values.put(DbConstants.COLUMN_SETTING.POWER, settingModel.power);
        if (isExists()) {
            database.update(DbConstants.TABLE.setting, values, null, null);
        } else {
            database.insert(DbConstants.TABLE.setting, null, values);
        }
    }

    private boolean isExists() {
        boolean result = Boolean.FALSE;
        Cursor cursor = null;
        try {
            String sql = "select count(*) as totalCount from " + DbConstants.TABLE.setting;
            Log.e("TAG", sql);
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

    /**
     * 获取配置
     *
     * @return
     */
    public SettingModel getSetting() {
        SettingModel model = null;
        Cursor cursor = null;
        try {
            String sql = "select * from " + DbConstants.TABLE.setting;
            cursor = database.rawQuery(sql, null);
            if (cursor != null && cursor.moveToFirst()) {
                model = new SettingModel();
                model.takeOverMode = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_SETTING.TAKE_OVER_MODE));
                model.autoSendSmsInterval = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_SETTING.AUTO_SEND_SMS_INTERVAL));
                model.cmccMonitorCount = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_SETTING.CMCC_MONITOR_COUNT));
                model.cuccMonitorCount = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_SETTING.CUCC_MONITOR_COUNT));
                model.autoSendSms = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_SETTING.AUTO_SEND_SMS));
                model.stationFrequency = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_SETTING.STATION_FREQUENCY));
                model.regionFrequency = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_SETTING.REGION_FREQUENCY));
                model.operator = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_SETTING.OPERATOR));
                model.intensity = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_SETTING.INTENSITY));
                model.power = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_SETTING.POWER));
            } else {
                model = new SettingModel();
                model.autoSendSmsInterval = 20;
                model.cmccMonitorCount = 0;
                model.cuccMonitorCount = 0;
                model.autoSendSms = SystemConstants.AUTO_SEND_SMS.DO_NOT_SEND;
            }
        } catch (Exception e) {
            Log.e("TAG", e.getLocalizedMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return model;
    }

    public void clear() {
        database.delete(DbConstants.TABLE.setting, null, null);
    }
}
