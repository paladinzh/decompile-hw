package com.huawei.netassistant.db;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.ArrayMap;
import com.huawei.netassistant.common.SimCardSettingsInfo;
import com.huawei.netassistant.db.NetAssistantStore.NetAccessTable;
import com.huawei.netassistant.db.NetAssistantStore.SettingTable;
import com.huawei.netassistant.db.NetAssistantStore.TrafficAdjustTable;
import com.huawei.netassistant.db.NetAssistantStore.TrafficAdjustTable.Columns;
import com.huawei.netassistant.util.CommonMethodUtil;
import com.huawei.netassistant.util.DateUtil;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.util.HwLog;

public class NetAssistantDBManager {
    private static String TAG = "NetAssistantDBManager";
    private static final Object sMutexNetAssistantDBManager = new Object();
    private static NetAssistantDBManager sSingleton;
    private Context mContext = GlobalContext.getContext();

    public static class TrafficSettingInfo {
        public int beginDate = -1;
        public int excessMonthType = -1;
        public int isAfterLocked = -1;
        public int isNotification = -1;
        public int isOvermarkDay = -1;
        public int isOvermarkMonth = -1;
        public int isSpeedNotification = -1;
        public long packageTotal = -1;
        public long regularAdjustBeginTime = -1;
        public int regularAdjustType = -1;

        public TrafficSettingInfo(String im) {
        }
    }

    private NetAssistantDBManager() {
    }

    public static NetAssistantDBManager getInstance() {
        NetAssistantDBManager netAssistantDBManager;
        synchronized (sMutexNetAssistantDBManager) {
            if (sSingleton == null) {
                sSingleton = new NetAssistantDBManager();
            }
            netAssistantDBManager = sSingleton;
        }
        return netAssistantDBManager;
    }

    public static void destroyInstance() {
        synchronized (sMutexNetAssistantDBManager) {
            sSingleton = null;
        }
    }

    public SimCardSettingsInfo getSimCardSettingsInfo(String imsi) {
        Cursor cursorSettings = getSettingCursor(imsi);
        SimCardSettingsInfo simCardSettingsInfo = new SimCardSettingsInfo(imsi);
        if (cursorSettings == null) {
            return simCardSettingsInfo;
        }
        if (cursorSettings.getCount() > 0) {
            cursorSettings.moveToNext();
            simCardSettingsInfo.getCardSettingInfo(cursorSettings);
        } else if (cursorSettings.getCount() == 0) {
            simCardSettingsInfo.setSettingDefaultInfo(this.mContext, imsi);
            setSettingRegularAdjustBeginTime(imsi, DateUtil.getCurrentTimeMills());
        }
        cursorSettings.close();
        simCardSettingsInfo.getAdjustInfo(this.mContext, getAdjustCursor(imsi), imsi);
        return simCardSettingsInfo;
    }

    public int getSettingColumnsIntInfo(String imsi, int columnsIndex) {
        int value = -1;
        Cursor cursor = getSettingCursor(imsi);
        if (cursor != null) {
            int count = cursor.getCount();
            if (1 < count) {
                HwLog.e(TAG, "/getSettingColumnsInfo table error");
            } else if (1 == count) {
                cursor.moveToNext();
                value = cursor.getInt(columnsIndex);
            }
            cursor.close();
        }
        return value;
    }

    public float getSettingColumnsFloatInfo(String imsi, int columnsIndex) {
        float value = -1.0f;
        Cursor cursor = getSettingCursor(imsi);
        if (cursor != null) {
            int count = cursor.getCount();
            if (1 < count) {
                HwLog.e(TAG, "/getSettingTotalPackageInfo table error");
            } else if (1 == count) {
                cursor.moveToNext();
                value = cursor.getFloat(columnsIndex);
            }
            cursor.close();
        }
        return value;
    }

    public long getSettingColumnsLongInfo(String imsi, int columnsIndex) {
        long value = -1;
        Cursor cursor = getSettingCursor(imsi);
        if (cursor != null) {
            int count = cursor.getCount();
            if (1 < count) {
                HwLog.e(TAG, "/getAdjustColumnsInfo table error");
            } else if (1 == count) {
                cursor.moveToNext();
                value = cursor.getLong(columnsIndex);
            }
            cursor.close();
        }
        return value;
    }

