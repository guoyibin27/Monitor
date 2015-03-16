package com.zte.monitor.app.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import com.zte.monitor.app.database.DbConstants;
import com.zte.monitor.app.model.MonitorLineModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sylar on 14-9-17.
 * 监控线路数据库操作类
 */
public class MonitorLineDao extends BaseDao {
    public MonitorLineDao(Context context) {
        super(context);
    }

    /**
     * 保存
     *
     * @param monitorLineModel
     */
    public void save(MonitorLineModel monitorLineModel) {
        ContentValues values = new ContentValues();
        values.put(DbConstants.COLUMN_MONITOR_LINE.IMSI, monitorLineModel.imsi);
        values.put(DbConstants.COLUMN_MONITOR_LINE.IMEI, monitorLineModel.imei);
        values.put(DbConstants.COLUMN_MONITOR_LINE.LINE_NO, monitorLineModel.lineNo);
        values.put(DbConstants.COLUMN_MONITOR_LINE.CARRIER, monitorLineModel.carrier);
        values.put(DbConstants.COLUMN_MONITOR_LINE.NETWORK_SYSTEMS, monitorLineModel.networkSystems);
        database.insert(DbConstants.TABLE.monitor_line, null, values);
    }

    /**
     * 删除
     *
     * @param imsi
     * @param imei
     */
    public void delete(String imsi, String imei) {
        database.delete(DbConstants.TABLE.monitor_line,
                DbConstants.COLUMN_MONITOR_LINE.IMSI + "=? and " + DbConstants.COLUMN_MONITOR_LINE.IMEI + "=?",
                new String[]{imsi, imei});
    }

    /**
     * 获取当前网络制式监听线路
     *
     * @param networkSystem
     * @return
     */
    public long getCountByNetworkSystems(String networkSystem) {
        long result = 0;
        Cursor cursor = null;
        try {
            String sql = "select count(*) from " + DbConstants.TABLE.monitor_line + " where " +
                    DbConstants.COLUMN_MONITOR_LINE.NETWORK_SYSTEMS + "=?";
            cursor = database.rawQuery(sql, new String[]{networkSystem});
            if (cursor != null && cursor.moveToFirst()) {
                result = cursor.getLong(0);
            }
        } catch (Exception e) {
            Log.e("TAG", e.getLocalizedMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }

    /**
     * 根据制式，运营商获取数量
     *
     * @param networkSystem
     * @param carrier
     * @return
     */
    public long getCountByNetworkSystemAndCarrier(String networkSystem, String carrier) {
        long result = 0;
        Cursor cursor = null;
        try {
            String sql = "select count(*) from " + DbConstants.TABLE.monitor_line + " where " +
                    DbConstants.COLUMN_MONITOR_LINE.NETWORK_SYSTEMS + "=? and " + DbConstants.COLUMN_MONITOR_LINE.CARRIER + "=?";
            cursor = database.rawQuery(sql, new String[]{networkSystem, carrier});
            if (cursor != null && cursor.moveToFirst()) {
                result = cursor.getLong(0);
            }
        } catch (Exception e) {
            Log.e("TAG", e.getLocalizedMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }

    /**
     * 获取所有监听路数列表
     *
     * @return
     */
    public List<MonitorLineModel> getList() {
        List<MonitorLineModel> result = new ArrayList<MonitorLineModel>();
        Cursor cursor = null;
        try {
            cursor = database.rawQuery("select * from " + DbConstants.TABLE.monitor_line + " order by " + DbConstants.COLUMN_MONITOR_LINE.LINE_NO, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    MonitorLineModel model = new MonitorLineModel();
                    model.imsi = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_MONITOR_LINE.IMSI));
                    model.networkSystems = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_MONITOR_LINE.IMSI));
                    model.carrier = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_MONITOR_LINE.IMSI));
                    model.imei = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_MONITOR_LINE.IMSI));
                    model.lineNo = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_MONITOR_LINE.IMSI));
                    result.add(model);
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
        database.delete(DbConstants.TABLE.monitor_line, null, null);
    }
}
