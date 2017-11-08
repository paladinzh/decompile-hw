package com.huawei.systemmanager.widget;

import android.content.Context;
import android.text.format.Formatter;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.optimize.MemoryManager;
import com.huawei.systemmanager.optimize.trimmer.ProcessTrimer;
import com.huawei.systemmanager.optimize.trimmer.TrimParam;
import com.huawei.systemmanager.optimize.trimmer.TrimResult;
import com.huawei.systemmanager.power.model.PowerManagementModel;
import com.huawei.systemmanager.util.HwLog;

public class WidgetCleanManager {
    private static final int MIN_APPS_NUM = 1;
    public static final String TAG = "WidgetCleanManager";

    public static int doOneKeyCleanTask(Context context, boolean keepForegound) {
        return doOneKeyCleanTask(TrimParam.createOnekeycleanParam(context, keepForegound));
    }

    public static int doOneKeyCleanTask(TrimParam param) {
        TrimResult result = new ProcessTrimer().doTrim(param);
        return Math.max(result.getRemovePkgNum(), result.getForcestopPkgNum());
    }

    public static int getBattery(int batteryLevel, Context context) {
        try {
            return (int) Double.parseDouble(PowerManagementModel.getInstance(context).load().getTimeByCurrentBattery(context, batteryLevel).get(PowerManagementModel.BENEFIT_POWER_APPS_KEY).toString());
        } catch (Exception e) {
            HwLog.e(TAG, "PowerManagementModel init failed! ");
            return 0;
        }
    }

    public static String getToastMessage(int resAppNum, long resMem, int resBattery, int usedPercent, Context context) {
        resAppNum = filteToastMessage(resAppNum, "appNum");
        resMem = filteToastMessage(resMem, "memory");
        resBattery = filteToastMessage(resBattery, "battery");
        if (resAppNum >= 1 && resMem <= 0) {
            return context.getResources().getQuantityString(R.plurals.widget_clean_message_clean_app_toast, resAppNum, new Object[]{Integer.valueOf(resAppNum)});
        } else if ((resAppNum == 0 || resMem == 0) && resBattery == 0) {
            return context.getString(R.string.widget_clean_message_optimal_new_toast);
        } else {
            if (resAppNum == 0 || resMem == 0) {
                return context.getResources().getQuantityString(R.plurals.widget_clean_message_save_time_new_toast, resBattery, new Object[]{Integer.valueOf(resBattery)});
            } else if (resBattery == 0) {
                return context.getResources().getQuantityString(R.plurals.widget_clean_message_kill_proc_new_toast, resAppNum, new Object[]{Integer.valueOf(resAppNum), Formatter.formatFileSize(context, resMem)});
            } else {
                String saveTime = context.getResources().getQuantityString(R.plurals.widget_clean_message_save_time_new_toast, resBattery, new Object[]{Integer.valueOf(resBattery)});
                return context.getResources().getQuantityString(R.plurals.widget_clean_message_optimal_all_Toast, resAppNum, new Object[]{Integer.valueOf(resAppNum), Formatter.formatFileSize(context, resMem), saveTime});
            }
        }
    }

    private static int filteToastMessage(int value, String string) {
        if (value >= 0) {
            return value;
        }
        HwLog.w(TAG, "the value of " + string + " = " + value);
        return 0;
    }

    private static long filteToastMessage(long value, String string) {
        if (value >= 0) {
            return value;
        }
        HwLog.w(TAG, "the value of " + string + " = " + value);
        return 0;
    }

    public static long getMemoryAvailSize(Context context) {
        return MemoryManager.getMemoryInfo(context).getFree();
    }

    protected static long getMemoryTotalSize(Context context) {
        return MemoryManager.getMemoryInfo(context).getTotal();
    }
}
