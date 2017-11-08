package com.huawei.systemmanager.spacecleanner.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.spacecleanner.db.SpaceCleannerStore.ProtectTable.Columns;
import com.huawei.systemmanager.spacecleanner.db.SpaceCleannerStore.TrashInfoTable;
import com.huawei.systemmanager.spacecleanner.engine.hwscanner.custom.HwCustTrashInfo;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;

public class SpaceCleannerDBManager {
    private static String TAG = "SpaceCleannerDBManager";
    private static final Object sMutexNetAssistantDBManager = new Object();
    private static SpaceCleannerDBManager sSingleton;
    private Context mContext = GlobalContext.getContext();
    SpaceCleannerHelper mDBHelper = new SpaceCleannerHelper(this.mContext);

    private SpaceCleannerDBManager() {
    }

    public static SpaceCleannerDBManager getInstance() {
        SpaceCleannerDBManager spaceCleannerDBManager;
        synchronized (sMutexNetAssistantDBManager) {
            if (sSingleton == null) {
                sSingleton = new SpaceCleannerDBManager();
            }
            spaceCleannerDBManager = sSingleton;
        }
        return spaceCleannerDBManager;
    }

    public static void destroyInstance() {
        synchronized (sMutexNetAssistantDBManager) {
            sSingleton = null;
        }
    }

