package com.android.contacts.util;

import android.os.SystemProperties;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import com.android.contacts.ContactsApplication;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.i18n.phonenumbers.AsYouTypeFormatter;
import com.android.i18n.phonenumbers.PhoneNumberUtil;
import com.huawei.contact.util.SettingsWrapper;
import com.huawei.cust.HwCustUtils;
import java.util.Locale;

public class PhoneNumberFormattingTextWatcherEx implements TextWatcher {
    private int afterRemovePos;
    private boolean isCustomFormatter;
    private HwCustPhoneNumberFormattingTextWatcherEx mCust;
    private AsYouTypeFormatter mFormatter;
    private String mOldInputStr;
    private boolean mSelfChange;
    private boolean mStopFormatting;
    private final String noFormateCountry;

    public PhoneNumberFormattingTextWatcherEx() {
        this(Locale.getDefault().getCountry());
    }

    public PhoneNumberFormattingTextWatcherEx(String countryCode) {
        this.mSelfChange = false;
        this.mCust = null;
        this.noFormateCountry = SystemProperties.get("ro.config.noFormateCountry", "");
        this.afterRemovePos = 0;
        this.isCustomFormatter = "true".equals(SettingsWrapper.getString(ContactsApplication.getContext().getContentResolver(), "custom_number_delete_bracket"));
        if (countryCode == null) {
            throw new IllegalArgumentException();
        }
        this.mFormatter = PhoneNumberUtil.getInstance().getAsYouTypeFormatter(countryCode);
        if (EmuiFeatureManager.isProductCustFeatureEnable()) {
            this.mCust = (HwCustPhoneNumberFormattingTextWatcherEx) HwCustUtils.createObj(HwCustPhoneNumberFormattingTextWatcherEx.class, new Object[0]);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void beforeTextChanged(CharSequence s, int start, int count, int after) {
        if (!this.mSelfChange && !this.mStopFormatting) {
            if (count > 0) {
                if (hasSeparator(s, start, count)) {
                    stopFormatting();
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void onTextChanged(CharSequence s, int start, int before, int count) {
        if (!this.mSelfChange && !this.mStopFormatting) {
            if (count > 0) {
                if (hasSeparator(s, start, count)) {
                    stopFormatting();
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void afterTextChanged(Editable s) {
        boolean z = true;
        synchronized (this) {
            if (this.mStopFormatting) {
                if (s.length() == 0) {
                    z = false;
                }
                this.mStopFormatting = z;
            } else if (this.mSelfChange) {
            } else if (s.length() >= 30 || s.length() <= 3) {
            } else {
                String formatted = reformat(s, Selection.getSelectionEnd(s));
                if (formatted != null) {
                    int numBrackts;
                    int i;
                    int rememberedPos = this.mFormatter.getRememberedPosition();
                    if (isCustomProcess()) {
                        numBrackts = 0;
                        i = 0;
                        while (i < rememberedPos && i < formatted.length()) {
                            if ('(' == formatted.charAt(i) || ')' == formatted.charAt(i)) {
                                numBrackts++;
                            }
                            i++;
                        }
                        formatted = stripBrackets(formatted);
                        rememberedPos -= numBrackts;
                    }
                    if (this.mCust != null && this.mCust.isCustRemoveSep()) {
                        numBrackts = 0;
                        i = 0;
                        while (i < rememberedPos && i < formatted.length()) {
                            if (!PhoneNumberUtils.isNonSeparator(formatted.charAt(i))) {
                                numBrackts++;
                            }
                            i++;
                        }
                        formatted = this.mCust.stripNoSep(formatted);
                        rememberedPos -= numBrackts;
                    }
                    if (this.mCust != null) {
                        formatted = this.mCust.getCustformatStr(formatted, rememberedPos);
                        rememberedPos = this.mCust.getCustRememberedPos(rememberedPos);
                    }
                    if (haveNoFormateCountry()) {
                        formatted = removeAllSeparate(formatted);
                        rememberedPos = getPosAfterRemoveSeparate(rememberedPos);
                    }
                    if (!formatted.equals(s.toString())) {
                        this.mSelfChange = true;
                        s.replace(0, s.length(), formatted, 0, formatted.length());
                        if (formatted.equals(s.toString())) {
                            Selection.setSelection(s, rememberedPos);
                        }
                        this.mSelfChange = false;
                    }
                    if (!(this.mOldInputStr == null || this.mOldInputStr.equals(formatted))) {
                        this.mOldInputStr = null;
                    }
                }
            }
        }
    }

    private String reformat(CharSequence s, int cursor) {
        int curIndex = cursor - 1;
        String formatted = null;
        String input = s.toString();
        char c = '\u0000';
        boolean hasCursor = false;
        if (TextUtils.isEmpty(this.mOldInputStr) || !input.startsWith(this.mOldInputStr) || input.length() <= this.mOldInputStr.length()) {
            this.mFormatter.clear();
        } else {
            int oldLen = this.mOldInputStr.length();
            input = input.substring(oldLen);
            c = input.charAt(0);
            input = input.substring(1);
            if (oldLen == curIndex) {
                hasCursor = true;
            }
        }
        int len = input.length();
        for (int i = 0; i < len; i++) {
            char c2 = input.charAt(i);
            if (PhoneNumberUtils.isNonSeparator(c2)) {
                if (c != '\u0000') {
                    formatted = getFormattedNumber(c, hasCursor);
                    hasCursor = false;
                }
                c = c2;
            }
            if (i == curIndex) {
                hasCursor = true;
            }
        }
        if (c != '\u0000') {
            formatted = getFormattedNumber(c, hasCursor);
        }
        this.mOldInputStr = formatted;
        return formatted;
    }

    private String getFormattedNumber(char lastNonSeparator, boolean hasCursor) {
        if (hasCursor) {
            return this.mFormatter.inputDigitAndRememberPosition(lastNonSeparator);
        }
        return this.mFormatter.inputDigit(lastNonSeparator);
    }

    private void stopFormatting() {
        this.mStopFormatting = true;
        this.mFormatter.clear();
        this.mOldInputStr = null;
    }

    private boolean hasSeparator(CharSequence s, int start, int count) {
        int i = start;
        while (i < start + count) {
            try {
                if (!PhoneNumberUtils.isNonSeparator(s.charAt(i))) {
                    return true;
                }
                i++;
            } catch (IndexOutOfBoundsException e) {
                return true;
            }
        }
        return false;
    }

    public boolean isCustomProcess() {
        return this.isCustomFormatter;
    }

    public String stripBrackets(String number) {
        if (TextUtils.isEmpty(number)) {
            return number;
        }
        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < number.length()) {
            if (!(number.charAt(i) == '(' || number.charAt(i) == ')')) {
                result.append(number.charAt(i));
            }
            i++;
        }
        return result.toString();
    }

    private boolean haveNoFormateCountry() {
        return this.noFormateCountry.contains(Locale.getDefault().getCountry());
    }

    private String removeAllSeparate(String input) {
        String inputStr = input;
        if (TextUtils.isEmpty(input)) {
            return input;
        }
        StringBuilder sb = new StringBuilder();
        int numBrackts = 0;
        int i = 0;
        while (i < input.length()) {
            if (!('*' == input.charAt(i) || '#' == input.charAt(i) || (input.charAt(i) >= '0' && input.charAt(i) <= '9'))) {
                if ('+' != input.charAt(i)) {
                    i++;
                }
            }
            sb.append(input.charAt(i));
            numBrackts++;
            i++;
        }
        this.afterRemovePos = numBrackts;
        return sb.toString();
    }

    private int getPosAfterRemoveSeparate(int rememberedPos) {
        if (this.afterRemovePos != 0) {
            return this.afterRemovePos;
        }
        return rememberedPos;
    }
}
