package com.huawei.notificationmanager.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.UserHandle;
import android.text.TextUtils;
import com.huawei.notificationmanager.common.CommonObjects.NotificationCfgInfo;
import com.huawei.notificationmanager.common.CommonObjects.NotificationLogInfo;
import com.huawei.notificationmanager.common.ConstValues;
import com.huawei.notificationmanager.common.NotificationBackend;
import com.huawei.notificationmanager.common.NotificationUtils;
import com.huawei.notificationmanager.util.Helper;
import com.huawei.notificationmanager.util.NmCenterDefValueXmlHelper;
import com.huawei.systemmanager.comm.misc.Closeables;
import com.huawei.systemmanager.comm.misc.ProviderUtils;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.customize.AbroadUtils;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPkgInfo;
import com.huawei.systemmanager.util.app.HsmPkgUtils;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBAdapter {
    private static final String TAG = "NotificationDBAdapter";
    private final NotificationBackend mBackend;
    private Context mContext = null;

    public DBAdapter(Context context) {
        this.mContext = context;
        this.mBackend = new NotificationBackend();
    }

    public List<NotificationCfgInfo> getCfgList() {
        List<HsmPkgInfo> appList = Helper.getMonitoredAppList(this.mContext);
        if (Utility.isNullOrEmptyList(appList)) {
            HwLog.w(TAG, "getCfgList: Fail to get monitored app list");
            return new ArrayList();
        }
        Map<String, NotificationCfgInfo> appCfgMapInDB = getCfgMapFromDB();
        HwLog.d(TAG, "getCfgList: appCfgMapInDB size = " + appCfgMapInDB.size());
        List<NotificationCfgInfo> list = getUpdatedCfgList(appList, appCfgMapInDB, false);
        updateListForMVersion(list);
        if (Helper.getProfileId(this.mContext) != UserHandle.myUserId()) {
            Map<String, NotificationCfgInfo> appUserCfgMapInDB = getUserCfgMapFromDB();
            List<HsmPkgInfo> appUserList = Helper.getMonitoredUserAppList(this.mContext);
            if (Utility.isNullOrEmptyList(appUserList)) {
                return list;
            }
            List<NotificationCfgInfo> NotificationUserCfgInfo = getUpdatedCfgList(appUserList, appUserCfgMapInDB, true);
            updateListForMVersion(NotificationUserCfgInfo);
            list.addAll(NotificationUserCfgInfo);
        }
        return list;
    }

    private void updateListForMVersion(List<NotificationCfgInfo> list) {
        if (list != null && !list.isEmpty()) {
            NotificationBackend backend = new NotificationBackend();
            for (NotificationCfgInfo info : list) {
                updateInfoForMVersion(info, backend);
            }
        }
    }

    public static void updateInfoForMVersion(NotificationCfgInfo info, NotificationBackend backend) {
        boolean z = false;
        if (info != null) {
            info.setCanForbid(NotificationUtils.isAppCanForbid(info.mPkgName, info.mUid));
            info.setMainNotificationEnabled(backend.getNotificationsBanned(info.mPkgName, info.mUid));
            int sensitive = backend.getSensitive(info.mPkgName, info.mUid);
            if (-1 != sensitive) {
                z = true;
            }
            info.setLockscreenNotificationEnable(z);
            if (sensitive == 0) {
                info.setHideContent(true);
            }
        }
    }

    public static void putLockscreenNotificationEnable(NotificationCfgInfo info, NotificationBackend backend) {
        if (AbroadUtils.isAbroad()) {
            info.setLockscreenNotificationEnable(true);
            backend.setSensitive(info.mPkgName, info.mUid, info.isLockscreenNotificationEnabled(), info.isHideContent());
            HwLog.d("DBAdapter", "Lockscreen=open");
        }
    }

    private List<NotificationCfgInfo> getUpdatedCfgList(List<HsmPkgInfo> appList, Map<String, NotificationCfgInfo> appCfgMapInDB, boolean isRunningAfW) {
        List<NotificationCfgInfo> result = new ArrayList();
        NmCenterDefValueXmlHelper defHelper = new NmCenterDefValueXmlHelper();
        for (HsmPkgInfo app : appList) {
            String pkgName = app.mPkgName;
            NotificationCfgInfo appCfgInfo = new NotificationCfgInfo(app);
            NotificationCfgInfo appCfgInfoInDB = (NotificationCfgInfo) appCfgMapInDB.get(pkgName);
            if (appCfgInfoInDB == null) {
                ContentValues defValue = Helper.getDefaultValue(defHelper, this.mContext, pkgName, false);
                HwLog.d(TAG, "getUpdatedCfgList: pkg = " + pkgName + ", defValue:" + defValue);
                appCfgInfo.copyCfgsFrom(initNewApp(pkgName, defValue, isRunningAfW));
            } else {
                appCfgInfo.copyCfgsFrom(appCfgInfoInDB);
                appCfgMapInDB.remove(pkgName);
            }
            updateInfoForMVersion(appCfgInfo, this.mBackend);
            result.add(appCfgInfo);
        }
        Collections.sort(result, NotificationCfgInfo.NOTIFICATION_ALP_COMPARATOR);
        return result;
    }

    public NotificationCfgInfo initNewApp(String pkgName, ContentValues cfgValues) {
        return initNewApp(pkgName, cfgValues, false);
    }

    public NotificationCfgInfo initNewApp(String pkgName, ContentValues cfgValues, boolean isRunningAfW) {
        boolean isAFwRunning = true;
        HwLog.i(TAG, "initNewApp: add to db:" + pkgName + ", default:" + cfgValues);
        NotificationCfgInfo afwAppCfgInfo = new NotificationCfgInfo(pkgName);
        NotificationCfgInfo newAppCfgInfo = initNotificationStatus(pkgName, cfgValues, HsmPkgUtils.getPackageUid(pkgName), false);
        int awfUser = Helper.getProfileId(this.mContext);
        if (awfUser == UserHandle.myUserId()) {
            isAFwRunning = false;
        }
        if (isAFwRunning) {
            try {
                afwAppCfgInfo = initNotificationStatus(pkgName, cfgValues, this.mContext.getPackageManager().getPackageUid(pkgName, awfUser), true);
            } catch (NameNotFoundException e) {
            }
        }
        if (isRunningAfW) {
            return afwAppCfgInfo;
        }
        return newAppCfgInfo;
    }

    private NotificationCfgInfo initNotificationStatus(String pkgName, ContentValues cfgValues, int uid, boolean isAFwRunning) {
        NotificationCfgInfo newAppCfgInfo = new NotificationCfgInfo(pkgName);
        if (cfgValues != null) {
            boolean nCfg;
            newAppCfgInfo.copyCfgsFrom(cfgValues);
            switch (cfgValues.getAsInteger(ConstValues.NOTIFICATION_CFG).intValue()) {
                case 0:
                    nCfg = false;
                    Helper.setNotificationsEnabledForPackage(pkgName, uid, Boolean.valueOf(false));
                    break;
                case 1:
                    nCfg = true;
                    Helper.setNotificationsEnabledForPackage(pkgName, uid, Boolean.valueOf(true));
                    break;
                default:
                    HwLog.w(TAG, "initNewApp: Invalid cfg = " + false + ", pkgname = " + pkgName);
                    nCfg = false;
                    break;
            }
            newAppCfgInfo.setMainNotificationEnabled(nCfg);
        }
        newAppCfgInfo.mUid = uid;
        addCfg(newAppCfgInfo, isAFwRunning);
        return newAppCfgInfo;
    }

    private Map<String, NotificationCfgInfo> getCfgMapFromDB() {
        HwLog.d(TAG, "getCfgMapFromDB: begin");
        Map<String, NotificationCfgInfo> appCfgMap = new HashMap();
        Cursor cursor = this.mContext.getContentResolver().query(DBProvider.URI_NOTIFICATION_CFG, null, null, null, null);
        if (Utility.isNullOrEmptyCursor(cursor, true)) {
            HwLog.w(TAG, "getCfgMapFromDB: Fail to get cfg from DB");
            return appCfgMap;
        }
        while (cursor.moveToNext()) {
            NotificationCfgInfo appInfo = new NotificationCfgInfo();
            appInfo.parseCfgsFrom(cursor);
            appCfgMap.put(appInfo.mPkgName, appInfo);
        }
        cursor.close();
        HwLog.d(TAG, "getCfgMapFromDB: end");
        return appCfgMap;
    }

    private Map<String, NotificationCfgInfo> getUserCfgMapFromDB() {
        HwLog.d(TAG, "getCfgMapFromDB for user: begin");
        Map<String, NotificationCfgInfo> appUserCfgMap = new HashMap();
        Context ctx = this.mContext;
        try {
            ctx = this.mContext.createPackageContextAsUser(this.mContext.getPackageName(), 0, new UserHandle(Helper.getProfileId(this.mContext)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Cursor cursor = ctx.getContentResolver().query(DBProvider.URI_NOTIFICATION_CFG, null, null, null, null);
        if (Utility.isNullOrEmptyCursor(cursor, true)) {
            HwLog.w(TAG, "getCfgMapFromDB for user: Fail to get cfg from DB");
            return appUserCfgMap;
        }
        while (cursor.moveToNext()) {
            NotificationCfgInfo appInfo = new NotificationCfgInfo();
            appInfo.parseCfgsFrom(cursor);
            appUserCfgMap.put(appInfo.mPkgName, appInfo);
        }
        cursor.close();
        HwLog.d(TAG, "getUserCfgMapFromDB for user: end");
        return appUserCfgMap;
    }

    public boolean addCfg(NotificationCfgInfo cfgInfo, boolean isAFWRunning) {
        if (TextUtils.isEmpty(cfgInfo.mPkgName)) {
            HwLog.w(TAG, "addCfg: packageName is empty");
            return false;
        }
        addGoogleOrigin(cfgInfo);
        return addHuaWei(cfgInfo, isAFWRunning);
    }

    public boolean addHuaWei(NotificationCfgInfo cfgInfo, boolean isAFWRunning) {
        ContentValues value = cfgInfo.getAsContentValue();
        Context ctx = this.mContext;
        if (isAFWRunning) {
            try {
                ctx = this.mContext.createPackageContextAsUser(this.mContext.getPackageName(), 0, new UserHandle(Helper.getProfileId(this.mContext)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Object uri = null;
        try {
            uri = ctx.getContentResolver().insert(DBProvider.URI_NOTIFICATION_CFG, value);
        } catch (RuntimeException e2) {
            HwLog.e(TAG, "addHuaWei", e2);
        }
        if (uri == null || DBProvider.URI_NOTIFICATION_CFG.equals(uri)) {
            return false;
        }
        return true;
    }

    private void addGoogleOrigin(NotificationCfgInfo cfgInfo) {
        NotificationBackend backend = new NotificationBackend();
        backend.setPeekable(cfgInfo.mPkgName, cfgInfo.mUid, cfgInfo.isHeadsupNotificationEnabled());
        backend.setSensitive(cfgInfo.mPkgName, cfgInfo.mUid, cfgInfo.isLockscreenNotificationEnabled(), cfgInfo.isHideContent());
    }

    public boolean addCfgList(List<HsmPkgInfo> appList) {
        boolean z = false;
        if (Utility.isNullOrEmptyList(appList)) {
            HwLog.w(TAG, "addCfgList: Invalid app list");
            return false;
        }
        int nIndex = 0;
        ContentValues[] values = new ContentValues[appList.size()];
        for (HsmPkgInfo pkgInfo : appList) {
            ContentValues value = new ContentValues();
            value.put("packageName", pkgInfo.mPkgName);
            value.put(ConstValues.NOTIFICATION_CFG, Integer.valueOf(0));
            int nIndex2 = nIndex + 1;
            values[nIndex] = value;
            nIndex = nIndex2;
        }
        if (((long) this.mContext.getContentResolver().bulkInsert(DBProvider.URI_NOTIFICATION_CFG, values)) > 0) {
            z = true;
        }
        return z;
    }

    public NotificationCfgInfo getNotificationCfgInfo(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            HwLog.w(TAG, "getNotificationCfgInfo: Invalid packagename");
            return null;
        }
        Closeable closeable = null;
        try {
            closeable = this.mContext.getContentResolver().query(DBProvider.URI_NOTIFICATION_CFG, null, "packageName = ?", new String[]{packageName}, null);
            if (Utility.isNullOrEmptyCursor(closeable, false)) {
                HwLog.w(TAG, "getNotificationCfgInfo: Fail to get cfg from DB");
                return null;
            }
            closeable.moveToFirst();
            NotificationCfgInfo appInfo = new NotificationCfgInfo();
            appInfo.parseCfgsFrom(closeable);
            updateInfoForMVersion(appInfo, this.mBackend);
            Closeables.close(closeable);
            return appInfo;
        } catch (RuntimeException e) {
            HwLog.w(TAG, "getNotificationCfgInfo RuntimeException", e);
            return null;
        } finally {
            Closeables.close(closeable);
        }
    }

    public boolean updateCfg(NotificationCfgInfo cfgInfo) {
        if (cfgInfo != null) {
            return updateCfg(cfgInfo.mPkgName, cfgInfo.getCfgUpdateContentValue());
        }
        return false;
    }

    public boolean updateCfg(String packageName, ContentValues values) {
        boolean z = true;
        if (TextUtils.isEmpty(packageName) || values == null) {
            HwLog.w(TAG, "updateCfg: packageName or values is empty");
            return false;
        }
        int nUpdate = this.mContext.getContentResolver().update(DBProvider.URI_NOTIFICATION_CFG, values, "packageName = ?", new String[]{packageName});
        HwLog.d(TAG, "updateCfg  nUpdate = " + nUpdate);
        if (nUpdate <= 0) {
            z = false;
        }
        return z;
    }

    public boolean updateCfg(String packageName, boolean nCfg) {
        boolean z = true;
        if (TextUtils.isEmpty(packageName)) {
            HwLog.w(TAG, "updateCfg: packageName is empty");
            return false;
        }
        int i;
        ContentValues values = new ContentValues();
        String str = ConstValues.NOTIFICATION_CFG;
        if (nCfg) {
            i = 1;
        } else {
            i = 0;
        }
        values.put(str, Integer.valueOf(i));
        if (this.mContext.getContentResolver().update(DBProvider.URI_NOTIFICATION_CFG, values, "packageName = ?", new String[]{packageName}) <= 0) {
            z = false;
        }
        return z;
    }

    public int updateAllCfg(int notificationCfg) {
        ContentValues values = new ContentValues();
        values.put(ConstValues.NOTIFICATION_CFG, Integer.valueOf(notificationCfg));
        return this.mContext.getContentResolver().update(DBProvider.URI_NOTIFICATION_CFG, values, null, null);
    }

    public boolean deleteCfg(String packageName) {
        boolean z = true;
        if (TextUtils.isEmpty(packageName)) {
            HwLog.w(TAG, "deleteCfg: packageName is empty");
            return false;
        }
        if (this.mContext.getContentResolver().delete(DBProvider.URI_NOTIFICATION_CFG, "packageName = ?", new String[]{packageName}) <= 0) {
            z = false;
        }
        return z;
    }

    public boolean deleteAllCfg() {
        if (ProviderUtils.deleteAll(this.mContext, DBProvider.URI_NOTIFICATION_CFG) > 0) {
            return true;
        }
        return false;
    }

    public List<ArrayList<NotificationLogInfo>> getLogList() {
        Cursor c = this.mContext.getContentResolver().query(DBProvider.URI_NOTIFICATION_LOG, null, null, null, "packageName ASC,logDatetime DESC");
        if (Utility.isNullOrEmptyCursor(c, true)) {
            return null;
        }
        List<ArrayList<NotificationLogInfo>> logList = new ArrayList();
        int nColIndexPkgName = c.getColumnIndex("packageName");
        int nColIndexLogTime = c.getColumnIndex("logDatetime");
        int nColIndexLogTitle = c.getColumnIndex("logTitle");
        int nColIndexLogText = c.getColumnIndex("logText");
        int nColIndexId = c.getColumnIndex("_id");
        String strLastPackage = "";
        String strLastInvalidPackage = "";
        ArrayList<String> invalidPackageList = new ArrayList();
        ArrayList<NotificationLogInfo> logGroup = new ArrayList();
        while (c.moveToNext()) {
            if (!strLastInvalidPackage.equals(c.getString(nColIndexPkgName))) {
                NotificationLogInfo logItem = NotificationLogInfo.createLogItem(c, nColIndexPkgName, nColIndexLogTime, nColIndexLogTitle, nColIndexLogText, nColIndexId);
                if (logItem == null) {
                    strLastInvalidPackage = c.getString(nColIndexPkgName);
                    invalidPackageList.add(strLastInvalidPackage);
                } else {
                    String pkgName = logItem.getPackageName();
                    if (strLastPackage.equals(pkgName)) {
                        logGroup.add(logItem);
                    } else {
                        addLogGroupToLogList(logList, logGroup);
                        logGroup = new ArrayList();
                        logGroup.add(logItem);
                        strLastPackage = pkgName;
                    }
                }
            }
        }
        addLogGroupToLogList(logList, logGroup);
        c.close();
        deleteInvalidLogs(invalidPackageList);
        return logList;
    }

    private void addLogGroupToLogList(List<ArrayList<NotificationLogInfo>> logList, ArrayList<NotificationLogInfo> logGroup) {
        if (!(logList == null || Utility.isNullOrEmptyList(logGroup))) {
            logList.add(logGroup);
        }
    }

    private void deleteInvalidLogs(ArrayList<String> invalidPackageList) {
        if (!Utility.isNullOrEmptyList(invalidPackageList)) {
            for (String packageName : invalidPackageList) {
                deleteLog(packageName);
            }
        }
    }

    public boolean insertLog(ContentValues values) {
        boolean z = false;
        if (values == null) {
            HwLog.w(TAG, "insertLog: Invalid params");
            return false;
        }
        Uri uri = this.mContext.getContentResolver().insert(DBProvider.URI_NOTIFICATION_LOG, values);
        if (!(uri == null || DBProvider.URI_NOTIFICATION_LOG.equals(uri))) {
            z = true;
        }
        return z;
    }

    public boolean deleteLog(String packageName) {
        boolean z = true;
        if (TextUtils.isEmpty(packageName)) {
            HwLog.w(TAG, "deleteLog: packageName is empty");
            return false;
        }
        if (this.mContext.getContentResolver().delete(DBProvider.URI_NOTIFICATION_LOG, "packageName = ?", new String[]{packageName}) <= 0) {
            z = false;
        }
        return z;
    }

    public boolean deleteAllLog() {
        if (ProviderUtils.deleteAll(this.mContext, DBProvider.URI_NOTIFICATION_LOG) > 0) {
            return true;
        }
        return false;
    }
}
