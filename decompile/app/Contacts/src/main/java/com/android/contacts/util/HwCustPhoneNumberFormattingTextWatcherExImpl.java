package com.android.contacts.util;

import android.os.SystemProperties;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
import com.android.contacts.hap.sprint.preload.HwCustPreloadContacts;
import java.util.regex.Pattern;

public class HwCustPhoneNumberFormattingTextWatcherExImpl extends HwCustPhoneNumberFormattingTextWatcherEx {
    private static final char HYPHEN_BEFORE_NUMBER = '-';
    private static final String MOBILE_NUMBER_CUST = "[^0-9|#|*|;|,|N]";
    private static final String MOBILE_NUMBER_PATTERN = "^[0-9\\-\\(\\)\\ ]*$";
    private static final String TAG = "HwCustPhoneNumberFormattingTextWatcherExImpl";
    private boolean isSpecialNum = SystemProperties.getBoolean("ro.config.special_format_turnum", false);
    private int mFormatRePos;
    private String mFormatStr = SystemProperties.get("ro.config.numformat", "");
    private String[] mFrontNumArray;
    private int mNumberSpecial = 0;
    private String mSplitStr;
    private String mSprintFomatNumber = null;

    private String formatPhoneNumber(String input) {
        if (PhoneNumberUtils.isEmergencyNumber(input) || input.length() == 0) {
            return input;
        }
        if ("b".equals(this.mSplitStr)) {
            this.mSplitStr = HwCustPreloadContacts.EMPTY_STRING;
        }
        boolean flag = false;
        int i = 0;
        if (input.indexOf("+") == 0) {
            input = input.substring(1);
            flag = true;
        }
        input = parseString(input);
        StringBuilder fromatNumber = new StringBuilder();
        while (input.length() > 0 && this.mFrontNumArray != null && i < this.mFrontNumArray.length) {
            try {
                if (Integer.parseInt(this.mFrontNumArray[i]) < input.length()) {
                    fromatNumber.append(input.substring(0, Integer.parseInt(this.mFrontNumArray[i])));
                    fromatNumber.append(this.mSplitStr);
                    input = input.substring(Integer.parseInt(this.mFrontNumArray[i]));
                    if (i < this.mFrontNumArray.length - 1) {
                        i++;
                    }
                } else {
                    fromatNumber.append(input);
                    input = "";
                }
            } catch (Exception e) {
                Log.e(TAG, "format number error");
            }
        }
        if (flag) {
            fromatNumber.insert(0, "+");
        }
        return fromatNumber.toString();
    }

    private String parseString(String sTR2) {
        return Pattern.compile(MOBILE_NUMBER_CUST).matcher(sTR2).replaceAll("").trim();
    }

    public String getCustformatStr(String input, int rememberedPos) {
        String formatInput = input;
        int i;
        String tmpNumber;
        if (HwCustContactFeatureUtils.isSupportDialNumberFormat() && !TextUtils.isEmpty(input) && input.matches(MOBILE_NUMBER_PATTERN)) {
            this.mNumberSpecial = 0;
            int index = 0;
            i = 0;
            while (i < rememberedPos) {
                if ('*' == input.charAt(i) || '#' == input.charAt(i) || ((input.charAt(i) >= '0' && input.charAt(i) <= '9') || 'N' == input.charAt(i) || ',' == input.charAt(i) || ';' == input.charAt(i))) {
                    index++;
                } else {
                    this.mNumberSpecial++;
                }
                i++;
            }
            tmpNumber = formatPhoneNumber4SPRINT(input);
            if (TextUtils.isEmpty(tmpNumber)) {
                this.mSprintFomatNumber = null;
                return formatInput;
            }
            formatInput = tmpNumber;
            this.mSprintFomatNumber = tmpNumber;
            return formatInput;
        } else if (TextUtils.isEmpty(input) || TextUtils.isEmpty(this.mFormatStr)) {
            return formatInput;
        } else {
            if (this.isSpecialNum && input.length() >= 2 && '+' == input.charAt(0) && '9' == input.charAt(1)) {
                this.mFormatStr = "2b3b3b4";
            }
            this.mSplitStr = new StringBuffer().append(this.mFormatStr.charAt(1)).toString();
            this.mFrontNumArray = this.mFormatStr.split(this.mSplitStr);
            int numBrackts = 0;
            i = 0;
            while (i < rememberedPos && i < input.length()) {
                if (!('*' == input.charAt(i) || '#' == input.charAt(i) || ((input.charAt(i) >= '0' && input.charAt(i) <= '9') || 'N' == input.charAt(i) || ',' == input.charAt(i)))) {
                    if (';' != input.charAt(i)) {
                        i++;
                    }
                }
                numBrackts++;
                i++;
            }
            tmpNumber = formatPhoneNumber(input);
            if (!TextUtils.isEmpty(tmpNumber)) {
                formatInput = tmpNumber;
            }
            this.mFormatRePos = getFormatCursorPos(input, numBrackts);
            return formatInput;
        }
    }

