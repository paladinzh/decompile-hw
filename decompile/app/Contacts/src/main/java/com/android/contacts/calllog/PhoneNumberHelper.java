package com.android.contacts.calllog;

import android.content.res.Resources;
import android.net.Uri;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.util.MultiUsersUtils;
import com.google.android.gms.R;
import com.huawei.cust.HwCustUtils;
import java.util.ArrayList;
import java.util.HashMap;

public class PhoneNumberHelper {
    private HwCustPhoneNumberHelper mCust;
    private String mPayPhone = null;
    private final Resources mResources;
    private HashMap<String, String> mSpecialNumberMap;
    private String mStrPrivateNum = null;
    private String mStrUnknown = null;

    public void init() {
        this.mSpecialNumberMap = new HashMap(3);
    }

    public PhoneNumberHelper(Resources resources) {
        this.mResources = resources;
        init();
    }

    public boolean canPlaceCallsTo(CharSequence number, int presentation) {
        return !TextUtils.isEmpty(number) && presentation == 1;
    }

    public CharSequence getDisplayNumber(CharSequence number, int presentation, CharSequence formattedNumber, CharSequence postDialDigits) {
        return getDisplayNumber(number, presentation, formattedNumber, postDialDigits, isVoicemailNumber(number));
    }

    public CharSequence getDisplayNumber(CharSequence number, int presentation, CharSequence formattedNumber, CharSequence postDialDigits, boolean isVoiceMailNumber) {
        CharSequence specialNumberName = getSepcialNumberName(number, presentation);
        if (!TextUtils.isEmpty(specialNumberName)) {
            return specialNumberName;
        }
        if (isVoiceMailNumber) {
            if (EmuiFeatureManager.isProductCustFeatureEnable()) {
                this.mCust = (HwCustPhoneNumberHelper) HwCustUtils.createObj(HwCustPhoneNumberHelper.class, new Object[0]);
                if (this.mCust != null) {
                    String tag = this.mCust.getVoicemailTag();
                    if (!TextUtils.isEmpty(tag)) {
                        return tag;
                    }
                }
            }
            if (!MultiUsersUtils.isCurrentUserGuest()) {
                return this.mResources.getString(R.string.voicemail);
            }
            if (TextUtils.isEmpty(number)) {
                return "";
            }
            return number;
        } else if (TextUtils.isEmpty(number)) {
            return "";
        } else {
            if (TextUtils.isEmpty(formattedNumber)) {
                return number.toString() + postDialDigits;
            }
            return formattedNumber;
        }
    }

    CharSequence getSepcialNumberName(CharSequence number, int presentation) {
        if (presentation == 3) {
            if (this.mStrUnknown == null) {
                this.mStrUnknown = this.mResources.getString(R.string.unknown);
            }
            return this.mStrUnknown;
        } else if (presentation == 2) {
            if (this.mStrPrivateNum == null) {
                this.mStrPrivateNum = this.mResources.getString(R.string.private_num);
            }
            return this.mStrPrivateNum;
        } else if (presentation != 4) {
            return "";
        } else {
            if (this.mPayPhone == null) {
                this.mPayPhone = this.mResources.getString(R.string.payphone);
            }
            return this.mPayPhone;
        }
    }

    public Uri getCallUri(String number) {
        if (isVoicemailNumber(number)) {
            return Uri.parse("voicemail:x");
        }
        if (isSipNumber(number)) {
            return Uri.fromParts("sip", number, null);
        }
        return Uri.fromParts("tel", number, null);
    }

    public boolean isVoicemailNumber(CharSequence number) {
        if (number != null) {
            return PhoneNumberUtils.isVoiceMailNumber(number.toString());
        }
        return false;
    }

    public boolean isSipNumber(CharSequence number) {
        if (number != null) {
            return PhoneNumberUtils.isUriNumber(number.toString());
        }
        return false;
    }

    public boolean canPlaceCallsToAvoidNullCheck(CharSequence number) {
        return !this.mSpecialNumberMap.containsKey(number);
    }

    public static boolean isUriNumber(String number) {
        if (number != null) {
            return !number.contains("@") ? number.contains("%40") : true;
        } else {
            return false;
        }
    }

    public static String getQueryCallNumber(String number) {
        if (number == null || number.length() <= 7) {
            return number;
        }
        int numCount = 0;
        int index = number.length() - 1;
        while (index >= 0 && numCount < 7) {
            if (isNonSeparator(number.charAt(index))) {
                numCount++;
            }
            index--;
        }
        return number.substring(index + 1);
    }

    private static boolean isNonSeparator(char c) {
        return (c >= '0' && c <= '9') || c == '*' || c == '#' || c == '+';
    }

    public static void getQueryCallLogNumberList(ArrayList<String> sourceList, HashMap<String, String> numberCountryHM, ArrayList<String> destNumberList) {
        if (destNumberList != null && sourceList != null && numberCountryHM != null) {
            int size = sourceList.size();
            boolean isSame = false;
            for (int i = 0; i < size; i++) {
                String tempNum = (String) sourceList.get(i);
                String tempCountryIso = (String) numberCountryHM.get(tempNum);
                for (int j = i + 1; j < size; j++) {
                    String tempNum1 = (String) sourceList.get(j);
                    if (CommonUtilMethods.compareNumsHw(tempNum, tempCountryIso, tempNum1, (String) numberCountryHM.get(tempNum1))) {
                        isSame = true;
                        break;
                    }
                }
                if (isSame) {
                    isSame = false;
                } else {
                    destNumberList.add(tempNum);
                }
            }
        }
    }
}
