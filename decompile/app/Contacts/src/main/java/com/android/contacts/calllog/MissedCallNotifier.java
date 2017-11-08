package com.android.contacts.calllog;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.CallLog.Calls;
import android.text.TextUtils;
import android.util.Log;
import com.android.contacts.CallUtil;
import com.android.contacts.activities.PeopleActivity;
import com.android.contacts.calllog.CallLogNotificationsHelper.NewCall;
import com.android.contacts.compatibility.UserManagerCompat;
import com.android.contacts.hap.CommonUtilMethods;
import com.google.android.gms.R;
import java.util.List;

public class MissedCallNotifier {
    private static volatile MissedCallNotifier sInstance;
    private Context mContext;

    public static MissedCallNotifier getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new MissedCallNotifier(context);
        }
        return sInstance;
    }

    private MissedCallNotifier(Context context) {
        this.mContext = context;
    }

    public void updateMissedCallNotification(int count, String number) {
        List<NewCall> newCalls = CallLogNotificationsHelper.getInstance(this.mContext).getNewMissedCalls();
        if (count == -1) {
            if (newCalls != null) {
                count = newCalls.size();
            } else {
                return;
            }
        }
        if (count == 0) {
            clearMissedCalls();
            return;
        }
        int titleResId;
        String expandedText;
        boolean useCallLog = newCalls != null && newCalls.size() == count;
        NewCall newCall = useCallLog ? (NewCall) newCalls.get(0) : null;
        long timeMs = useCallLog ? newCall.dateMs : System.currentTimeMillis();
        Builder builder = new Builder(this.mContext);
        if (count == 1) {
            String str;
            int i;
            CallLogNotificationsHelper instance = CallLogNotificationsHelper.getInstance(this.mContext);
            if (useCallLog) {
                str = newCall.number;
            } else {
                str = number;
            }
            if (useCallLog) {
                i = newCall.numberPresentation;
            } else {
                i = 1;
            }
            ContactInfo contactInfo = instance.getContactInfo(str, i, useCallLog ? newCall.countryIso : null);
            if (contactInfo.userType == 1) {
                titleResId = R.string.notification_missedWorkCallTitle;
            } else {
                titleResId = R.string.notification_missedCallTitle;
            }
            expandedText = contactInfo.name;
        } else {
            titleResId = R.string.notification_missedCallsTitle;
            expandedText = this.mContext.getString(R.string.notification_missedCallsMsg, new Object[]{Integer.valueOf(count)});
        }
        Builder publicBuilder = new Builder(this.mContext);
        publicBuilder.setSmallIcon(CommonUtilMethods.getBitampIcon(this.mContext, R.drawable.ic_notification_contacts)).setColor(this.mContext.getResources().getColor(R.color.dialer_theme_color)).setContentTitle(this.mContext.getText(R.string.userCallActivityLabel)).setContentText(this.mContext.getText(titleResId)).setContentIntent(createCallLogPendingIntent()).setAutoCancel(true).setWhen(timeMs).setDeleteIntent(createClearMissedCallsPendingIntent());
        builder.setSmallIcon(CommonUtilMethods.getBitampIcon(this.mContext, R.drawable.ic_notification_contacts)).setColor(this.mContext.getResources().getColor(R.color.dialer_theme_color)).setContentTitle(this.mContext.getText(titleResId)).setContentText(expandedText).setContentIntent(createCallLogPendingIntent()).setAutoCancel(true).setWhen(timeMs).setDeleteIntent(createClearMissedCallsPendingIntent()).setPublicVersion(publicBuilder.build());
        if (UserManagerCompat.isUserUnlocked(this.mContext) && count == 1 && !TextUtils.isEmpty(number)) {
            if (!TextUtils.equals(number, this.mContext.getString(R.string.handle_restricted))) {
                builder.addAction(R.drawable.ic_phone_24dp, this.mContext.getString(R.string.notification_missedCall_call_back), createCallBackPendingIntent(number));
                if (!PhoneNumberHelper.isUriNumber(number)) {
                    builder.addAction(R.drawable.ic_message_24dp, this.mContext.getString(R.string.notification_missedCall_message), createSendSmsFromNotificationPendingIntent(number));
                }
            }
        }
        Notification notification = builder.build();
        configureLedOnNotification(notification);
        Log.i("MissedCallNotifier", "Adding missed call notification.");
        getNotificationMgr().notify("MissedCallNotifier", 1, notification);
    }

    private void clearMissedCalls() {
        AsyncTask.execute(new Runnable() {
            public void run() {
                if (UserManagerCompat.isUserUnlocked(MissedCallNotifier.this.mContext)) {
                    ContentValues values = new ContentValues();
                    values.put("new", Integer.valueOf(0));
                    values.put("is_read", Integer.valueOf(1));
                    StringBuilder where = new StringBuilder();
                    where.append("new");
                    where.append(" = 1 AND ");
                    where.append("type");
                    where.append(" = ?");
                    try {
                        MissedCallNotifier.this.mContext.getContentResolver().update(Calls.CONTENT_URI, values, where.toString(), new String[]{Integer.toString(3)});
                    } catch (IllegalArgumentException e) {
                        Log.w("MissedCallNotifier", "ContactsProvider update command failed", e);
                    }
                }
                MissedCallNotifier.this.getNotificationMgr().cancel("MissedCallNotifier", 1);
            }
        });
    }

    public void callBackFromMissedCall(String number) {
        closeSystemDialogs(this.mContext);
        CallLogNotificationsHelper.removeMissedCallNotifications(this.mContext);
        try {
            Intent intent = new Intent("android.intent.action.CALL", CallUtil.getCallUri(number));
            intent.setFlags(268435456);
            this.mContext.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.e("MissedCallNotifier", "No call activity found");
        }
    }

    public void sendSmsFromMissedCall(String number) {
        closeSystemDialogs(this.mContext);
        CallLogNotificationsHelper.removeMissedCallNotifications(this.mContext);
        try {
            this.mContext.startActivity(new Intent("android.intent.action.SENDTO", Uri.parse("sms:" + number)));
        } catch (ActivityNotFoundException e) {
            Log.e("MissedCallNotifier", "No send sms activity found");
        }
    }

    private PendingIntent createCallLogPendingIntent() {
        Intent contentIntent = new Intent(this.mContext, PeopleActivity.class);
        contentIntent.setAction("com.android.phone.action.RECENT_CALLS");
        return PendingIntent.getActivity(this.mContext, 0, contentIntent, 134217728);
    }

    private PendingIntent createClearMissedCallsPendingIntent() {
        Intent intent = new Intent(this.mContext, CallLogNotificationsService.class);
        intent.setAction("com.android.dialer.calllog.ACTION_MARK_NEW_MISSED_CALLS_AS_OLD");
        return PendingIntent.getService(this.mContext, 0, intent, 0);
    }

    private PendingIntent createCallBackPendingIntent(String number) {
        Intent intent = new Intent(this.mContext, CallLogNotificationsService.class);
        intent.setAction("com.android.dialer.calllog.CALL_BACK_FROM_MISSED_CALL_NOTIFICATION");
        intent.putExtra("MISSED_CALL_NUMBER", number);
        return PendingIntent.getService(this.mContext, 0, intent, 0);
    }

    private PendingIntent createSendSmsFromNotificationPendingIntent(String number) {
        Intent intent = new Intent(this.mContext, CallLogNotificationsService.class);
        intent.setAction("com.android.dialer.calllog.SEND_SMS_FROM_MISSED_CALL_NOTIFICATION");
        intent.putExtra("MISSED_CALL_NUMBER", number);
        return PendingIntent.getService(this.mContext, 0, intent, 0);
    }

    private void configureLedOnNotification(Notification notification) {
        notification.flags |= 1;
        notification.defaults |= 4;
    }

    private void closeSystemDialogs(Context context) {
        context.sendBroadcast(new Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS"));
    }

    private NotificationManager getNotificationMgr() {
        return (NotificationManager) this.mContext.getSystemService("notification");
    }
}
