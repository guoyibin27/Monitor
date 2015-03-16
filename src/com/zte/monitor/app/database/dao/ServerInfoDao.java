package com.zte.monitor.app.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import com.zte.monitor.app.database.DbConstants;

/**
 * Created by Sylar on 14-9-12.
 * 服务器配置信息数据库操作类
 */
public class ServerInfoDao extends BaseDao {

    public ServerInfoDao(Context context) {
        super(context);
    }

    /**
     * 保存
     *
     * @param ip
     * @param port
     */
    public void save(String ip, int port) {
        ContentValues values = new ContentValues();
        values.put(DbConstants.COLUMN_SERVER.IP, ip);
        values.put(DbConstants.COLUMN_SERVER.PORT, port);
        database.insert(DbConstants.TABLE.server_info, null, values);
    }

    /**
     * 更新
     *
     * @param ip
     * @param port
     */
    public void update(String ip, int port) {
        ContentValues values = new ContentValues();
        values.put(DbConstants.COLUMN_SERVER.IP, ip);
        values.put(DbConstants.COLUMN_SERVER.PORT, port);
        database.update(DbConstants.TABLE.server_info, values, null, null);
    }

    /**
     * 获取服务器地址信息，IP，PORT
     *
     * @return String[], String[0]:ip;String[1]:port
     */
    public String[] get() {
        String[] info = null;
        Cursor cursor = null;
        try {
            cursor = database.query(true, DbConstants.TABLE.server_info, new String[]{
                    DbConstants.COLUMN_SERVER.IP, DbConstants.COLUMN_SERVER.PORT}, null, null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                info = new String[2];
                info[0] = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_SERVER.IP));
                info[1] = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_SERVER.PORT));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return info;
    }
}
