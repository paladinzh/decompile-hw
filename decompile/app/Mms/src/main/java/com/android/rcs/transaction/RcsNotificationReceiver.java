package com.android.rcs.transaction;

import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import com.android.mms.data.ContactList;
import com.android.mms.transaction.MessagingNotification.NotificationInfo;
import com.android.mms.transaction.NotificationReceiver;
import com.android.rcs.RcsCommonConfig;
import com.android.rcs.ui.RcsComposeMessage;
import com.android.rcs.ui.RcsMessageUtils;
import com.huawei.cspcommon.MLog;
import com.huawei.rcs.ui.RcsGroupChatComposeMessageActivity;
import com.huawei.rcs.utils.RcsTransaction;

public class RcsNotificationReceiver {
    private static final String TAG = RcsNotificationReceiver.class.getSimpleName();

    public static PendingIntent getContentIntent(Context context, Uri msgUri, long tid, Bundle bundle) {
        if (!RcsCommonConfig.isRCSSwitchOn()) {
            return null;
        }
        Intent viewIntent = new Intent("com.huawei.mms.action.headsup.viewmsg");
        viewIntent.putExtra("msg_uri", msgUri);
        viewIntent.putExtra("thread_id", tid);
        viewIntent.putExtra("mms_notification_id", 1390);
        viewIntent.setPackage("com.android.mms");
        viewIntent.putExtras(bundle);
        return PendingIntent.getBroadcast(context, SmartSmsSdkUtil.DUOQU_BUBBLE_DATA_CACHE_SIZE, viewIntent, 134217728);
    }

    public static PendingIntent getClickIntent(Context context, Uri msgUri, long tid, int subId, String type, String address, boolean isFirstMessage, int notificationId, NotificationInfo notificationInfo, boolean isHeadsUp) {
        if (!RcsCommonConfig.isRCSSwitchOn()) {
            return null;
        }
        int i;
        Intent clickIntent = new Intent("com.huawei.mms.action.headsup.clicked");
        clickIntent.putExtra("HandleType", 2);
        clickIntent.putExtra("msg_uri", msgUri);
        clickIntent.putExtra("thread_id", tid);
        String str = "mms_notification_id";
        if (isHeadsUp) {
            i = 1390;
        } else {
            i = notificationId;
        }
        clickIntent.putExtra(str, i);
        clickIntent.putExtra("sub_id", subId);
        clickIntent.putExtra(NumberInfo.TYPE_KEY, type);
        if ("isGroup".equals(type)) {
            clickIntent.putExtra("groupId", address);
        } else {
            clickIntent.putExtra("address", address);
        }
        if (isFirstMessage) {
            NotificationReceiver.initFirstBundle(context, msgUri, subId, isFirstMessage, notificationId, notificationInfo);
        }
        clickIntent.setPackage("com.android.mms");
        return PendingIntent.getBroadcast(context, (int) ContentUris.parseId(msgUri), clickIntent, 134217728);
    }

    public boolean sendOrSaveMessage(Context context, Intent intent, boolean send) {
        if (!RcsCommonConfig.isRCSSwitchOn()) {
            return false;
        }
        if (intent == null) {
            return false;
        }
        Bundle results = RemoteInput.getResultsFromIntent(intent);
        if (results == null) {
            return false;
        }
        String msg = results.getString("Quick_Reply");
        long tid = intent.getLongExtra("thread_id", 0);
        MLog.e(TAG, "sendOrSaveMessage tid" + tid);
        if (TextUtils.isEmpty(msg)) {
            MLog.e(TAG, "Can't send or save message as nothing input");
            return false;
        }
        String type = intent.getStringExtra(NumberInfo.TYPE_KEY);
        MLog.e(TAG, "sendOrSaveMessage: type " + type);
        if ("isChat".equals(type)) {
            String add = intent.getStringExtra("address");
            if (TextUtils.isEmpty(add)) {
                MLog.e(TAG, "Can't send message as address is null.");
                return false;
            } else if (send) {
                RcsTransaction.preSendImMessage(context, msg, add);
            } else {
                RcsMessageUtils.saveDraft(context, ContactList.getByNumbers(add, false, false), msg, 0);
            }
        } else if ("isGroup".equals(type)) {
            String groupId = intent.getStringExtra("groupId");
            if (TextUtils.isEmpty(groupId)) {
                MLog.e(TAG, "groupId is null");
                return false;
            } else if (send) {
                RcsTransaction.toSendGroupMessage(context, groupId, msg);
            } else {
                RcsMessageUtils.saveDraftForGroup(context, msg, tid, groupId);
            }
        } else {
            MLog.e(TAG, "unsupport type");
            return false;
        }
        return true;
    }

    public boolean viewMessage(Context context, Intent intent) {
        if (!RcsCommonConfig.isRCSSwitchOn()) {
            return false;
        }
        if (intent == null || intent.getExtras() == null || TextUtils.isEmpty(intent.getExtras().getString(NumberInfo.TYPE_KEY))) {
            MLog.e(TAG, "bundle is null or type is null");
            return false;
        }
        Bundle bundle = intent.getExtras();
        String type = bundle.getString(NumberInfo.TYPE_KEY);
        Object stringExtra = intent.hasExtra("EditText") ? intent.getStringExtra("EditText") : null;
        long tId = intent.getLongExtra("thread_id", 0);
        if ("isChat".equals(type)) {
            Intent chatIntent = RcsComposeMessage.createIntent(context, tId, 2);
            chatIntent.putExtra("received_flag", true);
            chatIntent.putExtra("fromNotification", true);
            chatIntent.setFlags(872415232);
            if (!TextUtils.isEmpty(stringExtra)) {
                chatIntent.putExtra("sms_body", stringExtra);
            }
            context.startActivity(chatIntent);
            return true;
        } else if ("isGroup".equals(type)) {
            String groupId = bundle.getString("groupId");
            if (TextUtils.isEmpty(groupId)) {
                MLog.e(TAG, "groupId is null");
                return false;
            }
            Intent groupintent = new Intent(context, RcsGroupChatComposeMessageActivity.class);
            groupintent.putExtra("received_flag", true);
            groupintent.putExtra("fromNotification", true);
            groupintent.putExtra("bundle_group_id", groupId);
            groupintent.setFlags(872415232);
            context.startActivity(groupintent);
            return true;
        } else {
            MLog.e(TAG, "unsupport type");
            return false;
        }
    }
}
