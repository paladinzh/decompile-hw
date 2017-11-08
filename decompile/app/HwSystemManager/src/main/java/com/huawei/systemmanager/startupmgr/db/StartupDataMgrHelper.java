package com.huawei.systemmanager.startupmgr.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.comm.misc.CursorHelper;
import com.huawei.systemmanager.comm.misc.TimeUtil;
import com.huawei.systemmanager.startupmgr.comm.AwakedRecordInfo;
import com.huawei.systemmanager.startupmgr.comm.AwakedStartupInfo;
import com.huawei.systemmanager.startupmgr.comm.NormalRecordInfo;
import com.huawei.systemmanager.startupmgr.comm.NormalStartupInfo;
import com.huawei.systemmanager.startupmgr.comm.StartupDBConst.AwakedViewKeys;
import com.huawei.systemmanager.startupmgr.comm.StartupDBConst.NormalViewKeys;
import com.huawei.systemmanager.startupmgr.db.StartupProvider.AwakedCallerProvider;
import com.huawei.systemmanager.startupmgr.db.StartupProvider.StartupGFeatureProvider;
import com.huawei.systemmanager.startupmgr.db.StartupProvider.StartupInfoQueryProvider;
import com.huawei.systemmanager.startupmgr.db.StartupProvider.StartupRecordProvider;
import com.huawei.systemmanager.util.HwLog;
import java.util.Calendar;
import java.util.List;

public class StartupDataMgrHelper {
    private static final String TAG = "StartupProviderHelper";

    public static NormalStartupInfo querySingleNormalStartupInfo(Context ctx, String pkgName) {
        Cursor cursor = null;
        try {
            cursor = ctx.getContentResolver().query(Uri.withAppendedPath(StartupInfoQueryProvider.CONTENT_URI_BASE, NormalViewKeys.NORMAL_STARTUP_INFO_VIEW), NormalStartupInfo.NORMAL_FULL_QUERY_PROJECTION, "packageName = ? ", new String[]{pkgName}, null);
            if (cursor == null || !cursor.moveToFirst()) {
                HwLog.w(TAG, "querySingleNormalStartupInfo can't get result for package: " + pkgName);
            } else {
                NormalStartupInfo result = NormalStartupInfo.fromCursor(cursor);
                if (result.validInfo()) {
                    return result;
                }
                HwLog.e(TAG, "querySingleNormalStartupInfo got invalid result for package:" + pkgName);
            }
            CursorHelper.closeCursor(cursor);
        } catch (SQLiteException ex) {
            HwLog.e(TAG, "querySingleNormalStartupInfo SQLiteException " + ex.getMessage());
        } catch (Exception ex2) {
            HwLog.e(TAG, "querySingleNormalStartupInfo Exception " + ex2.getMessage());
        } finally {
            CursorHelper.closeCursor(cursor);
        }
        return null;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static List<NormalStartupInfo> queryNormalStartupInfoList(Context ctx) {
        List<NormalStartupInfo> result = Lists.newArrayList();
        Cursor cursor = null;
        try {
            cursor = ctx.getContentResolver().query(Uri.withAppendedPath(StartupInfoQueryProvider.CONTENT_URI_BASE, NormalViewKeys.NORMAL_STARTUP_INFO_VIEW), NormalStartupInfo.NORMAL_FULL_QUERY_PROJECTION, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    NormalStartupInfo info = NormalStartupInfo.fromCursor(cursor);
                    if (info.validInfo()) {
                        result.add(info);
                    } else {
                        HwLog.w(TAG, "queryNormalStartupInfoList get an invalid AwakedStartupInfo");
                    }
                }
            } else {
                HwLog.w(TAG, "queryNormalStartupInfoList can't get result");
            }
            CursorHelper.closeCursor(cursor);
        } catch (SQLiteException ex) {
            HwLog.e(TAG, "queryNormalStartupInfoList SQLiteException " + ex.getMessage());
        } catch (Exception ex2) {
            HwLog.e(TAG, "queryNormalStartupInfoList Exception " + ex2.getMessage());
        } catch (Throwable th) {
            CursorHelper.closeCursor(cursor);
        }
        return result;
    }

    public static void modifyNormalStartupInfoStatus(Context ctx, String pkgName, boolean status) {
        NormalStartupInfo startupInfo = new NormalStartupInfo(pkgName);
        startupInfo.setStatus(status);
        startupInfo.persistStatusData(ctx, true);
    }