    private boolean setSettingColumnsInfo(String imsi, String columnsName, Object value) {
        return setSettingColumnsInfo(imsi, columnsName, value, false);
    }

    private boolean setSettingColumnsInfo(String imsi, String columnsName, Object value, boolean onlyUpdate) {
        HwLog.v(TAG, "setSettingColumnsInfo columnsName=" + columnsName);
        Cursor cursor = getSettingCursor(imsi);
        if (cursor == null) {
            return false;
        }
        Uri apUri = SettingTable.getContentUri();
        String[] columns = NetAssistantStore.getSettingColumns();
        StringBuilder where = new StringBuilder();
        where.append(columns[1]).append(" =? ");
        String userWhere = where.toString();
        String[] userWhereArgs = new String[]{imsi};
        ContentValues contentValues = new ContentValues();
        contentValues.put(columnsName, value.toString());
        int count = cursor.getCount();
        cursor.close();
        if (1 < count || count < 0) {
            HwLog.e(TAG, "/setSettingColumnsInfo table error");
            return false;
        } else if (1 == count) {
            if (this.mContext.getContentResolver().update(apUri, contentValues, userWhere, userWhereArgs) > 0) {
                return true;
            }
            return false;
        } else if (onlyUpdate) {
            return false;
        } else {
            contentValues.put("imsi", imsi);
            if (this.mContext.getContentResolver().insert(apUri, contentValues) != null) {
                return true;
            }
            return false;
        }
    }

    public int getAdjustColumnsIntInfo(String imsi, int columnsIndex) {
        int value = -1;
        Cursor cursor = getAdjustCursor(imsi);
        if (cursor != null) {
            int count = cursor.getCount();
            if (1 < count) {
                HwLog.e(TAG, "/getAdjustColumnsInfo table error");
            } else if (1 == count) {
                cursor.moveToNext();
                value = cursor.getInt(columnsIndex);
            }
            cursor.close();
        }
        return value;
    }

    public float getAdjustColumnsFloatInfo(String imsi, int columnsIndex) {
        float value = 0.0f;
        Cursor cursor = getAdjustCursor(imsi);
        if (cursor != null) {
            int count = cursor.getCount();
            if (1 < count) {
                HwLog.e(TAG, "/getAdjustColumnsInfo table error");
            } else if (1 == count) {
                cursor.moveToNext();
                value = cursor.getFloat(columnsIndex);
            }
            cursor.close();
        }
        return value;
    }

    public String getAdjustColumnsStringInfo(String imsi, int columnsIndex) {
        String value = "";
        Cursor cursor = getAdjustCursor(imsi);
        if (cursor != null) {
            int count = cursor.getCount();
            if (1 < count) {
                HwLog.e(TAG, "/getAdjustColumnsInfo table error");
            } else if (1 == count) {
                cursor.moveToNext();
                value = cursor.getString(columnsIndex);
            }
            cursor.close();
        }
        return value;
    }

    public long getAdjustColumnsLongInfo(String imsi, int columnsIndex) {
        long value = -1;
        Cursor cursor = getAdjustCursor(imsi);
        if (cursor != null) {
            int count = cursor.getCount();
            if (1 < count) {
                HwLog.e(TAG, "/getAdjustColumnsInfo table error");
            } else if (1 == count) {
                cursor.moveToNext();
                value = cursor.getLong(columnsIndex);
            }
            cursor.close();
        }
        return value;
    }

