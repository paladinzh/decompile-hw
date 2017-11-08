package com.android.mms.transaction;

import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Telephony.Sms.Inbox;
import android.text.TextUtils;
import com.android.mms.MmsConfig;
import com.android.mms.ui.MessageUtils;
import com.android.mms.ui.PreferenceUtils;
import com.google.android.mms.MmsException;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.cust.HwCustUtils;
import com.huawei.mms.util.DelaySendManager;
import com.huawei.mms.util.HwCustUpdateUserBehaviorImpl;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.HwSIMCardChangedHelper;
import com.huawei.mms.util.MmsRadarInfoManager;
import com.huawei.rcs.incallui.service.MessagePlusService;

public class SmsMessageSender implements MessageSender {
    private static final String[] SERVICE_CENTER_PROJECTION = new String[]{"reply_path_present", "service_center"};
    protected final Context mContext;
    private final String[] mDests;
    private HwCustSmsMessageSender mHwCustSmsMessageSender = ((HwCustSmsMessageSender) HwCustUtils.createObj(HwCustSmsMessageSender.class, new Object[0]));
    protected String mMessageText;
    MmsRadarInfoManager mMmsRadarInfoManager = null;
    protected final int mNumberOfDests;
    Handler mSendSmsHandler = null;
    protected final String mServiceCenter;
    protected int mSubId;
    protected final long mThreadId;
    protected long mTimestamp;

    public SmsMessageSender(Context context, String[] dests, String msgText, long threadId, int subId) {
        this.mContext = context;
        this.mMessageText = msgText;
        if (dests != null) {
            this.mNumberOfDests = dests.length;
            this.mDests = new String[this.mNumberOfDests];
            System.arraycopy(dests, 0, this.mDests, 0, this.mNumberOfDests);
        } else {
            this.mNumberOfDests = 0;
            this.mDests = null;
        }
        this.mTimestamp = System.currentTimeMillis();
        this.mThreadId = threadId;
        this.mServiceCenter = getOutgoingSmsCenterNumber(this.mThreadId, subId);
        this.mSubId = subId;
        this.mMmsRadarInfoManager = MmsRadarInfoManager.getInstance();
        this.mSendSmsHandler = this.mMmsRadarInfoManager.getHandler();
    }

    public boolean sendMessage(long token) throws MmsException {
        return queueMessage(token);
    }

    public boolean sendMessage(long token, long groupId) throws MmsException {
        return queueMessage(token, groupId);
    }

    private boolean queueMessage(long token) throws MmsException {
        return queueMessage(token, -1);
    }

