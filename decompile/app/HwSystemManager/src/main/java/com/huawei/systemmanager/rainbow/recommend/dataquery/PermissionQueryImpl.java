package com.huawei.systemmanager.rainbow.recommend.dataquery;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import com.huawei.permissionmanager.db.DBHelper;
import com.huawei.systemmanager.comm.misc.CursorHelper;
import com.huawei.systemmanager.rainbow.comm.crossutil.cvtmeta.PermissionCvtProxy;
import com.huawei.systemmanager.rainbow.comm.meta.AbsConfigItem;
import com.huawei.systemmanager.rainbow.comm.meta.CloudMetaMgr;
import com.huawei.systemmanager.rainbow.recommend.base.ConfigurationItem;
import com.huawei.systemmanager.rainbow.recommend.base.DataConstructUtils;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;
import java.util.Map;

public class PermissionQueryImpl implements IConfigItemQuery {
    private static final String TAG = PermissionQueryImpl.class.getSimpleName();

    public Map<String, List<ConfigurationItem>> getConfigurationOfItems(Context ctx) {
        Map<String, List<ConfigurationItem>> result = DataConstructUtils.generateEmptyResult();
        convertPermissionData(ctx, result);
        return result;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void convertPermissionData(Context ctx, Map<String, List<ConfigurationItem>> result) {
        Cursor cursor = null;
        try {
            cursor = ctx.getContentResolver().query(DBHelper.BLOCK_TABLE_NAME_URI, new String[]{"packageName", "permissionCode", "permissionCfg"}, null, null, null, null);
            if (cursor == null || cursor.getCount() <= 0) {
                HwLog.w(TAG, "convertPermissionData maybe empty cursor!");
            } else {
                List<AbsConfigItem> itemList = CloudMetaMgr.getBusinessInstance(6).getConfigItemList();
                while (cursor.moveToNext()) {
                    convertSingleAppData(ctx, result, itemList, cursor.getString(0), cursor.getInt(1), cursor.getInt(2));
                }
            }
            CursorHelper.closeCursor(cursor);
        } catch (SQLiteException ex) {
            HwLog.e(TAG, "convertPermissionData catch SQLiteException: " + ex.getMessage());
            ex.printStackTrace();
        } catch (Exception ex2) {
            HwLog.e(TAG, "convertPermissionData catch Exception: " + ex2.getMessage());
            ex2.printStackTrace();
        } catch (Throwable th) {
            CursorHelper.closeCursor(cursor);
        }
    }

    private void convertSingleAppData(Context ctx, Map<String, List<ConfigurationItem>> result, List<AbsConfigItem> itemList, String pkgName, int code, int cfg) {
        DataConstructUtils.generateDefaultPackageItemList(result, pkgName);
        if (itemList != null) {
            for (AbsConfigItem item : itemList) {
                int itemId = item.getCfgItemId();
                if (PermissionCvtProxy.subOfPermissionSet(ctx, pkgName, itemId)) {
                    ((List) result.get(pkgName)).add(new ConfigurationItem(itemId, PermissionCvtProxy.getPermissionConfigType(ctx, itemId, code, cfg)));
                }
            }
        }
    }
}
