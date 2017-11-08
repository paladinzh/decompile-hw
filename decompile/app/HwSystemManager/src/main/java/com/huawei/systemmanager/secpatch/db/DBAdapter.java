package com.huawei.systemmanager.secpatch.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import com.huawei.systemmanager.comm.misc.ProviderUtils;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.secpatch.common.ConstValues;
import com.huawei.systemmanager.secpatch.common.SecPatchItem;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.List;

public class DBAdapter {
    public static final String TAG = "SecPatchDBAdapter";
    private static final Uri URI_SEARCH_VIEW = Uri.withAppendedPath(Uri.parse("content://com.huawei.systemmanager.SecPatchDBProvider"), ConstValues.SEARCH_VIEW_NAME_SECPATCH);
    private static final Uri URI_SECPATCH = Uri.withAppendedPath(Uri.parse("content://com.huawei.systemmanager.SecPatchDBProvider"), ConstValues.TB_SECPATCH);
    private static final Uri URI_SYSTEM_VERSION_UPDATE = Uri.withAppendedPath(Uri.parse("content://com.huawei.systemmanager.SecPatchDBProvider"), ConstValues.SYSTEM_VERSION_SECPATCH);

    public static List<SecPatchItem> getSecPatchList(Context context) {
        if (context == null) {
            HwLog.w(TAG, "getSecPatchList: Invalid context");
            return null;
        }
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(URI_SECPATCH, null, null, null, null);
            if (Utility.isNullOrEmptyCursor(cursor, true)) {
                HwLog.i(TAG, "getSecPatchList: No secptach in db");
                return null;
            }
            List<SecPatchItem> secpatchList = new ArrayList();
            while (cursor.moveToNext()) {
                SecPatchItem item = new SecPatchItem();
                item.parseFrom(cursor);
                secpatchList.add(item);
            }
            cursor.close();
            return secpatchList;
        } catch (Exception e) {
            if (cursor != null) {
                cursor.close();
            }
            HwLog.e(TAG, "getSecPatchList: Exception", e);
            return null;
        }
    }

    public static boolean addSecPatch(Context context, SecPatchItem secpatch) {
        if (context == null) {
            HwLog.w(TAG, "addSecPatch: Invalid context");
            return false;
        }
        try {
            Uri uri = context.getContentResolver().insert(URI_SECPATCH, secpatch.getAsContentValues());
            if (uri != null && URI_SECPATCH != uri) {
                return true;
            }
            HwLog.w(TAG, "addSecPatch: Fails");
            return false;
        } catch (Exception e) {
            HwLog.e(TAG, "addSecPatch: Exception", e);
            return false;
        }
    }

    public static int addSecPatch(Context context, List<SecPatchItem> secpatchList, String updateStatus) {
        if (context == null || Utility.isNullOrEmptyList(secpatchList)) {
            HwLog.w(TAG, "addSecPatch: Invalid params");
            return 0;
        }
        try {
            int count = secpatchList.size();
            ContentValues[] values = new ContentValues[count];
            for (int nIndex = 0; nIndex < count; nIndex++) {
                values[nIndex] = ((SecPatchItem) secpatchList.get(nIndex)).getAsContentValues(updateStatus);
            }
            int nInsertCount = context.getContentResolver().bulkInsert(URI_SECPATCH, values);
            HwLog.d(TAG, "addSecPatch: secpatch count = " + count + ", inserted count = " + nInsertCount);
            return nInsertCount;
        } catch (Exception e) {
            HwLog.e(TAG, "addSecPatch: Exception", e);
            return 0;
        }
    }

    public static boolean updateSecPatch(Context context, SecPatchItem secpatch) {
        boolean z = true;
        if (context == null) {
            HwLog.w(TAG, "updateSecPatch: Invalid context");
            return false;
        }
        try {
            ContentValues value = secpatch.getAsContentValues();
            value.remove("sid");
            int nCount = context.getContentResolver().update(URI_SECPATCH, value, "sid = ?", new String[]{secpatch.mSid});
            HwLog.d(TAG, "updateSecPatch: Updated count = " + nCount);
            if (nCount <= 0) {
                z = false;
            }
            return z;
        } catch (Exception e) {
            HwLog.e(TAG, "updateSecPatch: Exception", e);
            return false;
        }
    }

    public static boolean deleteSecPatch(Context context, List<String> fixedVersioNameList, String updateStatus) {
        if (context == null || Utility.isNullOrEmptyList(fixedVersioNameList)) {
            HwLog.w(TAG, "deleteSecPatch: Invalid params");
            return false;
        }
        try {
            for (String fixedVersioName : fixedVersioNameList) {
                context.getContentResolver().delete(URI_SECPATCH, "fix_version = ? AND updated = ? ", new String[]{fixedVersioName, updateStatus});
            }
            return true;
        } catch (Exception e) {
            HwLog.e(TAG, "deleteSecPatch: Exception", e);
            return false;
        }
    }

    public static boolean deleteSecPatchByStatus(Context context, String updateStatus) {
        boolean z = true;
        if (context == null || TextUtils.isEmpty(updateStatus)) {
            HwLog.w(TAG, "deleteSecPatchByStatus: Invalid params");
            return false;
        }
        try {
            int nCount = context.getContentResolver().delete(URI_SECPATCH, "updated = ?", new String[]{updateStatus});
            HwLog.d(TAG, "deleteSecPatchByStatus: Deleted count = " + nCount + ", sid = " + updateStatus);
            if (nCount <= 0) {
                z = false;
            }
            return z;
        } catch (Exception e) {
            HwLog.e(TAG, "deleteSecPatchByStatus: Exception", e);
            return false;
        }
    }

    public static boolean deleteAllSecPatch(Context context) {
        boolean z = false;
        if (context == null) {
            HwLog.w(TAG, "deleteAllSecPatch: Invalid context");
            return false;
        }
        try {
            int nCount = ProviderUtils.deleteAll(context, URI_SECPATCH);
            HwLog.d(TAG, "deleteAllSecPatch: Deleted count = " + nCount);
            if (nCount > 0) {
                z = true;
            }
            return z;
        } catch (Exception e) {
            HwLog.e(TAG, "deleteAllSecPatch: Exception", e);
            return false;
        }
    }

    public static Cursor queryDBbyGivenCondition(Context context, String[] projection, String selection) {
        if (context == null) {
            HwLog.w(TAG, "queryDBbyGivenCondition: Invalid context");
            return null;
        }
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(URI_SEARCH_VIEW, projection, selection, null, null);
        } catch (Exception e) {
            HwLog.e(TAG, "queryDBbyGivenCondition: Exception", e);
        }
        return cursor;
    }

    public static List<String> getNeedUpdateVersionList(Context context) {
        List<String> toUpdateList = new ArrayList();
        if (context == null) {
            HwLog.w(TAG, "getNeedUpdateVersionList: Invalid context");
            return toUpdateList;
        }
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(URI_SYSTEM_VERSION_UPDATE, null, null, null, null);
            if (Utility.isNullOrEmptyCursor(cursor, true)) {
                HwLog.i(TAG, "getNeedUpdateVersionList: No secptach in db");
                return toUpdateList;
            }
            int nColIndexPver = cursor.getColumnIndex("pver");
            while (cursor.moveToNext()) {
                toUpdateList.add(cursor.getString(nColIndexPver));
            }
            cursor.close();
            return toUpdateList;
        } catch (Exception e) {
            if (cursor != null) {
                cursor.close();
            }
            HwLog.e(TAG, "getNeedUpdateVersionList: Exception", e);
            return toUpdateList;
        }
    }

    public static boolean addOneTobeUpdateVersion(Context context, String versionName) {
        if (context == null) {
            HwLog.w(TAG, "addOneTobeUpdateVersion: Invalid context");
            return false;
        }
        try {
            ContentValues values = new ContentValues();
            values.put("pver", versionName);
            Uri uri = context.getContentResolver().insert(URI_SYSTEM_VERSION_UPDATE, values);
            if (uri != null && URI_SECPATCH != uri) {
                return true;
            }
            HwLog.w(TAG, "addOneTobeUpdateVersion: Fails");
            return false;
        } catch (Exception e) {
            HwLog.e(TAG, "addOneTobeUpdateVersion: Exception", e);
            return false;
        }
    }

    public static int addMoreTobeUpdateVersion(Context context, List<String> versionNames) {
        if (context == null || Utility.isNullOrEmptyList(versionNames)) {
            HwLog.w(TAG, "addMoreTobeUpdateVersion: Invalid params");
            return 0;
        }
        try {
            int count = versionNames.size();
            List<ContentValues> valuesList = new ArrayList();
            ContentValues contentValue = new ContentValues();
            for (String versionName : versionNames) {
                contentValue.put("pver", versionName);
                valuesList.add(new ContentValues(contentValue));
                contentValue.clear();
            }
            if (valuesList.size() > 0) {
                int nInsertCount = context.getContentResolver().bulkInsert(URI_SYSTEM_VERSION_UPDATE, (ContentValues[]) valuesList.toArray(new ContentValues[valuesList.size()]));
                HwLog.d(TAG, "addMoreTobeUpdateVersion: versionNames count = " + count + ", inserted count = " + nInsertCount);
                return nInsertCount;
            }
        } catch (Exception e) {
            HwLog.e(TAG, "addMoreTobeUpdateVersion: Exception", e);
        }
        return 0;
    }

    public static boolean deleteVersionList(Context context, List<String> versionNameList) {
        if (context == null || Utility.isNullOrEmptyList(versionNameList)) {
            HwLog.w(TAG, "deleteOneVersion: Invalid params");
            return false;
        }
        try {
            for (String versionName : versionNameList) {
                context.getContentResolver().delete(URI_SYSTEM_VERSION_UPDATE, "pver = ?", new String[]{versionName});
            }
            return true;
        } catch (Exception e) {
            HwLog.e(TAG, "deleteOneVersion: Exception", e);
            return false;
        }
    }

    public static boolean deleteAllVersionList(Context context) {
        boolean z = false;
        if (context == null) {
            HwLog.w(TAG, "deleteAllVersionList: Invalid context");
            return false;
        }
        try {
            int nCount = ProviderUtils.deleteAll(context, URI_SYSTEM_VERSION_UPDATE);
            HwLog.d(TAG, "deleteAllVersionList: Deleted count = " + nCount);
            if (nCount > 0) {
                z = true;
            }
            return z;
        } catch (Exception e) {
            HwLog.e(TAG, "deleteAllVersionList: Exception", e);
            return false;
        }
    }

    public static boolean deleteFixedVersionListByGiven(Context context, List<SecPatchItem> fixedPatchList) {
        if (context == null || Utility.isNullOrEmptyList(fixedPatchList)) {
            HwLog.w(TAG, "deleteAllVersionList: Invalid context");
            return false;
        }
        List<String> fixedVersionNameList = new ArrayList();
        for (SecPatchItem item : fixedPatchList) {
            if (!fixedVersionNameList.contains(item.mFixed_version)) {
                fixedVersionNameList.add(item.mFixed_version);
            }
        }
        fixedVersionNameList.retainAll(getNeedUpdateVersionList(context));
        deleteSecPatch(context, fixedVersionNameList, "false");
        return deleteVersionList(context, fixedVersionNameList);
    }
}