    public boolean setProvinceInfo(String imsi, String provinceCode, String cityCode) {
        Cursor cursor = getAdjustCursor(imsi);
        Uri apUri = TrafficAdjustTable.getContentUri();
        ContentValues contentValues = new ContentValues();
        contentValues.put("imsi", imsi);
        contentValues.put(Columns.ADJUST_PROVINCE, provinceCode);
        contentValues.put(Columns.ADJUST_CITY, cityCode);
        if (cursor == null) {
            return false;
        }
        String[] columns = NetAssistantStore.getTrafficAdjustColumns();
        StringBuilder where = new StringBuilder();
        where.append(columns[1]).append(" =? ");
        String userWhere = where.toString();
        String[] userWhereArgs = new String[]{imsi};
        int count = cursor.getCount();
        cursor.close();
        if (1 < count || count < 0) {
            HwLog.e(TAG, "/setProvinceInfo table error");
            return false;
        } else if (1 == count) {
            HwLog.e(TAG, "cursor count is 1 !");
            if (this.mContext.getContentResolver().update(apUri, contentValues, userWhere, userWhereArgs) > 0) {
                return true;
            }
            return false;
        } else {
            HwLog.e(TAG, "cursor count is 0 !");
            if (this.mContext.getContentResolver().insert(apUri, contentValues) != null) {
                return true;
            }
            return false;
        }
    }

    public boolean setOperatorInfo(String imsi, String providerCode, String brandCode) {
        Cursor cursor = getAdjustCursor(imsi);
        Uri apUri = TrafficAdjustTable.getContentUri();
        ContentValues contentValues = new ContentValues();
        contentValues.put("imsi", imsi);
        contentValues.put(Columns.ADJUST_PROVIDER, providerCode);
        contentValues.put(Columns.ADJUST_BRAND, brandCode);
        if (cursor == null) {
            return false;
        }
        String[] columns = NetAssistantStore.getTrafficAdjustColumns();
        StringBuilder where = new StringBuilder();
        where.append(columns[1]).append(" =? ");
        String userWhere = where.toString();
        String[] userWhereArgs = new String[]{imsi};
        int count = cursor.getCount();
        cursor.close();
        if (1 < count || count < 0) {
            HwLog.e(TAG, "/setOperatorInfo table error");
            return false;
        } else if (1 == count) {
            HwLog.e(TAG, "cursor count is 1 !");
            if (this.mContext.getContentResolver().update(apUri, contentValues, userWhere, userWhereArgs) > 0) {
                return true;
            }
            return false;
        } else {
            HwLog.e(TAG, "cursor count is 0 !");
            if (this.mContext.getContentResolver().insert(apUri, contentValues) != null) {
                return true;
            }
            return false;
        }
    }

    public boolean setNetAccessInfos(int uid, int netAccessType) {
        String packageName = CommonMethodUtil.getPackageNameByUid(uid);
        Cursor cursor = getNetAccessCursor(uid);
        Uri apUri = NetAccessTable.getContentUri();
        ContentValues contentValues = new ContentValues();
        contentValues.put("package_name", packageName);
        contentValues.put(NetAccessTable.Columns.PACKAGE_NETACCESS_TYPE, Integer.valueOf(netAccessType));
        contentValues.put(NetAccessTable.Columns.PACKAGE_UID, Integer.valueOf(uid));
        contentValues.put(NetAccessTable.Columns.PACKAGE_TRUST, Integer.valueOf(-1));
        if (cursor == null) {
            return false;
        }
        String[] columns = NetAssistantStore.getNetAccessColumns();
        StringBuilder where = new StringBuilder();
        where.append(columns[2]).append(" =? ");
        String userWhere = where.toString();
        String[] userWhereArgs = new String[]{String.valueOf(uid)};
        int count = cursor.getCount();
        cursor.close();
        if (1 < count || count < 0) {
            HwLog.e(TAG, "/setNetAccessInfos table error");
            return false;
        } else if (1 == count) {
            HwLog.e(TAG, "cursor count is 1 !");
            if (this.mContext.getContentResolver().update(apUri, contentValues, userWhere, userWhereArgs) > 0) {
                return true;
            }
            return false;
        } else {
            HwLog.e(TAG, "cursor count is 0 !");
            if (this.mContext.getContentResolver().insert(apUri, contentValues) != null) {
                return true;
            }
            return false;
        }
    }

