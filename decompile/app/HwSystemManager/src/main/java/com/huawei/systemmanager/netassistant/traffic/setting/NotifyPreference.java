package com.huawei.systemmanager.netassistant.traffic.setting;

import com.huawei.netassistant.cardmanager.SimCardManager;
import com.huawei.netassistant.db.NetAssistantDBManager;
import com.huawei.netassistant.util.DateUtil;

public class NotifyPreference {
    private static final String DAILY_WARN_NOTIFY_KEY = "key_daily_warn";
    private static final long DEFAULT_VALUE = -1;
    private static final String MONTH_LIMIT_NOTIFY_KEY = "key_month_limit";
    private static final String MONTH_WARN_NOTIFY_KEY = "key_month_warn";
    static final long SET_VALUE = 1;
    private static NotifyPreference sInstance;

    private NotifyPreference() {
    }

    public static synchronized NotifyPreference getInstance() {
        NotifyPreference notifyPreference;
        synchronized (NotifyPreference.class) {
            if (sInstance == null) {
                sInstance = new NotifyPreference();
            }
            notifyPreference = sInstance;
        }
        return notifyPreference;
    }

    public boolean isLimitNotifyPreferenced() {
        String preferImsi = SimCardManager.getInstance().getPreferredDataSubscriberId();
        return !DateUtil.isInThisMonth(NetAssistantDBManager.getInstance().getMonthLimitSnooze(preferImsi), DateUtil.getCycleDayFromDB(preferImsi));
    }

    public void setLimitNotifyPrefenced() {
        NetAssistantDBManager.getInstance().setMonthLimitSnooze(SimCardManager.getInstance().getPreferredDataSubscriberId(), DateUtil.getCurrentTimeMills());
    }

    public void clearMonthLimitPreference(String imsi) {
        NetAssistantDBManager.getInstance().setMonthLimitSnooze(imsi, -1);
    }

    public boolean shouldDailyWarnNotify() {
        return !DateUtil.isInThisDay(NetAssistantDBManager.getInstance().getDailyWarnSnooze(SimCardManager.getInstance().getPreferredDataSubscriberId()));
    }

    public void setDailyWarnPreference() {
        long currentTime = DateUtil.getCurrentTimeMills();
        NetAssistantDBManager.getInstance().setDailyWarnSnooze(SimCardManager.getInstance().getPreferredDataSubscriberId(), currentTime);
    }

    public void clearDailyWarnPreference(String imsi) {
        NetAssistantDBManager.getInstance().setDailyWarnSnooze(imsi, -1);
    }

    public boolean shouldMonthWarnNotify() {
        String preferImsi = SimCardManager.getInstance().getPreferredDataSubscriberId();
        return !DateUtil.isInThisMonth(NetAssistantDBManager.getInstance().getMonthWarnSnooze(preferImsi), DateUtil.getCycleDayFromDB(preferImsi));
    }

    public void setMonthWarnPreference() {
        long currentTime = DateUtil.getCurrentTimeMills();
        NetAssistantDBManager.getInstance().setMonthWarnSnooze(SimCardManager.getInstance().getPreferredDataSubscriberId(), currentTime);
    }

    public void clearMonthWarnPreference(String imsi) {
        NetAssistantDBManager.getInstance().setMonthWarnSnooze(imsi, -1);
    }
}