    public static int queryNormalStartupAllowCount(Context ctx) {
        Cursor cursor = null;
        int count;
        try {
            cursor = ctx.getContentResolver().query(Uri.withAppendedPath(StartupInfoQueryProvider.CONTENT_URI_BASE, NormalViewKeys.NORMAL_STARTUP_INFO_VIEW), new String[]{"packageName"}, "status = ? ", new String[]{"1"}, null);
            if (cursor != null) {
                count = cursor.getCount();
                return count;
            }
            HwLog.w(TAG, "queryNormalStartupAllowCount can't get result");
            CursorHelper.closeCursor(cursor);
            return 0;
        } catch (SQLiteException ex) {
            count = TAG;
            HwLog.e(count, "queryNormalStartupAllowCount SQLiteException " + ex.getMessage());
        } catch (Exception ex2) {
            count = TAG;
            HwLog.e(count, "queryNormalStartupAllowCount Exception " + ex2.getMessage());
        } finally {
            CursorHelper.closeCursor(cursor);
        }
    }

    public static AwakedStartupInfo querySingleAwakedStartupInfo(Context ctx, String pkgName) {
        Cursor cursor = null;
        try {
            cursor = ctx.getContentResolver().query(Uri.withAppendedPath(StartupInfoQueryProvider.CONTENT_URI_BASE, AwakedViewKeys.AWAKED_STARTUP_INFO_VIEW), AwakedStartupInfo.AWAKED_FULL_QUERY_PROJECTION, "packageName = ? ", new String[]{pkgName}, null);
            if (cursor == null || !cursor.moveToFirst()) {
                HwLog.w(TAG, "querySingleAwakedStartupInfo can't get result for package: " + pkgName);
            } else {
                AwakedStartupInfo result = AwakedStartupInfo.fromCursor(cursor);
                if (result.validInfo()) {
                    return result;
                }
                HwLog.e(TAG, "querySingleAwakedStartupInfo got invalid result for package:" + pkgName);
            }
            CursorHelper.closeCursor(cursor);
        } catch (SQLiteException ex) {
            HwLog.e(TAG, "querySingleAwakedStartupInfo SQLiteException " + ex.getMessage());
        } catch (Exception ex2) {
            HwLog.e(TAG, "querySingleAwakedStartupInfo Exception " + ex2.getMessage());
        } finally {
            CursorHelper.closeCursor(cursor);
        }
        return null;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static List<AwakedStartupInfo> queryAwakedStartupInfoList(Context ctx) {
        List<AwakedStartupInfo> result = Lists.newArrayList();
        Cursor cursor = null;
        try {
            cursor = ctx.getContentResolver().query(Uri.withAppendedPath(StartupInfoQueryProvider.CONTENT_URI_BASE, AwakedViewKeys.AWAKED_STARTUP_INFO_VIEW), AwakedStartupInfo.AWAKED_FULL_QUERY_PROJECTION, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    AwakedStartupInfo info = AwakedStartupInfo.fromCursor(cursor);
                    if (info.validInfo()) {
                        result.add(info);
                    } else {
                        HwLog.w(TAG, "queryAwakedStartupInfoList get an invalid AwakedStartupInfo");
                    }
                }
            } else {
                HwLog.w(TAG, "queryAwakedStartupInfoList can't get result");
            }
            CursorHelper.closeCursor(cursor);
        } catch (SQLiteException ex) {
            HwLog.e(TAG, "queryAwakedStartupInfoList SQLiteException " + ex.getMessage());
        } catch (Exception ex2) {
            HwLog.e(TAG, "queryAwakedStartupInfoList Exception " + ex2.getMessage());
        } catch (Throwable th) {
            CursorHelper.closeCursor(cursor);
        }
        return result;
    }

    public static void modifyAwakedStartupInfoStatus(Context ctx, String pkgName, boolean status) {
        AwakedStartupInfo startupInfo = new AwakedStartupInfo(pkgName, status);
        startupInfo.setStatus(status);
        startupInfo.persistStatusData(ctx, true);
    }