    private Cursor getSettingCursor(String imsi) {
        Uri apUri = SettingTable.getContentUri();
        String[] columns = NetAssistantStore.getSettingColumns();
        StringBuilder where = new StringBuilder();
        where.append(columns[1]).append(" =? ");
        Cursor cursor = this.mContext.getContentResolver().query(apUri, columns, where.toString(), new String[]{imsi}, null);
        if (cursor == null || 1 >= cursor.getCount()) {
            return cursor;
        }
        cursor.close();
        HwLog.e(TAG, "/getSettingCursor: Setting table error.");
        return null;
    }

    private Cursor getAdjustCursor(String imsi) {
        Uri apUri = TrafficAdjustTable.getContentUri();
        String[] columns = NetAssistantStore.getTrafficAdjustColumns();
        StringBuilder where = new StringBuilder();
        where.append(columns[1]).append(" =? ");
        Cursor cursor = this.mContext.getContentResolver().query(apUri, columns, where.toString(), new String[]{imsi}, null);
        if (cursor == null || 1 >= cursor.getCount()) {
            return cursor;
        }
        cursor.close();
        HwLog.e(TAG, "/getAdjustCursor: Adjust table error.");
        return null;
    }

    private Cursor getNetAccessCursor(int uid) {
        Uri apUri = NetAccessTable.getContentUri();
        String[] columns = NetAssistantStore.getNetAccessColumns();
        StringBuilder where = new StringBuilder();
        where.append(columns[2]).append(" =? ");
        Cursor cursor = this.mContext.getContentResolver().query(apUri, columns, where.toString(), new String[]{String.valueOf(uid)}, null);
        if (cursor == null || 1 >= cursor.getCount()) {
            return cursor;
        }
        cursor.close();
        HwLog.e(TAG, "/getAdjustCursor: Adjust table error.");
        return null;
    }

    public boolean setSettingTotalPackage(String imsi, long value) {
        return setSettingColumnsInfo(imsi, SettingTable.Columns.PACKAGE_TOTAL, Long.valueOf(value));
    }

    public boolean setMonthLimitByte(String imsi, long value) {
        return setSettingColumnsInfo(imsi, SettingTable.Columns.MONTH_LIMIT, Long.valueOf(value));
    }

    public boolean setMonthLimitByte(String imsi, long value, boolean onlyUpdate) {
        return setSettingColumnsInfo(imsi, SettingTable.Columns.MONTH_LIMIT, Long.valueOf(value), onlyUpdate);
    }

    public long getMonthLimitByte(String imsi) {
        return getSettingColumnsLongInfo(imsi, 12);
    }

    public long getMonthLimitSnooze(String imsi) {
        return getSettingColumnsLongInfo(imsi, 13);
    }

    public boolean setMonthLimitSnooze(String imsi, long value) {
        return setSettingColumnsInfo(imsi, SettingTable.Columns.MONTH_LIMIT_SNOOZE, Long.valueOf(value));
    }

    public boolean setMonthWarnByte(String imsi, long value) {
        return setSettingColumnsInfo(imsi, SettingTable.Columns.MONTH_WARN, Long.valueOf(value));
    }

    public long getMonthWarnByte(String imsi) {
        return getSettingColumnsLongInfo(imsi, 14);
    }

    public long getMonthWarnSnooze(String imsi) {
        return getSettingColumnsLongInfo(imsi, 15);
    }

    public boolean setMonthWarnSnooze(String imsi, long value) {
        return setSettingColumnsInfo(imsi, SettingTable.Columns.MONTH_WARN_SNOOZE, Long.valueOf(value));
    }

    public boolean setDailyWarnByte(String imsi, long value) {
        return setSettingColumnsInfo(imsi, SettingTable.Columns.DAILY_WARN, Long.valueOf(value));
    }

    public long getDailyWarnByte(String imsi) {
        return getSettingColumnsLongInfo(imsi, 16);
    }

    public long getDailyWarnSnooze(String imsi) {
        return getSettingColumnsLongInfo(imsi, 17);
    }

    public boolean setDailyWarnSnooze(String imsi, long value) {
        return setSettingColumnsInfo(imsi, SettingTable.Columns.DAILY_WARN_SNOOZE, Long.valueOf(value));
    }

