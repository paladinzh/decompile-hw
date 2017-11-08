package com.huawei.systemmanager.power.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import com.huawei.systemmanager.power.provider.SmartProvider.WakeUp_Columns;
import com.huawei.systemmanager.power.util.AppRangeWrapper;
import com.huawei.systemmanager.power.util.SavingSettingUtil;
import com.huawei.systemmanager.power.util.SysCoreUtils;
import com.huawei.systemmanager.util.HwLog;
import java.util.HashMap;
import java.util.Set;

public class ProviderWrapper {
    private static final String TAG = ProviderWrapper.class.getSimpleName();

    public static void insertWakeUpDB(Context ctx, String pkg, int num_all, int num_h) {
        ContentValues values = new ContentValues();
        values.put("pkgname", pkg);
        values.put(WakeUp_Columns.WAKEUPNUM_ALL, Integer.valueOf(num_all));
        values.put(WakeUp_Columns.WAKEUPNUM_H, Integer.valueOf(num_h));
        try {
            SavingSettingUtil.insertWakeUpApp(ctx.getContentResolver(), pkg, values);
        } catch (SQLiteConstraintException e) {
            SavingSettingUtil.setWakeUpApp(ctx.getContentResolver(), pkg, values);
        } catch (SQLException e2) {
            e2.printStackTrace();
        }
        values.clear();
    }

    public static void updateWakeupNumDB(Context ctx) {
        Set<Integer> thirdUids = AppRangeWrapper.getRunningThirdUidSet(ctx);
        ContentValues values = new ContentValues();
        HashMap<String, Integer> appList = SavingSettingUtil.databaseQuery(ctx.getContentResolver());
        HwLog.d(TAG, "updateWakeupNumDB appList = " + appList);
        if (!appList.isEmpty()) {
            for (Integer uid : thirdUids) {
                String[] mPkgName = ctx.getPackageManager().getPackagesForUid(uid.intValue());
                if (mPkgName != null) {
                    for (int i = 0; i < mPkgName.length; i++) {
                        int newnum = SysCoreUtils.getAppWakeUpNum(ctx, mPkgName[i]);
                        try {
                            if (appList.containsKey(mPkgName[i])) {
                                int oldnum = ((Integer) appList.get(mPkgName[i])).intValue();
                                if (newnum - oldnum < 0) {
                                    oldnum = newnum;
                                }
                                values.put("pkgname", mPkgName[i]);
                                values.put(WakeUp_Columns.WAKEUPNUM_ALL, Integer.valueOf(newnum));
                                values.put(WakeUp_Columns.WAKEUPNUM_H, Integer.valueOf(newnum - oldnum));
                                HwLog.d(TAG, "updateWakeupNumDB PkgName = " + mPkgName[i] + "###newnum = " + newnum + "###oldnum = " + oldnum);
                                SavingSettingUtil.setWakeUpApp(ctx.getContentResolver(), mPkgName[i], values);
                                values.clear();
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public static void updateWakeupNumDBSingle(Context ctx, String pkg) {
        HashMap<String, Integer> appList = SavingSettingUtil.databaseQuery(ctx.getContentResolver());
        if (!appList.isEmpty()) {
            int newnum = SysCoreUtils.getAppWakeUpNum(ctx, pkg);
            ContentValues values = new ContentValues();
            try {
                if (appList.containsKey(pkg)) {
                    int oldnum = ((Integer) appList.get(pkg)).intValue();
                    int addnum = 0;
                    if (newnum - oldnum > 0) {
                        addnum = newnum - oldnum;
                    }
                    values.put("pkgname", pkg);
                    values.put(WakeUp_Columns.WAKEUPNUM_ALL, Integer.valueOf(newnum));
                    values.put(WakeUp_Columns.WAKEUPNUM_H, Integer.valueOf(addnum));
                    HwLog.d(TAG, "updateWakeupNumDB PkgName = " + pkg + "###newnum = " + newnum + "###oldnum = " + oldnum);
                    SavingSettingUtil.setWakeUpApp(ctx.getContentResolver(), pkg, values);
                    values.clear();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
