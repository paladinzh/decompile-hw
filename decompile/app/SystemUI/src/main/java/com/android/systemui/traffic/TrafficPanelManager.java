package com.android.systemui.traffic;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.systemui.R;
import com.android.systemui.observer.ObserverItem.OnChangeListener;
import com.android.systemui.observer.SystemUIObserver;
import com.android.systemui.statusbar.phone.HwPhoneStatusBar;
import com.android.systemui.statusbar.policy.PowerModeController.CallBack;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.SystemUIThread;
import com.android.systemui.utils.SystemUIThread.SimpleAsyncTask;
import com.android.systemui.utils.SystemUiUtil;
import com.android.systemui.utils.analyze.BDReporter;
import com.huawei.netassistant.service.INetAssistantService;
import com.huawei.netassistant.service.INetAssistantService.Stub;
import java.util.regex.Pattern;

public class TrafficPanelManager implements CallBack {
    private static TrafficPanelManager mInstance = null;
    private boolean isMultiSimEnabled = false;
    private Context mContext = null;
    private int mCurrentTrafficCard = 0;
    private OnChangeListener mDefaultDataSubListener = new OnChangeListener() {
        public void onChange(Object value) {
            TrafficPanelManager.this.mCurrentTrafficCard = ((Integer) SystemUIObserver.get(15)).intValue();
        }
    };
    private INetAssistantService mNetAssistantService = null;
    private boolean mShowTraffic = false;
    private boolean mShowTrafficKeguard = true;
    private boolean mSuperPowerSave = false;
    private TelephonyManager mTelephonyManager = null;
    private OnChangeListener mTrafficDataShowListener = new OnChangeListener() {
        public void onChange(Object value) {
            TrafficPanelManager.this.mShowTraffic = ((Boolean) SystemUIObserver.get(14)).booleanValue();
            TrafficPanelManager.this.notifyShowChanged();
        }
    };
    private TrafficPanelChangeListener mTrafficPanelChangeListener = null;

