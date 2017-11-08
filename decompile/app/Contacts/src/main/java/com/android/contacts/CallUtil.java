package com.android.contacts;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.PhoneNumberUtils;
import com.android.contacts.compatibility.CompatUtils;
import com.android.contacts.compatibility.QueryUtil;
import com.huawei.android.telephony.MSimSmsManagerEx;

public class CallUtil {
    public static final ComponentName CALL_INTENT_DESTINATION = new ComponentName("com.android.phone", "com.android.services.telephony.TelephonyConnectionService");

    public static Intent getCallIntent(String number) {
        return getCallIntent(number, null, null);
    }

    public static Intent getCallIntent(Uri uri) {
        return getCallIntent(uri, null, null);
    }

    public static Intent getCallIntent(String number, String callOrigin) {
        return getCallIntent(getCallUri(number), callOrigin, null);
    }

    public static Uri getCallUri(String number) {
        number = PhoneNumberUtils.convertPreDial(number);
        if (PhoneNumberUtils.isUriNumber(number)) {
            return Uri.fromParts("sip", number, null);
        }
        return Uri.fromParts("tel", number, null);
    }

    public static Intent getCallIntent(String number, int aSubId) {
        Intent intent = getCallIntentByPhoneAccount(number, makePstnPhoneAccountHandleWithPrefix(false, aSubId));
        if (aSubId != -1) {
            try {
                MSimSmsManagerEx.setSimIdToIntent(intent, aSubId);
            } catch (Exception e) {
                intent.putExtra("subscription", aSubId);
                e.printStackTrace();
            }
        }
        return intent;
    }

    public static Intent getCallIntent(String number, int presentation, int aSubId) {
        Intent intent = getCallIntent(number, aSubId);
        intent.putExtra("EXTRA_CALL_LOG_PRESENTATION", presentation);
        return intent;
    }

    public static Intent getCallIntentByPhoneAccount(String number, PhoneAccountHandle accountHandle) {
        return getCallIntent(number, null, accountHandle);
    }

    public static Intent getCallIntent(String number, String callOrigin, PhoneAccountHandle accountHandle) {
        return getCallIntent(getCallUri(number), callOrigin, accountHandle);
    }

    public static Intent getCallIntent(Uri uri, String callOrigin, PhoneAccountHandle accountHandle) {
        return getCallIntent(uri, callOrigin, accountHandle, 0);
    }

    public static Intent getCallIntent(Uri uri, String callOrigin, PhoneAccountHandle accountHandle, int videoState) {
        Intent intent = new Intent(QueryUtil.isSystemAppForContacts() ? "android.intent.action.CALL_PRIVILEGED" : "android.intent.action.CALL", uri);
        intent.setPackage("com.android.server.telecom");
        intent.putExtra("android.telecom.extra.START_CALL_WITH_VIDEO_STATE", videoState);
        if (callOrigin != null) {
            intent.putExtra("com.android.phone.CALL_ORIGIN", callOrigin);
        }
        if (accountHandle != null) {
            intent.putExtra("android.telecom.extra.PHONE_ACCOUNT_HANDLE", accountHandle);
        }
        return intent;
    }

    public static Intent getCallWithSubjectIntent(String number, PhoneAccountHandle phoneAccountHandle, String callSubject) {
        Intent intent = getCallIntent(getCallUri(number));
        intent.putExtra("android.telecom.extra.CALL_SUBJECT", callSubject);
        if (phoneAccountHandle != null) {
            intent.putExtra("android.telecom.extra.PHONE_ACCOUNT_HANDLE", phoneAccountHandle);
        }
        return intent;
    }

    public static boolean isCallWithSubjectSupported(Context context) {
        if (!CompatUtils.isCallSubjectCompatible()) {
            return false;
        }
        TelecomManager telecommMgr = (TelecomManager) context.getSystemService("telecom");
        if (telecommMgr == null) {
            return false;
        }
        for (PhoneAccountHandle accountHandle : telecommMgr.getCallCapablePhoneAccounts()) {
            PhoneAccount account = telecommMgr.getPhoneAccount(accountHandle);
            if (account != null && account.hasCapabilities(64)) {
                return true;
            }
        }
        return false;
    }

    public static PhoneAccountHandle makePstnPhoneAccountHandleWithPrefix(boolean isEmergency, int aSubId) {
        if (aSubId == -1) {
            return null;
        }
        return new PhoneAccountHandle(CALL_INTENT_DESTINATION, isEmergency ? "E" : String.valueOf(aSubId));
    }
}
