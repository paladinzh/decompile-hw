package com.huawei.netassistant.analyse;

import android.net.NetworkTemplate;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.text.TextUtils;
import android.util.SparseArray;
import com.huawei.netassistant.calculator.CalculateTrafficManager;
import com.huawei.netassistant.cardmanager.SimCardManager;
import com.huawei.netassistant.common.ParcelableAppItem;
import com.huawei.netassistant.util.CommonMethodUtil;
import com.huawei.netassistant.util.DateUtil;
import com.huawei.netassistant.util.ExternMethodUtil;
import com.huawei.netassistant.util.NotificationUtil;
import com.huawei.systemmanager.netassistant.traffic.netnotify.policy.INatTrafficNotifyBinder;
import com.huawei.systemmanager.netassistant.traffic.netnotify.policy.INatTrafficNotifyBinder.Stub;
import com.huawei.systemmanager.netassistant.traffic.netnotify.policy.NatTrafficNotifyService;
import com.huawei.systemmanager.netassistant.traffic.setting.NatSettingManager;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.Collections;

public class TrafficNotifyOverMarkForDay extends TrafficNotifyDecorator {
    private static final float DAY_MARK_RATE = 0.1f;
    private static final String TAG = "TrafficNotifyOverMarkForDay";
    private CalculateTrafficManager mCTM = CalculateTrafficManager.getInstance();
    private INatTrafficNotifyBinder mNotifyBinder;
    private long mTotalTodayBytes;

    public void notifyTraffic() {
        super.notifyTraffic();
        notifyTafficOverMarkForDay();
    }

    public void notifyTafficOverMarkForDay() {
        NetworkTemplate template = CommonMethodUtil.getTemplateMobileAutomatically();
        if (template != null) {
            String prefferredImsi = SimCardManager.getInstance().getPreferredDataSubscriberId();
            if (TextUtils.isEmpty(prefferredImsi)) {
                TrafficAnalyseManager.getInstance().cancelAllExcessNotification();
                HwLog.e(TAG, "notifyTafficOverMarkForDay: prefferredImsi is null.");
            } else if (TrafficAnalyseManager.getInstance().isPhoneSimCardStateNormal(prefferredImsi)) {
                long dayTrafficMark = getMarkRateOfDayLimit(prefferredImsi);
                if (dayTrafficMark < 0) {
                    HwLog.v(TAG, "notifyTafficOverMarkForDay: package not set.");
                    return;
                }
                this.mCTM = new CalculateTrafficManager();
                this.mTotalTodayBytes = this.mCTM.getTodaySpendTraffic(template);
                HwLog.v(TAG, "Day spend bytes:" + this.mTotalTodayBytes + ";Day traffic mark:" + dayTrafficMark);
                if (this.mTotalTodayBytes >= dayTrafficMark) {
                    notifyOperation(template, this.mTotalTodayBytes - dayTrafficMark, prefferredImsi);
                    this.mNotifyBinder = Stub.asInterface(ServiceManager.getService(NatTrafficNotifyService.TAG));
                    HwLog.i(TAG, "mNotifyBinder = " + this.mNotifyBinder);
                    try {
                        if (this.mNotifyBinder != null) {
                            this.mNotifyBinder.notifyDailyWarn(prefferredImsi, template.getNetworkId(), this.mTotalTodayBytes);
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                } else {
                    cancelNotification(prefferredImsi);
                }
            } else {
                cancelNotification(prefferredImsi);
                HwLog.e(TAG, "notifyTafficOverMarkForDay: phone sim state abnormal.");
            }
        }
    }

    private void cancelNotification(String imsi) {
        if (TrafficAnalyseManager.getInstance().isMainCard(imsi)) {
            NotificationUtil.cancelNotification(NotificationUtil.MAIN_CARD_CATEGORY_EXCESS_DAILY_MARK);
        } else {
            NotificationUtil.cancelNotification(NotificationUtil.SECONDARY_CARD_CATEGORY_EXCESS_DAILY_MARK);
        }
    }

    private long getMarkRateOfDayLimit(String imsi) {
        long setLimitByte = TrafficAnalyseManager.getInstance().getCardPackage(imsi);
        if (setLimitByte < 0) {
            return setLimitByte;
        }
        long dayMark = NatSettingManager.getDailyWarnNotifyByte(imsi);
        if (dayMark < 0) {
            dayMark = -1;
        }
        return dayMark;
    }

    private void notifyOperation(NetworkTemplate template, long dayExcessBytes, String imsi) {
        long dayStartMills = DateUtil.getDayStartTimeMills();
        long currentTimeMills = DateUtil.getCurrentTimeMills();
        ArrayList<ParcelableAppItem> itemListAfterCheck = appCheckFilter(this.mCTM.setAllUidSpendTraffic(template, dayStartMills, currentTimeMills), this.mCTM.setAllUidSpendTrafficForCalc(NetworkTemplate.buildTemplateWifiWildcard(), dayStartMills, currentTimeMills), dayStartMills, currentTimeMills);
        if (itemListAfterCheck.size() >= 0) {
            Collections.sort(itemListAfterCheck);
            if (TrafficAnalyseManager.getInstance().isMainCard(imsi)) {
                NotificationUtil.sendNotification(dayExcessBytes, 0, itemListAfterCheck, NotificationUtil.MAIN_CARD_CATEGORY_EXCESS_DAILY_MARK);
            } else {
                NotificationUtil.sendNotification(dayExcessBytes, 0, itemListAfterCheck, NotificationUtil.SECONDARY_CARD_CATEGORY_EXCESS_DAILY_MARK);
            }
        }
    }

    private ArrayList<ParcelableAppItem> appCheckFilter(ArrayList<ParcelableAppItem> itemList, SparseArray<ParcelableAppItem> wifiList, long start, long end) {
        ArrayList<ParcelableAppItem> itemListAfterCheck = new ArrayList();
        if (itemList == null || itemList.size() == 0) {
            return itemListAfterCheck;
        }
        for (ParcelableAppItem item : itemList) {
            if (!ExternMethodUtil.needCheckByUid(item.key)) {
                item.appType = 1;
            }
            ParcelableAppItem wifiItem = (ParcelableAppItem) wifiList.get(item.key);
            if (wifiItem != null) {
                item.wifitotal += wifiItem.wifitotal;
            }
            itemListAfterCheck.add(item);
            HwLog.v(TAG, "daily exceed:" + DateUtil.millisec2String(start) + "~" + DateUtil.millisec2String(end) + "; Uid:" + item.key + "; mobile total bytes:" + item.mobiletotal + "; wifi total bytes:" + item.wifitotal);
        }
        return itemListAfterCheck;
    }
}
