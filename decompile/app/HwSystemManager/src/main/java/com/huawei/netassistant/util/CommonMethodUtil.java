package com.huawei.netassistant.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkTemplate;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.widget.ArrayAdapter;
import com.huawei.netassistant.cardmanager.SimCardManager;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.util.HSMConst;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.PackageManagerWrapper;
import java.text.NumberFormat;

public class CommonMethodUtil {
    private static final String TAG = "CommonMethodUtil";

    public static String formatBytes(Context ctx, long size) {
        if (size < 0) {
            return ctx.getString(R.string.pref_not_set);
        }
        return Formatter.formatFileSize(ctx, size);
    }

    public static String formatShortBytes(Context ctx, long size) {
        return Formatter.formatShortFileSize(ctx, size);
    }

    public static long unitConvert(float converNum, CharSequence selected) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(GlobalContext.getContext(), R.array.size_unit_no_kb, 17367048);
        long flowInBytes;
        if (TextUtils.equals(selected, (CharSequence) adapter.getItem(0))) {
            flowInBytes = (long) (1048576.0f * converNum);
            HwLog.d(TAG, "MB and setValue is" + flowInBytes);
            return flowInBytes;
        } else if (!TextUtils.equals(selected, (CharSequence) adapter.getItem(1))) {
            return 0;
        } else {
            flowInBytes = (long) (1.07374182E9f * converNum);
            HwLog.d(TAG, "GB and setValue is" + flowInBytes);
            return flowInBytes;
        }
    }

    public static long convertFloatMb2Byte(float numInMb) {
        return (long) ((numInMb * 1024.0f) * 1024.0f);
    }

    public static NetworkTemplate getTemplateAutomatically() {
        String dataUsedImsi = SimCardManager.getInstance().getPreferredDataSubscriberId();
        if (TextUtils.isEmpty(dataUsedImsi)) {
            return NetworkTemplate.buildTemplateWifiWildcard();
        }
        return NetworkTemplate.buildTemplateMobileAll(dataUsedImsi);
    }

    public static NetworkTemplate getTemplateMobileAutomatically() {
        String dataUsedImsi = SimCardManager.getInstance().getPreferredDataSubscriberId();
        if (!TextUtils.isEmpty(dataUsedImsi)) {
            return NetworkTemplate.buildTemplateMobileAll(dataUsedImsi);
        }
        HwLog.e(TAG, "/getTemplateMobileAutomatically: no SIM card using data");
        return null;
    }

    public static String getPackageNameByUid(int uid) {
        try {
            String[] packageNameList = GlobalContext.getContext().getPackageManager().getPackagesForUid(uid);
            if (packageNameList != null) {
                return packageNameList[0];
            }
            HwLog.e(TAG, "No package name to uid:" + uid);
            return null;
        } catch (Exception ex) {
            HwLog.e(TAG, ex.getMessage());
            return null;
        }
    }

    public static PackageInfo getPackageInfoByPackageName(String pkgName) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = PackageManagerWrapper.getPackageInfo(GlobalContext.getContext().getPackageManager(), pkgName, 4096);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return packageInfo;
    }

    public static void toggleGprs(boolean isEnable) {
        try {
            ((TelephonyManager) GlobalContext.getContext().getSystemService("phone")).setDataEnabled(isEnable);
        } catch (NullPointerException e) {
            HwLog.e(TAG, "Phone module did not start!");
        }
    }

    public static long convertMBToByte(float packages) {
        return (long) ((packages * 1024.0f) * 1024.0f);
    }

    public static float convertByteToMB(long value) {
        return ((float) value) / 1048576.0f;
    }

    public static float convertByteToGB(long value) {
        return ((float) value) / 1.07374182E9f;
    }

    public static boolean checkNetWorkStatus(Context context) {
        NetworkInfo netinfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
        if (netinfo != null && netinfo.isConnected()) {
            HwLog.i("NetStatus", "The net was connected");
        }
        if (netinfo == null || !netinfo.isAvailable()) {
            HwLog.i("NetStatus", "The net was bad!");
            return false;
        }
        HwLog.i("NetStatus", "The net was available");
        return true;
    }

    public static String[] formatDivideFileSize(Context context, long number, boolean shorter) {
        if (context == null) {
            return new String[]{"", ""};
        }
        String value;
        float result = (((float) number) / 1024.0f) / 1024.0f;
        int suffix = 17039499;
        if (result > 900.0f) {
            suffix = 17039500;
            result /= 1024.0f;
        }
        if (result < Utility.ALPHA_MAX) {
            value = String.format("%.2f", new Object[]{Float.valueOf(result)});
        } else if (result < HSMConst.DEVICE_SIZE_100) {
            if (shorter) {
                value = String.format("%.1f", new Object[]{Float.valueOf(result)});
            } else {
                value = String.format("%.2f", new Object[]{Float.valueOf(result)});
            }
        } else if (result >= 100.0f) {
            value = String.format("%.0f", new Object[]{Float.valueOf(result)});
        } else if (shorter) {
            value = String.format("%.0f", new Object[]{Float.valueOf(result)});
        } else {
            value = String.format("%.2f", new Object[]{Float.valueOf(result)});
        }
        String resValue = String.valueOf(value);
        String resSuffix = context.getResources().getString(suffix);
        return new String[]{resValue, resSuffix};
    }

    public static String formatPercentString(int percent) {
        NumberFormat pnf = NumberFormat.getPercentInstance();
        double pValue = ((double) percent) / 100.0d;
        pnf.setMinimumFractionDigits(0);
        return pnf.format(pValue);
    }
}
