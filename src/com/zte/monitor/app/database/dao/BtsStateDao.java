package com.zte.monitor.app.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import com.zte.monitor.app.database.DbConstants;
import com.zte.monitor.app.model.StateModel;

/**
 * Created by Sylar on 14-9-16.
 * 站点状态数据库操作类
 */
public class BtsStateDao extends BaseDao {
    public BtsStateDao(Context context) {
        super(context);
    }

    /**
     * 添加或更新
     *
     * @param stateModel
     */
    public void saveOrUpdate(StateModel stateModel) {
        ContentValues values = new ContentValues();
        values.put(DbConstants.COLUMN_BTS_STATE.BTS_STATE, stateModel.btsState);
        values.put(DbConstants.COLUMN_BTS_STATE.PROXY_STATE, stateModel.proxyCardState);
        values.put(DbConstants.COLUMN_BTS_STATE.CONN_STATE, stateModel.connState);
        values.put(DbConstants.COLUMN_BTS_STATE.GET_PH_NUM_STATE, stateModel.getPhNumCardState);
        values.put(DbConstants.COLUMN_BTS_STATE.SYS_MODE, stateModel.sysMode);
        if (isExists()) {
            database.update(DbConstants.TABLE.bts_state, values, null, null);
        } else {
            database.insert(DbConstants.TABLE.bts_state, null, values);
        }
    }

    private boolean isExists() {
        return get() != null;
    }

    /**
     * 获取当前站点状态信息
     *
     * @return StateModel实体类
     */
    public StateModel get() {
        StateModel model = null;
        Cursor cursor = null;
        try {
            cursor = database.rawQuery("select * from " + DbConstants.TABLE.bts_state, null);
            if (cursor != null && cursor.moveToFirst()) {
                model = new StateModel();
                model.btsState = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_BTS_STATE.BTS_STATE));
                model.connState = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_BTS_STATE.CONN_STATE));
                model.proxyCardState = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_BTS_STATE.PROXY_STATE));
                model.getPhNumCardState = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_BTS_STATE.GET_PH_NUM_STATE));
                model.sysMode = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_BTS_STATE.SYS_MODE));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return model;
    }

    public void clear() {
        database.delete(DbConstants.TABLE.bts_state, null, null);
    }
}
