package com.android.mms.transaction;

import android.content.Context;
import android.text.TextUtils;
import com.android.mms.transaction.MessagingNotification.NotificationInfo;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.crypto.CryptoMessageServiceProxy;
import com.huawei.mms.crypto.CryptoMessageUtil;
import java.util.SortedSet;

public class CryptoMsgNotification {
    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public String updateNotificationContent(Context context, CharSequence content, SortedSet<NotificationInfo> set) {
        if (content == null) {
            return "";
        }
        if (!CryptoMessageUtil.isCryptoSmsEnabled()) {
            return content.toString();
        }
        if (context == null || set == null || set.isEmpty()) {
            return content.toString();
        }
        if (!areSmsEncrypted(set)) {
            return content.toString();
        }
        int messageCount = set.size();
        content = context.getResources().getQuantityString(R.plurals.encrypted_sms_notification_counts, messageCount, new Object[]{Integer.valueOf(messageCount)});
        String msg;
        if (messageCount == 1) {
            msg = ((NotificationInfo) set.first()).mMessage;
            if (!TextUtils.isEmpty(msg)) {
            }
        } else {
            boolean isThereNormalMessage = false;
            for (NotificationInfo info : set) {
                msg = info.mMessage;
                if (!TextUtils.isEmpty(msg) && !isMsgEncrypted(msg)) {
                    isThereNormalMessage = true;
                    break;
                }
            }
            if (isThereNormalMessage) {
                content = context.getResources().getQuantityString(R.plurals.message_count_notification, messageCount, new Object[]{Integer.valueOf(messageCount)});
            }
        }
        MLog.d("Mms_TX_NOTIFY_HWCUST", "handleEncryptedSmsNotification: content=" + content + ", messageCount=" + messageCount);
        return content.toString();
    }

    public String buildTickerBodyForEncryptedSms(Context context, String body) {
        if (!CryptoMessageUtil.isCryptoSmsEnabled() || TextUtils.isEmpty(body) || !isMsgEncrypted(body)) {
            return body;
        }
        return context.getResources().getQuantityString(R.plurals.encrypted_received_message, 1, new Object[]{Integer.valueOf(1)});
    }

    private boolean areSmsEncrypted(SortedSet<NotificationInfo> set) {
        if (!CryptoMessageUtil.isCryptoSmsEnabled() || set == null || set.isEmpty()) {
            return false;
        }
        boolean areSmsEncrypted = false;
        for (NotificationInfo info : set) {
            String msg = info.mMessage;
            if (!TextUtils.isEmpty(msg) && isMsgEncrypted(msg)) {
                areSmsEncrypted = true;
                break;
            }
        }
        MLog.d("Mms_TX_NOTIFY_HWCUST", "areSmsEncrypted: areSmsEncrypted=" + areSmsEncrypted);
        return areSmsEncrypted;
    }

    private boolean isMsgEncrypted(String msg) {
        int eType = CryptoMessageServiceProxy.getEncryptedType(msg);
        boolean encrypted = (4 == eType || 3 == eType || 2 == eType) ? true : 1 == eType;
        MLog.d("Mms_TX_NOTIFY_HWCUST", "isMsgEncrypted: encrypted=" + encrypted);
        return encrypted;
    }
}
