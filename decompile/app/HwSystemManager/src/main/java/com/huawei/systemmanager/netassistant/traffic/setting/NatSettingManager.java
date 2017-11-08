package com.huawei.systemmanager.netassistant.traffic.setting;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.Uri;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.ArrayMap;
import com.google.common.collect.Lists;
import com.huawei.netassistant.db.NetAssistantDBManager;
import com.huawei.netassistant.db.NetAssistantStore.SettingTable.Columns;
import com.huawei.netassistant.db.NetAssistantStore.TrafficAdjustTable;
import com.huawei.netassistant.service.INetAssistantService;
import com.huawei.netassistant.service.INetAssistantService.Stub;
import com.huawei.netassistant.service.NetAssistantService;
import com.huawei.systemmanager.comm.misc.Closeables;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.netassistant.traffic.statusspeed.NatSettingInfo;
import com.huawei.systemmanager.util.HwLog;
import java.io.Closeable;
import java.util.List;

public class NatSettingManager {
    private static final String TAG = "NatSettingManager";
    public static final int TYPE_OPERATOR = 3;
    public static final int TYPE_PROVINCE_AND_CITY = 1;

    public static long getMonthWarnNotifyByte(String imsi) {
        long monthPercent = NetAssistantDBManager.getInstance().getMonthWarnByte(imsi);
        long monthLimit = getLimitNotifyByte(imsi);
        if (monthLimit < 0) {
            return -1;
        }
        return (monthLimit * monthPercent) / 100;
    }

    public static long getDailyWarnNotifyByte(String imsi) {
        long dailyPercent = NetAssistantDBManager.getInstance().getDailyWarnByte(imsi);
        long monthLimit = getLimitNotifyByte(imsi);
        if (monthLimit < 0) {
            return -1;
        }
        return (monthLimit * dailyPercent) / 100;
    }

    public static long getLimitNotifyByte(String imsi) {
        return NetAssistantDBManager.getInstance().getMonthLimitByte(imsi);
    }

    public static void setMonthLimitNotifyByte(String imsi, long byteValue) {
        NetAssistantDBManager.getInstance().setMonthLimitByte(imsi, byteValue);
        INetAssistantService service = getService();
        try {
            service.clearMonthLimitPreference(imsi);
            service.clearMonthWarnPreference(imsi);
            service.clearDailyWarnPreference(imsi);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private static INetAssistantService getService() {
        return Stub.asInterface(ServiceManager.getService(NetAssistantService.NET_ASSISTANT));
    }

    public static void setMonthWarnNotifyByte(String imsi, long byteValue) {
        NetAssistantDBManager.getInstance().setMonthWarnByte(imsi, byteValue);
    }

    public static void setDailyWarnNotifyByte(String imsi, long byteValue) {
        NetAssistantDBManager.getInstance().setDailyWarnByte(imsi, byteValue);
    }

    @TargetApi(19)
    public static void setPackageSetting(String imsi, long pkgValue, int startDay, String province, String city, String operator, String brand, boolean lockSwitch) {
        ArrayMap<String, Object> map = new ArrayMap();
        map.put("imsi", imsi);
        map.put(Columns.PACKAGE_TOTAL, Long.valueOf(pkgValue));
        map.put(Columns.BEGIN_DATE, Integer.valueOf(startDay));
        map.put(Columns.REGULAR_ADJUST_BEGIN_TIME, Integer.valueOf(0));
        map.put(Columns.EXCESS_MONTH_TYPE, Integer.valueOf(2));
        map.put(Columns.IS_OVERMARK_MONTH, Integer.valueOf(1));
        map.put(Columns.IS_OVERMARK_DAY, Integer.valueOf(1));
        long limitByte = pkgValue + new ExtraTrafficSetting(imsi).get().getPackage();
        String str = Columns.MONTH_LIMIT;
        if (limitByte < 0) {
            limitByte = 0;
        }
        map.put(str, Long.valueOf(limitByte));
        NetAssistantDBManager.getInstance().updateSettings(map);
        NetAssistantDBManager.getInstance().setProvinceInfo(imsi, province, city);
        NetAssistantDBManager.getInstance().setOperatorInfo(imsi, operator, brand);
        NatSettingInfo.setUnlockScreenNotify(GlobalContext.getContext(), lockSwitch);
    }

    public static boolean hasPackageSet(String imsi) {
        if (NetAssistantDBManager.getInstance().getSettingTotalPackage(imsi) >= 0) {
            return true;
        }
        return false;
    }

    public static List<String> getAdjustInfo(Context ctx, String imsi, int infoType) {
        List<String> result = Lists.newArrayList();
        Uri apUri = TrafficAdjustTable.getContentUri();
        String[] columns = null;
        switch (infoType) {
            case 1:
                columns = new String[]{TrafficAdjustTable.Columns.ADJUST_PROVINCE, TrafficAdjustTable.Columns.ADJUST_CITY};
                break;
            case 3:
                columns = new String[]{TrafficAdjustTable.Columns.ADJUST_PROVIDER, TrafficAdjustTable.Columns.ADJUST_BRAND};
                break;
            default:
                HwLog.e(TAG, "getAdjustInfo unknown type:" + infoType);
                break;
        }
        if (columns == null) {
            return result;
        }
        Closeable closeable = null;
        try {
            closeable = ctx.getContentResolver().query(apUri, columns, "imsi=" + imsi, null, null);
            int columnCount = closeable.getColumnCount();
            if (closeable.moveToNext()) {
                for (int i = 0; i < columnCount; i++) {
                    result.add(closeable.getString(i));
                }
                return result;
            }
            Closeables.close(closeable);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Closeables.close(closeable);
        }
    }

    public static void deletePackageSetting(String imsi) {
        NetAssistantDBManager.getInstance().deleteSettings(imsi);
    }
}