    public boolean setSettingBeginDate(String imsi, int value) {
        return setSettingColumnsInfo(imsi, SettingTable.Columns.BEGIN_DATE, Integer.valueOf(value));
    }

    public boolean setSettingRegularAdjustType(String imsi, int value) {
        return setSettingColumnsInfo(imsi, SettingTable.Columns.REGULAR_ADJUST_TYPE, Integer.valueOf(value));
    }

    public boolean setSettingRegularAdjustBeginTime(String imsi, long time) {
        return setSettingColumnsInfo(imsi, SettingTable.Columns.REGULAR_ADJUST_BEGIN_TIME, Long.valueOf(time));
    }

    public boolean setSettingExcessMontyType(String imsi, int value) {
        return setSettingColumnsInfo(imsi, SettingTable.Columns.EXCESS_MONTH_TYPE, Integer.valueOf(value));
    }

    public boolean setSettingOverMarkMonth(String imsi, int value) {
        return setSettingColumnsInfo(imsi, SettingTable.Columns.IS_OVERMARK_MONTH, Integer.valueOf(value));
    }

    public boolean setSettingOverMarkDay(String imsi, int value) {
        return setSettingColumnsInfo(imsi, SettingTable.Columns.IS_OVERMARK_DAY, Integer.valueOf(value));
    }

    public boolean setSettingUnlockScreen(String imsi, int value) {
        return setSettingColumnsInfo(imsi, SettingTable.Columns.IS_AFTER_LOCKED, Integer.valueOf(value));
    }

    public boolean setSettingNotify(String imsi, int value) {
        return setSettingColumnsInfo(imsi, SettingTable.Columns.IS_NOTIFICATION, Integer.valueOf(value));
    }

    public boolean setSettingSpeedNotify(String imsi, int value) {
        return setSettingColumnsInfo(imsi, SettingTable.Columns.IS_SPEED_NOTIFICATION, Integer.valueOf(value));
    }

    public long getSettingTotalPackage(String imsi) {
        return getSettingColumnsLongInfo(imsi, 2);
    }

    public int getSettingBeginDate(String imsi) {
        return getSettingColumnsIntInfo(imsi, 3);
    }

    public int getSettingRegularAdjustType(String imsi) {
        return getSettingColumnsIntInfo(imsi, 4);
    }

    public long getSettingRegularAdjustBeginTime(String imsi) {
        return getSettingColumnsLongInfo(imsi, 5);
    }

    public int getSettingExcessMontyType(String imsi) {
        return getSettingColumnsIntInfo(imsi, 6);
    }

    public int getSettingOverMarkMonth(String imsi) {
        return getSettingColumnsIntInfo(imsi, 7);
    }

    public int getSettingOverMarkDay(String imsi) {
        return getSettingColumnsIntInfo(imsi, 8);
    }

    public int getSettingUnlockScreen(String imsi) {
        return getSettingColumnsIntInfo(imsi, 9);
    }

    public int getSettingNotify(String imsi) {
        return getSettingColumnsIntInfo(imsi, 10);
    }

    public int getSettingSpeedNotify(String imsi) {
        return getSettingColumnsIntInfo(imsi, 11);
    }

    public long getAdjustPackageValue(String imsi) {
        return getAdjustColumnsLongInfo(imsi, 2);
    }

    public long getAdjustDate(String imsi) {
        return getAdjustColumnsLongInfo(imsi, 4);
    }

    public int getAdjustTypeValue(String imsi) {
        return getAdjustColumnsIntInfo(imsi, 3);
    }

    public String getAdjustProvince(String imsi) {
        return getAdjustColumnsStringInfo(imsi, 5);
    }

    public String getAdjustCity(String imsi) {
        return getAdjustColumnsStringInfo(imsi, 6);
    }

    public String getAdjustProvider(String imsi) {
        return getAdjustColumnsStringInfo(imsi, 7);
    }

    public String getAdjustBrand(String imsi) {
        return getAdjustColumnsStringInfo(imsi, 8);
    }

