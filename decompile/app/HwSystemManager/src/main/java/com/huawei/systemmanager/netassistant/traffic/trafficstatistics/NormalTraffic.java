package com.huawei.systemmanager.netassistant.traffic.trafficstatistics;

import android.content.Context;
import android.net.NetworkTemplate;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.text.format.Formatter;
import android.util.SparseArray;
import com.huawei.netassistant.analyse.TrafficAnalyseManager;
import com.huawei.netassistant.calculator.CalculateTrafficManager;
import com.huawei.netassistant.common.ParcelableAppItem;
import com.huawei.netassistant.db.NetAssistantDBManager;
import com.huawei.netassistant.util.CommonMethodUtil;
import com.huawei.netassistant.util.DateUtil;
import com.huawei.netassistant.util.ExternMethodUtil;
import com.huawei.netassistant.util.NotificationUtil;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.FileUtil;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.netassistant.traffic.netnotify.policy.INatTrafficNotifyBinder;
import com.huawei.systemmanager.netassistant.traffic.netnotify.policy.INatTrafficNotifyBinder.Stub;
import com.huawei.systemmanager.netassistant.traffic.netnotify.policy.NatTrafficNotifyService;
import com.huawei.systemmanager.netassistant.traffic.setting.ExtraTrafficSetting;
import com.huawei.systemmanager.netassistant.traffic.setting.NatSettingManager;
import com.huawei.systemmanager.netassistant.traffic.setting.NotifyPreference;
import com.huawei.systemmanager.netassistant.traffic.trafficcorrection.NetState;
import com.huawei.systemmanager.netassistant.traffic.trafficstatistics.ITrafficInfo.TrafficData;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.Collections;

public class NormalTraffic extends ITrafficInfo {
    private static final int OPERATION_DISABLE_NETWORK = 2;
    private static final int OPERATION_NOTHING = 3;
    private static final int OPERATION_NOTIFY = 1;
    private static final String TAG = NormalTraffic.class.getSimpleName();
    ExtraTrafficSetting mExtraTrafficSetting;
    NormalSetting mSetting;

    private class NormalSetting {
        private int notifyType;
        private long totalLimit;
        private long totalPackage;
        private long totalWarn;

        public NormalSetting(String imsi) {
        }

        public long getTotalLimit() {
            return this.totalLimit;
        }

        public long getTotalWarn() {
            return this.totalWarn;
        }

        public void prepare() {
            this.totalPackage = NetAssistantDBManager.getInstance().getSettingTotalPackage(NormalTraffic.this.mImsi);
            this.totalLimit = NatSettingManager.getLimitNotifyByte(NormalTraffic.this.mImsi);
            this.totalWarn = NatSettingManager.getMonthWarnNotifyByte(NormalTraffic.this.mImsi);
            this.notifyType = NetAssistantDBManager.getInstance().getSettingExcessMontyType(NormalTraffic.this.mImsi);
        }

        public long getTotalPackage() {
            return this.totalPackage;
        }
    }

    public NormalTraffic(String imsi, int month) {
        super(imsi, month);
    }

    public void updateBytes(long deltaByte) {
        this.trafficBytes += deltaByte;
        notifyUI();
        super.updateBytes(this.trafficBytes);
    }

    protected void notifyUI() {
        if (NetState.isCurrentMobileType()) {
            long totalLimit = this.mSetting.getTotalLimit();
            long totalWarn = this.mSetting.getTotalWarn();
            HwLog.i(TAG, "updateBytes total limit = " + totalLimit + " warn = " + totalWarn + " used = " + this.trafficBytes);
            NetworkTemplate template = CommonMethodUtil.getTemplateMobileAutomatically();
            if (template != null) {
                if (totalWarn < this.trafficBytes) {
                    notifyWarnOperation(template, totalWarn, this.mImsi);
                }
                if (totalLimit < this.trafficBytes) {
                    HwLog.i(TAG, "update notification type = " + this.mSetting.notifyType);
                    switch (this.mSetting.notifyType) {
                        case 1:
                            doOperationNotify(template, this.mImsi, totalLimit);
                            break;
                        case 2:
                            doOperationDisableNetwork(template, this.mImsi, totalLimit);
                            break;
                        case 3:
                            cancelNotificaiton(this.mImsi);
                            break;
                    }
                }
            }
        }
    }