    public List<String> getProtectPaths() {
        List<String> list = Lists.newArrayList();
        Cursor cursor = null;
        try {
            cursor = this.mDBHelper.query(Boolean.valueOf(true), SpaceCleannerStore.TABLE_NAME_PROTECT_INFO, SpaceCleannerStore.getProtectPathColumns(), null, null, null, null, null, null);
            if (cursor == null) {
                HwLog.e(TAG, "getProtectPaths: Fail to get protect path");
                if (cursor != null) {
                    cursor.close();
                }
                return list;
            }
            while (cursor.moveToNext()) {
                String path = cursor.getString(2);
                if (!TextUtils.isEmpty(path)) {
                    list.add(path);
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            return list;
        } catch (Exception e) {
            HwLog.e(TAG, "getProtectPaths: msg is" + e.getMessage());
            e.printStackTrace();
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void setProtectPaths(String pkg, List<String> paths) {
        if (!TextUtils.isEmpty(pkg) && paths != null && paths.size() >= 1) {
            deleteProtectPath(pkg);
            for (String path : paths) {
                setProtectPath(pkg, path);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void setProtectPath(String pkg, String path) {
        if (!TextUtils.isEmpty(pkg) && !TextUtils.isEmpty(path) && !isProtectPathExist(path)) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("pkgname", pkg);
            contentValues.put(Columns.PROTECT_PATH, path);
            try {
                this.mDBHelper.insert(SpaceCleannerStore.TABLE_NAME_PROTECT_INFO, "id", contentValues);
            } catch (Exception e) {
                HwLog.e(TAG, "setProtectPath: msg is" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private boolean isProtectPathExist(String path) {
        boolean result = false;
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        Cursor cursor = null;
        String[] columns = SpaceCleannerStore.getProtectPathColumns();
        StringBuilder where = new StringBuilder();
        where.append(columns[2]).append(" =? ");
        try {
            cursor = this.mDBHelper.query(Boolean.valueOf(true), SpaceCleannerStore.TABLE_NAME_PROTECT_INFO, columns, where.toString(), new String[]{path}, null, null, null, null);
            if (cursor == null) {
                if (cursor != null) {
                    cursor.close();
                }
                return false;
            }
            if (cursor.getCount() > 0) {
                result = true;
                HwLog.d(TAG, "/setsetProtectPath: current path exist.");
            }
            if (cursor != null) {
                cursor.close();
            }
            return result;
        } catch (Exception e) {
            HwLog.e(TAG, "isProtectPathExist: msg is" + e.getMessage());
            e.printStackTrace();
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private boolean isTrashInfoExist(HwCustTrashInfo trash) {
        boolean result = false;
        if (trash == null) {
            return false;
        }
        String rule = trash.getMatchRule();
        Cursor cursor = null;
        String[] columns = SpaceCleannerStore.getTrashInfoColumns();
        StringBuilder where = new StringBuilder();
        where.append(columns[1]).append(" =? ");
        where.append(" and ").append(columns[2]).append(" =? ");
        if (!TextUtils.isEmpty(rule)) {
            where.append(" and ").append(columns[5]).append(" =? ");
        }
        try {
            cursor = this.mDBHelper.query(Boolean.valueOf(true), SpaceCleannerStore.TABLE_NAME_HW_TRASH_INFO, columns, where.toString(), !TextUtils.isEmpty(rule) ? new String[]{trash.getPkgName(), trash.getTrashPath(), rule} : new String[]{trash.getPkgName(), trash.getTrashPath()}, null, null, null, null);
            if (cursor == null) {
                if (cursor != null) {
                    cursor.close();
                }
                return false;
            }
            if (cursor.getCount() > 0) {
                result = true;
                HwLog.d(TAG, "/isTrashInfoExist: current path exist.");
            }
            if (cursor != null) {
                cursor.close();
            }
            return result;
        } catch (Exception e) {
            HwLog.e(TAG, "isTrashInfoExist: msg is" + e.getMessage());
            e.printStackTrace();
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void deleteTrashInfo(HwCustTrashInfo trash) {
        if (trash != null) {
            String[] columns = SpaceCleannerStore.getTrashInfoColumns();
            StringBuilder where = new StringBuilder();
            where.append(columns[1]).append(" =? ");
            where.append(" and ").append(columns[2]).append(" =? ");
            try {
                this.mDBHelper.delete(SpaceCleannerStore.TABLE_NAME_HW_TRASH_INFO, where.toString(), new String[]{trash.getPkgName(), trash.getTrashPath()});
            } catch (Exception e) {
                HwLog.e(TAG, "isTrashInfoExist: msg is" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void deletePkgTrashInfo(String pkg) {
        if (TextUtils.isEmpty(pkg)) {
            HwLog.e(TAG, "deleteTrashInfo: pkg is null");
            return;
        }
        String[] columns = SpaceCleannerStore.getTrashInfoColumns();
        StringBuilder where = new StringBuilder();
        where.append(columns[1]).append(" =? ");
        try {
            this.mDBHelper.delete(SpaceCleannerStore.TABLE_NAME_HW_TRASH_INFO, where.toString(), new String[]{pkg});
        } catch (Exception e) {
            HwLog.e(TAG, "deletePkgTrashInfo: pkg is null");
            e.printStackTrace();
        }
    }

    private void setTrashInfo(HwCustTrashInfo trash) {
        if (trash == null) {
            HwLog.e(TAG, "setTrashInfo: trash info is null");
            return;
        }
        String pkg = trash.getPkgName();
        String path = trash.getTrashPath();
        int type = trash.getTrashType();
        boolean recommend = trash.getRecommend();
        String rule = trash.getMatchRule();
        int keepTime = trash.getKeepTime();
        int keepLatest = trash.getKeeplatest();
        if (TextUtils.isEmpty(pkg) || TextUtils.isEmpty(path)) {
            HwLog.e(TAG, "setTrashInfo: invalid trash info");
            return;
        }
        if (isTrashInfoExist(trash)) {
            deleteTrashInfo(trash);
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put("pkgname", pkg);
        contentValues.put(TrashInfoTable.Columns.TRASH_PATH, path);
        contentValues.put(TrashInfoTable.Columns.TRASH_TYPE, Integer.valueOf(type));
        if (recommend) {
            contentValues.put(TrashInfoTable.Columns.TRASH_RECOMMENDED, Integer.valueOf(1));
        } else {
            contentValues.put(TrashInfoTable.Columns.TRASH_RECOMMENDED, Integer.valueOf(2));
        }
        if (!TextUtils.isEmpty(rule)) {
            contentValues.put(TrashInfoTable.Columns.TRASH_RULE, rule);
        }
        if (keepTime > 0) {
            contentValues.put(TrashInfoTable.Columns.TRASH_KEEP_TIME, Integer.valueOf(keepTime));
        }
        if (keepLatest > 0) {
            contentValues.put(TrashInfoTable.Columns.TRASH_KEEP_LATEST, Integer.valueOf(keepLatest));
        }
        try {
            this.mDBHelper.insert(SpaceCleannerStore.TABLE_NAME_HW_TRASH_INFO, "id", contentValues);
        } catch (Exception e) {
            HwLog.e(TAG, "setTrashInfo: pkg is null");
            e.printStackTrace();
        }
    }

    public void setTrashInfos(String pkg, List<HwCustTrashInfo> trash) {
        if (TextUtils.isEmpty(pkg) || trash == null) {
            HwLog.e(TAG, "setTrashInfos: pkg or trash is null");
        } else if (1 > trash.size()) {
            HwLog.e(TAG, "setTrashInfos: trash szie < 1");
        } else {
            deletePkgTrashInfo(pkg);
            for (HwCustTrashInfo detail : trash) {
                setTrashInfo(detail);
            }
        }
    }

    public List<HwCustTrashInfo> getTrashInfo() {
        List<HwCustTrashInfo> list = Lists.newArrayList();
        Cursor cursor = null;
        try {
            cursor = this.mDBHelper.query(Boolean.valueOf(true), SpaceCleannerStore.TABLE_NAME_HW_TRASH_INFO, SpaceCleannerStore.getTrashInfoColumns(), null, null, null, null, null, null);
            if (cursor == null) {
                HwLog.e(TAG, "getProtectPaths: Fail to get protect path");
                if (cursor != null) {
                    cursor.close();
                }
                return list;
            }
            while (cursor.moveToNext()) {
                String pkg = cursor.getString(1);
                String path = cursor.getString(2);
                int type = cursor.getInt(3);
                boolean recommend = true;
                if (2 == cursor.getInt(4)) {
                    recommend = false;
                }
                String rule = cursor.getString(5);
                int keepTime = cursor.getInt(6);
                int keepLatest = cursor.getInt(7);
                if (!(TextUtils.isEmpty(pkg) || TextUtils.isEmpty(path))) {
                    list.add(new HwCustTrashInfo(pkg, path, type, recommend, rule, keepTime, keepLatest));
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            return list;
        } catch (Exception e) {
            HwLog.e(TAG, "getTrashInfo: pkg is null");
            e.printStackTrace();
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public List<String> getTrashPaths() {
        List<String> list = Lists.newArrayList();
        Cursor cursor = null;
        try {
            cursor = this.mDBHelper.query(Boolean.valueOf(true), SpaceCleannerStore.TABLE_NAME_HW_TRASH_INFO, SpaceCleannerStore.getTrashInfoColumns(), null, null, null, null, null, null);
            if (cursor == null) {
                HwLog.e(TAG, "getProtectPaths: Fail to get protect path");
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            }
            while (cursor.moveToNext()) {
                String pkg = cursor.getString(1);
                String path = cursor.getString(2);
                if (!(TextUtils.isEmpty(pkg) || TextUtils.isEmpty(path))) {
                    list.add(path);
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            return list;
        } catch (Exception e) {
            HwLog.e(TAG, "getTrashInfo: pkg is null");
            e.printStackTrace();
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void deleteProtectPath(String pkg) {
        if (TextUtils.isEmpty(pkg)) {
            HwLog.e(TAG, "deleteProtectPath: pkg is null");
            return;
        }
        String[] columns = SpaceCleannerStore.getProtectPathColumns();
        StringBuilder where = new StringBuilder();
        where.append(columns[1]).append(" =? ");
        try {
            this.mDBHelper.delete(SpaceCleannerStore.TABLE_NAME_PROTECT_INFO, where.toString(), new String[]{pkg});
        } catch (Exception e) {
            HwLog.e(TAG, "deleteProtectPath exception e: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
