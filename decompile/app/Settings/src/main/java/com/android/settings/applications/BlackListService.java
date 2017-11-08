package com.android.settings.applications;

import android.app.IntentService;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import java.util.ArrayList;

public class BlackListService extends IntentService {
    private ArrayList<String> mBlackList;
    private ArrayList<String> mDisableAppList;

    public BlackListService() {
        super("BlackListService");
    }

    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String actionType = intent.getStringExtra("action_type");
            String packageName = intent.getStringExtra("package_name");
            if ("android.intent.action.BOOT_COMPLETED".equals(actionType)) {
                this.mBlackList = BlackListUtils.getBlackListApps();
                this.mDisableAppList = BlackListPreferenceHelper.getDisabledAppList(this);
                boolean needToPopNotification = checkBlackListAppUpdate(this.mBlackList, this.mDisableAppList);
                syncDisableRecord(this.mBlackList, this);
                if (needToPopNotification) {
                    popNotification(this.mBlackList);
                }
            } else if ("android.intent.action.PACKAGE_ADDED".equals(actionType) || "android.intent.action.PACKAGE_REPLACED".equals(actionType)) {
                if (!TextUtils.isEmpty(packageName)) {
                    packageList = new ArrayList();
                    if (BlackListUtils.isBlackListApp(packageName)) {
                        BlackListPreferenceHelper.saveDisableRecord(packageName, (Context) this);
                        packageList.add(packageName);
                        popNotification(packageList);
                    }
                }
            } else if ("android.intent.action.PACKAGE_REMOVED".equals(actionType) && !TextUtils.isEmpty(packageName)) {
                packageList = BlackListPreferenceHelper.getDisabledAppList(this);
                if (packageList != null && packageList.contains(packageName)) {
                    BlackListPreferenceHelper.removeDisableRecord(packageName, this);
                }
            }
        }
    }

    private void popNotification(ArrayList<String> packageList) {
        if (packageList != null && packageList.size() != 0) {
            int size = packageList.size();
            CharSequence contentTitle = "";
            CharSequence contentText = "";
            CharSequence contentTicker = "";
            if (size == 1) {
                if (!TextUtils.isEmpty(BlackListUtils.getApplicationLabelName((String) packageList.get(0), this))) {
                    contentTitle = String.format(getResources().getString(2131628370), new Object[]{packageLabel});
                    contentText = getResources().getString(2131628371);
                    contentTicker = String.format(getResources().getString(2131628369), new Object[]{packageLabel});
                } else {
                    return;
                }
            } else if (size > 1) {
                contentTitle = getResources().getQuantityString(2131689540, size, new Object[]{Integer.valueOf(size)});
                contentText = getResources().getString(2131628372);
                contentTicker = getResources().getQuantityString(2131689539, size, new Object[]{Integer.valueOf(size)});
            } else {
                return;
            }
            NotificationManager notificationManager = (NotificationManager) getSystemService("notification");
            Intent notificationIntent = new Intent("android.settings.MANAGE_APPLICATIONS_SETTINGS");
            notificationIntent.putExtra("showBlackListApp", true);
            Notification notification = new Builder(this).setSmallIcon(2130838300).setLargeIcon(BitmapFactory.decodeResource(getResources(), 2130838300)).setTicker(contentTicker).setContentTitle(contentTitle).setContentText(contentText).setAutoCancel(true).setContentIntent(PendingIntent.getActivity(this, 0, notificationIntent, 134217728)).build();
            notification.extras = getNotificationThemeData(2130838300, -1, 1, 15);
            notificationManager.notify(2130838300, notification);
        }
    }

    private boolean checkBlackListAppUpdate(ArrayList<String> mBlackList, ArrayList<String> mDisableAppList) {
        if (mBlackList == null || mBlackList.size() == 0) {
            return false;
        }
        return mDisableAppList == null || !mDisableAppList.containsAll(mBlackList);
    }

    private void syncDisableRecord(ArrayList<String> mBlackList, Context context) {
        BlackListPreferenceHelper.clearDisableRecord(context);
        BlackListPreferenceHelper.saveDisableRecord((ArrayList) mBlackList, context);
    }

    private Bundle getNotificationThemeData(int contIconId, int repIconId, int bgIndex, int repLocation) {
        Bundle bundle = new Bundle();
        if (contIconId > 0) {
            bundle.putInt("huawei.notification.contentIcon", contIconId);
        }
        if (repIconId > 0) {
            bundle.putInt("huawei.notification.replace.iconId", repIconId);
        }
        if (bgIndex >= 0) {
            bundle.putInt("huawei.notification.backgroundIndex", bgIndex);
        }
        if (repLocation > 0) {
            bundle.putInt("huawei.notification.replace.location", repLocation);
        }
        return bundle;
    }
}
