package com.android.mms.util;

import android.content.Context;
import android.os.AsyncTask;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.widget.TextView;
import com.android.mms.MmsApp;
import com.huawei.cspcommon.ex.ThreadEx;
import com.huawei.mms.util.OriginPhoneNumberWatcher;
import java.util.Locale;

public final class PhoneNumberFormatter {

    private static class TextWatcherLoadAsyncTask extends AsyncTask<Void, Void, PhoneNumberFormattingTextWatcher> {
        private final String mCountryCode;
        private final TextView mTextView;

        public TextWatcherLoadAsyncTask(String countryCode, TextView textView) {
            this.mCountryCode = countryCode;
            this.mTextView = textView;
        }

        protected PhoneNumberFormattingTextWatcher doInBackground(Void... params) {
            return new OriginPhoneNumberWatcher(this.mCountryCode);
        }

        protected void onPostExecute(PhoneNumberFormattingTextWatcher watcher) {
            if (watcher != null && !isCancelled()) {
                this.mTextView.addTextChangedListener(watcher);
            }
        }
    }

    private PhoneNumberFormatter() {
    }

    public static final void setPhoneNumberFormattingTextWatcher(Context context, TextView textView) {
        new TextWatcherLoadAsyncTask(MmsApp.getApplication().getCurrentCountryIso(), textView).executeOnExecutor(ThreadEx.getDefaultExecutor(), (Void[]) null);
    }

    public static String removeDashesAndBlanks(String paramString) {
        if (TextUtils.isEmpty(paramString)) {
            return paramString;
        }
        StringBuilder localStringBuilder = new StringBuilder();
        for (int i = 0; i < paramString.length(); i++) {
            char c = paramString.charAt(i);
            if (!(c == ' ' || c == '-')) {
                localStringBuilder.append(c);
            }
        }
        return localStringBuilder.toString();
    }

    public static String getFromatedNumber(String formatNumber) {
        String newNumber = removeDashesAndBlanks(formatNumber);
        if (TextUtils.isEmpty(newNumber)) {
            return newNumber;
        }
        return PhoneNumberUtils.formatNumber(newNumber, Locale.getDefault().getCountry());
    }

    public static String[] transSafeNumbers(String[] numbers) {
        if (numbers != null) {
            for (int i = 0; i < numbers.length; i++) {
                numbers[i] = transSafeNumber(numbers[i]);
            }
        }
        return numbers;
    }

    private static String transSafeNumber(String number) {
        if (number == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        int i = 0;
        while (i < number.length()) {
            char c = number.charAt(i);
            if (c == '-' || c == '@' || c == '.' || i < number.length() - 4) {
                builder.append(c);
            } else {
                builder.append('x');
            }
            i++;
        }
        return builder.toString();
    }
}
