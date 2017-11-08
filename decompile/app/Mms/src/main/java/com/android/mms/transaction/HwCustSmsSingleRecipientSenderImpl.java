package com.android.mms.transaction;

import android.app.PendingIntent;
import android.content.Context;
import android.content.res.Resources;
import android.os.SystemProperties;
import android.provider.SettingsEx.Systemex;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.GsmAlphabet.TextEncodingDetails;
import com.android.internal.telephony.Sms7BitEncodingTranslator;
import java.util.ArrayList;

public class HwCustSmsSingleRecipientSenderImpl extends HwCustSmsSingleRecipientSender {
    private static final int SUBSCRIPTION_FOR_NONE_VARIABLE_METHODS = -1;
    private static String TAG = "HwCustSmsSingleRecipientSenderImpl";
    private static final boolean isSprintDevice;
    private Context mContext = null;

    static {
        boolean equals;
        if (SystemProperties.get("ro.config.hw_opta", "0").equals("237")) {
            equals = SystemProperties.get("ro.config.hw_optb", "0").equals("840");
        } else {
            equals = false;
        }
        isSprintDevice = equals;
    }

    public HwCustSmsSingleRecipientSenderImpl(Context context) {
        super(context);
        this.mContext = context;
    }

    public boolean isNeedTempSmsTable() {
        return "true".equals(Systemex.getString(this.mContext.getContentResolver(), "hw_need_temp_smstable"));
    }

    public ArrayList<String> hwCustDevideMessage(String messageBody) {
        if (!isNeedTempSmsTable()) {
            return null;
        }
        TextEncodingDetails ted;
        int limit;
        if (SmsMessage.useCdmaFormatForMoSms()) {
            ted = com.android.internal.telephony.cdma.SmsMessage.calculateLength(messageBody, false);
        } else {
            ted = com.android.internal.telephony.gsm.SmsMessage.calculateLength(messageBody, false);
        }
        if (ted.codeUnitSize == 1) {
            int udhLength;
            if (ted.languageTable != 0 && ted.languageShiftTable != 0) {
                udhLength = 7;
            } else if (ted.languageTable == 0 && ted.languageShiftTable == 0) {
                udhLength = 0;
            } else {
                udhLength = 4;
            }
            if (ted.msgCount > 1) {
                udhLength += 6;
            }
            if (udhLength != 0) {
                udhLength++;
            }
            limit = 160 - udhLength;
        } else if (ted.msgCount > 1) {
            limit = 134;
        } else {
            limit = 140;
        }
        limit -= 5;
        String newMsgBody = null;
        if (Resources.getSystem().getBoolean(17957019)) {
            newMsgBody = Sms7BitEncodingTranslator.translate(messageBody);
        }
        if (TextUtils.isEmpty(newMsgBody)) {
            newMsgBody = messageBody;
        }
        int pos = 0;
        int textLen = newMsgBody.length();
        ArrayList<String> temp = new ArrayList(ted.msgCount);
        ArrayList<String> result = new ArrayList(ted.msgCount);
        while (pos < textLen) {
            int nextPos;
            if (ted.codeUnitSize == 1) {
                if (SmsMessage.useCdmaFormatForMoSms() && ted.msgCount == 1) {
                    nextPos = pos + Math.min(limit, textLen - pos);
                } else {
                    nextPos = GsmAlphabet.findGsmSeptetLimitIndex(newMsgBody, pos, limit, ted.languageTable, ted.languageShiftTable);
                }
                if (nextPos < textLen) {
                    int tempPos = newMsgBody.substring(pos, nextPos).lastIndexOf(32);
                    if (tempPos != -1) {
                        nextPos = (pos + tempPos) + 1;
                    }
                }
            } else {
                nextPos = pos + Math.min(limit / 2, textLen - pos);
            }
            if (nextPos < pos || nextPos > textLen) {
                Log.e(TAG, "hwCustDevideMessage fragmentText failed (" + pos + " >= " + nextPos + " or " + nextPos + " >= " + textLen + ")");
                break;
            }
            temp.add(newMsgBody.substring(pos, nextPos));
            pos = nextPos;
        }
        if (temp.size() <= 1) {
            return temp;
        }
        for (int id = 0; id < temp.size(); id++) {
            result.add("(" + (id + 1) + "/" + temp.size() + ")" + ((String) temp.get(id)));
        }
        return result;
    }

    public boolean hwCustSendSmsMessage(SmsManager smsManager, String destinationAddress, String scAddress, ArrayList<String> parts, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents) {
        if (!isNeedTempSmsTable()) {
            return false;
        }
        for (String message : parts) {
            ArrayList<String> tempMessage = new ArrayList();
            tempMessage.add(message);
            smsManager.sendMultipartTextMessage(destinationAddress, scAddress, tempMessage, sentIntents, deliveryIntents);
        }
        return true;
    }

    public String hwCustDestPlusCodeHandle(String number) {
        String numberWithPrefix = number;
        Context context = this.mContext;
        if (!isSprintDevice || TextUtils.isEmpty(number) || !number.startsWith("+") || context == null) {
            return number;
        }
        String plusSwitch = Systemex.getString(context.getContentResolver(), "sprint_plus_code_north_american");
        Log.d(TAG, "plusSwitch = " + plusSwitch);
        if ("N".equals(plusSwitch)) {
            return number;
        }
        int phoneType = TelephonyManager.getDefault().getPhoneType();
        Log.d(TAG, "phoneType = " + phoneType);
        if (phoneType != 2) {
            return number;
        }
        String mccmnc = TelephonyManager.getDefault().getSimOperator();
        Log.d(TAG, "mccmnc = " + mccmnc);
        if (mccmnc == null) {
            return number;
        }
        boolean isUSAndCanada;
        if (mccmnc.startsWith("310") || mccmnc.startsWith("311") || mccmnc.startsWith("312") || mccmnc.startsWith("313") || mccmnc.startsWith("314") || mccmnc.startsWith("315") || mccmnc.startsWith("316")) {
            isUSAndCanada = true;
        } else {
            isUSAndCanada = mccmnc.startsWith("302");
        }
        if (!isUSAndCanada) {
            String tmpPrefix = Systemex.getString(context.getContentResolver(), "sprint_plus_code_dialing");
            Log.d(TAG, "tmpPrefix = " + tmpPrefix);
            numberWithPrefix = tmpPrefix + number.substring(1, number.length());
        } else if (number.startsWith("+1")) {
            numberWithPrefix = number.substring(2, number.length());
        }
        return numberWithPrefix;
    }
}
