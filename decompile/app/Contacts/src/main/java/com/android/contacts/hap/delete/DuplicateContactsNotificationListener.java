package com.android.contacts.hap.delete;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.ContextThemeWrapper;
import android.widget.Toast;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.util.HwLog;
import com.android.contacts.vcard.CancelRequest;
import com.google.android.gms.R;

public class DuplicateContactsNotificationListener implements DuplicateContactsListener {
    private final Context mContext;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final NotificationManager mNotificationManager;

    public DuplicateContactsNotificationListener(Context aContext) {
        this.mContext = aContext;
        this.mNotificationManager = (NotificationManager) aContext.getSystemService("notification");
    }

    private void showToast(final String message) {
        this.mHandler.post(new Runnable() {
            public void run() {
                int themeID = DuplicateContactsNotificationListener.this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null);
                if (themeID == 0) {
                    HwLog.d("Contact", "if case value of themeID is ::::" + themeID);
                    Toast.makeText(DuplicateContactsNotificationListener.this.mContext, message, 0).show();
                    return;
                }
                HwLog.d("Contact", "else case value of themeID is ::::" + themeID);
                Toast.makeText(new ContextThemeWrapper(DuplicateContactsNotificationListener.this.mContext, themeID), message, 0).show();
            }
        });
    }

    public void onDeleteDuplicateContactsQueued(String aName, int aJobId) {
        String message = String.format(this.mContext.getString(R.string.str_start_search_duplicate_contacts), new Object[]{aName});
        showToast(message);
        this.mNotificationManager.notify("DuplicateContactsNotificationListener", aJobId, constructSearchNotification(this.mContext, this.mContext.getString(R.string.str_start_search_duplicate_contacts_title), message));
    }

    public void onDeleteDuplicateContactsFinished(String aName, int aCount, int aJobId) {
        this.mNotificationManager.cancelAll();
    }

    public void onDeleteDuplicateContactsFailed(String aName, int aJobId) {
        this.mNotificationManager.notify("DuplicateContactsNotificationListener", aJobId, constructFailNotification(this.mContext, String.format(this.mContext.getString(R.string.str_failed_duplicate_contacts_title), new Object[]{aName}), "", null));
    }

    public void onDeleteDuplicateContactsCanceled(String aDescription, int jobId) {
    }

    public void onCancelRequest(CancelRequest request) {
    }

    public void onNoDuplicateContactsFound(String aName, int aJobId) {
        if (this.mNotificationManager != null) {
            this.mNotificationManager.cancelAll();
        }
    }

    static Notification constructFailNotification(Context aContext, String aTitle, String aDescription, Intent aIntent) {
        CommonUtilMethods.constructAndSendSummaryNotification(aContext, aTitle);
        Builder showWhen = new Builder(aContext).setAutoCancel(true).setSmallIcon(CommonUtilMethods.getBitampIcon(aContext, R.drawable.ic_notification_contacts)).setContentTitle(aTitle).setGroup("group_key_contacts").setGroupSummary(false).setShowWhen(true);
        if (aIntent == null) {
            aIntent = new Intent();
        }
        Notification notification = showWhen.setContentIntent(PendingIntent.getActivity(aContext, 0, aIntent, 0)).setContentText(aDescription).getNotification();
        notification.tickerText = aDescription;
        return notification;
    }

    static Notification constructSearchNotification(Context aContext, String aTitle, String aDescription) {
        CommonUtilMethods.constructAndSendSummaryNotification(aContext, aTitle);
        Notification notification = new Builder(aContext).setAutoCancel(true).setSmallIcon(CommonUtilMethods.getBitampIcon(aContext, R.drawable.ic_notification_contacts)).setContentTitle(aTitle).setGroup("group_key_contacts").setGroupSummary(false).setShowWhen(true).setContentText(aDescription).getNotification();
        notification.flags |= 2;
        notification.tickerText = aDescription;
        return notification;
    }
}
