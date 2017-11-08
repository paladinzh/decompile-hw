package com.android.systemui.statusbar;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import com.android.systemui.statusbar.phone.HwPhoneStatusBar;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.SystemUIThread;
import com.android.systemui.utils.SystemUIThread.SimpleAsyncTask;
import com.android.systemui.utils.SystemUiUtil;
import java.util.ArrayList;

public class NotificationStateManager {
    private volatile ArrayList<ContentValues> mBannersList;
    private ContentObserver mContentObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            HwLog.i("NotificationStateManager", "onChange:userId=" + NotificationStateManager.this.mUserId);
            NotificationStateManager.this.updateAsync();
        }
    };
    private Context mContext;
    private volatile ArrayList<ContentValues> mPanelList;
    private volatile ArrayList<ContentValues> mScreenList;
    private volatile ArrayList<ContentValues> mSoundVibrateList;
    private volatile ArrayList<ContentValues> mStatusbarList;
    private int mUserId = 0;

    public NotificationStateManager(Context context, int userId) {
        this.mContext = context;
        this.mUserId = userId;
        this.mContentObserver.onChange(false);
        try {
            this.mContext.getContentResolver().registerContentObserver(Uri.parse("content://com.huawei.systemmanager.NotificationDBProvider/notificationCfg"), true, this.mContentObserver, this.mUserId);
        } catch (Exception e) {
            HwLog.e("NotificationStateManager", "NotificationStateManager:HwSystemManager is not install");
        }
    }

    public void setSoundVibrate(final String pkgName, final int state, final int uid) {
        if (this.mContext != null) {
            int oldState = getSoundVibrate(pkgName);
            if ((oldState == 1 || oldState == 2) && state == 3) {
                HwLog.i("NotificationStateManager", "sound or vibrate is on, do not change");
            } else {
                SystemUIThread.runAsync(new SimpleAsyncTask() {
                    public boolean runInThread() {
                        Bundle bundle = new Bundle();
                        bundle.putString("package_name", pkgName);
                        bundle.putInt("sound_vibrate_notification_item", state);
                        bundle.putInt("uid", uid);
                        try {
                            HwLog.i("NotificationStateManager", "call:uri=content://com.huawei.systemmanager.NotificationDBProvider/notificationCfg, type=5, userId=" + NotificationStateManager.this.mUserId + ", state=" + state);
                            SystemUiUtil.getContextForUser(NotificationStateManager.this.mContext, NotificationStateManager.this.mUserId, "com.huawei.systemmanager").getContentResolver().call(Uri.parse("content://com.huawei.systemmanager.NotificationDBProvider/notificationCfg"), "setSoundVibrate", null, bundle);
                        } catch (IllegalArgumentException e) {
                            HwLog.e("NotificationStateManager", "getContentValues:HwSystemManager is not install");
                        } catch (Exception e2) {
                            HwLog.e("NotificationStateManager", "getContentValues:HwSystemManager is not install");
                        }
                        return super.runInThread();
                    }
                });
            }
        }
    }

    public int getSoundVibrate(String pkgName) {
        int state = getNotificationState(pkgName, "5");
        HwLog.d("NotificationStateManager", "getSoundVibrate pkgName:" + pkgName + ", ret=" + state);
        return state;
    }

    public void updateAsync() {
        new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... params) {
                NotificationStateManager.this.update();
                return null;
            }

            protected void onPostExecute(Void result) {
                NotificationStateManager.this.refresh();
            }
        }.execute(new Void[0]);
    }

    private void update() {
        updateList("1");
        updateList("2");
        updateList("5");
    }

    private void refresh() {
        refreshStatus();
        refreshHeadsUp();
    }

    private void refreshStatus() {
        HwPhoneStatusBar.getInstance().requestNotificationUpdate();
    }

    private void refreshHeadsUp() {
        HwPhoneStatusBar.getInstance().requestNotificationUpdate();
    }

    private ArrayList<ContentValues> getList(String type) {
        if ("1".equals(type)) {
            return this.mStatusbarList;
        }
        if ("2".equals(type)) {
            return this.mBannersList;
        }
        if ("3".equals(type)) {
            return this.mPanelList;
        }
        if ("4".equals(type)) {
            return this.mScreenList;
        }
        if ("5".equals(type)) {
            return this.mSoundVibrateList;
        }
        return null;
    }

    private String getMethod(String type) {
        return "notification_list_query";
    }

    private String getBundleListKey(String type) {
        if ("1".equals(type)) {
            return "statusbar_notification_list";
        }
        if ("2".equals(type)) {
            return "banners_notification_list";
        }
        if ("3".equals(type)) {
            return "panel_notification_list";
        }
        if ("4".equals(type)) {
            return "lockscreen_notification_list";
        }
        if ("5".equals(type)) {
            return "sound_vibrate_notification_list";
        }
        return null;
    }

    private void updateList(String type) {
        if (type != null) {
            ArrayList<ContentValues> valueList = getContentValues(type);
            if (valueList != null && valueList.size() != 0) {
                if ("1".equals(type)) {
                    this.mStatusbarList = valueList;
                } else if ("2".equals(type)) {
                    this.mBannersList = valueList;
                } else if ("3".equals(type)) {
                    this.mPanelList = valueList;
                } else if ("4".equals(type)) {
                    this.mScreenList = valueList;
                } else if ("5".equals(type)) {
                    this.mSoundVibrateList = valueList;
                }
            }
        }
    }

    private ArrayList<ContentValues> getContentValues(String type) {
        if (this.mContext == null || type == null) {
            return null;
        }
        Bundle extras = null;
        try {
            HwLog.i("NotificationStateManager", "call:uri=content://com.huawei.systemmanager.NotificationDBProvider/notificationCfg, type=" + type + ", userId=" + this.mUserId);
            extras = SystemUiUtil.getContextForUser(this.mContext, this.mUserId, "com.huawei.systemmanager").getContentResolver().call(Uri.parse("content://com.huawei.systemmanager.NotificationDBProvider/notificationCfg"), getMethod(type), type, null);
        } catch (IllegalArgumentException e) {
            HwLog.e("NotificationStateManager", "getContentValues:HwSystemManager is not install");
        } catch (Exception e2) {
            HwLog.e("NotificationStateManager", "getContentValues:HwSystemManager is not install");
        }
        if (extras == null) {
            return null;
        }
        ArrayList<ContentValues> stateList = extras.getParcelableArrayList(getBundleListKey(type));
        HwLog.d("NotificationStateManager", "stateList:" + stateList);
        return stateList;
    }

    public int getNotificationState(String packageName, String type) {
        return getNotificationState(packageName, getList(type));
    }

    private int getNotificationState(String packageName, ArrayList<ContentValues> list) {
        int value = -1;
        if (packageName == null || list == null) {
            return -1;
        }
        for (ContentValues contentValues : list) {
            if (contentValues.containsKey(packageName)) {
                value = contentValues.getAsInteger(packageName).intValue();
                break;
            }
        }
        return value;
    }

    public void clear() {
        this.mContext.getContentResolver().unregisterContentObserver(this.mContentObserver);
    }
}