    private boolean queueMessage(long token, long groupId) throws MmsException {
        if (this.mMessageText == null || this.mNumberOfDests == 0) {
            throw new MmsException("Null message body or dest.");
        }
        boolean requestDeliveryReport;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.mContext);
        if (!MessageUtils.isMultiSimEnabled() || !MmsConfig.isSMSDeliveryRptMultiCardPerf()) {
            requestDeliveryReport = prefs.getBoolean("pref_key_sms_delivery_reports", false);
        } else if (this.mSubId == 0) {
            requestDeliveryReport = prefs.getBoolean("pref_key_sms_delivery_reports_sub0", false);
        } else {
            requestDeliveryReport = prefs.getBoolean("pref_key_sms_delivery_reports_sub1", false);
        }
        long groupIdTemp = groupId;
        if (-1 == groupId) {
            groupIdTemp = MessageUtils.allocGroupId(this.mContext.getContentResolver());
        }
        String uriString = "content://sms/queued_with_group_id";
        String address = this.mDests[0];
        String bodyAddressPos = HwMessageUtils.getAddressPos(this.mMessageText);
        String timeAddressPos = HwMessageUtils.getTimePosString(this.mMessageText);
        if (this.mHwCustSmsMessageSender == null || !this.mHwCustSmsMessageSender.supportSendToEmail()) {
            if (this.mNumberOfDests > 1) {
                StringBuffer stringBuf = new StringBuffer();
                stringBuf.append(this.mDests[0]);
                for (int i = 1; i < this.mNumberOfDests; i++) {
                    stringBuf.append(",").append(this.mDests[i]);
                }
                uriString = "content://sms/bulk_queued";
                address = stringBuf.toString();
            }
            log("updating Database with sub = " + this.mSubId);
            Uri uri = MessageUtils.smsAddMessageToUri(this.mContext, Uri.parse(uriString), address, this.mMessageText, null, Long.valueOf(this.mTimestamp), true, requestDeliveryReport, this.mThreadId, this.mSubId, groupIdTemp, bodyAddressPos, timeAddressPos);
            if (uri != null) {
                MessagePlusService.addToSmsUriList(uri.toString());
            }
            MLog.w("Mms_TXS_Sender", "Add Sms Message for send. sub[" + this.mSubId + "] group-" + groupIdTemp + ". time:" + this.mTimestamp);
            if (uri != null) {
                this.mMmsRadarInfoManager.writeLogMsg(1311, "1003");
                this.mSendSmsHandler.sendMessage(this.mSendSmsHandler.obtainMessage(101));
                long id = 0;
                try {
                    id = ContentUris.parseId(uri);
                } catch (Throwable e) {
                    MLog.w("Mms_TXS_Sender", "get Id Unsupported Operation", e);
                } catch (NumberFormatException e2) {
                    MLog.w("Mms_TXS_Sender", "get Id Number Format Exception", e2);
                }
                if (PreferenceUtils.isCancelSendEnable(this.mContext)) {
                    boolean z;
                    DelaySendManager inst = DelaySendManager.getInst();
                    String str = "sms";
                    if (this.mNumberOfDests > 1) {
                        z = true;
                    } else {
                        z = false;
                    }
                    inst.addDelayMsg(id, str, z);
                } else {
                    SmsReceiver.broadcastForSendSms(this.mContext);
                }
            } else {
                this.mMmsRadarInfoManager.writeLogMsg(1311, "sms save fail");
            }
            return false;
        }
        this.mHwCustSmsMessageSender.queueMessage(this.mNumberOfDests, this.mDests, this.mContext, timeAddressPos, this.mTimestamp, requestDeliveryReport, this.mThreadId, this.mSubId, groupIdTemp, bodyAddressPos, this.mMessageText, address);
        MLog.d("Mms_TXS_Sender", "mHwCustSmsMessageSender.queueMessage");
        return false;
    }

    private String getOutgoingServiceCenter(long threadId) {
        Cursor cursor = null;
        try {
            cursor = SqliteWrapper.query(this.mContext, this.mContext.getContentResolver(), Inbox.CONTENT_URI, SERVICE_CENTER_PROJECTION, "thread_id = " + threadId, null, "date DESC");
            if (cursor == null || !cursor.moveToFirst()) {
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            }
            String string;
            boolean replyPathPresent = 1 == cursor.getInt(0);
            HwMessageUtils.setIsUsingOutgoingServiceCenter(replyPathPresent);
            if (replyPathPresent) {
                string = cursor.getString(1);
            } else {
                string = null;
            }
            if (cursor != null) {
                cursor.close();
            }
            return string;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private String getOutgoingSmsCenterNumber(long threadId, int subID) {
        if (this.mHwCustSmsMessageSender != null) {
            String str = this.mHwCustSmsMessageSender.getSMSCAddress(subID);
            if (!TextUtils.isEmpty(str)) {
                return str;
            }
            str = this.mHwCustSmsMessageSender.getCustReplaceSmsCenterNumber(subID);
            if (!TextUtils.isEmpty(str)) {
                return str;
            }
        }
        if (!MmsConfig.getEnableModifySMSCenterNumber() || MmsConfig.isModifySMSCenterAddressOnCard()) {
            return getOutgoingServiceCenter(threadId);
        }
        String strCenterNumber;
        HwSIMCardChangedHelper.checkSimWasReplaced(this.mContext, subID);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.mContext);
        if (!MessageUtils.isMultiSimEnabled()) {
            strCenterNumber = sp.getString("sms_center_number", null);
        } else if (subID == 0) {
            strCenterNumber = sp.getString("pref_key_simuim1_message_center", null);
        } else {
            strCenterNumber = sp.getString("pref_key_simuim2_message_center", null);
        }
        if (strCenterNumber == null) {
            strCenterNumber = getOutgoingServiceCenter(threadId);
        }
        return strCenterNumber;
    }

    private void log(String msg) {
        MLog.d(HwCustUpdateUserBehaviorImpl.MMS, "[SmsMsgSender] " + msg);
    }
}
