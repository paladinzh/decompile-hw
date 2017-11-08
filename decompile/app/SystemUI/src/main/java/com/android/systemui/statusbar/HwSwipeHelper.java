package com.android.systemui.statusbar;

import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.android.systemui.R;
import com.android.systemui.SwipeHelper.LongPressListener;
import com.android.systemui.statusbar.phone.HwPhoneStatusBar;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.SystemUIThread;
import com.android.systemui.utils.SystemUIThread.SimpleAsyncTask;
import com.android.systemui.utils.SystemUiUtil;

public class HwSwipeHelper {
    private Context mContext;

    public class HwLongPressListener implements LongPressListener {
        public boolean onLongPress(View v, int x, int y) {
            if (!(v instanceof ExpandableNotificationRow)) {
                Log.e("HwSwipeHelper", "v is not ExpandableNotificationRow");
                return false;
            } else if (v.getWindowToken() == null) {
                Log.e("HwSwipeHelper", "Trying to show notification guts, but not attached to window");
                return false;
            } else {
                ExpandableNotificationRow row = (ExpandableNotificationRow) v;
                String pkgName = row.getStatusBarNotification().getPackageName();
                if (pkgName == null) {
                    HwLog.i("HwSwipeHelper", "pkgName == null");
                    return false;
                }
                if ("com.huawei.android.pushagent".equals(pkgName)) {
                    StatusBarNotification sbnf = row.getStatusBarNotification();
                    if (sbnf == null || sbnf.getNotification() == null || sbnf.getNotification().extras == null) {
                        HwLog.e("HwSwipeHelper", "some null exception happened");
                    } else {
                        String originSenderPkgName = sbnf.getNotification().extras.getString("hw_origin_sender_package_name");
                        HwLog.i("HwSwipeHelper", "originSenderPkgName:" + originSenderPkgName);
                        if (originSenderPkgName != null) {
                            pkgName = originSenderPkgName;
                        }
                    }
                }
                HwLog.i("HwSwipeHelper", "HwLongPressListener longclick pkgName:" + pkgName);
                String fPkgName = pkgName;
                return goToNotificationSetting(pkgName);
            }
        }

        public boolean goToNotificationSetting(final String pkgName) {
            boolean isInSuperPowerMode = SystemProperties.getBoolean("sys.super_power_save", false);
            HwLog.i("HwSwipeHelper", "HwLongPressListener onLongPress isInSuperPowerMode:" + isInSuperPowerMode);
            if (isInSuperPowerMode) {
                HwPhoneStatusBar.getInstance().animateCollapsePanels();
                Toast.makeText(HwSwipeHelper.this.mContext, HwSwipeHelper.this.mContext.getText(R.string.super_power_not_support), 0).show();
                return false;
            }
            SystemUIThread.runAsync(new SimpleAsyncTask() {
                public boolean runInThread() {
                    try {
                        boolean keyguardShowing = HwPhoneStatusBar.getInstance().mStatusBarKeyguardViewManager.isShowing();
                        HwLog.i("HwSwipeHelper", "onLongPress keyguardShowing:" + keyguardShowing);
                        if (keyguardShowing) {
                            SystemUiUtil.dismissKeyguard();
                        }
                        return true;
                    } catch (RemoteException e) {
                        HwLog.e("HwSwipeHelper", "RemoteException", e);
                        return false;
                    }
                }

                public void runInUI() {
                    HwSwipeHelper.this.startApplicationDetailsActivity(pkgName, HwSwipeHelper.this.mContext);
                    HwPhoneStatusBar.getInstance().animateCollapsePanels();
                    HwLog.i("HwSwipeHelper", "startApplicationDetailsActivity pkgName:" + pkgName + " and animateCollapsePanels");
                }
            });
            return true;
        }
    }

    public HwSwipeHelper(Context ctx) {
        this.mContext = ctx;
    }

    private void startApplicationDetailsActivity(String packageName, Context ctx) {
        Intent intent = new Intent("huawei.intent.action.NOTIFICATIONSETTING");
        intent.putExtra("packageName", packageName);
        intent.setPackage("com.huawei.systemmanager");
        intent.addFlags(805306368);
        try {
            TaskStackBuilder.create(ctx).addNextIntentWithParentStack(intent).startActivities(null, new UserHandle(-2));
        } catch (Exception e) {
            Log.e("HwSwipeHelper", "startApplicationDetailsActivity exception " + e);
        }
    }
}