    public static class TrafficInfo {
        boolean beyondToday;
        boolean isBeyondMeal;
        String residue;
        int subId;
        String today;
        String total;
        int trafficImageLevel;

        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("subId:").append(this.subId).append(",today:").append(this.today).append(",residue:").append(this.residue).append(",total:").append(this.total).append(",beyondToday").append(this.beyondToday).append(",isBeyondMeal").append(this.isBeyondMeal).append(",trafficImageLevel").append(this.trafficImageLevel);
            return sb.toString();
        }
    }

    public interface TrafficPanelChangeListener {
        void onNoMealSetted(int i);

        void onNoSims();

        void onShowStatusChange(boolean z);

        void refreshDataBySlot(TrafficInfo trafficInfo);
    }

    private static String[] getSettingColumns() {
        return new String[]{"id", "imsi", "daily_warn_byte", "package_total"};
    }

    private TrafficPanelManager() {
    }

    public static synchronized TrafficPanelManager getInstance() {
        TrafficPanelManager trafficPanelManager;
        synchronized (TrafficPanelManager.class) {
            if (mInstance == null) {
                mInstance = new TrafficPanelManager();
            }
            trafficPanelManager = mInstance;
        }
        return trafficPanelManager;
    }

    public void init(Context context) {
        HwLog.i("TrafficPanelManager", "init");
        this.mContext = context;
        this.isMultiSimEnabled = SystemUiUtil.isMulityCard(this.mContext);
        this.mCurrentTrafficCard = ((Integer) SystemUIObserver.get(15)).intValue();
        this.mShowTraffic = ((Boolean) SystemUIObserver.get(14)).booleanValue();
        this.mSuperPowerSave = HwPhoneStatusBar.getInstance().getPowerModeController().isSupperPowerSave();
        this.mTelephonyManager = TelephonyManager.from(this.mContext);
        registerContentObserver();
        notifyShowChanged();
    }

    public void destory() {
        HwLog.i("TrafficPanelManager", "destory");
        unregisterContentObserver();
    }

    private void registerContentObserver() {
        HwPhoneStatusBar.getInstance().getPowerModeController().register(this);
        SystemUIObserver.getObserver(15).addOnChangeListener(this.mDefaultDataSubListener);
        SystemUIObserver.getObserver(14).addOnChangeListener(this.mTrafficDataShowListener);
    }

    private void unregisterContentObserver() {
        HwPhoneStatusBar.getInstance().getPowerModeController().unRegister(this);
        SystemUIObserver.getObserver(15).removeOnChangeListener(this.mDefaultDataSubListener);
        SystemUIObserver.getObserver(14).removeOnChangeListener(this.mTrafficDataShowListener);
    }

    public void setTrafficPanelChangeListener(TrafficPanelChangeListener l) {
        this.mTrafficPanelChangeListener = l;
        notifyShowChanged();
    }

    public void onSupperPowerSaveChanged(boolean supperPowerSave) {
        HwLog.i("TrafficPanelManager", "onSupperPowerSaveChanged:" + supperPowerSave);
        this.mSuperPowerSave = supperPowerSave;
        notifyShowChanged();
    }

    public void onSaveChanged(boolean save) {
    }

    private void notifyShowChanged() {
        boolean z = false;
        if (this.mTrafficPanelChangeListener != null) {
            TrafficPanelChangeListener trafficPanelChangeListener = this.mTrafficPanelChangeListener;
            if (this.mShowTraffic && this.mShowTrafficKeguard && !this.mSuperPowerSave) {
                z = true;
            }
            trafficPanelChangeListener.onShowStatusChange(z);
        }
    }

    public void performOnClick() {
        if (SystemUiUtil.isOwner()) {
            BDReporter.c(this.mContext, 355);
            SystemUiUtil.startActivityDismissingKeyguard(this.mContext, new Intent("huawei.intent.action.HSM_NET_ASSISTANT_MAIN_ACTIVITY"));
        }
    }

    private void checkNotificationBinder() {
        if (this.mNetAssistantService == null) {
            try {
                IBinder b = ServiceManager.getService("com.huawei.netassistant.service.netassistantservice");
                if (b != null) {
                    this.mNetAssistantService = Stub.asInterface(b);
                    HwLog.i("TrafficPanelManager", "mNetAssistantService=" + this.mNetAssistantService);
                }
            } catch (Exception e) {
                HwLog.i("TrafficPanelManager", "getService exception: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public boolean isCardPresent(int slot) {
        int slotState = this.mTelephonyManager == null ? 0 : this.mTelephonyManager.getSimState(slot);
        if (slotState == 2 || slotState == 3 || slotState == 4 || slotState == 5) {
            return true;
        }
        return false;
    }

    private boolean isNoSimCard() {
        return (isCardPresent(0) || isCardPresent(1)) ? false : true;
    }

    public void refreshViewSimState(final int slot) {
        SystemUIThread.runAsync(new SimpleAsyncTask() {
            boolean hasSetMeal = true;
            boolean mIsNoSimCard = false;
            TrafficInfo trafficInfo = new TrafficInfo();

            public boolean runInThread() {
                this.mIsNoSimCard = TrafficPanelManager.this.isNoSimCard();
                if (this.mIsNoSimCard) {
                    HwLog.i("TrafficPanelManager", "refreshViewSimState mIsNoSimCard , show no sim inserted info.");
                    return true;
                }
                HwLog.i("TrafficPanelManager", "refreshViewSimState slot:" + slot + " mCurrentTrafficCard:" + TrafficPanelManager.this.mCurrentTrafficCard);
                if (slot != TrafficPanelManager.this.mCurrentTrafficCard) {
                    return false;
                }
                boolean beyondToday = false;
                long total = TrafficPanelManager.this.getMealTraffic(slot);
                if (total <= -1) {
                    this.hasSetMeal = false;
                    return true;
                }
                long today = TrafficPanelManager.this.getTodayTraffic(slot);
                long residue = TrafficPanelManager.this.getResidueTraffic(total, slot);
                boolean isBeyondMeal = false;
                if (residue < 0) {
                    residue = Math.abs(residue);
                    isBeyondMeal = true;
                }
                if (today > (total * TrafficPanelManager.this.getSettingColumnsLongInfo(slot, 2)) / 100) {
                    beyondToday = true;
                }
                int trafficImageLevel = TrafficPanelManager.this.getTrafficImageLevel(residue, total, beyondToday);
                String todayStr = SystemUiUtil.formatFileSize(TrafficPanelManager.this.mContext, today);
                String totalStr = SystemUiUtil.formatFileSize(TrafficPanelManager.this.mContext, total);
                String residueStr = SystemUiUtil.formatFileSize(TrafficPanelManager.this.mContext, residue);
                if (TrafficPanelManager.this.mContext == null) {
                    HwLog.e("TrafficPanelManager", "TrafficPanelManager$mContext = null");
                    return false;
                }
                this.trafficInfo.today = TrafficPanelManager.this.mContext.getString(R.string.traffic_today_new, new Object[]{todayStr});
                this.trafficInfo.total = TrafficPanelManager.this.mContext.getString(R.string.traffic_meal_mb_new, new Object[]{totalStr});
                this.trafficInfo.residue = TrafficPanelManager.this.mContext.getString(isBeyondMeal ? R.string.traffic_meal_beyond_new : R.string.traffic_rest_new, new Object[]{residueStr});
                this.trafficInfo.beyondToday = beyondToday;
                this.trafficInfo.isBeyondMeal = isBeyondMeal;
                this.trafficInfo.subId = slot;
                this.trafficInfo.trafficImageLevel = trafficImageLevel;
                return true;
            }

            public void runInUI() {
                if (TrafficPanelManager.this.mTrafficPanelChangeListener == null) {
                    HwLog.e("TrafficPanelManager", "refreshViewSimState mTrafficPanelChangeListener == null");
                } else if (this.mIsNoSimCard) {
                    TrafficPanelManager.this.mTrafficPanelChangeListener.onNoSims();
                } else if (this.hasSetMeal) {
                    TrafficPanelManager.this.mTrafficPanelChangeListener.refreshDataBySlot(this.trafficInfo);
                } else {
                    TrafficPanelManager.this.mTrafficPanelChangeListener.onNoMealSetted(slot);
                }
            }
        });
    }

    private int getTrafficImageLevel(long residue, long meal, boolean isBeyondMeal) {
        if (isBeyondMeal || meal <= 0) {
            return 1;
        }
        int t = (int) (residue / (meal / 8));
        if (Pattern.compile("^[1-8]{1}$").matcher(String.valueOf(t)).matches()) {
            return t;
        }
        return 1;
    }

    public void adjustTrafficMeal() {
        HwLog.i("TrafficPanelManager", "adjustTrafficMeal");
        if (this.mShowTraffic) {
            int N = this.mTelephonyManager == null ? 0 : this.mTelephonyManager.getSimCount();
            for (int i = 0; i < N; i++) {
                refreshViewSimState(i);
            }
            return;
        }
        HwLog.i("TrafficPanelManager", "adjustTrafficMeal traffic switch is off ,return");
    }

    public long getMealTraffic(int slot) {
        long ret = getMealTrafficImpl(slot);
        if (ret > -1 || this.mNetAssistantService != null) {
            return ret;
        }
        return getMealTrafficImpl(slot);
    }

    public long getMealTrafficImpl(int slot) {
        HwLog.i("TrafficPanelManager", "getMealTrafficImpl:" + slot);
        checkNotificationBinder();
        if (this.mTelephonyManager == null) {
            return -1;
        }
        String imsi;
        if (this.isMultiSimEnabled) {
            imsi = this.mTelephonyManager.getSubscriberId(slot);
        } else {
            imsi = this.mTelephonyManager.getSubscriberId();
        }
        if (imsi == null) {
            return -1;
        }
        try {
            long mealTraffic = this.mNetAssistantService.getMonthlyTotalBytes(imsi);
            HwLog.i("TrafficPanelManager", "getMealTrafficImpl: mealTraffic=" + mealTraffic);
            return mealTraffic;
        } catch (Exception e) {
            HwLog.e("TrafficPanelManager", "getMealTraffic Exception e=" + e.getMessage());
            e.printStackTrace();
            this.mNetAssistantService = null;
            return -1;
        }
    }

    public long getTodayTraffic(int sub) {
        checkNotificationBinder();
        if (this.mTelephonyManager == null) {
            return -1;
        }
        String imsi = this.mTelephonyManager.getSubscriberId(sub);
        if (imsi == null) {
            return -1;
        }
        try {
            return this.mNetAssistantService.getTodayMobileTotalBytes(imsi);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public long getResidueTraffic(long meal, int sub) {
        checkNotificationBinder();
        if (this.mTelephonyManager == null) {
            return meal;
        }
        String imsi = this.mTelephonyManager.getSubscriberId(sub);
        long used = 0;
        if (!(imsi == null || this.mNetAssistantService == null)) {
            try {
                used = this.mNetAssistantService.getMonthMobileTotalBytes(imsi);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        if (used <= 0) {
            used = 0;
        }
        return meal - used;
    }

    public Uri getContentUri() {
        return Uri.parse("content://com.huawei.systemmanager.NetAssistantProvider/settinginfo");
    }

    private Cursor getTrafficDataCursor(String imsi) {
        Uri apUri = getContentUri();
        String[] columns = getSettingColumns();
        StringBuilder where = new StringBuilder();
        where.append("imsi").append(" =? ");
        String userWhere = where.toString();
        String[] userWhereArgs = new String[]{imsi};
        if (this.mContext == null) {
            return null;
        }
        return this.mContext.getContentResolver().query(apUri, columns, userWhere, userWhereArgs, null);
    }

    public long getSettingColumnsLongInfo(String imsi, int columnsIndex) {
        long value = -1;
        Cursor cursor = getTrafficDataCursor(imsi);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    value = cursor.getLong(columnsIndex);
                }
            } catch (Exception e) {
                Log.e("TrafficPanelManager", "getSettingColumnsLongInfo Exception=" + e.getMessage());
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return value;
    }

    public long getSettingColumnsLongInfo(int slot, int columnsIndex) {
        String imsi = null;
        if (this.mTelephonyManager != null) {
            imsi = this.mTelephonyManager.getSubscriberId(slot);
        }
        return getSettingColumnsLongInfo(imsi, columnsIndex);
    }

    public void showTraffic() {
        HwLog.i("TrafficPanelManager", "showTraffic");
        this.mShowTrafficKeguard = true;
        notifyShowChanged();
    }

    public void hideTraffic() {
        HwLog.i("TrafficPanelManager", "hideTraffic");
        this.mShowTrafficKeguard = false;
        notifyShowChanged();
    }
}