    private void doOperationNotify(NetworkTemplate template, String imsi, long totalLimit) {
        if (NotifyPreference.getInstance().isLimitNotifyPreferenced()) {
            NotifyPreference.getInstance().setLimitNotifyPrefenced();
            notifyOperation(template, totalLimit, imsi);
            INatTrafficNotifyBinder notifyBinder = Stub.asInterface(ServiceManager.getService(NatTrafficNotifyService.TAG));
            HwLog.i(TAG, "notifyBinder = " + notifyBinder);
            if (notifyBinder != null) {
                try {
                    notifyBinder.notifyMonthLimit(imsi, template.getNetworkId(), totalLimit);
                    return;
                } catch (RemoteException e) {
                    e.printStackTrace();
                    return;
                }
            }
            return;
        }
        HwLog.e(TAG, "Month Limit Notification not create");
    }

    protected void onCreate() {
        super.onCreate();
        HwLog.d(TAG, "onCreate");
        this.mExtraTrafficSetting = new ExtraTrafficSetting(this.mImsi).get();
        this.mSetting = new NormalSetting(this.mImsi);
        this.mSetting.prepare();
    }

    public int getType() {
        return 301;
    }

    private void notifyOperation(NetworkTemplate template, long totalMonthBytes, String imsi) {
        long monthStartMills = DateUtil.monthStart(TrafficAnalyseManager.getInstance().getAccountDay(imsi));
        long currentTimeMills = DateUtil.getCurrentTimeMills();
        ArrayList<ParcelableAppItem> itemListAfterCheck = appCheckFilter(CalculateTrafficManager.getInstance().setAllUidSpendTraffic(template, monthStartMills, currentTimeMills), CalculateTrafficManager.getInstance().setAllUidSpendTrafficForCalc(NetworkTemplate.buildTemplateWifiWildcard(), monthStartMills, currentTimeMills), monthStartMills, currentTimeMills);
        if (itemListAfterCheck.size() >= 0) {
            Collections.sort(itemListAfterCheck);
            if (TrafficAnalyseManager.getInstance().isMainCard(imsi)) {
                NotificationUtil.sendNotification(totalMonthBytes, 0, itemListAfterCheck, 1074041823);
            } else {
                NotificationUtil.sendNotification(totalMonthBytes, 0, itemListAfterCheck, NotificationUtil.SECONDARY_CARD_CATEGORY_EXCESS_MONTH_LIMIT);
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
        }
        return itemListAfterCheck;
    }

    private void doOperationDisableNetwork(NetworkTemplate template, String prefferredImsi, long totalMonthBytes) {
        if (NotifyPreference.getInstance().isLimitNotifyPreferenced()) {
            NotifyPreference.getInstance().setLimitNotifyPrefenced();
            notifyOperation(template, totalMonthBytes, prefferredImsi);
            cancelNotificaiton(prefferredImsi);
            INatTrafficNotifyBinder notifyBinder = Stub.asInterface(ServiceManager.getService(NatTrafficNotifyService.TAG));
            HwLog.i(TAG, "notifyBinder = " + notifyBinder);
            if (notifyBinder != null) {
                try {
                    notifyBinder.notifyMonthLimit(prefferredImsi, template.getNetworkId(), totalMonthBytes);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            disableNetworkOperation();
            return;
        }
        HwLog.i(TAG, "Month Limit Notification not create");
    }

    private void cancelNotificaiton(String imsi) {
        if (TrafficAnalyseManager.getInstance().isMainCard(imsi)) {
            NotificationUtil.cancelNotification(1074041823);
        } else {
            NotificationUtil.cancelNotification(NotificationUtil.SECONDARY_CARD_CATEGORY_EXCESS_MONTH_LIMIT);
        }
    }

    private void disableNetworkOperation() {
        TrafficAnalyseManager.getInstance().getAnalyseHandler().removeMessages(14);
        TrafficAnalyseManager.getInstance().getAnalyseHandler().sendEmptyMessage(14);
        HwLog.v(TAG, "disableNetworkOperation success!");
    }

    private void notifyWarnOperation(NetworkTemplate template, long totalMonthBytes, String imsi) {
        long monthStartMills = DateUtil.monthStart(TrafficAnalyseManager.getInstance().getAccountDay(imsi));
        long currentTimeMills = DateUtil.getCurrentTimeMills();
        ArrayList<ParcelableAppItem> itemListAfterCheck = appCheckFilter(CalculateTrafficManager.getInstance().setAllUidSpendTraffic(template, monthStartMills, currentTimeMills), CalculateTrafficManager.getInstance().setAllUidSpendTrafficForCalc(NetworkTemplate.buildTemplateWifiWildcard(), monthStartMills, currentTimeMills), monthStartMills, currentTimeMills);
        if (itemListAfterCheck.size() >= 0) {
            Collections.sort(itemListAfterCheck);
            if (TrafficAnalyseManager.getInstance().isMainCard(imsi)) {
                NotificationUtil.sendNotification(totalMonthBytes, 0, itemListAfterCheck, NotificationUtil.MAIN_CARD_CATEGORY_EXCESS_MONTH_MARK);
            } else {
                NotificationUtil.sendNotification(totalMonthBytes, 0, itemListAfterCheck, NotificationUtil.SECONDARY_CARD_CATEGORY_EXCESS_MONTH_MARK);
            }
        }
    }

    public TrafficData getTrafficData() {
        Context context = GlobalContext.getContext();
        TrafficData td = new TrafficData();
        long usedTraffic = this.trafficBytes >= 0 ? this.trafficBytes : 0;
        HwLog.i(TAG, "total limit = " + this.mSetting.getTotalLimit() + " traffic bytes = " + usedTraffic);
        long restTraffic = this.mSetting.getTotalLimit() - usedTraffic;
        String[] restTrafficSizeStr;
        String[] usedTrafficSizeStr;
        if (restTraffic >= 0) {
            td.setTrafficUsedMessage(context.getString(R.string.net_assistant_normal_package_text, new Object[]{Formatter.formatFileSize(context, restTraffic), Formatter.formatFileSize(context, usedTraffic)}));
            restTrafficSizeStr = FileUtil.formatFileSizeByString(context, restTraffic);
            td.setTrafficLeftData(restTrafficSizeStr[0]);
            td.setTrafficLeftUnit(restTrafficSizeStr[1]);
            usedTrafficSizeStr = FileUtil.formatFileSizeByString(context, usedTraffic);
            td.setTrafficUsedData(usedTrafficSizeStr[0]);
            td.setTrafficUsedUnit(usedTrafficSizeStr[1]);
            td.setOverData(false);
        } else {
            td.setTrafficUsedMessage(context.getString(R.string.net_assistant_normal_package_over_text, new Object[]{Formatter.formatFileSize(context, -restTraffic), Formatter.formatFileSize(context, usedTraffic)}));
            restTrafficSizeStr = FileUtil.formatFileSizeByString(context, -restTraffic);
            td.setTrafficLeftData(restTrafficSizeStr[0]);
            td.setTrafficLeftUnit(restTrafficSizeStr[1]);
            usedTrafficSizeStr = FileUtil.formatFileSizeByString(context, usedTraffic);
            td.setTrafficUsedData(usedTrafficSizeStr[0]);
            td.setTrafficUsedUnit(usedTrafficSizeStr[1]);
            td.setOverData(true);
        }
        td.setTrafficStateMessage(context.getString(R.string.net_assistant_normal_package_message));
        td.setNormalUsedTraffic(usedTraffic);
        return td;
    }

    public long getLeftTraffic() {
        return this.mSetting.getTotalLimit() - getTraffic();
    }

    public long getTotalLimit() {
        return this.mSetting.getTotalLimit();
    }
}
