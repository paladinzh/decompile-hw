package com.android.mms.transaction;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.RemoteInput;
import android.text.TextUtils;
import com.android.mms.ui.MessageUtils;
import com.huawei.mms.util.HwMessageUtils;

public class ReplyService extends Service {
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void onCreate() {
        super.onCreate();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            stopSelf();
            return 1;
        }
        super.onStartCommand(intent, flags, startId);
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {
            Object charSequence = remoteInput.getCharSequence("extra_voice_reply") == null ? null : remoteInput.getCharSequence("extra_voice_reply").toString();
            long[] threadId = intent.getLongArrayExtra("threadIds");
            int[] subscription = intent.getIntArrayExtra("subscriptions");
            if (!(threadId == null || threadId[0] <= 0 || subscription == null || !isVaildSubscription(subscription[0]) || TextUtils.isEmpty(charSequence))) {
                HwMessageUtils.sendSms(getApplicationContext(), threadId[0], charSequence, subscription[0]);
            }
        }
        stopSelf();
        return 1;
    }

    private boolean isVaildSubscription(int subscription) {
        if (MessageUtils.isMultiSimEnabled()) {
            if (1 != MessageUtils.getIccCardStatus(subscription)) {
                return false;
            }
        } else if (1 != MessageUtils.getIccCardStatus()) {
            return false;
        }
        return true;
    }
}
