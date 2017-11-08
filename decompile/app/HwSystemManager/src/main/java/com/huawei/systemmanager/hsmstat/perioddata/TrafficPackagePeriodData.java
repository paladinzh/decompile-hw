package com.huawei.systemmanager.hsmstat.perioddata;

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.text.format.Formatter;
import com.google.android.collect.Lists;
import com.huawei.netassistant.cardmanager.SimCardManager;
import com.huawei.netassistant.common.PhoneSimCardInfo;
import com.huawei.netassistant.common.SimCardInfo;
import com.huawei.netassistant.service.INetAssistantService;
import com.huawei.netassistant.service.INetAssistantService.Stub;
import com.huawei.netassistant.service.NetAssistantService;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.NetWorkMgr;
import com.huawei.systemmanager.hsmstat.base.StatEntry;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;

public class TrafficPackagePeriodData implements IPeriodData {
    public String getSharePreferenceKey() {
        return NetWorkMgr.KEY_LAST_REPORT_TRAFFICT_PACKAGE;
    }

    public long getIntervalTime() {
        return 86400000;
    }

    public List<StatEntry> getRecordData(Context ctx) {
        List<StatEntry> result = Lists.newArrayList();
        PhoneSimCardInfo info = null;
        try {
            info = SimCardManager.getInstance().getPhoneSimCardInfo();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        if (info == null) {
            HwLog.e(HsmStatConst.TAG, "could not getPhoneSimCardInfo");
            return null;
        }
        IBinder b = ServiceManager.getService(NetAssistantService.NET_ASSISTANT);
        if (b == null) {
            HwLog.e(HsmStatConst.TAG, "could not get NetAssistantService binder");
            return null;
        }
        INetAssistantService mService = Stub.asInterface(b);
        if (mService == null) {
            HwLog.e(HsmStatConst.TAG, "could not get NetAssistantService");
            return null;
        }
        String key = NetWorkMgr.ACTION_TRAFFIC_PACKAGE;
        String value = "";
        String t1;
        String u1;
        switch (info.getPhoneCardState()) {
            case -1:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                SimCardInfo simCardInfo = SimCardManager.getInstance().getPreferredDataSimCardInfo();
                if (simCardInfo != null) {
                    try {
                        t1 = Formatter.formatFileSize(ctx, mService.getSettingTotalPackage(simCardInfo.getImsiNumber()));
                        u1 = Formatter.formatFileSize(ctx, mService.getMonthMobileTotalBytes(simCardInfo.getImsiNumber()));
                        value = HsmStatConst.constructValue(NetWorkMgr.KEY_CARD1_TOTAL, t1, NetWorkMgr.KEY_CARD1_USED, u1);
                        break;
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        break;
                    } catch (Exception e2) {
                        e2.printStackTrace();
                        break;
                    }
                }
                break;
            case 6:
                String imsi1 = SimCardManager.getInstance().getSubscriberId(0);
                String imsi2 = SimCardManager.getInstance().getSubscriberId(1);
                try {
                    t1 = Formatter.formatFileSize(ctx, mService.getSettingTotalPackage(imsi1));
                    u1 = Formatter.formatFileSize(ctx, mService.getMonthMobileTotalBytes(imsi1));
                    String t2 = Formatter.formatFileSize(ctx, mService.getSettingTotalPackage(imsi2));
                    String u2 = Formatter.formatFileSize(ctx, mService.getMonthMobileTotalBytes(imsi2));
                    value = HsmStatConst.constructValue(NetWorkMgr.KEY_CARD1_TOTAL, t1, NetWorkMgr.KEY_CARD1_USED, u1, NetWorkMgr.KEY_CARD2_TOTAL, t2, NetWorkMgr.KEY_CARD2_USED, u2);
                    break;
                } catch (RemoteException e3) {
                    e3.printStackTrace();
                    break;
                } catch (Exception e22) {
                    e22.printStackTrace();
                    break;
                }
            default:
                return null;
        }
        result.add(new StatEntry(key, value));
        return result;
    }
}
