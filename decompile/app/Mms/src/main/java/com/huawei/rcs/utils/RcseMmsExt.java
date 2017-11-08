package com.huawei.rcs.utils;

import android.content.Intent;
import android.os.RemoteException;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import com.android.mms.data.WorkingMessage;
import com.huawei.cspcommon.MLog;
import com.huawei.rcs.ui.RcsAudioMessage;
import java.util.List;

public final class RcseMmsExt {
    private static boolean mIsSendModeLocked;
    private static SendModeSetListener mListListener;
    private static final Object mListenerLock = new Object();
    private static int mSendMessageMode;
    private static final Object mSendModeSetLock = new Object();

    public interface SendModeSetListener {
        int autoSetSendMode(boolean z, boolean z2);

        void onSendModeSet(boolean z, boolean z2);
    }

    public static void registerSendModeSetListener(SendModeSetListener listener) {
        if (listener == null) {
            MLog.w("RcseMmsExt", "set a null SendModeSetListener in registerSendModeSetListener");
            return;
        }
        synchronized (mListenerLock) {
            mListListener = listener;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void unRegisterSendModeSetListener(SendModeSetListener listener) {
        synchronized (mListenerLock) {
            if (mListListener == null) {
                MLog.w("RcseMmsExt", "unRegisterSendModeSetListener error, hasn't init listener list.");
            } else if (mListListener == listener) {
                mListListener = null;
            }
        }
    }

    protected RcseMmsExt() {
    }

    public static void init() {
        synchronized (mListenerLock) {
            mListListener = null;
        }
        resetRcsMode();
    }

    public static void resetRcsMode() {
        Intent emptyIntent = new Intent();
        MLog.d("RcseMmsExt", "resetRcsMode() will updateRcsMode");
        updateRcsMode(emptyIntent);
    }

    public static void updateRcsMode(Intent intent) {
        if (intent == null) {
            MLog.e("RcseMmsExt", "updateRcsMode error, set intent is null.");
            return;
        }
        int sendMessageMode = mSendMessageMode;
        boolean z = false;
        boolean z2 = true;
        boolean z3 = false;
        boolean isSendModeLocked = mIsSendModeLocked;
        boolean attempToSetSendMode = attempToSetSendMode(intent);
        boolean z4 = false;
        boolean z5 = false;
        if (intent.hasExtra("auto_set_send_mode")) {
            z = intent.getBooleanExtra("auto_set_send_mode", false);
            MLog.d("RcseMmsExt", "updateRcsMode set KEY_AUTO_SET_SEND_MODE:" + z);
        }
        if (intent.hasExtra("force_set_send_mode")) {
            z3 = intent.getBooleanExtra("force_set_send_mode", false);
            MLog.d("RcseMmsExt", "updateRcsMode set KEY_FORCE_SET_SEND_MODE:" + z3);
        }
        if (intent.hasExtra("send_mode")) {
            sendMessageMode = intent.getIntExtra("send_mode", 0);
            MLog.d("RcseMmsExt", "updateRcsMode set KEY_SEND_MODE:" + sendMessageMode);
        }
        if (intent.hasExtra("ignore_cap_time_out")) {
            z2 = intent.getBooleanExtra("ignore_cap_time_out", true);
            MLog.d("RcseMmsExt", "updateRcsMode set KEY_IGNORE_CAP_TIME_OUT:" + z2);
        }
        if (intent.hasExtra("ignore_login_status")) {
            z4 = intent.getBooleanExtra("ignore_login_status", false);
            MLog.d("RcseMmsExt", "updateRcsMode set KEY_IGNORE_LOGIN_STATUS:" + z4);
        }
        if (intent.hasExtra("ignore_mode_lock")) {
            z5 = intent.getBooleanExtra("ignore_mode_lock", false);
            MLog.d("RcseMmsExt", "updateRcsMode set KEY_IGNORE_MODE_LOCK:" + z5);
        }
        if (!attempToSetSendMode) {
            sendMessageMode = 0;
            isSendModeLocked = false;
        } else if (z3) {
            isSendModeLocked = true;
        } else {
            if (z) {
                synchronized (mListenerLock) {
                    if (mListListener != null) {
                        sendMessageMode = mListListener.autoSetSendMode(z2, z4);
                    } else {
                        MLog.w("RcseMmsExt", "sendModeSetlistener call autoSetSendMode() is invalid.Please make sure it has been register by anyone");
                    }
                }
            }
            if (!(mIsSendModeLocked ? z5 : true)) {
                MLog.d("RcseMmsExt", "Try to set send mode failed.It has been locked.");
                synchronized (mListenerLock) {
                    if (mListListener != null) {
                        MLog.d("RcseMmsExt", "Call back onSendModeSet(), isRcsMode = " + isRcsMode() + ", mIsSendModeLocked:" + mIsSendModeLocked);
                        mListListener.onSendModeSet(isRcsMode(), mIsSendModeLocked);
                    }
                }
                return;
            }
        }
        synchronized (mSendModeSetLock) {
            mIsSendModeLocked = isSendModeLocked;
            mSendMessageMode = sendMessageMode;
        }
        synchronized (mListenerLock) {
            if (mListListener != null) {
                MLog.d("RcseMmsExt", "Call back onSendModeSet(), isRcsMode = " + isRcsMode() + ", mIsSendModeLocked:" + mIsSendModeLocked);
                mListListener.onSendModeSet(isRcsMode(), mIsSendModeLocked);
            } else {
                MLog.d("RcseMmsExt", "Send mode is not change, so do not need call back onSendModeSet()");
            }
        }
        if (isRcsMode()) {
            RcsAudioMessage.setCurrentView(1);
        } else {
            RcsAudioMessage.setCurrentView(3);
        }
    }

    private static boolean attempToSetSendMode(Intent intent) {
        if (intent == null) {
            return false;
        }
        boolean z;
        if (intent.hasExtra("auto_set_send_mode") || intent.hasExtra("force_set_send_mode") || intent.hasExtra("send_mode") || intent.hasExtra("ignore_cap_time_out") || intent.hasExtra("ignore_login_status")) {
            z = true;
        } else {
            z = intent.hasExtra("ignore_mode_lock");
        }
        return z;
    }

    public static void refreshUI() {
        synchronized (mListenerLock) {
            if (mListListener != null) {
                MLog.d("RcseMmsExt", "Call back onSendModeSet(), isRcsMode = " + isRcsMode() + ", mIsSendModeLocked:" + mIsSendModeLocked);
                mListListener.onSendModeSet(isRcsMode(), mIsSendModeLocked);
                return;
            }
            MLog.w("RcseMmsExt", "sendModeSetlistener onSendModeSet() is invalid.Please make sure it has been register by anyone.");
        }
    }

    public static boolean isRcsMode() {
        return mSendMessageMode == 1;
    }

    public static String getFirstRecipients(WorkingMessage workingMessage) {
        String[] RcseNumber = workingMessage.getConversation().getRecipients().getNumbers();
        if (RcseNumber.length > 0) {
            return PhoneNumberUtils.normalizeNumber(RcseNumber[0]);
        }
        return null;
    }

    public static String getFirstRecipients(List<String> numberList) {
        if (numberList == null || numberList.size() < 1) {
            return null;
        }
        String[] RcseNumber = new String[numberList.size()];
        numberList.toArray(RcseNumber);
        return PhoneNumberUtils.normalizeNumber(RcseNumber[0]);
    }

    public static boolean isNeedStoreNotification(WorkingMessage workingMessage) {
        if (RcsProfile.getRcsService() == null) {
            return false;
        }
        try {
            return RcsProfile.getRcsService().needStoreNotification(getFirstRecipients(workingMessage));
        } catch (Exception e) {
            MLog.e("RcseMmsExt", "isNeedStoreNotification() call service send im fail and return false");
            return false;
        }
    }

    public static boolean isNeedShowAddAttachmentForFt(List<String> numberList) {
        if (mSendMessageMode != 1) {
            return false;
        }
        MLog.i("RcseMmsExt FileTrans: ", "send mode is im and check whether can we send Im");
        return isCanSendFt(numberList);
    }

    public static boolean isNeedShowSendButton(boolean cardEnabled, boolean readyToSend, WorkingMessage workingMessage, boolean imCapCache) {
        boolean isCanConvertToIm = isCanConvertToIm(workingMessage);
        switch (mSendMessageMode) {
            case 0:
                if (!readyToSend) {
                    cardEnabled = false;
                }
                return cardEnabled;
            case 1:
                if (!(readyToSend && imCapCache)) {
                    isCanConvertToIm = false;
                }
                return isCanConvertToIm;
            default:
                return false;
        }
    }

    public static boolean isCanConvertToIm(WorkingMessage workingMessage) {
        if (workingMessage == null) {
            return false;
        }
        boolean hasText = !workingMessage.hasAttachment() ? workingMessage.hasText() : true;
        if (TextUtils.isEmpty(workingMessage.getSubject()) && !workingMessage.requiresMmsExceptLength()) {
            hasText = true;
        }
        return hasText;
    }

    public static boolean checkInvalidGroupStatus(int groupStatus) {
        if (3 == groupStatus || 6 == groupStatus || 33 == groupStatus) {
            return true;
        }
        return false;
    }

    public static boolean isCanSendFt(List<String> numberList) {
        boolean z = true;
        if (RcsProfile.getRcsService() == null || numberList == null) {
            MLog.w("RcseMmsExt", "isCanSendFt() numberList is null or service is null return false");
            return false;
        }
        try {
            if (!RcsProfile.getRcsService().isFtAvailable(getFirstRecipients((List) numberList)) || !RcsProfile.isRcsImServiceSwitchEnabled()) {
                z = false;
            } else if (numberList.size() != 1) {
                z = false;
            }
            return z;
        } catch (RemoteException e) {
            MLog.e("RcseMmsExt", "RcsProfile error");
            return false;
        }
    }
}