    public static int queryAwakedStartupCount(Context ctx) {
        Cursor cursor = null;
        int count;
        try {
            cursor = ctx.getContentResolver().query(Uri.withAppendedPath(StartupInfoQueryProvider.CONTENT_URI_BASE, AwakedViewKeys.AWAKED_STARTUP_INFO_VIEW), new String[]{"packageName"}, null, null, null);
            if (cursor != null) {
                count = cursor.getCount();
                return count;
            }
            HwLog.w(TAG, "queryAwakedStartupCount can't get result");
            CursorHelper.closeCursor(cursor);
            return 0;
        } catch (SQLiteException ex) {
            count = TAG;
            HwLog.e(count, "queryAwakedStartupCount SQLiteException " + ex.getMessage());
        } catch (Exception ex2) {
            count = TAG;
            HwLog.e(count, "queryAwakedStartupCount Exception " + ex2.getMessage());
        } finally {
            CursorHelper.closeCursor(cursor);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static List<NormalRecordInfo> queryNormalRecordInfoList(Context ctx) {
        Calendar calendar = TimeUtil.getTodayStartCalendar();
        calendar.add(5, 1);
        long timeEnd = calendar.getTimeInMillis();
        calendar.add(5, -7);
        long timeStart = calendar.getTimeInMillis();
        List<NormalRecordInfo> result = Lists.newArrayList();
        Cursor cursor = null;
        try {
            cursor = ctx.getContentResolver().query(Uri.withAppendedPath(StartupRecordProvider.CONTENT_URI_BASE, NormalRecordTable.TABLE_NAME), NormalRecordInfo.NORMAL_RECORD_FULL_QUERY_PROJECTION, "timeOfLastExact >= ? and timeOfLastExact <= ? ", new String[]{String.valueOf(timeStart), String.valueOf(timeEnd)}, "timeOfLastExact DESC");
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    NormalRecordInfo info = NormalRecordInfo.fromCursor(cursor);
                    if (info.validInfoForDB()) {
                        result.add(info);
                    } else {
                        HwLog.w(TAG, "queryNormalRecordInfoList get an invalid AwakedStartupInfo");
                    }
                }
            } else {
                HwLog.w(TAG, "queryNormalRecordInfoList can't get result");
            }
            CursorHelper.closeCursor(cursor);
        } catch (SQLiteException ex) {
            HwLog.e(TAG, "queryNormalRecordInfoList SQLiteException " + ex.getMessage());
        } catch (Exception ex2) {
            HwLog.e(TAG, "queryNormalRecordInfoList Exception " + ex2.getMessage());
        } catch (Throwable th) {
            CursorHelper.closeCursor(cursor);
        }
        return result;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static List<AwakedRecordInfo> queryAwakedRecordInfoList(Context ctx) {
        Calendar calendar = TimeUtil.getTodayStartCalendar();
        calendar.add(5, 1);
        long timeEnd = calendar.getTimeInMillis();
        calendar.add(5, -7);
        long timeStart = calendar.getTimeInMillis();
        List<AwakedRecordInfo> result = Lists.newArrayList();
        Cursor cursor = null;
        try {
            cursor = ctx.getContentResolver().query(Uri.withAppendedPath(StartupRecordProvider.CONTENT_URI_BASE, AwakedRecordTable.TABLE_NAME), AwakedRecordInfo.AWAKED_RECORD_FULL_QUERY_PROJECTION, "timeOfLastExact >= ? and timeOfLastExact <= ? ", new String[]{String.valueOf(timeStart), String.valueOf(timeEnd)}, "timeOfLastExact DESC");
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    AwakedRecordInfo info = AwakedRecordInfo.fromCursor(cursor);
                    if (info.validInfoForDB()) {
                        result.add(info);
                    } else {
                        HwLog.w(TAG, "queryAwakedRecordInfoList get an invalid AwakedStartupInfo");
                    }
                }
            } else {
                HwLog.w(TAG, "queryAwakedRecordInfoList can't get result");
            }
            CursorHelper.closeCursor(cursor);
        } catch (SQLiteException ex) {
            HwLog.e(TAG, "queryAwakedRecordInfoList SQLiteException " + ex.getMessage());
        } catch (Exception ex2) {
            HwLog.e(TAG, "queryAwakedRecordInfoList Exception " + ex2.getMessage());
        } catch (Throwable th) {
            CursorHelper.closeCursor(cursor);
        }
        return result;
    }

    public static void checkRecordTable(Context ctx, String tableName) {
        try {
            HwLog.d(TAG, "checkRecordTable " + tableName);
            ctx.getContentResolver().call(StartupProvider.AUTHORITY_URI, StartupRecordProvider.CALL_CHECK_TABLE_FUNCTION, tableName, null);
        } catch (SQLiteException ex) {
            HwLog.e(TAG, "checkRecordTable SQLiteException " + ex.getMessage());
        } catch (Exception ex2) {
            HwLog.e(TAG, "checkRecordTable Exception " + ex2.getMessage());
        }
    }

    public static void deleteNotExistPackageData(Context ctx, String pkgName) {
        try {
            ctx.getContentResolver().call(StartupProvider.AUTHORITY_URI, StartupGFeatureProvider.CALL_DELETE_FUNCTION, pkgName, null);
            ctx.getContentResolver().delete(AwakedCallerProvider.CONTENT_URI, "packageName = ? ", new String[]{pkgName});
            ctx.getContentResolver().delete(Uri.withAppendedPath(StartupRecordProvider.CONTENT_URI_BASE, NormalRecordTable.TABLE_NAME), "packageName = ? ", new String[]{pkgName});
            ctx.getContentResolver().delete(Uri.withAppendedPath(StartupRecordProvider.CONTENT_URI_BASE, AwakedRecordTable.TABLE_NAME), "packageName = ? ", new String[]{pkgName});
        } catch (SQLiteException ex) {
            HwLog.e(TAG, "deleteDataWhenPackageRemoved SQLiteException " + ex.getMessage());
        } catch (Exception ex2) {
            HwLog.e(TAG, "deleteDataWhenPackageRemoved Exception " + ex2.getMessage());
        }
    }
}
