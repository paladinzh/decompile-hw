package com.android.contacts.calllog;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import com.android.contacts.hap.util.IntentServiceWithWakeLock;
import com.android.contacts.util.HwLog;

public class CallLogNotificationsService extends IntentServiceWithWakeLock {
    private CallLogQueryHandler mCallLogQueryHandler;

    public CallLogNotificationsService() {
        super("CallLogNotificationsService");
    }

    protected void doWakefulWork(Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if (action.equals("com.android.contacts.calllog.ACTION_MARK_NEW_VOICEMAILS_AS_OLD")) {
                if (this.mCallLogQueryHandler == null) {
                    this.mCallLogQueryHandler = new CallLogQueryHandler(getApplicationContext(), getContentResolver(), null);
                }
                this.mCallLogQueryHandler.markNewVoicemailsAsOld();
            } else if (action.equals("com.android.dialer.calllog.UPDATE_VOICEMAIL_NOTIFICATIONS")) {
                DefaultVoicemailNotifier.getInstance(this).updateNotification((Uri) intent.getParcelableExtra("NEW_VOICEMAIL_URI"));
            } else if (action.equals("com.android.dialer.calllog.UPDATE_MISSED_CALL_NOTIFICATIONS")) {
                MissedCallNotifier.getInstance(this).updateMissedCallNotification(intent.getIntExtra("MISSED_CALL_COUNT", -1), intent.getStringExtra("MISSED_CALL_NUMBER"));
            } else if (action.equals("com.android.dialer.calllog.ACTION_MARK_NEW_MISSED_CALLS_AS_OLD")) {
                CallLogNotificationsHelper.removeMissedCallNotifications(this);
            } else if (action.equals("com.android.dialer.calllog.CALL_BACK_FROM_MISSED_CALL_NOTIFICATION")) {
                MissedCallNotifier.getInstance(this).callBackFromMissedCall(intent.getStringExtra("MISSED_CALL_NUMBER"));
            } else if (action.equals("com.android.dialer.calllog.SEND_SMS_FROM_MISSED_CALL_NOTIFICATION")) {
                MissedCallNotifier.getInstance(this).sendSmsFromMissedCall(intent.getStringExtra("MISSED_CALL_NUMBER"));
            } else if (HwLog.HWDBG) {
                HwLog.d("CallLogNotificationsService", "doWakefulWork: could not handle: " + intent);
            }
        }
    }

    public static void updateMissedCallNotifications(Context context, int count, String number) {
        Intent serviceIntent = new Intent(context, CallLogNotificationsService.class);
        serviceIntent.setAction("com.android.dialer.calllog.UPDATE_MISSED_CALL_NOTIFICATIONS");
        serviceIntent.putExtra("MISSED_CALL_COUNT", count);
        serviceIntent.putExtra("MISSED_CALL_NUMBER", number);
        context.startService(serviceIntent);
    }
}
