package com.android.internal.telephony.gsm;

import android.os.Message;
import android.telephony.Rlog;
import com.android.internal.telephony.ImsSMSDispatcher;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.SMSDispatcher.SmsTracker;
import com.android.internal.telephony.SMSDispatcherUtils;
import com.android.internal.telephony.SmsStorageMonitor;
import com.android.internal.telephony.SmsUsageMonitor;
import java.util.ArrayList;
import java.util.List;

public class HwGsmSMSDispatcher extends GsmSMSDispatcher {
    protected static final int EVENT_SMS_SENDING_TIMEOUT = 1000;
    protected static final int SMS_SENDING_TIMOUEOUT = 60000;
    private static final String TAG = "HwGsmSMSDispatcher";
    protected List<SmsTracker> mTrackerList = new ArrayList();

    public HwGsmSMSDispatcher(Phone phone, SmsStorageMonitor storageMonitor, SmsUsageMonitor usageMonitor, ImsSMSDispatcher imsSMSDispatcher, GsmInboundSmsHandler gsmInboundSmsHandler) {
        super(phone, storageMonitor, usageMonitor, imsSMSDispatcher, gsmInboundSmsHandler);
    }

    protected void sendSms(SmsTracker tracker) {
        Rlog.d(TAG, "sendSms: tracker is:" + tracker);
        int ss = this.mPhone.getServiceState().getState();
        if (tracker == null || isIms() || ss == 0) {
            this.mTrackerList.add(tracker);
            Rlog.d(TAG, "sendSms: mTrackerList = " + this.mTrackerList.size());
            if (1 == this.mTrackerList.size() && sendSmsImmediately(tracker)) {
                removeMessages(1000);
                sendMessageDelayed(obtainMessage(1000, tracker), 60000);
            }
            return;
        }
        tracker.onFailed(this.mContext, getNotInServiceError(ss), 0);
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 2:
                Rlog.d(TAG, "handleSendComplete Remove EVENT_SMS_SENDING_TIMEOUT");
                removeMessages(1000);
                super.handleMessage(msg);
                return;
            case 1000:
                SmsTracker tracker = msg.obj;
                Rlog.d(TAG, "EVENT_SMS_SENDING_TIMEOUT, failed tracker is: " + tracker + "blocked size is: " + this.mTrackerList.size());
                if (this.mTrackerList.size() > 0 && this.mTrackerList.remove(tracker)) {
                    Rlog.e(TAG, "EVENT_SMS_SENDING_TIMEOUT, failed tracker is: " + tracker + "blocked size is: " + this.mTrackerList.size());
                    removeMessages(1000);
                    if (this.mTrackerList.size() > 0 && sendSmsImmediately((SmsTracker) this.mTrackerList.get(0))) {
                        sendMessageDelayed(obtainMessage(1000, this.mTrackerList.get(0)), 60000);
                        return;
                    }
                    return;
                }
                return;
            default:
                if (SMSDispatcherUtils.getEventSendRetry() == msg.what) {
                    Rlog.d(TAG, "SMS send retry..");
                    sendMessageDelayed(obtainMessage(1000, (SmsTracker) msg.obj), 60000);
                }
                super.handleMessage(msg);
                return;
        }
    }

    protected boolean sendSmsImmediately(SmsTracker tracker) {
        if (tracker == null) {
            return false;
        }
        super.sendSms(tracker);
        triggerSendSmsOverLoadCheck();
        return true;
    }

    protected void sendSmsSendingTimeOutMessageDelayed(SmsTracker tracker) {
        Rlog.d(TAG, "handleSendComplete: tracker is:" + tracker);
        if (this.mTrackerList.remove(tracker) && this.mTrackerList.size() > 0 && sendSmsImmediately((SmsTracker) this.mTrackerList.get(0))) {
            sendMessageDelayed(obtainMessage(1000, this.mTrackerList.get(0)), 60000);
        }
    }
}