    private String formatPhoneNumber4SPRINT(String input) {
        if (TextUtils.isEmpty(input) || input.startsWith("+")) {
            return input;
        }
        input = input.replace("-", "").replace("(", "").replace(")", "").replace(HwCustPreloadContacts.EMPTY_STRING, "");
        if (input.startsWith(CallInterceptDetails.BRANDED_STATE)) {
            if (input.length() == 1 || input.length() > 11) {
                return input;
            }
            if (input.length() <= 6) {
                return input.substring(0, 1) + HYPHEN_BEFORE_NUMBER + input.substring(1, input.length());
            }
            if (input.length() <= 6 || input.length() > 8) {
                return input.substring(0, 1) + HYPHEN_BEFORE_NUMBER + input.substring(1, 4) + HYPHEN_BEFORE_NUMBER + input.substring(4, 7) + HYPHEN_BEFORE_NUMBER + input.substring(7, input.length());
            }
            return input.substring(0, 1) + HYPHEN_BEFORE_NUMBER + input.substring(1, 4) + HYPHEN_BEFORE_NUMBER + input.substring(4, input.length());
        } else if (input.length() <= 5 || input.length() > 10) {
            return input;
        } else {
            if (input.length() <= 5 || input.length() > 7) {
                return input.substring(0, 3) + HYPHEN_BEFORE_NUMBER + input.substring(3, 6) + HYPHEN_BEFORE_NUMBER + input.substring(6, input.length());
            }
            return input.substring(0, 3) + HYPHEN_BEFORE_NUMBER + input.substring(3, input.length());
        }
    }

    private int getFormatCursorPos(String input, int numBrackts) {
        if (PhoneNumberUtils.isEmergencyNumber(input) || input.length() == 0) {
            return input.length();
        }
        boolean flag = false;
        if (input.indexOf("+") == 0) {
            input = input.substring(1);
            flag = true;
        }
        int formatRePos = 0;
        int i = 0;
        int j = 0;
        int curPos = 0;
        while (input.length() > 0 && this.mFrontNumArray != null && i < this.mFrontNumArray.length) {
            try {
                curPos += Integer.parseInt(this.mFrontNumArray[i]);
                if (curPos >= numBrackts) {
                    formatRePos = (numBrackts + i) + j;
                    break;
                }
                if (i == this.mFrontNumArray.length - 1) {
                    j++;
                }
                if (i < this.mFrontNumArray.length - 1) {
                    i++;
                }
            } catch (Exception e) {
                Log.e(TAG, "format number error");
            }
        }
        if (flag) {
            formatRePos++;
        }
        return formatRePos;
    }

    public int getCustRememberedPos(int rememberedPos) {
        if (HwCustContactFeatureUtils.isSupportDialNumberFormat() && !TextUtils.isEmpty(this.mSprintFomatNumber)) {
            int indexPos = 0;
            int numSeperators = 0;
            int length = rememberedPos;
            if (this.mSprintFomatNumber.length() < rememberedPos) {
                length = this.mSprintFomatNumber.length();
            }
            for (int i = 0; i < length; i++) {
                if (HYPHEN_BEFORE_NUMBER == this.mSprintFomatNumber.charAt(i)) {
                    numSeperators++;
                } else {
                    indexPos++;
                }
            }
            if (numSeperators > this.mNumberSpecial) {
                rememberedPos += numSeperators - this.mNumberSpecial;
            }
            if (rememberedPos > this.mSprintFomatNumber.length()) {
                return this.mSprintFomatNumber.length();
            }
            return rememberedPos;
        } else if (TextUtils.isEmpty(this.mFormatStr)) {
            return rememberedPos;
        } else {
            return this.mFormatRePos;
        }
    }

    public boolean isCustRemoveSep() {
        return "true".equals(SystemProperties.get("ro.config.number_remove_sep", "false"));
    }

    public String stripNoSep(String number) {
        if (TextUtils.isEmpty(number)) {
            return number;
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < number.length(); i++) {
            if (PhoneNumberUtils.isNonSeparator(number.charAt(i))) {
                result.append(number.charAt(i));
            }
        }
        return result.toString();
    }
}
