package com.huawei.netassistant.service;

import android.net.NetworkTemplate;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import com.google.common.collect.Lists;
import com.huawei.netassistant.common.ParcelableDailyTrafficItem;
import com.huawei.netassistant.db.NetAssistantDBManager;
import com.huawei.netassistant.service.INetAssistantService.Stub;
import com.huawei.netassistant.util.DateUtil;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;

public class NetAssistantManager {
    private static final String TAG = NetAssistantManager.class.getSimpleName();

    public static INetAssistantService getService() {
        IBinder b = ServiceManager.getService(NetAssistantService.NET_ASSISTANT);
        if (b == null) {
            HwLog.e(TAG, "can't find the binder of net assistant service");
            return null;
        }
        INetAssistantService service = Stub.asInterface(b);
        if (service != null) {
            return service;
        }
        HwLog.e(TAG, "the netassistant service is null");
        return null;
    }

    public static List getMonthPerDayTraffic(NetworkTemplate template, int uid) {
        List<ParcelableDailyTrafficItem> list = null;
        try {
            list = getService().getMonthPerDayTraffic(template, uid);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NullPointerException e2) {
            e2.printStackTrace();
        }
        return list;
    }

    public static List getDayPerHourTraffic(NetworkTemplate template, int uid) {
        List<ParcelableDailyTrafficItem> list = null;
        try {
            list = getService().getDayPerHourTraffic(template, uid);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NullPointerException e2) {
            e2.printStackTrace();
        }
        return list;
    }

    public static long getForegroundBytes(NetworkTemplate template, int uid, long start, long end) {
        long bytes = 0;
        try {
            bytes = getService().getForeGroundBytesByUid(template, uid, start, end, DateUtil.getCurrentTimeMills());
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NullPointerException e2) {
            e2.printStackTrace();
        }
        return bytes;
    }

    public static long getBackgroundBytes(NetworkTemplate template, int uid, long start, long end) {
        long bytes = 0;
        try {
            bytes = getService().getBackGroundBytesByUid(template, uid, start, end, DateUtil.getCurrentTimeMills());
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NullPointerException e2) {
            e2.printStackTrace();
        }
        return bytes;
    }

    public static long getMonthlyTotalBytes(String imsi) {
        return NetAssistantDBManager.getInstance().getMonthLimitByte(imsi);
    }

    public static List<ParcelableDailyTrafficItem> getMonthTrafficDailyDetailList(String mImsi) {
        INetAssistantService service = getService();
        List<ParcelableDailyTrafficItem> list = Lists.newArrayList();
        try {
            list = service.getMonthTrafficDailyDetailList(mImsi);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NullPointerException e2) {
            e2.printStackTrace();
        }
        return list;
    }

    public static int getNetworkUsageDays(String imsi) {
        int days = 1;
        try {
            days = getService().getNetworkUsageDays(imsi);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NullPointerException e2) {
            e2.printStackTrace();
        }
        return days;
    }

    public static boolean setSettingRegularAdjustType(String imsi, int value) {
        boolean res = false;
        try {
            res = getService().setSettingRegularAdjustType(imsi, value);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NullPointerException e2) {
            e2.printStackTrace();
        }
        return res;
    }

    public static void sendAdjustSMS(String imsi) {
        INetAssistantService service = getService();
        if (service != null) {
            try {
                service.sendAdjustSMS(imsi);
            } catch (RemoteException e) {
                HwLog.e(TAG, e.getMessage());
            }
        }
    }
}
