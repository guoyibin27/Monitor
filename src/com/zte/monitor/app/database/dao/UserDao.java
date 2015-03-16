package com.zte.monitor.app.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import com.zte.monitor.app.database.DbConstants;
import com.zte.monitor.app.model.UserModel;
import com.zte.monitor.app.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sylar on 8/26/14.
 * 用户数据库操作类
 */
public class UserDao extends BaseDao {

    public UserDao(Context context) {
        super(context);
    }

    /**
     * 更新本机电话号码
     *
     * @param userModel
     */
    public void updateUserPhoneNumber(UserModel userModel) {
        ContentValues values = new ContentValues();
        values.put(DbConstants.COLUMN_USER.IMSI, userModel.imsi);
        if (!StringUtils.isBlank(userModel.phoneNumber)) {
            values.put(DbConstants.COLUMN_USER.PHONE_NUMBER, userModel.phoneNumber);
        }
        if (!StringUtils.isBlank(userModel.imsi)) {
            database.update(DbConstants.TABLE.user_info, values, DbConstants.COLUMN_USER.IMSI + "=?",
                    new String[]{userModel.imsi});
        } else if (!StringUtils.isBlank(userModel.imei)) {
            database.update(DbConstants.TABLE.user_info, values, DbConstants.COLUMN_USER.IMEI + "=?",
                    new String[]{userModel.imei});
        }
    }