    public TrafficSettingInfo getTrafficSettingInfo(String imsi) {
        TrafficSettingInfo trafficSettingInfo = new TrafficSettingInfo(imsi);
        Cursor cursor = getSettingCursor(imsi);
        if (cursor != null) {
            int count = cursor.getCount();
            if (1 < count) {
                HwLog.e(TAG, "/getSettingColumnsInfo table error");
            } else if (1 == count) {
                cursor.moveToNext();
                trafficSettingInfo.packageTotal = cursor.getLong(2);
                trafficSettingInfo.beginDate = cursor.getInt(3);
                trafficSettingInfo.regularAdjustType = cursor.getInt(4);
                trafficSettingInfo.regularAdjustBeginTime = cursor.getLong(5);
                trafficSettingInfo.excessMonthType = cursor.getInt(6);
                trafficSettingInfo.isOvermarkMonth = cursor.getInt(7);
                trafficSettingInfo.isOvermarkDay = cursor.getInt(8);
                trafficSettingInfo.isAfterLocked = cursor.getInt(9);
                trafficSettingInfo.isNotification = cursor.getInt(10);
                trafficSettingInfo.isSpeedNotification = cursor.getInt(11);
                HwLog.i(TAG, "packageTotal = " + trafficSettingInfo.packageTotal + " beginDate = " + trafficSettingInfo.beginDate + " regularAdjustType = " + trafficSettingInfo.regularAdjustType + " regularAdjustBeginTime = " + trafficSettingInfo.regularAdjustBeginTime + " excessMonthType = " + trafficSettingInfo.excessMonthType + " isOvermarkMonth = " + trafficSettingInfo.isOvermarkMonth + " isOvermarkDay = " + trafficSettingInfo.isOvermarkDay + " isAfterLocked = " + trafficSettingInfo.isAfterLocked + " isNotification = " + trafficSettingInfo.isNotification + " isSpeedNotification = " + trafficSettingInfo.isSpeedNotification);
            }
            cursor.close();
        }
        return trafficSettingInfo;
    }

    public boolean setAdjustItemInfo(String imsi, int adjustType, long realAdjustBytes) {
        return SimCardSettingsInfo.setAdjustItemInfo(this.mContext, imsi, getAdjustCursor(imsi), adjustType, realAdjustBytes);
    }

    @TargetApi(19)
    public void updateSettings(ArrayMap<String, Object> setValue) {
        Cursor cursor = getSettingCursor(String.valueOf(setValue.get("imsi")));
        boolean isSuccess = false;
        if (cursor != null) {
            Uri apUri = SettingTable.getContentUri();
            String[] columns = NetAssistantStore.getSettingColumns();
            StringBuilder where = new StringBuilder();
            where.append(columns[1]).append(" =? ");
            String userWhere = where.toString();
            String[] userWhereArgs = new String[]{imsi};
            ContentValues contentValues = new ContentValues();
            for (String column : setValue.keySet()) {
                contentValues.put(column, String.valueOf(setValue.get(column)));
            }
            int count = cursor.getCount();
            cursor.close();
            if (1 < count || count < 0) {
                HwLog.e(TAG, "/setSettingColumnsInfo table error");
            } else if (1 == count) {
                if (this.mContext.getContentResolver().update(apUri, contentValues, userWhere, userWhereArgs) > 0) {
                    isSuccess = true;
                }
            } else if (this.mContext.getContentResolver().insert(apUri, contentValues) != null) {
                isSuccess = true;
            }
        }
        HwLog.i(TAG, "updateSettings " + isSuccess);
    }

    public int deleteSettings(String imsi) {
        Uri apUri = SettingTable.getContentUri();
        String[] columns = NetAssistantStore.getSettingColumns();
        StringBuilder where = new StringBuilder();
        where.append(columns[1]).append(" =? ");
        int deleteCount = this.mContext.getContentResolver().delete(apUri, where.toString(), new String[]{imsi});
        HwLog.v(TAG, "deleted rows from SettingInfo Table: " + deleteCount);
        return deleteCount;
    }
}
