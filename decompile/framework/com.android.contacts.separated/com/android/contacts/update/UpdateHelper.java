package com.android.contacts.update;

import android.content.Context;
import android.content.Intent;
import android.util.SparseArray;
import com.android.contacts.compatibility.NumberLocationCache;
import com.android.contacts.hap.numbermark.YellowPageDataManager;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.SharePreferenceUtil;
import com.huawei.numberlocation.NLContentProvider;

public class UpdateHelper {
    private static final String TAG = "DownloadService";
    private static boolean isInit = false;
    private static final SparseArray<IUpdate> mUpdateMap = new SparseArray();

    public static class CCUpdater extends Updater {
        public CCUpdater(Context context) {
            super(context, 3);
        }

        public String getTitle() {
            return this.mContext.getResources().getString(2131363258);
        }

        public String getSucessNoti() {
            return this.mContext.getResources().getString(2131363264);
        }
    }

    public static class NLUpdater extends Updater {
        public NLUpdater(Context context) {
            super(context, 1);
        }

        public void setItem(int item) {
            super.setItem(item);
            notifyChange();
        }

        public void handleComplete(DownloadResponse response) {
            super.handleComplete(response);
            NumberLocationCache.clearLocation();
            NumberLocationCache.clear();
            UpdateHelper.refreshCallLog(this.mContext);
            this.mContext.sendBroadcast(new Intent("com.huawei.intent.action.UPDATE_NUMBER_LOCATION_ALL"), "com.huawei.contacts.permission.HW_CONTACTS_ALL");
            notifyChange();
        }

        public String getTitle() {
            return this.mContext.getResources().getString(2131363257);
        }

        public String getSucessNoti() {
            return this.mContext.getResources().getString(2131363263);
        }

        private void notifyChange() {
            this.mContext.getContentResolver().notifyChange(NLContentProvider.CONTENT_URI_FVERSION, null, false);
        }
    }

    public static class YPUpdater extends Updater {
        public YPUpdater(Context context) {
            super(context, 4);
        }

        public void handleComplete(DownloadResponse response) {
            super.handleComplete(response);
            new YellowPageDataManager(this.mContext).prepareYellowPageDataAsync(true);
            UpdateHelper.refreshCallLog(this.mContext);
        }

        public String getTitle() {
            return this.mContext.getResources().getString(2131363259);
        }

        public String getSucessNoti() {
            return this.mContext.getResources().getString(2131363265);
        }
    }

    public static synchronized void init(Context context) {
        synchronized (UpdateHelper.class) {
            if (context == null) {
                HwLog.i(TAG, "update helper init null");
                return;
            }
            initUpdateMap(context);
            sheduleAlarm();
        }
    }

    public static synchronized IUpdate getUpdaterInstance(int fileId, Context context) {
        IUpdate updater;
        synchronized (UpdateHelper.class) {
            updater = (IUpdate) mUpdateMap.get(fileId);
            if (updater == null) {
                updater = getUpdaterAndInit(context, fileId);
            }
        }
        return updater;
    }

    private static synchronized IUpdate getUpdaterAndInit(Context context, int fileId) {
        synchronized (UpdateHelper.class) {
            if (context == null) {
                HwLog.i(TAG, "update helper getUpdaterAndInit null");
                return null;
            }
            initUpdateMap(context);
            IUpdate iUpdate = (IUpdate) mUpdateMap.get(fileId);
            return iUpdate;
        }
    }

    private static synchronized void initUpdateMap(Context context) {
        synchronized (UpdateHelper.class) {
            if (!isInit) {
                context = context.getApplicationContext();
                mUpdateMap.put(1, new NLUpdater(context));
                mUpdateMap.put(3, new CCUpdater(context));
                mUpdateMap.put(4, new YPUpdater(context));
                isInit = true;
                if (HwLog.HWDBG) {
                    HwLog.d(TAG, "update helper init finish");
                }
            }
        }
    }

    private static synchronized void sheduleAlarm() {
        synchronized (UpdateHelper.class) {
            for (int i = 0; i < mUpdateMap.size(); i++) {
                ((IUpdate) mUpdateMap.valueAt(i)).scheduleAutoUpdate();
            }
        }
    }

    private static void refreshCallLog(Context context) {
        SharePreferenceUtil.getDefaultSp_de(context).edit().putBoolean("reference_is_refresh_calllog", true).commit();
        context.sendBroadcast(new Intent("com.android.contacts.action.UPDATE_YP"), "com.huawei.contacts.permission.HW_CONTACTS_ALL");
    }
}