    public void updateCallNumber(UserModel userModel) {
        try {
            ContentValues values = new ContentValues();
            values.put(DbConstants.COLUMN_USER.IMSI, userModel.imsi);
            if (!StringUtils.isBlank(userModel.callNumber)) {
                values.put(DbConstants.COLUMN_USER.CALL_NUMBER, userModel.callNumber);
            }
            if (isExists(userModel)) {
                if (!StringUtils.isBlank(userModel.imsi)) {
                    database.update(DbConstants.TABLE.user_info, values, DbConstants.COLUMN_USER.IMSI + "=?",
                            new String[]{userModel.imsi});
                } else if (!StringUtils.isBlank(userModel.imei)) {
                    database.update(DbConstants.TABLE.user_info, values, DbConstants.COLUMN_USER.IMEI + "=?",
                            new String[]{userModel.imei});
                }
            } else {
                values.put(DbConstants.COLUMN_USER.NAME, userModel.username);
                values.put(DbConstants.COLUMN_USER.PROPERTY, userModel.property);
                database.insert(DbConstants.TABLE.user_info, null, values);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 功能站用户信息更新
     *
     * @param userModel
     */
    public void updateUserInfoAtFunctionStation(UserModel userModel) {
        try {
            ContentValues values = new ContentValues();
            values.put(DbConstants.COLUMN_USER.IMSI, userModel.imsi);
            if (!StringUtils.isBlank(userModel.imei)) {
                values.put(DbConstants.COLUMN_USER.IMEI, userModel.imei);
            }
            if (userModel.status != -2) {
                values.put(DbConstants.COLUMN_USER.STATUS, userModel.status);
            }
            if (!StringUtils.isBlank(userModel.lastUpdated)) {
                values.put(DbConstants.COLUMN_USER.UP_TIME, userModel.lastUpdated);
            }
            if (!StringUtils.isBlank(userModel.area)) {
                values.put(DbConstants.COLUMN_USER.AREA, userModel.area);
            }
            if (userModel.power != -1) {
                values.put(DbConstants.COLUMN_USER.POWER, userModel.power);
            }
            if (isExists(userModel)) {
                if (!StringUtils.isBlank(userModel.imsi)) {
                    database.update(DbConstants.TABLE.user_info, values, DbConstants.COLUMN_USER.IMSI + "=?",
                            new String[]{userModel.imsi});
                } else if (!StringUtils.isBlank(userModel.imei)) {
                    database.update(DbConstants.TABLE.user_info, values, DbConstants.COLUMN_USER.IMEI + "=?",
                            new String[]{userModel.imei});
                }
            } else {
                values.put(DbConstants.COLUMN_USER.NAME, userModel.username);
                values.put(DbConstants.COLUMN_USER.PROPERTY, userModel.property);
                database.insert(DbConstants.TABLE.user_info, null, values);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存更新用户基本信息
     *
     * @param userModel
     */
    public void saveOrUpdate(UserModel userModel) {
        try {
            ContentValues values = new ContentValues();
            values.put(DbConstants.COLUMN_USER.IMSI, userModel.imsi);
            if (!StringUtils.isBlank(userModel.imei)) {
                values.put(DbConstants.COLUMN_USER.IMEI, userModel.imei);
            }
            if (!StringUtils.isBlank(userModel.phoneNumber)) {
                values.put(DbConstants.COLUMN_USER.PHONE_NUMBER, userModel.phoneNumber);
            }
            if (userModel.status != -2) {
                values.put(DbConstants.COLUMN_USER.STATUS, userModel.status);
            }
            if (!StringUtils.isBlank(userModel.tmsi)) {
                values.put(DbConstants.COLUMN_USER.TMSI, userModel.tmsi);
            }
            if (!StringUtils.isBlank(userModel.lastUpdated)) {
                values.put(DbConstants.COLUMN_USER.UP_TIME, userModel.lastUpdated);
            }
            if (!StringUtils.isBlank(userModel.area)) {
                values.put(DbConstants.COLUMN_USER.AREA, userModel.area);
            }
            if (userModel.power != -1) {
                values.put(DbConstants.COLUMN_USER.POWER, userModel.power);
            }
            if (!StringUtils.isBlank(userModel.property)) {
                values.put(DbConstants.COLUMN_USER.PROPERTY, userModel.property);
            }

            if (!StringUtils.isBlank(userModel.callNumber)) {
                values.put(DbConstants.COLUMN_USER.CALL_NUMBER, userModel.callNumber);
            }
//        values.put(DbConstants.COLUMN_USER.IS_SMS_AUDIT, userModel.isSmsAudit);
//        values.put(DbConstants.COLUMN_USER.IS_LOCATE, userModel.isLocate);
//        values.put(DbConstants.COLUMN_USER.IS_MONITOR, userModel.isMonitor);
//        values.put(DbConstants.COLUMN_USER.IS_CHECK_POWER, userModel.isCheckPower);
            if (isExists(userModel)) {
                if (!StringUtils.isBlank(userModel.imsi)) {
                    database.update(DbConstants.TABLE.user_info, values, DbConstants.COLUMN_USER.IMSI + "=?",
                            new String[]{userModel.imsi});
                } else if (!StringUtils.isBlank(userModel.imei)) {
                    database.update(DbConstants.TABLE.user_info, values, DbConstants.COLUMN_USER.IMEI + "=?",
                            new String[]{userModel.imei});
                }
            } else {
                values.put(DbConstants.COLUMN_USER.NAME, userModel.username);
                values.put(DbConstants.COLUMN_USER.PROPERTY, userModel.property);
                database.insert(DbConstants.TABLE.user_info, null, values);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新用户状态,监听，定位，短信审计，功率检测
     *
     * @param userModel
     */
    public void updateStatus(UserModel userModel) {
        try {
            ContentValues values = new ContentValues();
            values.put(DbConstants.COLUMN_USER.IMSI, userModel.imsi);
            values.put(DbConstants.COLUMN_USER.IS_SMS_AUDIT, userModel.isSmsAudit);
            values.put(DbConstants.COLUMN_USER.IS_LOCATE, userModel.isLocate);
            values.put(DbConstants.COLUMN_USER.IS_MONITOR, userModel.isMonitor);
            values.put(DbConstants.COLUMN_USER.IS_CHECK_POWER, userModel.isCheckPower);
            values.put(DbConstants.COLUMN_USER.STATUS, userModel.status);
            database.update(DbConstants.TABLE.user_info, values, DbConstants.COLUMN_USER.IMSI + "=?",
                    new String[]{userModel.imsi});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private boolean isExists(UserModel userModel) {
        boolean result = Boolean.FALSE;
        Cursor cursor = null;
        try {
            StringBuilder builder = new StringBuilder();
            if (!StringUtils.isBlank(userModel.imsi)) {
                builder.append("select count(*) as totalCount from ").append(DbConstants.TABLE.user_info)
                        .append(" where ").append(DbConstants.COLUMN_USER.IMSI + "=?");
                cursor = database.rawQuery(builder.toString(), new String[]{userModel.imsi});
            } else if (StringUtils.isBlank(userModel.imei)) {
                builder.append("select count(*) as totalCount from ").append(DbConstants.TABLE.user_info)
                        .append(" where ").append(DbConstants.COLUMN_USER.IMEI).append("=?");
                cursor = database.rawQuery(builder.toString(), new String[]{userModel.imei});
            }
            Log.e("TAG", builder.toString());
            if (cursor != null && cursor.moveToFirst()) {
                result = cursor.getLong(0) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
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
     * @param imsi
     * @param imei
     */
    public void delete(String imsi, String imei) {
        if (!StringUtils.isBlank(imsi)) {
            database.delete(DbConstants.TABLE.user_info, DbConstants.COLUMN_USER.IMSI + "=? ", new String[]{imsi});
        } else if (!StringUtils.isBlank(imei)) {
            database.delete(DbConstants.TABLE.user_info, DbConstants.COLUMN_USER.IMEI + "=?", new String[]{imei});
        }
    }

    /**
     * 根据用户属性获取列表
     *
     * @param property
     * @param pageIndex
     * @return
     */
    public List<UserModel> getUserListByProperty(String property, long pageIndex, int pageSize) {
        List<UserModel> result = null;
        Cursor cursor = null;
        try {
            String sql = "select * from " + DbConstants.TABLE.user_info + " where " + DbConstants.COLUMN_USER.PROPERTY + "=? " +
                    " order by date(" + DbConstants.COLUMN_USER.UP_TIME + ") desc , time(" + DbConstants.COLUMN_USER.UP_TIME + ") desc " +
                    "limit " + pageIndex * pageSize + ", " + pageSize;
            Log.e("TAG", sql);
            cursor = database.rawQuery(sql, new String[]{property});
            if (cursor != null) {
                result = new ArrayList<UserModel>();
                while (cursor.moveToNext()) {
                    UserModel userModel = new UserModel();
                    userModel.username = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.NAME));
                    userModel.imsi = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.IMSI));
                    userModel.imei = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.IMEI));
                    userModel.phoneNumber = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.PHONE_NUMBER));
                    userModel.status = (byte) cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_USER.STATUS));
                    userModel.tmsi = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.TMSI));
                    userModel.lastUpdated = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.UP_TIME));
                    userModel.area = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.AREA));
                    userModel.property = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.PROPERTY));
                    userModel.isSmsAudit = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_USER.IS_SMS_AUDIT));
                    userModel.power = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_USER.POWER));
                    userModel.isLocate = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_USER.IS_LOCATE));
                    userModel.isMonitor = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_USER.IS_MONITOR));
                    userModel.isCheckPower = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_USER.IS_CHECK_POWER));
                    userModel.callNumber = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.CALL_NUMBER));
                    userModel.hasSensitiveNumber = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_USER.HAS_SENSITIVE_NUMBER));
                    userModel.hasSensitiveWord = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_USER.HAS_SENSITIVE_WORD));
                    result.add(userModel);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }

    /**
     * 获取黑白名单用户列表
     *
     * @param black
     * @param white
     * @return
     */
    public List<UserModel> getUserListByProperty(String black, String white, int pageSize, long currentPage) {
        List<UserModel> result = null;
        Cursor cursor = null;
        try {
            StringBuffer sql = new StringBuffer("select * from " + DbConstants.TABLE.user_info + " where 1=1 ");
            sql.append(" and (").append(DbConstants.COLUMN_USER.PROPERTY + "=?");
            sql.append(" or ").append(DbConstants.COLUMN_USER.PROPERTY + "=?)");
//            sql.append(" order by ").append(DbConstants.COLUMN_USER.STATUS + " desc");
            sql.append(" order by date(" + DbConstants.COLUMN_USER.UP_TIME + ") desc , time(" + DbConstants.COLUMN_USER.UP_TIME + ") desc");
            sql.append(" limit ").append(currentPage * pageSize).append(" , ").append(pageSize);
            cursor = database.rawQuery(sql.toString(), new String[]{black, white});
            if (cursor != null) {
                result = new ArrayList<UserModel>();
                while (cursor.moveToNext()) {
                    UserModel userModel = new UserModel();
                    userModel.username = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.NAME));
                    userModel.imsi = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.IMSI));
                    userModel.imei = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.IMEI));
                    userModel.phoneNumber = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.PHONE_NUMBER));
                    userModel.status = (byte) cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_USER.STATUS));
                    userModel.tmsi = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.TMSI));
                    userModel.lastUpdated = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.UP_TIME));
                    userModel.area = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.AREA));
                    userModel.property = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.PROPERTY));
                    userModel.isSmsAudit = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_USER.IS_SMS_AUDIT));
                    userModel.power = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_USER.POWER));
                    userModel.isLocate = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_USER.IS_LOCATE));
                    userModel.isMonitor = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_USER.IS_MONITOR));
                    userModel.isCheckPower = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_USER.IS_CHECK_POWER));
                    userModel.callNumber = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.CALL_NUMBER));
                    userModel.hasSensitiveNumber = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_USER.HAS_SENSITIVE_NUMBER));
                    userModel.hasSensitiveWord = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_USER.HAS_SENSITIVE_WORD));
                    result.add(userModel);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }

    /**
     * 搜索用户
     *
     * @param username
     * @param imsi
     * @param imei
     * @param phoneNum
     * @param area
     * @return
     */
    public List<UserModel> getUserListBySearch(String username, String imsi, String imei, String phoneNum,
                                               String area) {
        List<UserModel> result = null;
        Cursor cursor = null;
        try {
            StringBuilder sql = new StringBuilder("select * from " + DbConstants.TABLE.user_info + " where 1=1 ");
            if (!StringUtils.isBlank(username)) {
                sql.append(" and ").append(DbConstants.COLUMN_USER.NAME).append(" like '%").append(username).append("%' ");
            }
            if (!StringUtils.isBlank(imsi)) {
                sql.append(" and ").append(DbConstants.COLUMN_USER.IMSI).append(" like '%").append(imsi).append("%' ");
            }
            if (!StringUtils.isBlank(imei)) {
                sql.append(" and ").append(DbConstants.COLUMN_USER.IMEI).append(" like '%").append(imei).append("%' ");
            }
            if (!StringUtils.isBlank(phoneNum)) {
                sql.append(" and ").append(DbConstants.COLUMN_USER.PHONE_NUMBER).append(" like '%").append(phoneNum).append("%' ");
            }
            if (!StringUtils.isBlank(area)) {
                sql.append(" and ").append(DbConstants.COLUMN_USER.AREA).append(" like '%").append(area).append("%' ");
            }

            Log.e("TAG", sql.toString());
            cursor = database.rawQuery(sql.toString(), null);
            if (cursor != null) {
                result = new ArrayList<UserModel>();
                while (cursor.moveToNext()) {
                    UserModel userModel = new UserModel();
                    userModel.username = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.NAME));
                    userModel.imsi = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.IMSI));
                    userModel.imei = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.IMEI));
                    userModel.phoneNumber = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.PHONE_NUMBER));
                    userModel.status = (byte) cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_USER.STATUS));
                    userModel.tmsi = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.TMSI));
                    userModel.lastUpdated = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.UP_TIME));
                    userModel.area = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.AREA));
                    userModel.property = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.PROPERTY));
                    userModel.isSmsAudit = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_USER.IS_SMS_AUDIT));
                    userModel.power = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_USER.POWER));
                    userModel.isLocate = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_USER.IS_LOCATE));
                    userModel.isMonitor = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_USER.IS_MONITOR));
                    userModel.isCheckPower = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_USER.IS_CHECK_POWER));
                    userModel.callNumber = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.CALL_NUMBER));
                    userModel.hasSensitiveNumber = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_USER.HAS_SENSITIVE_NUMBER));
                    userModel.hasSensitiveWord = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_USER.HAS_SENSITIVE_WORD));
                    result.add(userModel);
                }
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
     * 更新用户场强，定位时使用更新
     *
     * @param imsi
     * @param power
     */
    public void updatePower(String imsi, int power) {
        ContentValues values = new ContentValues();
        values.put(DbConstants.COLUMN_USER.POWER, power);
        database.update(DbConstants.TABLE.user_info, values, DbConstants.COLUMN_USER.IMSI + "=?",
                new String[]{imsi});
    }

    /**
     * 根据IMSI获取用户详细信息
     *
     * @param imsi
     * @return
     */
    public UserModel getByImsi(String imsi) {
        UserModel userModel = null;
        Cursor cursor = null;
        try {
            String sql = "select * from " + DbConstants.TABLE.user_info + " where " + DbConstants.COLUMN_USER.IMSI + "=?";
            cursor = database.rawQuery(sql, new String[]{imsi});
            if (cursor != null && cursor.moveToFirst()) {
                userModel = new UserModel();
                userModel.username = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.NAME));
                userModel.imsi = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.IMSI));
                userModel.imei = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.IMEI));
                userModel.phoneNumber = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.PHONE_NUMBER));
                userModel.status = (byte) cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_USER.STATUS));
                userModel.tmsi = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.TMSI));
                userModel.lastUpdated = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.UP_TIME));
                userModel.area = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.AREA));
                userModel.property = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.PROPERTY));
                userModel.isSmsAudit = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_USER.IS_SMS_AUDIT));
                userModel.power = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_USER.POWER));
                userModel.isLocate = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_USER.IS_LOCATE));
                userModel.isMonitor = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_USER.IS_MONITOR));
                userModel.isCheckPower = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_USER.IS_CHECK_POWER));
                userModel.callNumber = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.CALL_NUMBER));
                userModel.hasSensitiveNumber = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_USER.HAS_SENSITIVE_NUMBER));
                userModel.hasSensitiveWord = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_USER.HAS_SENSITIVE_WORD));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return userModel;
    }

    /**
     * 更新用户状态
     *
     * @param userModel
     */
    public void updateUserStatusChanged(UserModel userModel) {
        ContentValues values = new ContentValues();
        values.put(DbConstants.COLUMN_USER.IS_STATUS_CHANGED, userModel.isStatusChanged);
        if (!StringUtils.isBlank(userModel.imsi)) {
            database.update(DbConstants.TABLE.user_info, values, DbConstants.COLUMN_USER.IMSI + "=?",
                    new String[]{userModel.imsi});
        } else if (!StringUtils.isBlank(userModel.imei)) {
            database.update(DbConstants.TABLE.user_info, values, DbConstants.COLUMN_USER.IMEI + "=?",
                    new String[]{userModel.imei});
        }
    }

    /**
     * 用户status是否发生彼岸花
     *
     * @param userModel
     * @return
     */
    public boolean isUserStatusChange(UserModel userModel) {
        boolean result = Boolean.FALSE;
        Cursor cursor = null;
        try {
            StringBuilder builder = new StringBuilder();
            if (!StringUtils.isBlank(userModel.imsi)) {
                builder.append("select count(*) as totalCount from ").append(DbConstants.TABLE.user_info)
                        .append(" where ").append(DbConstants.COLUMN_USER.IMSI + "=? ")
                        .append(" and ").append(DbConstants.COLUMN_USER.STATUS + " != ?");
                cursor = database.rawQuery(builder.toString(), new String[]{userModel.imsi, String.valueOf(userModel.status)});
            } else if (StringUtils.isBlank(userModel.imei)) {
                builder.append("select count(*) as totalCount from ").append(DbConstants.TABLE.user_info)
                        .append(" where ").append(DbConstants.COLUMN_USER.IMEI).append(" =? ")
                        .append(" and ").append(DbConstants.COLUMN_USER.STATUS + " != ?");
                cursor = database.rawQuery(builder.toString(), new String[]{userModel.imei, String.valueOf(userModel.status)});
            }
            Log.e("TAG", builder.toString());
            if (cursor != null && cursor.moveToFirst()) {
                result = cursor.getLong(0) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }

    /**
     * 更新has_sensitive_word,
     * 如果用户短信种有敏感词汇，更新词状态
     *
     * @param userModel
     */
    public void updateHasSensitiveWord(UserModel userModel) {
        ContentValues values = new ContentValues();
        values.put(DbConstants.COLUMN_USER.HAS_SENSITIVE_WORD, userModel.hasSensitiveWord);
        if (!StringUtils.isBlank(userModel.imsi)) {
            database.update(DbConstants.TABLE.user_info, values, DbConstants.COLUMN_USER.IMSI + "=?",
                    new String[]{userModel.imsi});
        } else if (!StringUtils.isBlank(userModel.imei)) {
            database.update(DbConstants.TABLE.user_info, values, DbConstants.COLUMN_USER.IMEI + "=?",
                    new String[]{userModel.imei});
        }
    }

    /**
     * 更新用户电话种有敏感电话
     *
     * @param userModel
     */
    public void updateHasSensitiveNumber(UserModel userModel) {
        ContentValues values = new ContentValues();
        values.put(DbConstants.COLUMN_USER.HAS_SENSITIVE_NUMBER, userModel.hasSensitiveNumber);
        if (!StringUtils.isBlank(userModel.imsi)) {
            database.update(DbConstants.TABLE.user_info, values, DbConstants.COLUMN_USER.IMSI + "=?",
                    new String[]{userModel.imsi});
        } else if (!StringUtils.isBlank(userModel.imei)) {
            database.update(DbConstants.TABLE.user_info, values, DbConstants.COLUMN_USER.IMEI + "=?",
                    new String[]{userModel.imei});
        }
    }

    /**
     * 获取当前前定位中的用户
     *
     * @return
     */
    public UserModel getCurrentLocateUser() {
        UserModel userModel = null;
        Cursor cursor = null;
        try {
            String sql = "select * from " + DbConstants.TABLE.user_info + " where " + DbConstants.COLUMN_USER.IS_LOCATE + " = 1";
            cursor = database.rawQuery(sql, null);
            if (cursor != null && cursor.moveToNext()) {
                userModel = new UserModel();
                userModel.username = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.NAME));
                userModel.imsi = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.IMSI));
                userModel.imei = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.IMEI));
                userModel.phoneNumber = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.PHONE_NUMBER));
                userModel.status = (byte) cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_USER.STATUS));
                userModel.tmsi = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.TMSI));
                userModel.lastUpdated = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.UP_TIME));
                userModel.area = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.AREA));
                userModel.property = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.PROPERTY));
                userModel.isSmsAudit = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_USER.IS_SMS_AUDIT));
                userModel.power = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_USER.POWER));
                userModel.isLocate = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_USER.IS_LOCATE));
                userModel.isMonitor = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_USER.IS_MONITOR));
                userModel.isCheckPower = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_USER.IS_CHECK_POWER));
                userModel.callNumber = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.CALL_NUMBER));
                userModel.hasSensitiveNumber = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_USER.HAS_SENSITIVE_NUMBER));
                userModel.hasSensitiveWord = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_USER.HAS_SENSITIVE_WORD));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return userModel;
    }

    /**
     * 获取当前前检测功率的用户
     *
     * @return
     */
    public UserModel getCurrentCheckPowerUser() {
        UserModel userModel = null;
        Cursor cursor = null;
        try {
            String sql = "select * from " + DbConstants.TABLE.user_info + " where " + DbConstants.COLUMN_USER.IS_CHECK_POWER + " = 1";
            cursor = database.rawQuery(sql, null);
            if (cursor != null && cursor.moveToNext()) {
                userModel = new UserModel();
                userModel.username = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.NAME));
                userModel.imsi = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.IMSI));
                userModel.imei = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.IMEI));
                userModel.phoneNumber = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.PHONE_NUMBER));
                userModel.status = (byte) cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_USER.STATUS));
                userModel.tmsi = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.TMSI));
                userModel.lastUpdated = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.UP_TIME));
                userModel.area = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.AREA));
                userModel.property = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.PROPERTY));
                userModel.isSmsAudit = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_USER.IS_SMS_AUDIT));
                userModel.power = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_USER.POWER));
                userModel.isLocate = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_USER.IS_LOCATE));
                userModel.isMonitor = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_USER.IS_MONITOR));
                userModel.isCheckPower = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_USER.IS_CHECK_POWER));
                userModel.callNumber = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.CALL_NUMBER));
                userModel.hasSensitiveNumber = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_USER.HAS_SENSITIVE_NUMBER));
                userModel.hasSensitiveWord = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_USER.HAS_SENSITIVE_WORD));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return userModel;
    }

    /**
     * 获取当前空闲和被拒绝的人数
     *
     * @param statusFree
     * @param statusReject
     * @return
     */
    public long getUserCount(int statusFree, int statusReject) {
        long result = 0;
        Cursor cursor = null;
        try {
            String sql = "select count(*) from " + DbConstants.TABLE.user_info + " where " + DbConstants.COLUMN_USER.STATUS + "=? or " + DbConstants.COLUMN_USER.STATUS + "=?";
            cursor = database.rawQuery(sql, new String[]{String.valueOf(statusFree), String.valueOf(statusReject)});
            if (cursor != null && cursor.moveToNext()) {
                result = cursor.getLong(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }

    /**
     * 获取用户属性的人数
     *
     * @param userProperties
     * @return
     */
    public long getUserCount(String[] userProperties) {
        long result = 0;
        if (userProperties != null) {
            Cursor cursor = null;
            try {
                StringBuffer sql = new StringBuffer("select count(*) from " + DbConstants.TABLE.user_info + " where 1=1 ");
                if (userProperties.length > 0) {
                    sql.append(" and ( ");
                    for (int i = 0; i < userProperties.length; i++) {
                        sql.append(DbConstants.COLUMN_USER.PROPERTY + "=?");
                        if (i != userProperties.length - 1) {
                            sql.append(" or ");
                        }
                    }
                    sql.append(" ) ");
                }
                Log.e("TAG", sql.toString());
                cursor = database.rawQuery(sql.toString(), userProperties);
                if (cursor != null && cursor.moveToNext()) {
                    result = cursor.getLong(0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return result;
    }

    /**
     * 清楚所有用户当前动作为初始值，并切用户状态为为脱网(255)
     */
    public void clearAllUserStatus() {
        ContentValues values = new ContentValues();
        values.put(DbConstants.COLUMN_USER.IS_SMS_AUDIT, 0);
        values.put(DbConstants.COLUMN_USER.IS_LOCATE, 0);
        values.put(DbConstants.COLUMN_USER.IS_MONITOR, 0);
        values.put(DbConstants.COLUMN_USER.IS_CHECK_POWER, 0);
        values.put(DbConstants.COLUMN_USER.STATUS, 255);
        database.update(DbConstants.TABLE.user_info, values, null, null);
    }

    /**
     * 清空数据库
     */
    public void clear() {
        database.delete(DbConstants.TABLE.user_info, null, null);
    }


    /**
     * 获取所有用户列表
     *
     * @return
     */
    public List<UserModel> getAllUserList() {
        List<UserModel> result = null;
        Cursor cursor = null;
        try {
            cursor = database.rawQuery("select * from " + DbConstants.TABLE.user_info, null);
            if (cursor != null) {
                result = new ArrayList<UserModel>();
                while (cursor.moveToNext()) {
                    UserModel userModel = new UserModel();
                    userModel.username = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.NAME));
                    userModel.imsi = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.IMSI));
                    userModel.imei = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.IMEI));
                    userModel.phoneNumber = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.PHONE_NUMBER));
                    userModel.status = (byte) cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_USER.STATUS));
                    userModel.tmsi = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.TMSI));
                    userModel.lastUpdated = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.UP_TIME));
                    userModel.area = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.AREA));
                    userModel.property = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.PROPERTY));
                    userModel.isSmsAudit = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_USER.IS_SMS_AUDIT));
                    userModel.power = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_USER.POWER));
                    userModel.isLocate = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_USER.IS_LOCATE));
                    userModel.isMonitor = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_USER.IS_MONITOR));
                    userModel.isCheckPower = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_USER.IS_CHECK_POWER));
                    userModel.callNumber = cursor.getString(cursor.getColumnIndex(DbConstants.COLUMN_USER.CALL_NUMBER));
                    userModel.hasSensitiveNumber = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_USER.HAS_SENSITIVE_NUMBER));
                    userModel.hasSensitiveWord = cursor.getInt(cursor.getColumnIndex(DbConstants.COLUMN_USER.HAS_SENSITIVE_WORD));
                    result.add(userModel);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }
}
